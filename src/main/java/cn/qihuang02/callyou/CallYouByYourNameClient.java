package cn.qihuang02.callyou;

import cn.qihuang02.callyou.core.client.screen.MentionPreferencesScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.jetbrains.annotations.NotNull;

@Mod(value = CallYouByYourName.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = CallYouByYourName.MODID, value = Dist.CLIENT)
public class CallYouByYourNameClient {
    private static final KeyMapping MENTION_PREFS_KEY = new KeyMapping(
            "key.callyou.mention_preferences",
            InputConstants.KEY_M,
            "key.categories.multiplayer"
    );

    public CallYouByYourNameClient(@NotNull ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        container.registerExtensionPoint(IConfigScreenFactory.class, (minecraft, parent) -> new MentionPreferencesScreen(parent));
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        CallYouByYourName.LOGGER.info("HELLO FROM CLIENT SETUP");
        CallYouByYourName.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    @SubscribeEvent
    static void registerKeyMappings(@NotNull RegisterKeyMappingsEvent event) {
        event.register(MENTION_PREFS_KEY);
    }

    @EventBusSubscriber(modid = CallYouByYourName.MODID, value = Dist.CLIENT)
    public static final class ClientEvents {
        private ClientEvents() {
        }

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            Minecraft minecraft = Minecraft.getInstance();
            while (MENTION_PREFS_KEY.consumeClick()) {
                if (minecraft.player != null) {
                    minecraft.setScreen(MentionPreferencesScreen.createWithRefresh(minecraft.screen));
                }
            }
        }
    }
}
