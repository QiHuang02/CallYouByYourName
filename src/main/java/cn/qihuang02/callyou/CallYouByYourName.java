package cn.qihuang02.callyou;

import cn.qihuang02.callyou.config.CallYouConfig;
import cn.qihuang02.callyou.core.handler.*;
import cn.qihuang02.callyou.core.network.CallYouNetwork;
import cn.qihuang02.callyou.registry.BuiltInCallYouRegistries;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

@Mod(CallYouByYourName.MODID)
public class CallYouByYourName {
    public static final String MODID = "callyou";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CallYouByYourName() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::onCommonSetup);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CallYouConfig.COMMON_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CallYouConfig.CLIENT_SPEC);

        // Register game event handlers on the Forge event bus
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;
        forgeBus.register(MentionChatHandler.class);
        forgeBus.register(MentionLogHandler.class);
        forgeBus.register(MentionPreferencesHandler.class);
        forgeBus.register(OnlinePlayersHandler.class);
        forgeBus.register(PermissionsHandler.class);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        BuiltInCallYouRegistries.registerAll();
        CallYouNetwork.register();
    }

    @Contract("_ -> new")
    public static @NotNull ResourceLocation getRl(String path) {
        return new ResourceLocation(MODID, path);
    }
}
