package com.tejaslamba.vanillacore.features;

public class NetherLockFeature extends DimensionLockFeature {
    public NetherLockFeature() {
        super("nether");
    }

    @Override
    public int getDisplayOrder() {
        return 20;
    }
}
