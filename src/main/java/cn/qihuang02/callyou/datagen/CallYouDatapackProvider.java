package cn.qihuang02.callyou.datagen;

import cn.qihuang02.callyou.CallYouByYourName;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Generates MentionType JSON files for the callyou datapack registry.
 * Replaces NeoForge's DatapackBuiltinEntriesProvider which is not available in Forge 1.20.1.
 */
public final class CallYouDatapackProvider implements DataProvider {
    private final PackOutput output;

    public CallYouDatapackProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        Path dataPath = this.output.getOutputFolder().resolve("data").resolve(CallYouByYourName.MODID)
                .resolve("callyou").resolve("mention_type");

        // @near mention
        futures.add(DataProvider.saveStable(cache, buildNearMention(), dataPath.resolve("near.json")));
        // @player mention
        futures.add(DataProvider.saveStable(cache, buildPlayerMention(), dataPath.resolve("player.json")));
        // @item mention
        futures.add(DataProvider.saveStable(cache, buildItemMention(), dataPath.resolve("item.json")));
        // @here mention
        futures.add(DataProvider.saveStable(cache, buildHereMention(), dataPath.resolve("here.json")));
        // @spot mention
        futures.add(DataProvider.saveStable(cache, buildSpotMention(), dataPath.resolve("spot.json")));
        // @team mention
        futures.add(DataProvider.saveStable(cache, buildTeamMention(), dataPath.resolve("team.json")));

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public @NotNull String getName() {
        return "CallYou Mention Types";
    }

    private static @NotNull JsonObject buildNearMention() {
        JsonObject json = new JsonObject();
        JsonObject targetProvider = new JsonObject();
        targetProvider.addProperty("type", "callyou:radius");
        targetProvider.addProperty("radius", 32.0D);
        json.add("target_provider", targetProvider);

        JsonObject textFormatter = new JsonObject();
        textFormatter.addProperty("type", "callyou:simple_formatter");
        textFormatter.addProperty("display_text", "@near");
        JsonObject decorator = new JsonObject();
        decorator.addProperty("type", "callyou:text_color");
        decorator.addProperty("color", "aqua");
        textFormatter.add("interaction_decorator", decorator);
        json.add("text_formatter", textFormatter);

        json.add("notifier", buildSoundNotifier());

        JsonObject rules = new JsonObject();
        rules.addProperty("cooldown_ticks", 0);
        rules.addProperty("is_mass_mention", true);
        json.add("rules", rules);

        return json;
    }

    private static @NotNull JsonObject buildPlayerMention() {
        JsonObject json = new JsonObject();
        JsonObject targetProvider = new JsonObject();
        targetProvider.addProperty("type", "callyou:player");
        json.add("target_provider", targetProvider);

        JsonObject textFormatter = new JsonObject();
        textFormatter.addProperty("type", "callyou:player_name");
        JsonObject composite = new JsonObject();
        composite.addProperty("type", "callyou:composite");
        JsonArray decorators = new JsonArray();
        JsonObject colorDec = new JsonObject();
        colorDec.addProperty("type", "callyou:text_color");
        colorDec.addProperty("color", "yellow");
        decorators.add(colorDec);
        JsonObject replyDec = new JsonObject();
        replyDec.addProperty("type", "callyou:reply");
        replyDec.addProperty("scope", "target");
        decorators.add(replyDec);
        composite.add("decorators", decorators);
        textFormatter.add("interaction_decorator", composite);
        json.add("text_formatter", textFormatter);

        json.add("notifier", buildSoundNotifier());
        json.add("rules", buildDefaultRules());

        return json;
    }

    private static @NotNull JsonObject buildItemMention() {
        JsonObject json = new JsonObject();
        JsonObject targetProvider = new JsonObject();
        targetProvider.addProperty("type", "callyou:none");
        json.add("target_provider", targetProvider);

        JsonObject textFormatter = new JsonObject();
        textFormatter.addProperty("type", "callyou:item");
        JsonObject composite = new JsonObject();
        composite.addProperty("type", "callyou:composite");
        JsonArray decorators = new JsonArray();
        JsonObject itemRender = new JsonObject();
        itemRender.addProperty("type", "callyou:item_render");
        decorators.add(itemRender);
        JsonObject rarityStyle = new JsonObject();
        rarityStyle.addProperty("type", "callyou:rarity_style");
        decorators.add(rarityStyle);
        JsonObject brackets = new JsonObject();
        brackets.addProperty("type", "callyou:square_brackets");
        decorators.add(brackets);
        composite.add("decorators", decorators);
        textFormatter.add("interaction_decorator", composite);
        json.add("text_formatter", textFormatter);

        json.add("notifier", buildSoundNotifier());
        json.add("rules", buildDefaultRules());

        return json;
    }

