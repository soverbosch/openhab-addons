package org.openhab.binding.cololight.internal;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.openhab.binding.cololight.internal.exception.CommunicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jersey.repackaged.com.google.common.collect.ImmutableMap;

public class LedStripDriver {
    private final Logger logger = LoggerFactory.getLogger(LedStripDriver.class);

    private final String host;
    private final int port;
    private final int socketTimeout;

    // Taken from https://haus-automatisierung.com/projekt/2019/04/05/projekt-cololight-fhem.html
    protected static byte[] prefix = { (byte) 0x53, (byte) 0x5A, (byte) 0x30, (byte) 0x30, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00 };
    protected static byte[] configCmd = { (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };
    protected static byte[] colorCmd = { (byte) 0x23, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0x01, (byte) 0x06,
            (byte) 0x02, (byte) 0xFF };
    protected static byte[] on = { (byte) 0x04, (byte) 0x01, (byte) 0x03, (byte) 0x01, (byte) 0xcf, (byte) 0x35 };
    protected static byte[] off = { (byte) 0x04, (byte) 0x01, (byte) 0x03, (byte) 0x01, (byte) 0xce, (byte) 0x1e };
    protected static byte[] brightnessZeroPercent = { (byte) 0x53, (byte) 0x5A, (byte) 0x30, (byte) 0x30, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x16, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04,
            (byte) 0x16, (byte) 0x03, (byte) 0x01, (byte) 0xCF, (byte) 0x00 };
    protected static byte[] statusCheckMessageOne = { (byte) 0x53, (byte) 0x5a, (byte) 0x30, (byte) 0x30, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x1e, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x11, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0b,
            (byte) 0x07, (byte) 0x01, (byte) 0x86 };
    protected static byte[] statusCheckMessageTwo = { (byte) 0x53, (byte) 0x5a, (byte) 0x30, (byte) 0x30, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x1e, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x08, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03,
            (byte) 0x08, (byte) 0x01, (byte) 0x01 };

    final static Map<String, byte[]> effects = ImmutableMap.<String, byte[]> builder()
            .put("80sclub", new byte[] { (byte) 0x04, (byte) 0x9a, (byte) 0x00, (byte) 0x00 })
            .put("cherryBlossom", new byte[] { (byte) 0x04, (byte) 0x94, (byte) 0x08, (byte) 0x00 })
            .put("cocktailParade", new byte[] { (byte) 0x05, (byte) 0xbd, (byte) 0x06, (byte) 0x90 })
            .put("savasana", new byte[] { (byte) 0x04, (byte) 0x97, (byte) 0x04, (byte) 0x00 })
            .put("sunrise", new byte[] { (byte) 0x01, (byte) 0xc1, (byte) 0x0a, (byte) 0x00 })
            .put("unicorns", new byte[] { (byte) 0x04, (byte) 0x9a, (byte) 0x0e, (byte) 0x00 })
            .put("pensieve", new byte[] { (byte) 0x04, (byte) 0xc4, (byte) 0x06, (byte) 0x00 })
            .put("theCircus", new byte[] { (byte) 0x04, (byte) 0x81, (byte) 0x01, (byte) 0x30 })
            .put("instargrammer", new byte[] { (byte) 0x03, (byte) 0xbc, (byte) 0x01, (byte) 0x90 }).build();

    public LedStripDriver() {
        this("localhost", 8900, 3000);
    }

    public LedStripDriver(String host, int port) {
        this(host, port, 3000);
    }

    public LedStripDriver(String host, int port, int socketTimeout) {
        this.host = host;
        this.port = port;
        this.socketTimeout = socketTimeout;
    }

    protected byte[] getBytesForPower(boolean switchOn) {
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
        // Check if we can reach it
        try {
            if (InetAddress.getByName(this.host).isReachable(this.socketTimeout)) {
                // Check if we can communicate wit it
                this.getLedStripStatus();
                logger.debug("Yes we can!!!!");
                return true;
            }
        } catch (IOException | CommunicationException e) {
            e.printStackTrace();
        }
        logger.debug("No we can't!!!!");
        return false;
    }

    public void setPowerOn() throws CommunicationException {
        logger.debug("Turning light on");
        sendRaw(getBytesForPower(true));
    }

    public void setPowerOff() throws CommunicationException {
        logger.debug("Turning light off");
        sendRaw(getBytesForPower(false));
    }

    public LedStripStatus getLedStripStatus() throws CommunicationException {
        logger.debug("Getting Cololight status");
        sendRaw(statusCheckMessageOne);
        byte[] received = sendRaw(statusCheckMessageTwo);
        LedStripStatus result = new LedStripStatus();

        // byte 40 is power status
        if (received[40] == (byte) 0xCF) {
            logger.debug("Cololight is on");
            result.setPower(OnOffType.ON);
        } else if (received[40] == (byte) 0xCE) {
            logger.debug("Cololight is off");
            result.setPower(OnOffType.OFF);
        }
        // byte 41 is brightness
        result.setBrightness(new PercentType(Byte.toUnsignedInt(received[41])));
        return result;
    }

    public void setBrightness(int percentage) throws CommunicationException {
        logger.debug("Brightness {}%", percentage);
        sendRaw(getBytesForBrightness(percentage));
    }

    protected byte[] getBytesForBrightness(int percentage) {
        byte[] brightness = new byte[LedStripDriver.brightnessZeroPercent.length];
        System.arraycopy(LedStripDriver.brightnessZeroPercent, 0, brightness, 0, brightness.length);
        brightness[brightness.length - 1] = (byte) percentage;
        return brightness;
    }

    public void setEffect(String effectName) throws CommunicationException {
        sendRaw(getBytesForEffect(effectName));
    }

    protected byte[] getBytesForEffect(String effectName) {
        byte[] effect = new byte[prefix.length + colorCmd.length + effects.get(effectName).length];
        System.arraycopy(prefix, 0, effect, 0, prefix.length);
        System.arraycopy(colorCmd, 0, effect, prefix.length, colorCmd.length);
        System.arraycopy(effects.get(effectName), 0, effect, prefix.length + colorCmd.length,
                effects.get(effectName).length);
        return effect;
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte aByte : bytes) {
            builder.append(String.format("%02x", aByte));
        }
        return builder.toString();
    }

    private byte[] sendRaw(byte[] data) throws CommunicationException {
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
