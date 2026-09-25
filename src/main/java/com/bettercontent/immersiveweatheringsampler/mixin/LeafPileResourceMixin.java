package com.bettercontent.immersiveweatheringsampler.mixin;

import com.bettercontent.immersiveweatheringsampler.ImmersiveWeatheringSampler;
import com.ordana.immersive_weathering.dynamicpack.ServerDynamicResourcesHandler;
import net.mehvahdjukaar.moonlight.api.resources.StaticResource;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(ServerDynamicResourcesHandler.class)
public abstract class LeafPileResourceMixin {
    private static final Set<String> HEXEREI_LEAVES = Set.of(
            "mahogany_leaves", "willow_leaves", "witch_hazel_leaves");

    @Shadow(remap = false)
    public abstract void addLeafPileJson(StaticResource resource, String pileId, String leafId);

    @Inject(method = "addLeafPileJson", at = @At("HEAD"), cancellable = true, require = 1, remap = false)
    private void immersiveWeatheringSampler$useHexereiLeafItem(
            StaticResource resource, String pileId, String leafId, CallbackInfo callback) {
        ResourceLocation dynamicLeaf = ResourceLocation.tryParse(leafId);
        if (dynamicLeaf == null
                || !dynamicLeaf.getNamespace().equals("dynamic_trees_hexerei")
                || !HEXEREI_LEAVES.contains(dynamicLeaf.getPath())) {
            return;
        }

        ResourceLocation hexereiLeaf = new ResourceLocation("hexerei", dynamicLeaf.getPath());
        if (!ForgeRegistries.ITEMS.containsKey(hexereiLeaf)) {
            throw new IllegalStateException("Missing Hexerei leaf item for " + dynamicLeaf);
        }
        ImmersiveWeatheringSampler.LOGGER.info("Using {} for Immersive Weathering leaf pile {}", hexereiLeaf, pileId);
        addLeafPileJson(resource, pileId, hexereiLeaf.toString());
        callback.cancel();
    }
}
