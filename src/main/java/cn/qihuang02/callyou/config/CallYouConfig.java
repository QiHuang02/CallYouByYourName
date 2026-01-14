package cn.qihuang02.callyou.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

public class CallYouConfig {
    public static final ModConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    static {
        Pair<Common, ModConfigSpec> pair =
                new ModConfigSpec.Builder().configure(Common::new);
        COMMON = pair.getLeft();
        COMMON_SPEC = pair.getRight();
    }

    public static final class Common {
        public final ModConfigSpec.IntValue maxMentionsPerMessage;
        public final ModConfigSpec.IntValue maxTargetsPerMention;
        public final ModConfigSpec.IntValue maxOfflineTargetsPerMention;
        public final ModConfigSpec.IntValue globalCooldownTicks;
        public final ModConfigSpec.IntValue perTargetCooldownTicks;
        public final ModConfigSpec.BooleanValue renderItemIconAndPlaceholder;
        public final ModConfigSpec.BooleanValue enableServerSideHistory;
        public final ModConfigSpec.IntValue historyRetentionDays;
        public final ModConfigSpec.IntValue maxHistoryPerPlayer;

        Common(ModConfigSpec.@NotNull Builder builder) {
            builder.push("mentions");

            maxMentionsPerMessage = builder
                    .comment("Maximum number of effective mentions (i.e. tokens that resolve to a MentionType) allowed per single chat message. 0 = no limit.")
                    .defineInRange("maxMentionsPerMessage", 5, 0, 100);

            maxTargetsPerMention = builder
                    .comment("Maximum number of target players a single mention is allowed to ping. 0 = no limit.")
                    .defineInRange("maxTargetsPerMention", 16, 0, 200);

            maxOfflineTargetsPerMention = builder
                    .comment("Maximum number of offline targets recorded per mention. 0 = no limit.")
                    .defineInRange("maxOfflineTargetsPerMention", 100, 0, 1000);

            globalCooldownTicks = builder
                    .comment("Global cooldown in ticks between chat messages that contain at least one mention. 0 = no limit.")
                    .defineInRange("globalCooldownTicks", 20, 0, 20 * 60); // up to 60s

            perTargetCooldownTicks = builder
                    .comment("Cooldown in ticks between mentions from the same sender to the same target. 0 = no limit.")
                    .defineInRange("perTargetCooldownTicks", 40, 0, 20 * 60);

            renderItemIconAndPlaceholder = builder
                    .comment("Whether to enable rendering of item icons and placeholder spaces after an @item mention.")
                    .define("renderItemIconAndPlaceholder", true);

            enableServerSideHistory = builder
                    .comment("Whether to store mention history on the server for players to review later.")
                    .define("enableServerSideHistory", true);

            historyRetentionDays = builder
                    .comment("How many days to keep mention history records on the server. 0 = never expire.")
                    .defineInRange("historyRetentionDays", 7, 0, 365);

            maxHistoryPerPlayer = builder
                    .comment("Maximum number of mention history records stored per player. 0 = no limit.")
                    .defineInRange("maxHistoryPerPlayer", 50, 0, 500);

            builder.pop();
        }
    }
}
