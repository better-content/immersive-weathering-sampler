package com.bettercontent.immersiveweatheringsampler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class EndpointActionOrderTest {
    @Test
    void resolvesFormerXorSortCollisionsByStableDiscriminator() {
        // 1 ^ 2 equals 0 ^ 3: the previous XOR key made this pair depend on
        // collection order whenever rule collection came from a HashSet.
        assertTrue(EndpointActionOrder.compare(1L, 2L, "rain", 0L, 3L, "clear") > 0);
        assertTrue(EndpointActionOrder.compare(0L, 3L, "clear", 1L, 2L, "rain") < 0);
    }

    @Test
    void samePositionUsesRuleIdentityBeforeUnsignedSeed() {
        assertTrue(EndpointActionOrder.compare(42L, -1L, "rain:fungus", 42L, 1L, "rain:moss") < 0);
        assertTrue(EndpointActionOrder.compare(42L, 1L, "same", 42L, -1L, "same") < 0);
        assertEquals(0, EndpointActionOrder.compare(42L, 1L, "same", 42L, 1L, "same"));
    }
}
