package com.bettercontent.immersiveweatheringsampler;

import net.minecraft.util.RandomSource;

public final class ChanceBypass {
    private static final ThreadLocal<RandomSource> ACTIVE_RANDOM = new ThreadLocal<>();

    private ChanceBypass() {
    }

    public static boolean active() {
        return ACTIVE_RANDOM.get() != null;
    }

    public static RandomSource randomOr(final RandomSource fallback) {
        final RandomSource random = ACTIVE_RANDOM.get();
        return random == null ? fallback : random;
    }

    public static void run(final RandomSource random, final Runnable action) {
        if (ACTIVE_RANDOM.get() != null) throw new IllegalStateException("Nested endpoint sampling context");
        ACTIVE_RANDOM.set(random);
        try {
            action.run();
        } finally {
            ACTIVE_RANDOM.remove();
        }
    }
}

