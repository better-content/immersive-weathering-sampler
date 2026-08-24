package com.bettercontent.immersiveweatheringsampler.mixin;

import com.ordana.immersive_weathering.data.block_growths.growths.builtin.BuiltinBlockGrowth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BuiltinBlockGrowth.class)
public interface BuiltinBlockGrowthAccessor {
    @Accessor("growthChance")
    float immersiveWeatheringSampler$getGrowthChance();
}

