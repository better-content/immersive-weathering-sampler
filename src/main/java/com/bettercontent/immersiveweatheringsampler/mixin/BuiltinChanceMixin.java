package com.bettercontent.immersiveweatheringsampler.mixin;

import com.bettercontent.immersiveweatheringsampler.ChanceBypass;
import com.ordana.immersive_weathering.data.block_growths.growths.builtin.CampfireSootGrowth;
import com.ordana.immersive_weathering.data.block_growths.growths.builtin.FireSootGrowth;
import com.ordana.immersive_weathering.data.block_growths.growths.builtin.GrassGrowth;
import com.ordana.immersive_weathering.data.block_growths.growths.builtin.IceGrowth;
import com.ordana.immersive_weathering.data.block_growths.growths.builtin.LeavesGrowth;
import com.ordana.immersive_weathering.data.block_growths.growths.builtin.SandGrowth;
import com.ordana.immersive_weathering.data.block_growths.growths.builtin.SandLayerGrowth;
import com.ordana.immersive_weathering.data.block_growths.growths.builtin.SnowGrowth;
import com.ordana.immersive_weathering.data.block_growths.growths.builtin.SnowIcicleGrowth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = {
        CampfireSootGrowth.class,
        FireSootGrowth.class,
        GrassGrowth.class,
        IceGrowth.class,
        LeavesGrowth.class,
        SandGrowth.class,
        SandLayerGrowth.class,
        SnowGrowth.class,
        SnowIcicleGrowth.class
})
public abstract class BuiltinChanceMixin {
    @Redirect(
            method = "tryGrowing",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextFloat()F", ordinal = 0),
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
}

