package cn.qihuang02.cyyn.server.mention.function;

import cn.qihuang02.cyyn.common.mention.MentionTextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class SpotMentionFunction extends BaseMentionFunction {
    @Override
    public @NotNull String name() {
        return "spot";
    }

    @Override
    protected @NotNull MentionDecision handleMention(@NotNull ServerPlayer sender,
                                                     @NotNull Component message,
                                                     @NotNull MentionTextUtils.MentionTokenRange range) {
        return replace(createSpotComponent(sender));
    }

    private @NotNull MutableComponent createSpotComponent(@NotNull ServerPlayer sender) {
        BlockPos blockPos = sender.blockPosition();
        MutableComponent location = Component.translatable("message.cyyn.spot", blockPos.getX(), blockPos.getY(), blockPos.getZ());
        MutableComponent hoverText = Component.empty().append(location);

        return ComponentUtils.wrapInSquareBrackets(Component.literal("Spot"))
                .withStyle(style -> style
                        .withColor(ChatFormatting.GREEN)
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText)));
    }
}
