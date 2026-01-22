package cn.qihuang02.callyou.datagen;

import cn.qihuang02.callyou.CallYouByYourName;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

public class CallYouLangProvider extends LanguageProvider {
    private static final List<TranslationEntry> ENTRIES = List.of(
            // Config Titles and Comments
            new TranslationEntry("callyou.configuration.mentions", "Mentions Settings", "提及设置"),
            new TranslationEntry("callyou.configuration.maxMentionsPerMessage", "Max Mentions Per Message", "每条消息最大提及数"),
            new TranslationEntry("callyou.configuration.maxMentionsPerMessage.comment", "Maximum number of effective mentions (i.e. tokens that resolve to a MentionType) allowed per single chat message. 0 = no limit.", "单条聊天消息中允许的有效提及（即解析为提及类型的令牌）数量上限。0 = 不限制。"),
            new TranslationEntry("callyou.configuration.maxTargetsPerMention", "Max Targets Per Mention", "单次提及最多目标"),
            new TranslationEntry("callyou.configuration.maxTargetsPerMention.comment", "Maximum number of target players a single mention is allowed to ping. 0 = no limit.", "单个提及允许提醒的目标玩家数量上限。0 = 不限制。"),
            new TranslationEntry("callyou.configuration.globalCooldownTicks", "Global Cooldown Ticks", "全局冷却（刻）"),
            new TranslationEntry("callyou.configuration.globalCooldownTicks.comment", "Global cooldown in ticks between chat messages that contain at least one mention. 0 = no limit.", "含有至少一个提及的两条聊天消息之间的全局冷却（以刻为单位）。0 = 不限制。"),
            new TranslationEntry("callyou.configuration.perTargetCooldownTicks", "Per Target Cooldown Ticks", "单目标冷却（刻）"),
            new TranslationEntry("callyou.configuration.perTargetCooldownTicks.comment", "Cooldown in ticks between mentions from the same sender to the same target. 0 = no limit.", "同一发送者对同一目标的两次提及之间的冷却（以刻为单位）。0 = 不限制。"),
            new TranslationEntry("callyou.configuration.itemIconRenderMode", "Item Icon Render Mode (@item)", "物品图标渲染模式 (@item)"),
            new TranslationEntry("callyou.configuration.itemIconRenderMode.comment", "Where to render @item icons: INLINE = in chat line, HOVER = left of cursor, NONE = disable icon rendering.", "指定 @item 的物品图标渲染位置：INLINE = 聊天行内，HOVER = 鼠标左侧，NONE = 禁用图标渲染。"),
            new TranslationEntry("callyou.configuration.itemIconRenderMode.inline", "Inline", "行内"),
            new TranslationEntry("callyou.configuration.itemIconRenderMode.hover", "Hover", "悬浮"),
            new TranslationEntry("callyou.configuration.itemIconRenderMode.none", "None", "不渲染"),

            // Messages
            new TranslationEntry("message.callyou.no_permission", "You do not have permission to mention %s", "你没有权限提及 %s"),
            new TranslationEntry("message.callyou.too_many_mentions", "Too many mentions in one message!", "单条消息中提及过多！"),
            new TranslationEntry("message.callyou.spot.label", "Spot", "位置"),
            new TranslationEntry("message.callyou.spot", "at X: %s Y: %s Z: %s", "位置 X: %s Y: %s Z: %s"),
            new TranslationEntry("message.callyou.spot.ftb.add", "Click to add a temporary waypoint", "点击添加临时标记点"),
            new TranslationEntry("message.callyou.spot.ftb.shared_by", "%s's shared location", "由 %s 分享的位置"),
            new TranslationEntry("message.callyou.spot.ftb.added", "Temporary waypoint added: %s", "已添加临时标记点：%s"),
            new TranslationEntry("message.callyou.item.empty", "Cannot mention empty hand!", "无法提及空手！"),
            new TranslationEntry("message.callyou.ftbteams_missing", "FTB Teams is not installed; @team mention is unavailable.", "未安装 FTB Teams，@team 提及不可用。"),
            new TranslationEntry("message.callyou.notify.toast.title", "You were mentioned by %s", "你被 %s 提及"),
            new TranslationEntry("message.callyou.notify.toast.description", "Mentioned by %s in chat.", "%s 在聊天中提及了你。"),
            new TranslationEntry("message.callyou.notify.default", "You were mentioned by %s", "你被 %s 提及了"),
            new TranslationEntry("message.callyou.unread_mentions", "Welcome back! You have %s unread mentions, press %s to view.", "欢迎回来，你有 %s 条未读提及消息，按 %s 查看。"),
            new TranslationEntry("message.callyou.reply.hover", "Click to reply", "点击回复"),

            // Keybinds
            new TranslationEntry("key.callyou.mention_preferences", "Mention Preferences", "提及偏好"),

            // Mention preference screen
            new TranslationEntry("screen.callyou.mention_preferences.title", "Mention Preferences", "提及偏好设置"),
            new TranslationEntry("screen.callyou.mention_preferences.allow_all", "Allow Mentions", "允许提及"),
            new TranslationEntry("screen.callyou.mention_preferences.allow_all.tooltip", "Whether other players may mention you.", "是否允许其他玩家提及你。"),
            new TranslationEntry("screen.callyou.mention_preferences.allow_mass", "Allow Mass Mentions", "允许群体提及"),
            new TranslationEntry("screen.callyou.mention_preferences.allow_mass.tooltip", "Whether mass mention types (e.g., @here) may ping you.", "是否允许群体提及类型（如 @here）提醒你。"),
            new TranslationEntry("screen.callyou.mention_preferences.done.tooltip", "Save and exit", "保存并退出"),
            new TranslationEntry("screen.callyou.mention_preferences.blocked.none", "No blocked players yet.", "暂无被屏蔽的玩家。"),

            // Blocked players screen
            new TranslationEntry("screen.callyou.blocked_senders.title", "Blocked Players", "已屏蔽的玩家"),
            new TranslationEntry("screen.callyou.blocked_senders.unblock", "Unblock", "取消屏蔽"),
            new TranslationEntry("screen.callyou.blocked_senders.block_label", "Block Player", "屏蔽玩家"),
            new TranslationEntry("screen.callyou.blocked_senders.block_input.tooltip", "Enter the name of an online player to block mentions from them.", "输入一名在线玩家的名字以屏蔽其提及。"),
            new TranslationEntry("screen.callyou.blocked_senders.block_action", "Block", "屏蔽"),
            new TranslationEntry("screen.callyou.blocked_senders.block_action.tooltip", "Block the specified player.", "屏蔽指定的玩家。"),
            new TranslationEntry("screen.callyou.blocked_senders.error.not_found", "Player not found online.", "未找到该在线玩家。"),
            new TranslationEntry("screen.callyou.blocked_senders.error.already_blocked", "Player is already blocked.", "该玩家已被屏蔽。"),

            // Mention type labels
            new TranslationEntry("mention_type.callyou.near", "Nearby Players (@near)", "附近玩家 (@near)"),
            new TranslationEntry("mention_type.callyou.player", "Player (@player)", "玩家 (@player)"),
            new TranslationEntry("mention_type.callyou.item", "Item (@item)", "物品 (@item)"),
            new TranslationEntry("mention_type.callyou.here", "Here (@here)", "这里 (@here)"),
            new TranslationEntry("mention_type.callyou.spot", "Location (@spot)", "位置 (@spot)"),
            new TranslationEntry("mention_type.callyou.ftb_team", "FTB Team (@team)", "FTB 队伍 (@team)"),

            // Mention history screen
            new TranslationEntry("screen.callyou.history.title", "History", "历史记录"),
            new TranslationEntry("screen.callyou.history.empty", "No history yet", "暂无历史记录"),
            new TranslationEntry("screen.callyou.history.loading", "Loading...", "正在加载..."),
            new TranslationEntry("screen.callyou.history.refresh", "Refresh", "刷新"),
            new TranslationEntry("screen.callyou.history.mark_all", "Mark All Read", "全部标记为已读"),
            new TranslationEntry("screen.callyou.history.mark_read", "Mark Read", "已读"),
            new TranslationEntry("screen.callyou.history.reply", "Reply", "回复"),
            new TranslationEntry("screen.callyou.history.count", "Total %s records", "共 %s 条记录"),
            new TranslationEntry("screen.callyou.history.delete", "Delete", "删除"),
            new TranslationEntry("screen.callyou.history.coords", "Create Waypoint", "创建传送点"),
            new TranslationEntry("screen.callyou.history.coords_copied", "Copied coords: %s", "已复制坐标：%s")
    );

    private final Function<TranslationEntry, String> translationSelector;

    private CallYouLangProvider(PackOutput output, String locale, Function<TranslationEntry, String> translationSelector) {
        super(output, CallYouByYourName.MODID, locale);
        this.translationSelector = translationSelector;
    }

    @Contract("_ -> new")
    public static @NotNull CallYouLangProvider enUs(PackOutput output) {
        return new CallYouLangProvider(output, "en_us", TranslationEntry::enUs);
    }

    @Contract("_ -> new")
    public static @NotNull CallYouLangProvider zhCn(PackOutput output) {
        return new CallYouLangProvider(output, "zh_cn", TranslationEntry::zhCn);
    }

    @Override
    protected void addTranslations() {
        ENTRIES.forEach(entry -> add(entry.key(), translationSelector.apply(entry)));
    }

    private record TranslationEntry(String key, String enUs, String zhCn) {
    }
}
