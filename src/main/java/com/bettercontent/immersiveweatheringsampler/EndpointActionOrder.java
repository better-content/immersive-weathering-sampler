package com.bettercontent.immersiveweatheringsampler;

/**
 * Establishes one stable application order for endpoint actions collected from
 * unordered Immersive Weathering rule sets.
 */
final class EndpointActionOrder {
    private EndpointActionOrder() {
    }

    static int compare(
            final long firstPosition,
            final long firstSeed,
            final String firstDiscriminator,
            final long secondPosition,
            final long secondSeed,
            final String secondDiscriminator
    ) {
        int comparison = Long.compare(firstPosition, secondPosition);
        if (comparison != 0) return comparison;
        comparison = firstDiscriminator.compareTo(secondDiscriminator);
        if (comparison != 0) return comparison;
        return Long.compareUnsigned(firstSeed, secondSeed);
    }
}
