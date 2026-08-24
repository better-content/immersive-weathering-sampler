package com.bettercontent.immersiveweatheringsampler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class ProbabilityMathTest {
    @Test
    void randomTickSelectionMatchesThreeIndependentSectionPicks() {
        final double expected = 1.0D - Math.pow(4095.0D / 4096.0D, 3);
        assertEquals(expected, ProbabilityMath.randomSelectionChance(3), 1.0E-15D);
    }

    @Test
    void elapsedSamplingComputesAtLeastOneWithoutReplayingTicks() {
        final double oneTick = ProbabilityMath.atLeastOne(1, 0.125D);
        final double longAbsence = ProbabilityMath.atLeastOne(10_000, 0.001D);

        assertEquals(0.125D, oneTick, 1.0E-15D);
        assertTrue(longAbsence > 0.999D);
        assertEquals(0.0D, ProbabilityMath.atLeastOne(0, 0.5D));
    }

    @Test
    void probabilitiesAreBounded() {
        assertEquals(0.0D, ProbabilityMath.clampProbability(Double.NaN));
        assertEquals(0.0D, ProbabilityMath.clampProbability(-1.0D));
        assertEquals(1.0D, ProbabilityMath.clampProbability(2.0D));
    }
}
