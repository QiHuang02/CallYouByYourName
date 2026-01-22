package cn.qihuang02.callyou.core.client.screen.row;

import cn.qihuang02.callyou.core.client.screen.MentionUIStyles;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.UITemplate;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.utils.XmlUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class BlockedSenderRow {
    private static final ResourceLocation UI_XML = ResourceLocation.parse("callyou:ui/blocked_sender_row.xml");
    private static UITemplate template;

    public static @NotNull UIElement create(
            @NotNull UUID senderId,
            @NotNull Component displayName,
            @NotNull Runnable onUnblock
    ) {
        UI ui = template().createUI();
        Label nameLabel = require(ui, "#blocked-name", Label.class);
        Label uuidLabel = require(ui, "#blocked-uuid", Label.class);
        Button unblockButton = require(ui, "#unblock-button", Button.class);
        UIElement root = ui.getRootElement();

        nameLabel.setText(displayName);

        uuidLabel.setText(Component.literal(senderId.toString()));
        MentionUIStyles.applyButtonStyle(unblockButton);
        unblockButton.setOnClick(event -> onUnblock.run());

        return root;
    }

    private static @NotNull UITemplate template() {
        if (template == null) {
            var xml = XmlUtils.loadXml(UI_XML);
            if (xml == null) {
                throw new IllegalStateException("UI xml not found: " + UI_XML);
            }
            template = UI.of(xml).toTemplate();
        }
        return template;
    }

    private static <T extends UIElement> @NotNull T require(
            @NotNull UI ui,
            @NotNull String selector,
            @NotNull Class<T> type
    ) {
        return ui.select(selector, type)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing UI element: " + selector));
    }
}