    private static @NotNull JsonObject buildHereMention() {
        JsonObject json = new JsonObject();
        JsonObject targetProvider = new JsonObject();
        targetProvider.addProperty("type", "callyou:same_dimension");
        json.add("target_provider", targetProvider);

        JsonObject textFormatter = new JsonObject();
        textFormatter.addProperty("type", "callyou:simple_formatter");
        textFormatter.addProperty("display_text", "@here");
        JsonObject decorator = new JsonObject();
        decorator.addProperty("type", "callyou:text_color");
        decorator.addProperty("color", "aqua");
        textFormatter.add("interaction_decorator", decorator);
        json.add("text_formatter", textFormatter);

        json.add("notifier", buildSoundNotifier());

        JsonObject rules = new JsonObject();
        rules.addProperty("cooldown_ticks", 0);
        rules.addProperty("is_mass_mention", true);
        json.add("rules", rules);

        return json;
    }

    private static @NotNull JsonObject buildSpotMention() {
        JsonObject json = new JsonObject();
        JsonObject targetProvider = new JsonObject();
        targetProvider.addProperty("type", "callyou:none");
        json.add("target_provider", targetProvider);

        JsonObject textFormatter = new JsonObject();
        textFormatter.addProperty("type", "callyou:spot");
        JsonObject composite = new JsonObject();
        composite.addProperty("type", "callyou:composite");
        JsonArray decorators = new JsonArray();
        JsonObject colorDec = new JsonObject();
        colorDec.addProperty("type", "callyou:text_color");
        colorDec.addProperty("color", "green");
        decorators.add(colorDec);
        JsonObject brackets = new JsonObject();
        brackets.addProperty("type", "callyou:square_brackets");
        decorators.add(brackets);
        composite.add("decorators", decorators);
        textFormatter.add("interaction_decorator", composite);
        json.add("text_formatter", textFormatter);

        json.add("notifier", buildSoundNotifier());
        json.add("rules", buildDefaultRules());

        return json;
    }

    private static @NotNull JsonObject buildTeamMention() {
        JsonObject json = new JsonObject();
        JsonObject targetProvider = new JsonObject();
        targetProvider.addProperty("type", "callyou:ftb_team");
        json.add("target_provider", targetProvider);

        JsonObject textFormatter = new JsonObject();
        textFormatter.addProperty("type", "callyou:simple_formatter");
        textFormatter.addProperty("display_text", "@team");
        JsonObject decorator = new JsonObject();
        decorator.addProperty("type", "callyou:text_color");
        decorator.addProperty("color", "gold");
        textFormatter.add("interaction_decorator", decorator);
        json.add("text_formatter", textFormatter);

        JsonObject notifier = new JsonObject();
        notifier.addProperty("type", "callyou:toast");
        notifier.addProperty("title_key", "message.callyou.notify.toast.title");
        notifier.addProperty("description_key", "message.callyou.notify.toast.description");
        notifier.addProperty("show_sender_name", true);
        notifier.addProperty("icon_item", "minecraft:name_tag");
        notifier.addProperty("advancement_type", "goal");
        json.add("notifier", notifier);

        JsonObject rules = new JsonObject();
        rules.addProperty("cooldown_ticks", 0);
        rules.addProperty("is_mass_mention", true);
        json.add("rules", rules);

        return json;
    }

    private static @NotNull JsonObject buildSoundNotifier() {
        JsonObject notifier = new JsonObject();
        notifier.addProperty("type", "callyou:sound");
        notifier.addProperty("sound", "minecraft:entity.experience_orb.pickup");
        notifier.addProperty("volume", 1.0F);
        notifier.addProperty("pitch", 1.0F);
        return notifier;
    }

    private static @NotNull JsonObject buildDefaultRules() {
        JsonObject rules = new JsonObject();
        rules.addProperty("cooldown_ticks", 0);
        rules.addProperty("is_mass_mention", false);
        return rules;
    }
}
