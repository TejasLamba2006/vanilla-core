package com.tejaslamba.vanillacore.features;

public class EndLockFeature extends DimensionLockFeature {
    public EndLockFeature() {
        super("end");
    }

    @Override
    public int getDisplayOrder() {
        return 21;
    }
}
