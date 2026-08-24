package com.bettercontent.immersiveweatheringsampler;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public final class ExposureSavedData extends SavedData {
    private static final String NAME = ImmersiveWeatheringSampler.MOD_ID + "_exposure";
    private ExposureClock clock = ExposureClock.ZERO;
    private long lastObservedGameTime = Long.MIN_VALUE;

    public static ExposureSavedData get(final ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(ExposureSavedData::load, ExposureSavedData::new, NAME);
    }

    public void advance(final ServerLevel level) {
        final long now = level.getGameTime();
        if (lastObservedGameTime == Long.MIN_VALUE || now < lastObservedGameTime) {
            lastObservedGameTime = now;
            setDirty();
            return;
        }
        final long elapsed = now - lastObservedGameTime;
        if (elapsed > 0) {
            clock = clock.add(elapsed, level.isRaining(), level.isDay());
            lastObservedGameTime = now;
            setDirty();
        }
    }

    public ExposureClock clock() {
        return clock;
    }

    @Override
    public CompoundTag save(final CompoundTag tag) {
        clock.save(tag, "");
        tag.putLong("LastObservedGameTime", lastObservedGameTime);
        return tag;
    }

    private static ExposureSavedData load(final CompoundTag tag) {
        final ExposureSavedData data = new ExposureSavedData();
        data.clock = ExposureClock.load(tag, "");
        data.lastObservedGameTime = tag.getLong("LastObservedGameTime");
        return data;
    }
}

