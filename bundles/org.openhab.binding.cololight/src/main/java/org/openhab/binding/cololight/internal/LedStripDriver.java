package org.openhab.binding.cololight.internal;

import java.io.IOException;
import java.net.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LedStripDriver {
    private final Logger logger = LoggerFactory.getLogger(LedStripDriver.class);

    private final String host;
    private final int port;

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
    protected static byte[] brightnessFiftyPercent = { (byte) 0x53, (byte) 0x5A, (byte) 0x30, (byte) 0x30, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x17, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04,
            (byte) 0x16, (byte) 0x03, (byte) 0x01, (byte) 0xCF, (byte) 0x2F };
    protected static byte[] brightnessHundredPercent = { (byte) 0x53, (byte) 0x5a, (byte) 0x30, (byte) 0x30,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x20, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x15, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x04, (byte) 0x16, (byte) 0x03, (byte) 0x01, (byte) 0xCF, (byte) 0x63 };

    public LedStripDriver() {
        this.port = 8900;
        this.host = "localhost";
    }

    public LedStripDriver(String host, int port) {
        this.host = host;
        this.port = port;
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

    public void setPowerOn() {
        sendRaw(getBytesForPower(true));
    }

    public void setPowerOff() {
        sendRaw(getBytesForPower(false));
    }

    public void setBrightness(int percentage) {
        logger.debug("Brightness {}%", percentage);

        sendRaw(getBytesForBrightness(percentage));
    }

    protected byte[] getBytesForBrightness(int percentage) {
        byte[] result;
        switch (percentage) {
            default:
            case 0:
                result = brightnessZeroPercent;
                break;
            case 50:
                result = brightnessFiftyPercent;
                break;
            case 100:
                result = brightnessHundredPercent;
                break;
        }
        return result;
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte aByte : bytes) {
            builder.append(String.format("%02x", aByte));
        }
        return builder.toString();
    }

    private void sendRaw(byte[] data) {
        try {
            DatagramSocket socket = new DatagramSocket();
            InetAddress address = InetAddress.getByName(host);

            System.out.printf("Sending %s%n", bytesToHex(data));
            DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
            socket.send(packet);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
