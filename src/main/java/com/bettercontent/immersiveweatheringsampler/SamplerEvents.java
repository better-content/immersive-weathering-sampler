package com.bettercontent.immersiveweatheringsampler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkDataEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class SamplerEvents {
    private static final Map<ResourceKey<Level>, Set<Long>> PENDING = new HashMap<>();
    private static final Map<ResourceKey<Level>, Set<Long>> DEFERRED_UNLOAD_SAVES = new HashMap<>();

    private SamplerEvents() {
    }

    @SubscribeEvent
    public static void onChunkDataLoad(final ChunkDataEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        deferred(level).remove(event.getChunk().getPos().toLong());
        ChunkExposureData.load(level, event.getChunk(), event.getData());
    }

    @SubscribeEvent
    public static void onChunkLoad(final ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getChunk() instanceof LevelChunk chunk)) return;
        final ExposureClock now = ExposureSavedData.get(level).clock();
        if (event.isNewChunk() || !ChunkExposureData.initialized(level, chunk)) {
            ChunkExposureData.write(level, chunk, now);
            return;
        }
        pending(level).add(chunk.getPos().toLong());
    }

    @SubscribeEvent
    public static void onLevelTick(final TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.START || !(event.level instanceof ServerLevel level)) return;
        final ExposureSavedData exposure = ExposureSavedData.get(level);
        exposure.advance(level);
        final Set<Long> positions = PENDING.remove(level.dimension());
        if (positions == null || positions.isEmpty()) return;
        final ExposureClock now = exposure.clock();
        for (long packedPos : positions) {
            final ChunkPos pos = new ChunkPos(packedPos);
            final LevelChunk chunk = level.getChunkSource().getChunkNow(pos.x, pos.z);
            if (chunk == null || !ChunkExposureData.initialized(level, chunk)) continue;
            final ExposureClock elapsed = now.subtractClamped(ChunkExposureData.read(level, chunk));
            if (SamplerConfig.ENABLED.get() && elapsed.total() >= SamplerConfig.MINIMUM_UNLOADED_TICKS.get()) {
                EndpointSampler.sample(level, chunk, elapsed);
            }
            ChunkExposureData.write(level, chunk, now);
        }
    }

    @SubscribeEvent
    public static void onChunkSave(final ChunkDataEvent.Save event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getChunk() instanceof LevelChunk chunk)) return;
        final long position = chunk.getPos().toLong();
        if (!pending(level).contains(position) && !deferred(level).remove(position)) {
            ChunkExposureData.write(level, chunk, ExposureSavedData.get(level).clock());
        }
        ChunkExposureData.save(level, chunk, event.getData());
    }

    @SubscribeEvent
    public static void onChunkUnload(final ChunkEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getChunk() instanceof LevelChunk chunk)) return;
        final long position = chunk.getPos().toLong();
        if (pending(level).remove(position)) {
            // If unloading wins the race with the first level tick, keep the
            // old snapshot so this elapsed exposure remains available later.
            deferred(level).add(position);
        } else {
            ChunkExposureData.write(level, chunk, ExposureSavedData.get(level).clock());
        }
    }

    private static Set<Long> pending(final ServerLevel level) {
        return PENDING.computeIfAbsent(level.dimension(), ignored -> new HashSet<>());
    }

    private static Set<Long> deferred(final ServerLevel level) {
        return DEFERRED_UNLOAD_SAVES.computeIfAbsent(level.dimension(), ignored -> new HashSet<>());
    }
}
