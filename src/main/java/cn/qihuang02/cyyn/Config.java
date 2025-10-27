package cn.qihuang02.cyyn;

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

    private static final ForgeConfigSpec.LongValue MENTION_COOLDOWN_MS = BUILDER
            .comment("Cooldown (in milliseconds) between mention notifications sent by the same player.")
            .defineInRange("mentionCooldownMs", 5000L, 0L, Long.MAX_VALUE);
    private static final ForgeConfigSpec.BooleanValue ENABLE_MENTION_SOUND = BUILDER
            .comment("Whether to play a sound for mentioned players.")
            .define("enableMentionSound", true);
    private static final ForgeConfigSpec.BooleanValue RENDER_ITEM_TEXTURES = BUILDER
            .comment("Whether to render item textures inline with chat messages.")
            .define("renderItemTextures", true);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static long mentionCooldownMs;
    public static boolean enableMentionSound;
    public static boolean renderItemTextures = true;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent.@NotNull Loading event) {
        mentionCooldownMs = MENTION_COOLDOWN_MS.get();
        enableMentionSound = ENABLE_MENTION_SOUND.get();
        renderItemTextures = RENDER_ITEM_TEXTURES.get();
    }

    @SubscribeEvent
    static void onReload(final ModConfigEvent.@NotNull Reloading event) {
        mentionCooldownMs = MENTION_COOLDOWN_MS.get();
        enableMentionSound = ENABLE_MENTION_SOUND.get();
        renderItemTextures = RENDER_ITEM_TEXTURES.get();
    }
}
