package cn.qihuang02.callyou.datagen;

import cn.qihuang02.callyou.CallYouByYourName;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class CallYouLangProvider extends LanguageProvider {
    public CallYouLangProvider(PackOutput output) {
        super(output, CallYouByYourName.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        // Config Titles and Comments
        add("callyou.config.mentions", "Mentions Settings");
        add("callyou.config.mentions.maxMentionsPerMessage", "Max Mentions Per Message");
        add("callyou.config.mentions.maxMentionsPerMessage.comment", "Maximum number of effective mentions (i.e. tokens that resolve to a MentionType) allowed per single chat message. 0 = no limit.");
        add("callyou.config.mentions.maxTargetsPerMention", "Max Targets Per Mention");
        add("callyou.config.mentions.maxTargetsPerMention.comment", "Maximum number of target players a single mention is allowed to ping. 0 = no limit.");
        add("callyou.config.mentions.globalCooldownTicks", "Global Cooldown Ticks");
        add("callyou.config.mentions.globalCooldownTicks.comment", "Global cooldown in ticks between chat messages that contain at least one mention. 0 = no limit.");
        add("callyou.config.mentions.perTargetCooldownTicks", "Per Target Cooldown Ticks");
        add("callyou.config.mentions.perTargetCooldownTicks.comment", "Cooldown in ticks between mentions from the same sender to the same target. 0 = no limit.");
        add("callyou.config.mentions.renderItemIconAndPlaceholder", "Render Item Icon and Placeholder for @item");
        add("callyou.config.mentions.renderItemIconAndPlaceholder.comment", "Whether to enable rendering of item icons and placeholder spaces after an @item mention.");

        // Messages
        add("message.callyou.no_permission", "You do not have permission to mention %s");
        add("message.callyou.too_many_mentions", "Too many mentions in one message!");
        add("message.callyou.spot", "at X: %s Y: %s Z: %s");
        add("message.callyou.item.empty", "Cannot mention empty hand!");
        add("message.callyou.ftbteams_missing", "FTB Teams is not installed; @team mention is unavailable.");
        add("message.callyou.notify.toast.title", "You were mentioned by %s");
        add("message.callyou.notify.toast.description", "Mentioned by %s in chat.");

        // Keybinds
        add("key.callyou.mention_preferences", "Mention Preferences");

        // Mention preference screen
        add("screen.callyou.mention_preferences.title", "Mention Preferences");
        add("screen.callyou.mention_preferences.allow_all", "Allow Mentions");
        add("screen.callyou.mention_preferences.allow_all.tooltip", "Whether other players may mention you.");
        add("screen.callyou.mention_preferences.allow_mass", "Allow Mass Mentions");
        add("screen.callyou.mention_preferences.allow_mass.tooltip", "Whether mass mention types (e.g., @here) may ping you.");
        add("screen.callyou.mention_preferences.blocked.none", "No blocked players yet.");

        // Blocked players screen
        add("screen.callyou.blocked_senders.title", "Blocked Players");
        add("screen.callyou.blocked_senders.unblock", "Unblock");
        add("screen.callyou.blocked_senders.block_label", "Block Player");
        add("screen.callyou.blocked_senders.block_input.tooltip", "Enter the name of an online player to block mentions from them.");
        add("screen.callyou.blocked_senders.block_action", "Block");
        add("screen.callyou.blocked_senders.block_action.tooltip", "Block the specified player.");
        add("screen.callyou.blocked_senders.error.not_found", "Player not found online.");
        add("screen.callyou.blocked_senders.error.already_blocked", "Player is already blocked.");

        // Mention type labels
        add("mention_type.callyou.near", "Nearby Players (@near)");
        add("mention_type.callyou.player", "Player (@player)");
        add("mention_type.callyou.item", "Item (@item)");
        add("mention_type.callyou.here", "Here (@here)");
        add("mention_type.callyou.spot", "Location (@spot)");
        add("mention_type.callyou.ftb_team", "FTB Team (@team)");
    }
}
