package com.tunxxvn.modernice.config;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.File;

public final class IceConfig {
    public static final double MIN = 0.98D;
    public static final double MAX = 1.0D;
    public static final String NOTE = "0.98 is normal ice's slipperiness\n0.989 is blue ice's slipperiness";
    private final Configuration file;
    private boolean enabled = true;
    private boolean customSlipperiness = true;
    private double slipperiness = MIN;

    public IceConfig(File path) {
        file = new Configuration(path);
        file.load();
        enabled = file.get("physics", "enabled", true).getBoolean(true);
        customSlipperiness = file.get("physics", "customSlipperiness", true).getBoolean(true);
        Property value = property();
        slipperiness = clamp(value.getDouble(MIN));
        save();
    }

    private Property property() {
        return file.get("physics", "slipperiness", MIN, NOTE, MIN, MAX);
    }

    public boolean enabled() { return enabled; }
    public boolean customSlipperiness() { return customSlipperiness; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setCustomSlipperiness(boolean enabled) { customSlipperiness = enabled; }
    public double value() { return slipperiness; }
    public float slipperiness() { return (float) slipperiness; }
    public void set(double value) { slipperiness = clamp(value); }

    public void save() {
        file.get("physics", "enabled", true).set(enabled);
        file.get("physics", "customSlipperiness", true).set(customSlipperiness);
        property().set(slipperiness);
        if (file.hasChanged()) file.save();
    }

    private static double clamp(double value) {
        return Double.isNaN(value) || Double.isInfinite(value) ? MIN : Math.max(MIN, Math.min(MAX, value));
    }
}
