package com.bettercontent.immersiveweatheringsampler;

import net.minecraft.util.RandomSource;

public final class ProbabilityMath {
    private ProbabilityMath() {
    }

    public static double randomSelectionChance(final int randomTickSpeed) {
        if (randomTickSpeed <= 0) return 0.0D;
        return -Math.expm1(randomTickSpeed * Math.log1p(-1.0D / 4096.0D));
    }

    public static double surfaceSelectionChance(final int randomTickSpeed) {
        return clampProbability(randomTickSpeed / (48.0D * 256.0D));
    }

    public static double atLeastOne(final long exposureTicks, final double perTickChance) {
        if (exposureTicks <= 0 || perTickChance <= 0.0D) return 0.0D;
        if (perTickChance >= 1.0D) return 1.0D;
        return clampProbability(-Math.expm1(exposureTicks * Math.log1p(-perTickChance)));
    }

    public static boolean occurs(final RandomSource random, final long exposureTicks, final double perTickChance) {
        return random.nextDouble() < atLeastOne(exposureTicks, perTickChance);
    }

    public static double clampProbability(final double value) {
        if (!Double.isFinite(value) || value <= 0.0D) return 0.0D;
        return Math.min(1.0D, value);
    }
}

