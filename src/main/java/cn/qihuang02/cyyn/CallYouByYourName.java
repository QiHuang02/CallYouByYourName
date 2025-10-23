package cn.qihuang02.cyyn;

import cn.qihuang02.cyyn.network.CYYN$Messages;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

@Mod(CallYouByYourName.MODID)
public class CallYouByYourName {
    public static final String MODID = "cyyn";
    public static final Logger LOGGER = LogUtils.getLogger();

    @Contract("_ -> new")
    public static @NotNull ResourceLocation getRl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public CallYouByYourName(@NotNull FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        CYYN$Messages.register();
    }
}
