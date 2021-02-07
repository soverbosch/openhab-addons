package org.openhab.binding.cololight.internal;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import jersey.repackaged.com.google.common.collect.Lists;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.junit.*;
import org.openhab.binding.cololight.internal.exception.CommunicationException;

@Ignore
public class LedStripDriverTest {

    @Before
    public void setup() throws SocketException {
        new LedStripMock().start();
    }

    @After
    public void tearDown() throws IOException {
        DatagramSocket socket = new DatagramSocket();
        InetAddress address = InetAddress.getByName("localhost");
        byte[] buf = "end".getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 8900);
        socket.send(packet);
        socket.close();
    }

    @Test
    public void testSetPowerOn() throws Throwable {
        // given
        LedStripDriver underTest = new LedStripDriver();

        // when
        boolean result = underTest.setPowerOn();

        // Then
        Assert.assertTrue(result);
    }

    @Test
    public void testSetPowerOff() throws Throwable {
        // given
        LedStripDriver underTest = new LedStripDriver();

        // when
        boolean result = underTest.setPowerOff();

        // Then
        Assert.assertTrue(result);
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
    public void testGetStatusIsOk() {
        // given
        LedStripDriver underTest = new LedStripDriver();

        // when
        boolean result = underTest.getStatusIsOk();

        // then
        Assert.assertTrue(result);
    }

    @Test
    public void testGetLedStripStatus() throws CommunicationException {
        // given
        LedStripDriver underTest = new LedStripDriver();

        // when
        LedStripStatus result = underTest.getLedStripStatus();

        // then
        Assert.assertTrue(result.getPower().equals(OnOffType.ON));
        Assert.assertEquals(new PercentType("53"), result.getBrightness());
        // Following are for now default values, as we cannot (yet) get this information from the LED
        Assert.assertEquals(new PercentType("50"), result.getDelay());
        Assert.assertEquals(new StringType("pensieve"), result.getEffect());
    }

    @Test
    public void testSetBrightness() throws CommunicationException {
        // given
        LedStripDriver underTest = new LedStripDriver();

        // when
        int result1 = underTest.setBrightness(0);
        int result2 = underTest.setBrightness(10);

        // then
        Assert.assertEquals(0, result1);
        Assert.assertNotEquals(10, result2);
    }

    @Test
    public void testSetEffect() throws CommunicationException {
        // given
        LedStripDriver underTest = new LedStripDriver();

        // when
        underTest.setEffect("anystring");
        underTest.setEffect("cherryBlossom");

        // then
        Assert.assertEquals("cherryBlossom", underTest.getLedStripStatus().getEffect().toString());
    }

    @Test
    @Ignore
    public void liveTester() throws CommunicationException {
        // given
        LedStripDriver underTest = new LedStripDriver("192.168.0.218", 8900, 3000);

        // when
//        LedStripStatus result = underTest.getLedStripStatus();
//        System.out.println(result.toString());
//        underTest.setPowerOn();
//        result = underTest.getLedStripStatus();
//        System.out.println(result.toString());
//        underTest.setEffect("cherryBlossom");
//        result = underTest.getLedStripStatus();
//        System.out.println(result.toString());
//        underTest.setBrightness(51);
//        underTest.setBrightness(32);
//        result = underTest.getLedStripStatus();
//        System.out.println(result.toString());
//        underTest.setEffect("pensieve");
//        result = underTest.getLedStripStatus();
//        System.out.println(result.toString());
//        underTest.setPowerOff();
//        result = underTest.getLedStripStatus();
//        System.out.println(result.toString());
        AtomicReference<LedStripStatus> result = new AtomicReference<>(underTest.getLedStripStatus());
        System.out.println(result.toString());
        underTest.setPowerOn();
        result.set(underTest.getLedStripStatus());
        System.out.println(result.toString());
        Lists.newArrayList(30 /*,20,30,40,50,60,70,80,90,100*/).forEach(index -> {
            try {
                underTest.setEffectDelay(index);
                underTest.setEffect("pensieve");
                result.set(underTest.getLedStripStatus());
                System.out.println(result.toString());
                Thread.sleep(1000);
            } catch (CommunicationException | InterruptedException communicationException) {
                communicationException.printStackTrace();
            }
        });
//        underTest.setPowerOff();
    }
}
