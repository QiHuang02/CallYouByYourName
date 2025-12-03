package cn.qihuang02.callyou.api;

import cn.qihuang02.callyou.registry.CallYouRegistries;
import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public interface NotificationRule {
    Codec<NotificationRule> CODEC =
            CallYouRegistries.NOTIFICATION_RULE_TYPES
                    .byNameCodec()
                    .dispatch(
                            "type",
                            NotificationRule::type,
                            NotificationRuleType::mapCodec
                    );

    NotificationRuleType type();

    /**
     * Apply the notification side effects for this mention.
     * This method is not wired up anywhere yet – you will call it
     * from your actual chat handling logic later.
     */
    void apply(MentionContext context, List<ServerPlayer> targets);
}