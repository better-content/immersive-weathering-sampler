package com.bettercontent.immersiveweatheringsampler;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ImmersiveWeatheringSampler.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SamplerGameTests {
    private SamplerGameTests() {
    }

    public static void register(final RegisterGameTestsEvent event) {
        ImmersiveWeatheringSampler.LOGGER.info("Registering Immersive Weathering Sampler game tests");
        event.register(SamplerGameTests.class);
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", timeoutTicks = 40)
    public static void exposureSnapshotRoundTrips(final GameTestHelper helper) {
        final ExposureClock expected = new ExposureClock(120, 80, 40, 20);
        final CompoundTag tag = new CompoundTag();
        expected.save(tag, "Exposure");
        helper.assertTrue(expected.equals(ExposureClock.load(tag, "Exposure")), "exposure snapshot must be lossless");
        helper.assertTrue(expected.total() == 260, "partitioned exposure must preserve elapsed time");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", timeoutTicks = 40)
    public static void endpointProbabilityIsBounded(final GameTestHelper helper) {
        final double shortAbsence = ProbabilityMath.atLeastOne(20, 0.001D);
        final double longAbsence = ProbabilityMath.atLeastOne(20_000, 0.001D);
        helper.assertTrue(shortAbsence > 0.0D && shortAbsence < 1.0D, "short endpoint chance must be bounded");
        helper.assertTrue(longAbsence > shortAbsence && longAbsence <= 1.0D, "elapsed exposure must only increase chance");
        helper.succeed();
    }
}
