package com.bettercontent.immersiveweatheringsampler;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;

/** Stores snapshots in chunk NBT without attaching a ticking capability. */
public final class ChunkExposureData {
    private static final String ROOT = "BetterContentImmersiveWeatheringSampler";
    private static final int SCHEMA = 1;
    private static final Map<ResourceKey<Level>, Map<Long, Entry>> LOADED = new HashMap<>();

    private ChunkExposureData() {
    }

    public static boolean initialized(final ServerLevel level, final ChunkAccess chunk) {
        final Entry entry = entry(level, chunk);
        return entry != null && entry.initialized;
    }

    public static ExposureClock read(final ServerLevel level, final ChunkAccess chunk) {
        final Entry entry = entry(level, chunk);
        return entry == null ? ExposureClock.ZERO : entry.clock;
    }

    public static void write(final ServerLevel level, final ChunkAccess chunk, final ExposureClock clock) {
        entries(level).put(chunk.getPos().toLong(), new Entry(true, clock));
        chunk.setUnsaved(true);
    }

    public static void load(final ServerLevel level, final ChunkAccess chunk, final CompoundTag chunkTag) {
        final CompoundTag data = chunkTag.getCompound(ROOT);
        final boolean initialized = data.getInt("Schema") == SCHEMA;
        entries(level).put(
            chunk.getPos().toLong(),
            new Entry(initialized, initialized ? ExposureClock.load(data, "Exposure") : ExposureClock.ZERO)
        );
    }

    public static void save(final ServerLevel level, final ChunkAccess chunk, final CompoundTag chunkTag) {
        final Entry entry = entry(level, chunk);
        if (entry == null || !entry.initialized) return;
        final CompoundTag data = new CompoundTag();
        data.putInt("Schema", SCHEMA);
        entry.clock.save(data, "Exposure");
        chunkTag.put(ROOT, data);
    }

    private static Entry entry(final ServerLevel level, final ChunkAccess chunk) {
        return entries(level).get(chunk.getPos().toLong());
    }

    private static Map<Long, Entry> entries(final ServerLevel level) {
        return LOADED.computeIfAbsent(level.dimension(), ignored -> new HashMap<>());
    }

    private record Entry(boolean initialized, ExposureClock clock) {
    }
}
