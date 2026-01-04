package cn.qihuang02.callyou.client.screen;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.style.LayoutStyle;
import net.minecraft.network.chat.Component;
import org.appliedenergistics.yoga.YogaAlign;
import org.appliedenergistics.yoga.YogaFlexDirection;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class BlockedSenderRow {
    private static final Component UNBLOCK = Component.translatable("screen.callyou.blocked_senders.unblock");

    public static @NotNull UIElement create(
            @NotNull UUID senderId,
            @NotNull Component displayName,
            @NotNull Runnable onUnblock
    ) {
        Label nameLabel = new Label();
        nameLabel.setText(displayName);
        nameLabel.layout(LayoutStyle::widthStretch);

        Label uuidLabel = new Label();
        uuidLabel.setText(Component.literal(senderId.toString()));
        uuidLabel.textStyle(style -> style.textColor(0x888888));
        uuidLabel.layout(LayoutStyle::widthStretch);

        UIElement info = new UIElement()
                .layout(style -> style.flexDirection(YogaFlexDirection.COLUMN)
                        .gapRow(1)
                        .flexGrow(1))
                .addChildren(nameLabel, uuidLabel);

        Button unblockButton = new Button();
        unblockButton.setText(UNBLOCK);
        unblockButton.layout(style -> style.width(70).height(16));
        MentionUIStyles.applyButtonStyle(unblockButton);
        unblockButton.setOnClick(event -> onUnblock.run());

        return new UIElement()
                .layout(style -> style.flexDirection(YogaFlexDirection.ROW)
                        .alignItems(YogaAlign.CENTER)
                        .gapColumn(8)
                        .widthStretch()
                        .height(28))
                .addChildren(info, unblockButton);
    }
}
