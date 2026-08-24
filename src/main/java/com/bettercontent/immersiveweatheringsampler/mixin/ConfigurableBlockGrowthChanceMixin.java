package com.bettercontent.immersiveweatheringsampler.mixin;

import com.bettercontent.immersiveweatheringsampler.ChanceBypass;
import com.ordana.immersive_weathering.data.block_growths.growths.ConfigurableBlockGrowth;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ConfigurableBlockGrowth.class)
public abstract class ConfigurableBlockGrowthChanceMixin {
    @Redirect(
            method = "canGrow",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextFloat()F"),
            require = 0)
    private float immersiveWeatheringSampler$consumeSampledChance(final RandomSource random) {
        return ChanceBypass.active() ? 0.0F : random.nextFloat();
    }

    @Redirect(
            method = "tryGrowing",
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/Level;random:Lnet/minecraft/util/RandomSource;"),
            require = 0)
    private RandomSource immersiveWeatheringSampler$deterministicRandom(final Level level) {
        return ChanceBypass.randomOr(level.random);
    }

    @Redirect(
            method = "tryGrowing",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;destroyBlock(Lnet/minecraft/core/BlockPos;Z)Z"),
            require = 0)
    private boolean immersiveWeatheringSampler$suppressHistoricalDrops(
            final ServerLevel level,
            final BlockPos pos,
            final boolean drops
    ) {
        return level.destroyBlock(pos, ChanceBypass.active() ? false : drops);
    }
}

