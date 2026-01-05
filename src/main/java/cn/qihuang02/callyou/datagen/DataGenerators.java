package cn.qihuang02.callyou.datagen;

import cn.qihuang02.callyou.CallYouByYourName;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = CallYouByYourName.MODID)
public class DataGenerators {
    @SubscribeEvent
    public static void getData(@NotNull GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeServer(), new CallYouDatapackProvider(packOutput, lookupProvider));
        generator.addProvider(event.includeClient(), CallYouLangProvider.enUs(packOutput));
        generator.addProvider(event.includeClient(), CallYouLangProvider.zhCn(packOutput));
    }
}
