package cn.qihuang02.callyou.core.client.screen;

import cn.qihuang02.callyou.api.MentionType;
import cn.qihuang02.callyou.api.components.Notifier;
import cn.qihuang02.callyou.core.attachment.MentionPreferences;
import cn.qihuang02.callyou.registry.CallYouMentionRegistries;
import cn.qihuang02.callyou.registry.CallYouRegistries;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Switch;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.appliedenergistics.yoga.YogaAlign;
import org.appliedenergistics.yoga.YogaFlexDirection;
import org.jetbrains.annotations.NotNull;

public class MentionTypeRow {
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

        Label labelElement = new Label();
        labelElement.setText(label);
        labelElement.layout(style -> style.flexGrow(1));

        // Master Enable Switch
        this.masterSwitch = new Switch();
        this.masterSwitch.layout(style -> style.width(28).height(12));
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
        this.notifierSwitch = new Switch();
        this.notifierSwitch.layout(style -> style.width(28).height(12));
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
                        String transKey = "notifier." + this.resolvedNotifierId.getNamespace() + "." + this.resolvedNotifierId.getPath();
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

        this.element = new UIElement()
                .layout(style -> style.flexDirection(YogaFlexDirection.ROW)
                        .alignItems(YogaAlign.CENTER)
                        .gapColumn(4)
                        .widthStretch()
                        .height(20))
                .addChildren(labelElement, this.notifierSwitch, this.masterSwitch);
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
            if (type != null) {
                isMassType = type.rules().isMass();
            }
        }

        boolean isBlocked = prefs.getBlockedTypes().contains(id);
        boolean isGloballyAllowed = globalAllow;

        if (isMassType && !globalMassAllow) {
            isGloballyAllowed = false;
        }

        boolean isActive = isGloballyAllowed && !isBlocked;

        // Update Master Switch
        this.masterSwitch.setOn(!isBlocked, false);
        this.masterSwitch.setActive(isGloballyAllowed);

        // Update Notifier Switch
        if (this.resolvedNotifierId != null) {
            this.notifierSwitch.setOn(prefs.isNotifierEnabled(id, this.resolvedNotifierId), false);
            this.notifierSwitch.setActive(isActive);
        }
    }
}