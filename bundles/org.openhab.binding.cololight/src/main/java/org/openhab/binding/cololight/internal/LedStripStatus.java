package org.openhab.binding.cololight.internal;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;

public class LedStripStatus {
    private OnOffType power;
    private PercentType brightness;
    private PercentType delay;
    private StringType effect;

    public PercentType getDelay() {
        return delay;
    }

    public void setDelay(PercentType delay) {
        this.delay = delay;
    }

    public PercentType getBrightness() {
        return brightness;
    }

    public void setBrightness(PercentType brightness) {
        this.brightness = brightness;
    }

    public void setPower(OnOffType power) {
        this.power = power;
    }

    public OnOffType getPower() {
        return power;
    }

    public void setEffect(StringType effect) {
        this.effect = effect;
    }

    public StringType getEffect() {
        return this.effect;
    }

    @Override
    public String toString() {
        return "LedStripStatus{power = " + power + ", brightness = " + brightness + ", effect = " + effect
                + ", delay = " + delay + '}';
    }
}
