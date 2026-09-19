package com.bettercontent.immersiveweatheringsampler;

import com.bettercontent.immersiveweatheringsampler.mixin.BlockGrowthHandlerAccessor;
import com.bettercontent.immersiveweatheringsampler.mixin.BuiltinBlockGrowthAccessor;
import com.ordana.immersive_weathering.blocks.cracked.Crackable;
import com.ordana.immersive_weathering.blocks.mossy.CrackableMossable;
import com.ordana.immersive_weathering.blocks.mossy.Mossable;
import com.ordana.immersive_weathering.blocks.rusty.Rustable;
import com.ordana.immersive_weathering.configs.CommonConfigs;
import com.ordana.immersive_weathering.data.block_growths.BlockGrowthHandler;
import com.ordana.immersive_weathering.data.block_growths.TickSource;
import com.ordana.immersive_weathering.data.block_growths.growths.ConfigurableBlockGrowth;
import com.ordana.immersive_weathering.data.block_growths.growths.IBlockGrowth;
import com.ordana.immersive_weathering.data.block_growths.growths.builtin.BuiltinBlockGrowth;
import com.ordana.immersive_weathering.reg.ModTags;
import com.ordana.immersive_weathering.util.Weatherable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;

public final class EndpointSampler {
    private EndpointSampler() {
    }

    public static void sample(final ServerLevel level, final LevelChunk chunk, final ExposureClock elapsed) {
        final int randomTickSpeed = level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
        if (randomTickSpeed <= 0 || elapsed.total() <= 0) return;

        final List<EndpointAction> actions = new ArrayList<>();
        final double randomSelection = ProbabilityMath.randomSelectionChance(randomTickSpeed);
        final double density = SamplerConfig.DENSITY_MULTIPLIER.get();
        final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        final LevelChunkSection[] sections = chunk.getSections();

        for (int sectionIndex = 0; sectionIndex < sections.length; sectionIndex++) {
            final LevelChunkSection section = sections[sectionIndex];
            if (section == null || section.hasOnlyAir()) continue;
            final int baseY = SectionPos.sectionToBlockCoord(chunk.getSectionYFromSectionIndex(sectionIndex));
            for (int localY = 0; localY < 16; localY++) {
                for (int localZ = 0; localZ < 16; localZ++) {
                    for (int localX = 0; localX < 16; localX++) {
                        final BlockState state = section.getBlockState(localX, localY, localZ);
                        if (state.isAir()) continue;
                        cursor.set(chunk.getPos().getMinBlockX() + localX, baseY + localY, chunk.getPos().getMinBlockZ() + localZ);
                        collectStructural(level, cursor.immutable(), state, elapsed, randomSelection, density, actions);
                        collectRules(level, cursor.immutable(), state, TickSource.BLOCK_TICK, elapsed.total(), randomSelection, density, actions);
                    }
                }
            }
        }

        collectSurfaceRules(level, chunk, elapsed, randomTickSpeed, density, actions);
        actions.sort((first, second) -> EndpointActionOrder.compare(
                first.pos().asLong(), first.seed(), first.discriminator(),
                second.pos().asLong(), second.seed(), second.discriminator()
        ));
        int applied = 0;
        for (EndpointAction action : actions) {
            if (!level.getBlockState(action.pos()).equals(action.expectedState())) continue;
            action.operation().run();
            applied++;
        }
        if (SamplerConfig.DEBUG_LOGGING.get()) {
            ImmersiveWeatheringSampler.LOGGER.info(
                    "Sampled {} endpoint actions for chunk {} after {} unloaded ticks",
                    applied,
                    chunk.getPos(),
                    elapsed.total());
        }
    }

