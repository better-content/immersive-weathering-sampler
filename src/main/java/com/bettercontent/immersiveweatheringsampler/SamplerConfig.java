package com.bettercontent.immersiveweatheringsampler;

import net.minecraftforge.common.ForgeConfigSpec;

public final class SamplerConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ENABLED;
    public static final ForgeConfigSpec.IntValue MINIMUM_UNLOADED_TICKS;
    public static final ForgeConfigSpec.DoubleValue DENSITY_MULTIPLIER;
    public static final ForgeConfigSpec.BooleanValue DEBUG_LOGGING;

    static {
        final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("endpointSampler");
        ENABLED = builder.comment("Samples one direct Immersive Weathering endpoint when a chunk returns.")
                .define("enabled", true);
        MINIMUM_UNLOADED_TICKS = builder.comment("Minimum elapsed game ticks before a chunk is sampled.")
                .defineInRange("minimumUnloadedTicks", 100, 1, 24_000);
        DENSITY_MULTIPLIER = builder.comment("Multiplier applied to Immersive Weathering rule probabilities.")
                .defineInRange("densityMultiplier", 1.0D, 0.0D, 8.0D);
        DEBUG_LOGGING = builder.define("debugLogging", false);
        builder.pop();
        SPEC = builder.build();
    }

    private SamplerConfig() {
    }
}

