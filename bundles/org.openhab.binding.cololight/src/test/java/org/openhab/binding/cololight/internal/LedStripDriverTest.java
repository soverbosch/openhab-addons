package org.openhab.binding.cololight.internal;

import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class LedStripDriverTest {

    @Test
    @Ignore
    public void testSetPowerOn() throws Throwable {
        // give
        LedStripDriver underTest = new LedStripDriver();

        // when
        underTest.setPowerOn();
    }

    @Test
    @Ignore
    public void testSetPowerOff() throws Throwable {
        // give
        LedStripDriver underTest = new LedStripDriver();

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