    private static void collectStructural(
            final ServerLevel level,
            final BlockPos pos,
            final BlockState state,
            final ExposureClock elapsed,
            final double selectionChance,
            final double density,
            final List<EndpointAction> actions
    ) {
        if (state.getBlock() instanceof Rustable rustable) {
            final long seed = seed(level, pos, elapsed, "rust");
            final BlockState endpoint = sampleRustEndpoint(level, pos, state, rustable, elapsed, selectionChance, density, RandomSource.create(seed));
            if (!endpoint.equals(state)) {
                actions.add(new EndpointAction(pos, state, seed, "rust", () -> level.setBlock(pos, endpoint, 2)));
            }
            return;
        }
        if (!(state.getBlock() instanceof Weatherable weatherable) || !state.hasProperty(Weatherable.WEATHERABLE)) return;
        final long seed = seed(level, pos, elapsed, "patch");
        final RandomSource random = RandomSource.create(seed);
        if (!ProbabilityMath.occurs(random, elapsed.total(), selectionChance * weatherable.getWeatherChanceSpeed() * density)) return;

        BlockState endpoint = state;
        if (state.getBlock() instanceof CrackableMossable combined) {
            if (combined.getMossSpreader().getWantedWeatheringState(true, pos, level)) {
                endpoint = combined.getNextMossy(state).orElse(state);
            } else if (combined.getCrackSpreader().getWantedWeatheringState(true, pos, level)) {
                endpoint = combined.getNextCracked(state).orElse(state);
            }
        } else if (state.getBlock() instanceof Mossable mossable
                && mossable.getMossSpreader().getWantedWeatheringState(true, pos, level)) {
            endpoint = mossable.getNextMossy(state).orElse(state);
        } else if (state.getBlock() instanceof Crackable crackable
                && crackable.getCrackSpreader().getWantedWeatheringState(true, pos, level)) {
            endpoint = crackable.getNextCracked(state).orElse(state);
        } else {
            endpoint = state.setValue(Weatherable.WEATHERABLE, Weatherable.WeatheringState.FALSE);
        }
        if (!endpoint.equals(state)) {
            final BlockState finalEndpoint = endpoint;
            actions.add(new EndpointAction(pos, state, seed, "patch", () -> level.setBlock(pos, finalEndpoint, 2)));
        }
    }

    private static BlockState sampleRustEndpoint(
            final ServerLevel level,
            final BlockPos pos,
            final BlockState original,
            final Rustable initialRustable,
            final ExposureClock elapsed,
            final double selectionChance,
            final double density,
            final RandomSource random
    ) {
        BlockState current = original;
        Rustable rustable = initialRustable;
        for (int stage = 0; stage < 3; stage++) {
            final double clearCatalyst = rustCatalyst(level, pos, current, rustable, false);
            final double rainCatalyst = rustCatalyst(level, pos, current, rustable, true);
            final double clearNoChange = noOccurrence(elapsed.clear(), selectionChance * clearCatalyst * density);
            final double rainNoChange = noOccurrence(elapsed.rain(), selectionChance * rainCatalyst * density);
            if (random.nextDouble() >= 1.0D - clearNoChange * rainNoChange) break;
            final Optional<Block> next = Rustable.getIncreasedRustBlock(current.getBlock());
            if (next.isEmpty()) break;
            current = next.get().withPropertiesOf(current);
            if (!(current.getBlock() instanceof Rustable nextRustable)) break;
            rustable = nextRustable;
        }
        return current;
    }

    private static double rustCatalyst(
            final ServerLevel level,
            final BlockPos pos,
            final BlockState state,
            final Rustable rustable,
            final boolean raining
    ) {
        final double rate = CommonConfigs.RUSTING_RATE.get();
        double noCatalyst = 1.0D;
        for (Direction direction : Direction.values()) {
            final BlockState neighbor = level.getBlockState(pos.relative(direction));
            if (neighbor.is(Blocks.WET_SPONGE)) noCatalyst *= 1.0D - rate;
            if (neighbor.getFluidState().is(FluidTags.WATER)) noCatalyst *= 1.0D - rate / 1.25D;
            if (state.is(ModTags.CLEAN_IRON) && neighbor.isAir()) noCatalyst *= 1.0D - rate / 5.0D;
        }
        if (raining && (state.is(ModTags.CLEAN_IRON) || state.is(ModTags.EXPOSED_IRON)) && level.canSeeSky(pos.above())) {
            noCatalyst *= 1.0D - rate / 2.0D;
        }
        return (1.0D - noCatalyst) * rustNeighborInfluence(level, pos, rustable);
    }

    private static double rustNeighborInfluence(final ServerLevel level, final BlockPos pos, final Rustable rustable) {
        final int age = rustable.getAge().ordinal();
        int same = 0;
        int older = 0;
        final int radius = rustable.getInfluenceRadius();
        for (BlockPos neighborPos : BlockPos.withinManhattan(pos, radius, radius, radius)) {
            if (neighborPos.equals(pos) || neighborPos.distManhattan(pos) > radius) continue;
            if (!(level.getBlockState(neighborPos).getBlock() instanceof Rustable neighbor)) continue;
            final int neighborAge = neighbor.getAge().ordinal();
            if (neighborAge < age) return 0.0D;
            if (neighborAge == age) same++; else older++;
        }
        final double ratio = (older + 1.0D) / (older + same + 1.0D);
        final double speed = age == 0 ? 0.75D : 1.0D;
        return ratio * ratio * speed;
    }

