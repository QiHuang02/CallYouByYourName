package cn.qihuang02.callyou.core.client.screen.row;

import cn.qihuang02.callyou.api.MentionType;
import cn.qihuang02.callyou.api.components.Notifier;
import cn.qihuang02.callyou.core.attachment.MentionPreferences;
import cn.qihuang02.callyou.core.client.screen.MentionUIStyles;
import cn.qihuang02.callyou.registry.CallYouMentionRegistries;
import cn.qihuang02.callyou.registry.CallYouRegistries;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.UITemplate;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Switch;
import com.lowdragmc.lowdraglib2.utils.XmlUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class MentionTypeRow {
    private static final ResourceLocation UI_XML = ResourceLocation.parse("callyou:ui/mention_type_row.xml");
    private static UITemplate template;

    private final ResourceLocation id;
    private final Switch masterSwitch;
    private final Switch notifierSwitch;
    private final UIElement element;

    private ResourceLocation resolvedNotifierId;

    public MentionTypeRow(
            @NotNull ResourceLocation id,
            @NotNull Component label,
            @NotNull MentionPreferences prefs,
            @NotNull Runnable onUpdate,
            @NotNull Runnable onRefresh
    ) {
        this.id = id;

        UI ui = template().createUI();
        Label labelElement = require(ui, "#type-label", Label.class);
        this.masterSwitch = require(ui, "#master-switch", Switch.class);
        this.notifierSwitch = require(ui, "#notifier-switch", Switch.class);
        this.element = ui.getRootElement();

        labelElement.setText(label);

        // Master Enable Switch
        this.masterSwitch.style(style -> style.tooltips(Component.literal("Enable/Disable")));
        MentionUIStyles.applySwitchStyle(this.masterSwitch);
        this.masterSwitch.setOnSwitchChanged(value -> {
            if (value) {
                prefs.unblockMentionType(id);
            } else {
                prefs.blockMentionType(id);
            }
            onRefresh.run();
            onUpdate.run();
        });

        // Resolve Notifier
        MentionUIStyles.applySwitchStyle(this.notifierSwitch);
        this.notifierSwitch.setVisible(false); // Hidden by default
        this.notifierSwitch.setDisplay(false);

        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            Registry<MentionType> registry = mc.level.registryAccess()
                    .registryOrThrow(CallYouMentionRegistries.MENTION_TYPE_REGISTRY_KEY);
            MentionType type = registry.get(id);
            if (type != null) {
                Notifier notifier = type.notifier();
                if (notifier != null) {
                    this.resolvedNotifierId = CallYouRegistries.NOTIFICATION_RULE_TYPES.getKey(notifier.type());

                    if (this.resolvedNotifierId != null) {
                        String transKey = "notifier." + this.resolvedNotifierId.getNamespace() + "."
                                + this.resolvedNotifierId.getPath();
                        this.notifierSwitch.style(style -> style.tooltips(Component.translatable(transKey)));
                        this.notifierSwitch.setVisible(true);
                        this.notifierSwitch.setDisplay(true);

                        this.notifierSwitch.setOnSwitchChanged(value -> {
                            prefs.setNotifierEnabled(id, this.resolvedNotifierId, value);
                            onUpdate.run();
                        });
                    }
                }
            }
        }

    }

    public UIElement getElement() {
        return element;
    }

    public void updateState(
            boolean globalAllow,
            boolean globalMassAllow,
            MentionPreferences prefs
    ) {
        boolean isMassType = false;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            Registry<MentionType> registry = mc.level.registryAccess()
                    .registryOrThrow(CallYouMentionRegistries.MENTION_TYPE_REGISTRY_KEY);
            var type = registry.get(id);
            if (type != null && type.rules() != null) {
                isMassType = type.rules().isMass();
            }
        }

        boolean isBlocked = prefs.getBlockedTypes().contains(id);

        // Effective master state: Not blocked AND global rules allow it
        boolean effectiveMasterAllowed = globalAllow && (!isMassType || globalMassAllow);
        boolean isMasterOn = effectiveMasterAllowed && !isBlocked;

        // Update Master Switch
        this.masterSwitch.setOn(isMasterOn, false);
        this.masterSwitch.setActive(effectiveMasterAllowed);

        // Update Notifier Switch
        if (this.resolvedNotifierId != null) {
            boolean isNotifierEnabled = prefs.isNotifierEnabled(id, this.resolvedNotifierId);
            this.notifierSwitch.setOn(isMasterOn && isNotifierEnabled, false);
            this.notifierSwitch.setActive(isMasterOn);
        }
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
