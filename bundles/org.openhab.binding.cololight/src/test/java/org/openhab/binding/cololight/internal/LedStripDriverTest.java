package org.openhab.binding.cololight.internal;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class LedStripDriverTest {

    @Test
    @Ignore
    public void testSetPowerOn() throws Throwable {
        // give
        LedStripDriver underTest = new LedStripDriver("192.168.0.218", 8900);

        // when
        underTest.setPowerOn();
    }

    @Test
    @Ignore
    public void testSetPowerOff() throws Throwable {
        // give
        LedStripDriver underTest = new LedStripDriver("192.168.0.218", 8900);

        // when
        underTest.setPowerOff();
    }

    @Test
    public void testBytesToHex() {
        // given
        byte[] bytes = { (byte) 0x12, (byte) 0xFF };

        // when
        String result = LedStripDriver.bytesToHex(bytes);

        // then
        Assert.assertEquals("12ff", result);
    }

    @Test
    public void testGetBytesForPower() {
        // given
        LedStripDriver underTest = new LedStripDriver();

        // when
        byte[] on = underTest.getBytesForPower(true);
        byte[] off = underTest.getBytesForPower(false);

        // then
        String onString = LedStripDriver.bytesToHex(on);
        String offString = LedStripDriver.bytesToHex(off);
        Assert.assertNotEquals(onString, offString);
        Assert.assertEquals("535a3030000000000020000000000000000000000000000000000100000000000000000004010301cf35",
                onString);
        Assert.assertEquals("535a3030000000000020000000000000000000000000000000000100000000000000000004010301ce1e",
                offString);
    }

    @Test
    public void testGetBytesForBrightness() {
        // given
        LedStripDriver underTest = new LedStripDriver();

        // when
        byte[] zeroPercentBrightness = underTest.getBytesForBrightness(0);
        byte[] fiftyPercentBrightness = underTest.getBytesForBrightness(50);
        byte[] hundredPercentBrightness = underTest.getBytesForBrightness(100);

        // then
        String zeroPercentBrightnessString = LedStripDriver.bytesToHex(zeroPercentBrightness);
        String fiftyPercentBrightnessString = LedStripDriver.bytesToHex(fiftyPercentBrightness);
        String hunderdPercentBrightnessString = LedStripDriver.bytesToHex(hundredPercentBrightness);
        Assert.assertEquals("535a3030000000000020000000000000000000000000000000001600000000000000000004160301cf00",
                zeroPercentBrightnessString); // 0
        Assert.assertEquals("535a3030000000000020000000000000000000000000000000001700000000000000000004160301cf2f",
                fiftyPercentBrightnessString); // ~50
        Assert.assertEquals("535a3030000000000020000000000000000000000000000000001500000000000000000004160301cf63",
                hunderdPercentBrightnessString); // 100
    }

    // Send following requests twice
    // Req: 53 5a 30 30 00 00 00 00 00 1e 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 20 00 00
    // 00 00 0b 01 01 86
    // 535a303080000000002c0000000000000418000000000000000001000020000000000040800001430d01384341414235413543304543
    // 535a303080000000002c00000000000003d8000000000000000001000020000000000000800001430d01384341414235413543304543
    // 535a303080000000002c0000000000000458000000000000000001000020000000000080800001430d01384341414235413543304543

    // Req: 53 5a 30 30 00 00 00 00 00 1e 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 00 00 00 00 00 00 00 00 00
    // 03 02 01 01
    // 535a303080000000002e00000000000004c100000000000000000200000000000000003f83020301cf350d01384341414235413543304543
    // 535a303080000000002e00000000000004c200000000000000000200000000000000004083020301cf350d01384341414235413543304543
    // 535a303080000000002e00000000000004c100000000000000000200000000000000003f83020301cf350d01384341414235413543304543
    // 535a303080000000002e00000000000005350000000000000000020000000000000000b383020301cf350d01384341414235413543304543
    // 535a303080000000002e000000000000048200000000000000000200000000000000000083020301cf350d01384341414235413543304543

    // Req: 53 5a 30 30 00 00 00 00 00 1e 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 11 00 00 00 00 00 00 00 00 00
    // 0b 07 01 86
    // 535a303080000000004100000000000004670000000000000000110000000000000000408b071600003c003c03000000000000000000000000000000000d01384341414235413543304543

    // Req: 53 5a 30 30 00 00 00 00 00 1e 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 08 00 00 00 00 00 00 00 00 00
    // 03 08 01 01
    // 535a303080000000002e00000000000004cd00000000000000000800000000000000003f83080301cf350d01384341414235413543304543

    @Test
    @Ignore
    public void tester() throws Throwable {
        byte[] cmd1 = { (byte) 0x53, (byte) 0x5a, (byte) 0x30, (byte) 0x30, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x1e, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0b,
                (byte) 0x01, (byte) 0x01, (byte) 0x86 };
        byte[] cmd2 = { (byte) 0x53, (byte) 0x5a, (byte) 0x30, (byte) 0x30, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x1e, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x02, (byte) 0x01,
                (byte) 0x01 };

        byte[] msg1 = { (byte) 0x53, (byte) 0x5a, (byte) 0x30, (byte) 0x30, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x1e, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x11, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0b, (byte) 0x07, (byte) 0x01,
                (byte) 0x86 };
        byte[] msg2 = { (byte) 0x53, (byte) 0x5a, (byte) 0x30, (byte) 0x30, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x1e, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x08, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x08, (byte) 0x01,
                (byte) 0x01 };

        // given
        LedStripDriver underTest = new LedStripDriver();

        // byte[] data = msg2;

        try {
            DatagramSocket socket = new DatagramSocket();
            System.out.println(socket.getSoTimeout());
            socket.setSoTimeout(3000);
            System.out.println(socket.getSoTimeout());
            if (!InetAddress.getByName("192.168.0.218").isReachable(3000))
                System.err.println("Not reachable");
            InetAddress address = InetAddress.getByName("192.168.0.218");

            System.out.printf("Sending  %s%n", LedStripDriver.bytesToHex(msg1));
            DatagramPacket packet = new DatagramPacket(msg1, msg1.length, address, 8900);
            socket.send(packet);

            byte[] buf = new byte[256];
            packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            byte[] received = new byte[packet.getLength()];
            System.arraycopy(packet.getData(), 0, received, 0, packet.getLength());
            System.out.printf("Received %s (numberOfBytes %d)%n", LedStripDriver.bytesToHex(received),
                    packet.getLength());

            // TimeUnit.MICROSECONDS.sleep(50000);
            // socket = new DatagramSocket();
            System.out.printf("Sending  %s%n", LedStripDriver.bytesToHex(msg2));
            packet = new DatagramPacket(msg2, msg2.length, address, 8900);
            socket.send(packet);

            buf = new byte[256];
            packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            received = new byte[packet.getLength()];
            System.arraycopy(packet.getData(), 0, received, 0, packet.getLength());
            System.out.printf("Received %s (numberOfBytes %d)%n", LedStripDriver.bytesToHex(received),
                    packet.getLength());
            System.out.printf("received[40]: %s (%s)%n", LedStripDriver.bytesToHex(new byte[] { received[40] }),
                    received[40] == (byte) 0xCF);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    @Ignore
    public void testBrightness() throws InterruptedException {
        // given
        LedStripDriver underTest = new LedStripDriver();
        byte[] brightness = new byte[LedStripDriver.brightnessZeroPercent.length];
        System.arraycopy(LedStripDriver.brightnessZeroPercent, 0, brightness, 0, brightness.length);

        // Brightness steps of ..
        int step = 10;
        for (Integer i = 0; i <= 100; i += step) {
            brightness[brightness.length - 1] = i.byteValue();
            System.out.println(LedStripDriver.bytesToHex(brightness));
            // underTest.sendRawCommand(brightness);
            TimeUnit.SECONDS.sleep(2);
        }
        System.out.println("going to fifty");
        // underTest.sendRawCommand(LedStripDriver.brightnessFiftyPercent);
        System.out.println("going to hundred");
        // underTest.sendRawCommand(LedStripDriver.brightnessHundredPercent);
    }
}
