package com.bettercontent.immersiveweatheringsampler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

final class ExposureClockTest {
    @Test
    void partitionsWeatherAndDaylightWithoutLosingTime() {
        final ExposureClock clock = ExposureClock.ZERO
            .add(10, false, true)
            .add(20, false, false)
            .add(30, true, true)
            .add(40, true, false);

        assertEquals(100, clock.total());
        assertEquals(30, clock.clear());
        assertEquals(70, clock.rain());
        assertEquals(new ExposureClock(10, 20, 30, 40), clock);
    }

    @Test
    void subtractionClampsAfterClockReset() {
        final ExposureClock current = new ExposureClock(2, 4, 6, 8);
        final ExposureClock previous = new ExposureClock(3, 1, 9, 2);

        assertEquals(new ExposureClock(0, 3, 0, 6), current.subtractClamped(previous));
    }

    @Test
    void accumulatedExposureSaturatesInsteadOfWrappingAtLongBoundary() {
        final ExposureClock clock = new ExposureClock(Long.MAX_VALUE - 2, 0, 0, 0)
            .add(10, false, true);

        assertEquals(Long.MAX_VALUE, clock.clearDay());
        assertEquals(Long.MAX_VALUE, clock.total());
    }

    @Test
    void totalSaturatesWhenSeveralValidBucketsExceedLongRange() {
        assertEquals(Long.MAX_VALUE, new ExposureClock(Long.MAX_VALUE, 1, 0, 0).total());
    }

    @Test
    void subtractingNegativeCorruptSnapshotCannotOverflowPositive() {
        assertEquals(Long.MAX_VALUE, new ExposureClock(Long.MAX_VALUE, 0, 0, 0)
            .subtractClamped(new ExposureClock(Long.MIN_VALUE, 0, 0, 0)).clearDay());
    }

    @Test
    void nbtRoundTripIsLossless() {
        final ExposureClock expected = new ExposureClock(11, 22, 33, 44);
        final CompoundTag tag = new CompoundTag();
        expected.save(tag, "Test");

        assertEquals(expected, ExposureClock.load(tag, "Test"));
    }
}
