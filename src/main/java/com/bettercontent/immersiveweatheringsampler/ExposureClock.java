package com.bettercontent.immersiveweatheringsampler;

import net.minecraft.nbt.CompoundTag;

public record ExposureClock(long clearDay, long clearNight, long rainDay, long rainNight) {
    public static final ExposureClock ZERO = new ExposureClock(0, 0, 0, 0);

    public long total() {
        return clearDay + clearNight + rainDay + rainNight;
    }

    public long clear() {
        return clearDay + clearNight;
    }

    public long rain() {
        return rainDay + rainNight;
    }

    public ExposureClock add(final long ticks, final boolean raining, final boolean day) {
        if (ticks <= 0) return this;
        if (raining && day) return new ExposureClock(clearDay, clearNight, rainDay + ticks, rainNight);
        if (raining) return new ExposureClock(clearDay, clearNight, rainDay, rainNight + ticks);
        if (day) return new ExposureClock(clearDay + ticks, clearNight, rainDay, rainNight);
        return new ExposureClock(clearDay, clearNight + ticks, rainDay, rainNight);
    }

    public ExposureClock subtractClamped(final ExposureClock earlier) {
        return new ExposureClock(
                Math.max(0, clearDay - earlier.clearDay),
                Math.max(0, clearNight - earlier.clearNight),
                Math.max(0, rainDay - earlier.rainDay),
                Math.max(0, rainNight - earlier.rainNight));
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

