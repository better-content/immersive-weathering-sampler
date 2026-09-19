package com.bettercontent.immersiveweatheringsampler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

final class EndpointSamplingPolicyTest {
    @Test
    void fixedSeedHasStableAcceptanceAndRejectionCounts() {
        final RandomSource random = RandomSource.create(0x5EED_0707L);
        int accepted = 0;
        final int attempts = 20_000;
        for (int attempt = 0; attempt < attempts; attempt++) {
            if (ProbabilityMath.occurs(random, 240L, 0.0002D)) accepted++;
        }
        assertEquals(1014, accepted, "fixed seed guards both accepted and rejected endpoint samples");
    }

    @Test
    void unloadedOrZeroExposureNeverCreatesAnEndpointOpportunity() {
        final RandomSource random = RandomSource.create(12345L);
        assertFalse(ProbabilityMath.occurs(random, 0L, 1.0D));
        assertFalse(ProbabilityMath.occurs(random, 400L, 0.0D));
        assertTrue(ProbabilityMath.occurs(RandomSource.create(12345L), 400L, 1.0D));
    }

    @Test
    void splittingAndReplayingAnExposureDoesNotIncreaseTheSingleEndpointBound() {
        final double oneEndpoint = ProbabilityMath.atLeastOne(800L, 0.0005D);
        final double firstHalf = ProbabilityMath.atLeastOne(400L, 0.0005D);
        final double secondHalf = ProbabilityMath.atLeastOne(400L, 0.0005D);
        assertEquals(oneEndpoint, 1.0D - (1.0D - firstHalf) * (1.0D - secondHalf), 1.0E-15D);
        assertTrue(oneEndpoint <= 1.0D);
    }
}
