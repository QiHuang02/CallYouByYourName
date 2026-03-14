package cn.qihuang02.callyou.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

public class CallYouConfig {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final Common COMMON;
    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;

    static {
        Pair<Common, ForgeConfigSpec> pair =
                new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON = pair.getLeft();
        COMMON_SPEC = pair.getRight();

        Pair<Client, ForgeConfigSpec> clientPair =
                new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT = clientPair.getLeft();
        CLIENT_SPEC = clientPair.getRight();
    }

    public enum ItemIconRenderMode {
        INLINE,
        HOVER,
        NONE
    }

    public static final class Common {
        public final ForgeConfigSpec.IntValue maxMentionsPerMessage;
        public final ForgeConfigSpec.IntValue maxTargetsPerMention;
        public final ForgeConfigSpec.IntValue globalCooldownTicks;
        public final ForgeConfigSpec.IntValue perTargetCooldownTicks;
        public final ForgeConfigSpec.BooleanValue enableServerSideHistory;
        public final ForgeConfigSpec.IntValue historyRetentionDays;
        public final ForgeConfigSpec.IntValue maxHistoryPerPlayer;

        Common(ForgeConfigSpec.@NotNull Builder builder) {
            builder.push("mentions");

            maxMentionsPerMessage = builder
                    .comment("Maximum number of effective mentions (i.e. tokens that resolve to a MentionType) allowed per single chat message. 0 = no limit.")
                    .defineInRange("maxMentionsPerMessage", 5, 0, 100);

            maxTargetsPerMention = builder
                    .comment("Maximum number of target players a single mention is allowed to ping. 0 = no limit.")
                    .defineInRange("maxTargetsPerMention", 16, 0, 200);

            globalCooldownTicks = builder
                    .comment("Global cooldown in ticks between chat messages that contain at least one mention. 0 = no limit.")
                    .defineInRange("globalCooldownTicks", 20, 0, 20 * 60);

            perTargetCooldownTicks = builder
                    .comment("Cooldown in ticks between mentions from the same sender to the same target. 0 = no limit.")
                    .defineInRange("perTargetCooldownTicks", 40, 0, 20 * 60);

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

    public static final class Client {
        public final ForgeConfigSpec.EnumValue<ItemIconRenderMode> itemIconRenderMode;

        Client(ForgeConfigSpec.@NotNull Builder builder) {
            builder.push("mentions");

            itemIconRenderMode = builder
                    .comment("Client-only: where to render @item icons. INLINE = in chat line, HOVER = left of cursor, NONE = disable icon rendering.")
                    .defineEnum("itemIconRenderMode", ItemIconRenderMode.INLINE);

            builder.pop();
        }
    }
}