    private static double noOccurrence(final long ticks, final double chance) {
        if (ticks <= 0 || chance <= 0.0D) return 1.0D;
        if (chance >= 1.0D) return 0.0D;
        return Math.exp(ticks * Math.log1p(-chance));
    }

    private static void collectSurfaceRules(
            final ServerLevel level,
            final LevelChunk chunk,
            final ExposureClock elapsed,
            final int randomTickSpeed,
            final double density,
            final List<EndpointAction> actions
    ) {
        final double selectionChance = ProbabilityMath.surfaceSelectionChance(randomTickSpeed);
        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                final int x = chunk.getPos().getMinBlockX() + localX;
                final int z = chunk.getPos().getMinBlockZ() + localZ;
                final BlockPos air = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(x, 0, z));
                final BlockPos pos = air.below();
                final BlockState state = level.getBlockState(pos);
                collectRules(level, pos, state, TickSource.CLEAR_SKY, elapsed.clear(), selectionChance, density, actions);
                final Biome.Precipitation precipitation = level.getBiome(pos).value().getPrecipitationAt(pos);
                if (precipitation == Biome.Precipitation.RAIN) {
                    collectRules(level, pos, state, TickSource.RAIN, elapsed.rain(), selectionChance, density, actions);
                } else if (precipitation == Biome.Precipitation.SNOW) {
                    collectRules(level, pos, state, TickSource.SNOW, elapsed.rain(), selectionChance, density, actions);
                }
            }
        }
    }

    private static void collectRules(
            final ServerLevel level,
            final BlockPos pos,
            final BlockState state,
            final TickSource source,
            final long exposure,
            final double selectionChance,
            final double density,
            final List<EndpointAction> actions
    ) {
        if (exposure <= 0) return;
        final Set<IBlockGrowth> rules = new HashSet<>();
        BlockGrowthHandler.getBlockGrowths(source, state.getBlock()).ifPresent(rules::addAll);
        final Set<IBlockGrowth> universal = BlockGrowthHandlerAccessor.immersiveWeatheringSampler$getUniversalGrowths().get(source);
        if (universal != null) rules.addAll(universal);
        for (IBlockGrowth rule : rules) {
            if (rule instanceof BuiltinBlockGrowth builtin && builtin.getName().startsWith("lightning")) continue;
            final double growthChance = growthChance(rule);
            final long seed = seed(level, pos, new ExposureClock(exposure, 0, 0, 0), ruleKey(rule, source));
            final RandomSource random = RandomSource.create(seed);
            if (!ProbabilityMath.occurs(random, exposure, selectionChance * growthChance * density)) continue;
            actions.add(new EndpointAction(pos, state, seed, ruleKey(rule, source), () -> ChanceBypass.run(
                    RandomSource.create(seed),
                    () -> rule.tryGrowing(pos, state, level, () -> level.getBiome(pos)))));
        }
    }

    private static double growthChance(final IBlockGrowth rule) {
        if (rule instanceof ConfigurableBlockGrowth configurable) return configurable.getGrowthChance();
        if (rule instanceof BuiltinBlockGrowth builtin) {
            if (builtin.getName().equals("grass_growth")) return 0.1D;
            return ((BuiltinBlockGrowthAccessor) builtin).immersiveWeatheringSampler$getGrowthChance();
        }
        return 1.0D;
    }

    private static String ruleKey(final IBlockGrowth rule, final TickSource source) {
        if (rule instanceof BuiltinBlockGrowth builtin) return source.getName() + ':' + builtin.getName();
        if (rule instanceof ConfigurableBlockGrowth configurable) {
            return source.getName() + ":configurable:" + configurable.getGrowthChance() + ':' + configurable.targetSelf();
        }
        return source.getName() + ':' + rule.getClass().getName();
    }

    private static long seed(
            final ServerLevel level,
            final BlockPos pos,
            final ExposureClock elapsed,
            final String discriminator
    ) {
        long value = level.getSeed();
        value ^= pos.asLong();
        value ^= Long.rotateLeft(elapsed.clearDay(), 7);
        value ^= Long.rotateLeft(elapsed.clearNight(), 19);
        value ^= Long.rotateLeft(elapsed.rainDay(), 31);
        value ^= Long.rotateLeft(elapsed.rainNight(), 43);
        value ^= ((long) level.dimension().location().hashCode() << 32);
        value ^= discriminator.hashCode();
        value ^= value >>> 30;
        value *= 0xbf58476d1ce4e5b9L;
        value ^= value >>> 27;
        value *= 0x94d049bb133111ebL;
        return value ^ value >>> 31;
    }

    private record EndpointAction(
            BlockPos pos, BlockState expectedState, long seed, String discriminator, Runnable operation
    ) {
    }
}
