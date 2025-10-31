package cn.qihuang02.cyyn.common.config;

import cn.qihuang02.cyyn.CallYouByYourName;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(modid = CallYouByYourName.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    public static final ResourceLocation MENTION_SOUND_ID =
            ResourceLocation.fromNamespaceAndPath("minecraft", "block.note_block.bell");
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.IntValue MENTION_COOLDOWN_TICKS = BUILDER
            .comment("Cooldown (in ticks) between mention notifications sent by the same player.")
            .defineInRange("mentionCooldownTicks", 5 * 20, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.BooleanValue ENABLE_MENTION_SOUND = BUILDER
            .comment("Whether to play a sound for mentioned players.")
            .define("enableMentionSound", true);
    private static final ForgeConfigSpec.BooleanValue RENDER_ITEM_TEXTURES = BUILDER
            .comment("Whether to render item textures inline with chat messages.")
            .define("renderItemTextures", true);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static int mentionCooldownTicks;
    public static boolean enableMentionSound;
    public static boolean renderItemTextures = true;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent.@NotNull Loading event) {
        mentionCooldownTicks = MENTION_COOLDOWN_TICKS.get();
        enableMentionSound = ENABLE_MENTION_SOUND.get();
        renderItemTextures = RENDER_ITEM_TEXTURES.get();
    }

    @SubscribeEvent
    static void onReload(final ModConfigEvent.@NotNull Reloading event) {
        mentionCooldownTicks = MENTION_COOLDOWN_TICKS.get();
        enableMentionSound = ENABLE_MENTION_SOUND.get();
        renderItemTextures = RENDER_ITEM_TEXTURES.get();
    }
}
