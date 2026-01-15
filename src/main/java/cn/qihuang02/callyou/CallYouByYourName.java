package cn.qihuang02.callyou;

import cn.qihuang02.callyou.config.CallYouConfig;
import cn.qihuang02.callyou.core.attachment.CallYouAttachments;
import cn.qihuang02.callyou.registry.BuiltInCallYouRegistries;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

@Mod(CallYouByYourName.MODID)
public class CallYouByYourName {
    public static final String MODID = "callyou";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CallYouByYourName(IEventBus modEventBus, @NotNull ModContainer modContainer) {
        BuiltInCallYouRegistries.register(modEventBus);
        CallYouAttachments.ATTACHMENT_TYPES.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.COMMON, CallYouConfig.COMMON_SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, CallYouConfig.CLIENT_SPEC);
    }

    @Contract("_ -> new")
    public static @NotNull ResourceLocation getRl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
