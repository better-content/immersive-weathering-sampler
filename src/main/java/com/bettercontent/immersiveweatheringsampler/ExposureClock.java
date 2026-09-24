package com.bettercontent.immersiveweatheringsampler;

import net.minecraft.nbt.CompoundTag;

public record ExposureClock(long clearDay, long clearNight, long rainDay, long rainNight) {
    public static final ExposureClock ZERO = new ExposureClock(0, 0, 0, 0);

    public long total() {
        return saturatingAdd(saturatingAdd(clearDay, clearNight), saturatingAdd(rainDay, rainNight));
    }

    public long clear() {
        return saturatingAdd(clearDay, clearNight);
    }

    public long rain() {
        return saturatingAdd(rainDay, rainNight);
    }

    public ExposureClock add(final long ticks, final boolean raining, final boolean day) {
        if (ticks <= 0) return this;
        if (raining && day) return new ExposureClock(clearDay, clearNight, saturatingAdd(rainDay, ticks), rainNight);
        if (raining) return new ExposureClock(clearDay, clearNight, rainDay, saturatingAdd(rainNight, ticks));
        if (day) return new ExposureClock(saturatingAdd(clearDay, ticks), clearNight, rainDay, rainNight);
        return new ExposureClock(clearDay, saturatingAdd(clearNight, ticks), rainDay, rainNight);
    }

    public ExposureClock subtractClamped(final ExposureClock earlier) {
        return new ExposureClock(
                subtractClamped(clearDay, earlier.clearDay),
                subtractClamped(clearNight, earlier.clearNight),
                subtractClamped(rainDay, earlier.rainDay),
                subtractClamped(rainNight, earlier.rainNight));
    }

    private static long saturatingAdd(final long value, final long increment) {
        if (increment <= 0) return value;
        return value > Long.MAX_VALUE - increment ? Long.MAX_VALUE : value + increment;
    }

    private static long subtractClamped(final long value, final long earlier) {
        if (value <= earlier) return 0;
        if (earlier < 0 && value > Long.MAX_VALUE + earlier) return Long.MAX_VALUE;
        return value - earlier;
    }

    public void save(final CompoundTag tag, final String prefix) {
        tag.putLong(prefix + "ClearDay", clearDay);
        tag.putLong(prefix + "ClearNight", clearNight);
        tag.putLong(prefix + "RainDay", rainDay);
        tag.putLong(prefix + "RainNight", rainNight);
    }

    public static ExposureClock load(final CompoundTag tag, final String prefix) {
        return new ExposureClock(
                tag.getLong(prefix + "ClearDay"),
                tag.getLong(prefix + "ClearNight"),
                tag.getLong(prefix + "RainDay"),
                tag.getLong(prefix + "RainNight"));
    }
}
