package cn.qihuang02.cyyn;

import cn.qihuang02.cyyn.event.RegisterMentionGroupsEvent;
import cn.qihuang02.cyyn.mention.MentionGroupRegistry;
import cn.qihuang02.cyyn.mention.group.HereMentionGroup;
import cn.qihuang02.cyyn.mention.group.NearMentionGroup;
import cn.qihuang02.cyyn.network.CYYNMessages;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

@Mod(CallYouByYourName.MODID)
public class CallYouByYourName {
    public static final String MODID = "cyyn";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CallYouByYourName(@NotNull FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        modEventBus.addListener(this::onCommonSetup);
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC, "CallYouByYourName" + "-common.toml");

        CYYNMessages.register();
    }

    @Contract("_ -> new")
    public static @NotNull ResourceLocation getRl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        MentionGroupRegistry.register(new HereMentionGroup());
        MentionGroupRegistry.register(new NearMentionGroup());
        MinecraftForge.EVENT_BUS.post(new RegisterMentionGroupsEvent());
    }
}
