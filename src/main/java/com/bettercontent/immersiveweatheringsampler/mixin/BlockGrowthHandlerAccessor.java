package com.bettercontent.immersiveweatheringsampler.mixin;

import com.ordana.immersive_weathering.data.block_growths.BlockGrowthHandler;
import com.ordana.immersive_weathering.data.block_growths.TickSource;
import com.ordana.immersive_weathering.data.block_growths.growths.IBlockGrowth;
import java.util.Map;
import java.util.Set;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockGrowthHandler.class)
public interface BlockGrowthHandlerAccessor {
    @Accessor("UNIVERSAL_GROWTHS")
    static Map<TickSource, Set<IBlockGrowth>> immersiveWeatheringSampler$getUniversalGrowths() {
        throw new AssertionError();
    }
}

