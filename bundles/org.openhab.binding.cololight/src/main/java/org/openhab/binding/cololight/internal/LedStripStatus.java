package org.openhab.binding.cololight.internal;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;

public class LedStripStatus {
    private OnOffType power;
    private PercentType brightness;

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

    @Override
    public String toString() {
        return "LedStripStatus{" + "power = " + power + ", brightness = " + brightness + '}';
    }
}
