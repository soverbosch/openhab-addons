package org.openhab.binding.cololight.internal;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.openhab.binding.cololight.internal.exception.CommunicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jersey.repackaged.com.google.common.collect.ImmutableMap;

public class LedStripDriver {
    private final Logger logger = LoggerFactory.getLogger(LedStripDriver.class);

    // Configuration values
    private final String host;
    private final int port;
    private final int socketTimeout;

    private static final int BRIGHTNESS_BYTE_LOCATION = 41;
    private static final int DELAY_BYTE_LOCATION = 43;
    private static final byte DEFAULT_BRIGHTNESS_VALUE = (byte) 0x36;
    private static final int ON_OFF_BYTE_LOCATION = 40;
    private static final byte ON = (byte) 0xCF;
    private static final byte OFF = (byte) 0xCE;
    private static final byte DEFAULT_DELAY = 0x32;

    private int delay = 50;
    private String effect = "pensieve";

    private LedStripStatus cachedStatus = null;

    // Taken from https://haus-automatisierung.com/projekt/2019/04/05/projekt-cololight-fhem.html
    //
    // Turning the LED on/off in wanted brightness
    // 000102030405060708091011121314151617181920212223242526272829303132333435363738394041 - Index
    // 535a3030000000000020000000000000000000000000000000000100000000000000000004000301ce33
    // ^^^^^^^^^^^^^^^^^^-------------------------------------------------------------------- Prefix
    // ..................^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^-------------- ConfigCmd
    // ....................................................^^-------------------------------- Request count? (27)
    // ..................^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^--^^^^^^^^^^^^^^^^^^--^^^^^^------ Unknown
    // ................................................................................^^---- ON(CF)/OFF(CE) byte (40)
    // ..................................................................................^^-- BRIGHTNESS byte (41)
    // ^^^^^^^^^^^^^^^^^^^^--------------------------------^^------------------^^--^^^^^^^^-- Needed
    //
    // Setting Effect at wanted delay
    // 000102030405060708091011121314151617181920212223242526272829303132333435363738394041424344 - Index
    // 535a3030000000000023000000000000000000000000000000000100000000000000000004000602ff04c47f00
    // ^^^^^^^^^^^^^^^^^^-------------------------------------------------------------------------- Prefix
    // ..................^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^-------------------- ColorCmd
    // ....................................................^^-------------------------------------- Request count? (27)
    // ..................^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^--^^^^^^^^^^^^^^^^^^--^^^^^^------------ Unknown
    // ................................................................................^^---------- Flow ON(FF)/OFF(EE)
    // ..................................................................................^^^^------ Effect (41,42)
    // ......................................................................................^^---- Delay (43)
    // ........................................................................................^^-- LED's used/Fading?
    // ^^^^^^^^^^^^^^^^^^^^--------------------------------^^------------------^^--^^^^^^^^^^^^^^-- Needed
    //
    // Delay: 1 means it will go fast and 100 means it will go slow (0 does not work)
    //
    // Status
    // 00010203040506070809101112131415161718192021222324252627282930313233343536373839 - Index
    // 535a303000000000001e00000000000000000000000000000000010000000000000000000b070186
    // ^^^^^^^^^^^^^^^^^^---------------------------------------------------------------- Prefix
    // ..................^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^--^^^^^^^^^^^^^^^^^^^^^^^^^^-- Unknown
    // ....................................................^^---------------------------- Request count? (27)
    // ^^^^^^^^^^^^^^^^^^^^--------------------------------^^------------------^^^^^^^^-- Needed
    //
    private static final byte[] prefix = { (byte) 0x53, (byte) 0x5A, (byte) 0x30, (byte) 0x30, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00 };
    private static final byte[] configCmd = { (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0x00,
            (byte) 0x03, (byte) 0x01 };
    private static final byte[] colorCmd = { (byte) 0x23, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0x00,
            (byte) 0x06, (byte) 0x02, (byte) 0xFF };
    // byte index 6: brightness
    private static final byte[] on = { ON, DEFAULT_BRIGHTNESS_VALUE };
    private static final byte[] off = { OFF, DEFAULT_BRIGHTNESS_VALUE };

    // byte index 27: request counter (> 0x00)
    private static final byte[] statusCheckMessageOne = { (byte) 0x1E, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x0b, (byte) 0x07, (byte) 0x01, (byte) 0x86 };
    // byte index 27: request counter (> 0x00)
    private static final byte[] statusCheckMessageTwo = { (byte) 0x1E, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x03, (byte) 0x08, (byte) 0x01, (byte) 0x01 };

    private static final Map<String, byte[]> effects = ImmutableMap.<String, byte[]> builder()
            .put("80sclub", new byte[] { (byte) 0x04, (byte) 0x9a, DEFAULT_DELAY, (byte) 0x00 })
            .put("cherryBlossom", new byte[] { (byte) 0x04, (byte) 0x94, DEFAULT_DELAY, (byte) 0x00 })
            .put("cocktailParade", new byte[] { (byte) 0x05, (byte) 0xbd, DEFAULT_DELAY, (byte) 0x90 })
            .put("savasana", new byte[] { (byte) 0x04, (byte) 0x97, DEFAULT_DELAY, (byte) 0x00 })
            .put("sunrise", new byte[] { (byte) 0x01, (byte) 0xc1, DEFAULT_DELAY, (byte) 0x00 })
            .put("unicorns", new byte[] { (byte) 0x04, (byte) 0x9a, DEFAULT_DELAY, (byte) 0x00 })
            .put("pensieve", new byte[] { (byte) 0x04, (byte) 0xc4, DEFAULT_DELAY, (byte) 0x00 })
            .put("theCircus", new byte[] { (byte) 0x04, (byte) 0x81, DEFAULT_DELAY, (byte) 0x30 })
            .put("instargrammer", new byte[] { (byte) 0x03, (byte) 0xbc, DEFAULT_DELAY, (byte) 0x90 }).build();

    public LedStripDriver() {
        this("localhost", 8900, 3000);
    }

    public LedStripDriver(final String host, final int port, final int socketTimeout) {
        this.host = host;
        this.port = port;
        this.socketTimeout = socketTimeout;
    }

    private byte[] getBytesForPower(final boolean switchOn) {
        byte[] result = new byte[prefix.length + configCmd.length + on.length];
        System.arraycopy(prefix, 0, result, 0, prefix.length);
        System.arraycopy(configCmd, 0, result, prefix.length, configCmd.length);
        if (switchOn) {
            System.arraycopy(on, 0, result, prefix.length + configCmd.length, on.length);
        } else {
            System.arraycopy(off, 0, result, prefix.length + configCmd.length, off.length);
        }
        return result;
    }

    public boolean getStatusIsOk() {
        logger.debug("Check if we can reach and communicate with the Cololight");
        try {
            if (InetAddress.getByName(this.host).isReachable(this.socketTimeout)) {
                cachedStatus = this.getLedStripStatus();
                logger.debug("\tYes we can!!!!");
                return true;
            }
        } catch (IOException | CommunicationException e) {
            e.printStackTrace();
        }
        logger.debug("\tNo we can't!!!!");
        return false;
    }

    public boolean setPowerOn() throws CommunicationException {
        logger.debug("Turning light on");
        byte[] command = getBytesForPower(true);
        byte[] received = sendRaw(addBrightness(command, BRIGHTNESS_BYTE_LOCATION));
        return received[received.length - 16] == ON;
    }

    private byte[] addBrightness(byte[] command, final int location) {
        if (cachedStatus != null) {
            command[location] = cachedStatus.getBrightness().byteValue();
        } else {
            command[location] = DEFAULT_BRIGHTNESS_VALUE;
        }
        return command;
    }

    public boolean setPowerOff() throws CommunicationException {
        logger.debug("Turning light off");
        byte[] command = getBytesForPower(false);
        byte[] received = sendRaw(addBrightness(command, BRIGHTNESS_BYTE_LOCATION));
        return received[received.length - 16] == OFF;
    }

    public LedStripStatus getLedStripStatus() throws CommunicationException {
        logger.debug("Getting Cololight status");
        byte[] command = new byte[prefix.length + statusCheckMessageOne.length];
        System.arraycopy(prefix, 0, command, 0, prefix.length);
        System.arraycopy(statusCheckMessageOne, 0, command, prefix.length, statusCheckMessageOne.length);
        sendRaw(command);
        System.arraycopy(statusCheckMessageTwo, 0, command, prefix.length, statusCheckMessageTwo.length);
        byte[] received = sendRaw(command);
        LedStripStatus result = new LedStripStatus();

        if (received[ON_OFF_BYTE_LOCATION] == ON) {
            logger.debug("\tCololight is on");
            result.setPower(OnOffType.ON);
        } else if (received[ON_OFF_BYTE_LOCATION] == OFF) {
            logger.debug("\tCololight is off");
            result.setPower(OnOffType.OFF);
        }
        result.setBrightness(new PercentType(Byte.toUnsignedInt(received[BRIGHTNESS_BYTE_LOCATION])));
        result.setDelay(new PercentType(this.delay));
        result.setEffect(new StringType(this.effect));
        cachedStatus = result;
        return result;
    }

    public int setBrightness(final int percentage) throws CommunicationException {
        logger.debug("Brightness {}%", percentage);
        byte[] received = sendRaw(getBytesForBrightness(percentage));
        return Byte.toUnsignedInt(received[BRIGHTNESS_BYTE_LOCATION]);
    }

    private byte[] getBytesForBrightness(final int percentage) {
        byte[] brightness = getBytesForPower(true);
        brightness[brightness.length - 1] = (byte) percentage;
        return brightness;
    }

    public void setEffect(final String effectName) throws CommunicationException {
        logger.debug("Effect {}", effectName);
        byte[] effect = getBytesForEffect(effectName);
        if (effect != null) {
            byte[] result = sendRaw(effect);
            this.setEffectDelay(convertByteToPercentage(result[/*effect.length - 2*/DELAY_BYTE_LOCATION]));
            this.effect = effectName;
        }
    }

    private int convertByteToPercentage(byte toConvert) {
        int unsigned = Byte.toUnsignedInt(toConvert);
        logger.trace("converting {} to unsigned int {} and then dividing it by {}", String.format("%02x", toConvert),
                unsigned, 2.55);
        return (int) (unsigned / 2.55);
    }

    private byte convertPercentageToByte(int toConvert) {
        logger.trace("{} multiplying it by {} and then converting it to byte {}", toConvert, 2.55,
                String.format("%02x", (byte) (toConvert * 2.55)));
        return (byte) (toConvert * 2.55);
    }

    public void setEffectDelay(final int delay) {
        logger.debug("Updating delay with value {}", delay);
        this.delay = delay;
    }

    private byte[] getBytesForEffect(final String effectName) {
        if (effects.containsKey(effectName)) {
            byte[] effectCmd = new byte[prefix.length + colorCmd.length + effects.get(effectName).length];
            byte[] effect = effects.get(effectName);
            System.arraycopy(prefix, 0, effectCmd, 0, prefix.length);
            System.arraycopy(colorCmd, 0, effectCmd, prefix.length, colorCmd.length);
            System.arraycopy(effect, 0, effectCmd, prefix.length + colorCmd.length, effect.length);
            effectCmd[effectCmd.length - 2] = convertPercentageToByte(delay);
            return effectCmd;
        }
        logger.error("Effect `{}` is unknown", effectName);
        return null;
    }

    public static String bytesToHex(final byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte aByte : bytes) {
            builder.append(String.format("%02x", aByte));
        }
        return builder.toString();
    }

    private byte[] sendRaw(final byte[] data) throws CommunicationException {
        try {
            byte[] received;
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(this.socketTimeout);
            InetAddress address = InetAddress.getByName(host);

            logger.trace("Sending {}", bytesToHex(data));
            DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
            socket.send(packet);

            byte[] buf = new byte[256];
            packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            received = new byte[packet.getLength()];
            System.arraycopy(packet.getData(), 0, received, 0, packet.getLength());
            logger.trace("Received {}", bytesToHex(received));
            socket.close();

            return received;
        } catch (IOException e) {
            throw new CommunicationException(e.getMessage());
        }
    }
}
