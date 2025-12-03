package cn.qihuang02.callyou;

import cn.qihuang02.callyou.registry.BuiltInCallYouRegistries;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(CallYouByYourName.MODID)
public class CallYouByYourName {
    public static final String MODID = "callyou";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CallYouByYourName(IEventBus modEventBus, ModContainer modContainer) {
        BuiltInCallYouRegistries.register(modEventBus);
    }
}
