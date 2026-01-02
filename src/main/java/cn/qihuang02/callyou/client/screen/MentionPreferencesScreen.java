package cn.qihuang02.callyou.client.screen;

import cn.qihuang02.callyou.client.ClientMentionPreferences;
import cn.qihuang02.callyou.core.attachment.MentionPreferences;
import cn.qihuang02.callyou.network.CallYouNetwork;
import cn.qihuang02.callyou.registry.CallYouMentionRegistries;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Switch;
import com.lowdragmc.lowdraglib2.gui.ui.style.LayoutStyle;
import com.lowdragmc.lowdraglib2.gui.ui.style.Stylesheet;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.appliedenergistics.yoga.YogaAlign;
import org.appliedenergistics.yoga.YogaFlexDirection;
import org.appliedenergistics.yoga.YogaJustify;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MentionPreferencesScreen extends ModularUIScreen {
    private static final int PANEL_WIDTH = 320;
    private static final int PANEL_HEIGHT = 240;
    private static final Component TITLE = Component.translatable("screen.callyou.mention_preferences.title");
    private static final Component ALLOW_MENTIONS = Component.translatable("screen.callyou.mention_preferences.allow_all");
    private static final Component ALLOW_MASS = Component.translatable("screen.callyou.mention_preferences.allow_mass");
    private static final Component TYPE_LABEL = Component.translatable("screen.callyou.mention_preferences.type_label");
    private static final Component MANAGE_BLOCKED = Component.translatable("screen.callyou.mention_preferences.manage_blocked");

    private final Screen parent;
    private MentionPreferences workingCopy;
    private final Map<ResourceLocation, Switch> typeToggles = new HashMap<>();
    private final ScrollerView mentionTypeList;
    private final Switch allowMentionsSwitch;
    private final Switch allowMassMentionsSwitch;
    private final Button manageBlockedButton;
    private final Button doneButton;

    public MentionPreferencesScreen(@Nullable Screen parent) {
        this(parent, buildUIRefs());
    }

    private MentionPreferencesScreen(@Nullable Screen parent, @NotNull UIRefs refs) {
        super(refs.modularUI, TITLE);
        this.parent = parent;
        this.mentionTypeList = refs.mentionTypeList;
        this.allowMentionsSwitch = refs.allowMentionsSwitch;
        this.allowMassMentionsSwitch = refs.allowMassMentionsSwitch;
        this.manageBlockedButton = refs.manageBlockedButton;
        this.doneButton = refs.doneButton;
        this.configureActions();
    }

    @Contract("_ -> new")
    public static @NotNull MentionPreferencesScreen createWithRefresh(@Nullable Screen parent) {
        ClientMentionPreferences.markAwaitingSync();
        CallYouNetwork.requestPreferencesSync();
        return new MentionPreferencesScreen(parent);
    }

    private void configureActions() {
        this.allowMentionsSwitch.style(style -> style.tooltips(
                Component.translatable("screen.callyou.mention_preferences.allow_all.tooltip")));
        this.allowMentionsSwitch.setOnSwitchChanged(value -> {
            if (this.workingCopy == null) {
                return;
            }
            this.workingCopy.setAllowMentions(value);
            this.refreshTypeButtons();
            this.sendUpdate();
        });

        this.allowMassMentionsSwitch.style(style -> style.tooltips(
                Component.translatable("screen.callyou.mention_preferences.allow_mass.tooltip")));
        this.allowMassMentionsSwitch.setOnSwitchChanged(value -> {
            if (this.workingCopy == null) {
                return;
            }
            this.workingCopy.setAllowMassMentions(value);
            this.sendUpdate();
        });

        this.manageBlockedButton.setOnClick(event -> {
            if (this.minecraft != null) {
                this.minecraft.setScreen(new BlockedSendersScreen(this, this.workingCopy, true));
            }
        });

        this.doneButton.setOnClick(event -> onClose());
    }

    @Override
    public void init() {
        super.init();
        this.requestLatestPreferences();
        this.workingCopy = ClientMentionPreferences.copy();

        this.allowMentionsSwitch.setOn(this.workingCopy.isAllowMentions(), false);
        this.allowMassMentionsSwitch.setOn(this.workingCopy.isAllowMassMentions(), false);

        this.populateMentionTypeList();
    }

    @Override
    public void tick() {
        super.tick();
        this.getModularUI().tick();
        if (ClientMentionPreferences.isSyncPending()) {
            return;
        }
        this.workingCopy = ClientMentionPreferences.copy();
        this.refreshTypeButtons();
        this.allowMentionsSwitch.setOn(this.workingCopy.isAllowMentions(), false);
        this.allowMassMentionsSwitch.setOn(this.workingCopy.isAllowMassMentions(), false);
    }

    @Override
    public void removed() {
        super.removed();
        this.getModularUI().onRemoved();
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    private void populateMentionTypeList() {
        List<ResourceLocation> mentionTypes = this.resolveMentionTypes();
        this.typeToggles.clear();
        this.mentionTypeList.clearAllScrollViewChildren();
        for (ResourceLocation id : mentionTypes) {
            this.mentionTypeList.addScrollViewChild(this.buildMentionTypeRow(id));
        }
        this.refreshTypeButtons();
    }

    private void refreshTypeButtons() {
        if (this.workingCopy == null) {
            return;
        }
        boolean allowMentions = this.workingCopy.isAllowMentions();
        for (Map.Entry<ResourceLocation, Switch> entry : this.typeToggles.entrySet()) {
            ResourceLocation id = entry.getKey();
            Switch toggle = entry.getValue();
            boolean enabled = !this.workingCopy.getBlockedTypes().contains(id);
            toggle.setOn(enabled, false);
            toggle.setActive(allowMentions);
        }
    }

    private @NotNull UIElement buildMentionTypeRow(@NotNull ResourceLocation id) {
        Label label = new Label();
        label.setText(this.buildComponent(id));
        label.layout(style -> style.flexGrow(1));

        Switch toggle = new Switch();
        toggle.setOn(!this.workingCopy.getBlockedTypes().contains(id), false);
        toggle.layout(style -> style.width(34).height(14));
        toggle.setOnSwitchChanged(value -> {
            if (value) {
                this.workingCopy.unblockMentionType(id);
            } else {
                this.workingCopy.blockMentionType(id);
            }
            this.sendUpdate();
        });

        this.typeToggles.put(id, toggle);

        return new UIElement()
                .layout(style -> style.flexDirection(YogaFlexDirection.ROW)
                        .alignItems(YogaAlign.CENTER)
                        .gapColumn(8)
                        .widthStretch()
                        .height(20))
                .addChildren(label, toggle);
    }

    private @NotNull Component buildComponent(@NotNull ResourceLocation id) {
        String key = "mention_type." + id.getNamespace() + "." + id.getPath();
        if (I18n.exists(key)) {
            return Component.translatable(key);
        }
        return Component.literal(id.toString());
    }

    private List<ResourceLocation> resolveMentionTypes() {
        Minecraft mc = this.minecraft;
        if (mc == null || mc.level == null) {
            return List.of();
        }
        Optional<List<ResourceLocation>> result = mc.level.registryAccess()
                .registry(CallYouMentionRegistries.MENTION_TYPE_REGISTRY_KEY)
                .map(registry -> registry.entrySet().stream()
                        .map(entry -> entry.getKey().location())
                        .sorted(Comparator.comparing(ResourceLocation::toString))
                        .toList());
        return result.orElseGet(List::of);
    }

    private void sendUpdate() {
        MentionPreferences payload = new MentionPreferences();
        payload.copyFrom(this.workingCopy);
        ClientMentionPreferences.markPending(payload);
        CallYouNetwork.sendPreferenceUpdate(payload);
    }

    private void requestLatestPreferences() {
        if (!ClientMentionPreferences.isAwaitingSync()) {
            ClientMentionPreferences.markAwaitingSync();
            CallYouNetwork.requestPreferencesSync();
        }
    }

    void onBlockedSendersChanged() {
        this.sendUpdate();
    }

    @Contract(" -> new")
    private static @NotNull UIRefs buildUIRefs() {
        Label titleLabel = new Label();
        titleLabel.setText(TITLE);
        titleLabel.textStyle(style -> style.textAlignHorizontal(Horizontal.CENTER));
        titleLabel.layout(LayoutStyle::widthStretch);

        Switch allowMentionsSwitch = new Switch();
        allowMentionsSwitch.layout(style -> style.width(34).height(14));
        Label allowMentionsLabel = new Label();
        allowMentionsLabel.setText(ALLOW_MENTIONS);
        allowMentionsLabel.layout(style -> style.flexGrow(1));
        UIElement allowRow = new UIElement()
                .layout(style -> style.flexDirection(YogaFlexDirection.ROW)
                        .alignItems(YogaAlign.CENTER)
                        .gapColumn(8)
                        .widthStretch())
                .addChildren(allowMentionsLabel, allowMentionsSwitch);

        Switch allowMassSwitch = new Switch();
        allowMassSwitch.layout(style -> style.width(34).height(14));
        Label allowMassLabel = new Label();
        allowMassLabel.setText(ALLOW_MASS);
        allowMassLabel.layout(style -> style.flexGrow(1));
        UIElement allowMassRow = new UIElement()
                .layout(style -> style.flexDirection(YogaFlexDirection.ROW)
                        .alignItems(YogaAlign.CENTER)
                        .gapColumn(8)
                        .widthStretch())
                .addChildren(allowMassLabel, allowMassSwitch);

        Label typeLabel = new Label();
        typeLabel.setText(TYPE_LABEL);
        typeLabel.layout(LayoutStyle::widthStretch);

        ScrollerView mentionList = new ScrollerView();
        mentionList.layout(style -> style.flexGrow(1).widthStretch());
        mentionList.viewContainer(container -> container.layout(layout -> layout.flexDirection(YogaFlexDirection.COLUMN)
                .gapRow(2)
                .widthStretch()));

        Button manageBlockedButton = new Button();
        manageBlockedButton.setText(MANAGE_BLOCKED);
        manageBlockedButton.layout(style -> style.width(150).height(16));

        Button doneButton = new Button();
        doneButton.setText(CommonComponents.GUI_DONE);
        doneButton.layout(style -> style.width(100).height(16));

        UIElement buttonRow = new UIElement()
                .layout(style -> style.flexDirection(YogaFlexDirection.ROW)
                        .alignItems(YogaAlign.CENTER)
                        .justifyItems(YogaJustify.SPACE_BETWEEN)
                        .widthStretch())
                .addChildren(manageBlockedButton, doneButton);

        UIElement root = new UIElement()
                .addClass("panel_bg")
                .layout(style -> style.width(PANEL_WIDTH)
                        .height(PANEL_HEIGHT)
                        .flexDirection(YogaFlexDirection.COLUMN)
                        .alignItems(YogaAlign.STRETCH))
                .addChildren(titleLabel, allowRow, allowMassRow, typeLabel, mentionList, buttonRow);

        Stylesheet mcStyle = StylesheetManager.INSTANCE.getStylesheetSafe(StylesheetManager.MC);
        ModularUI modularUI = ModularUI.of(UI.of(root, mcStyle));

        return new UIRefs(modularUI, mentionList, allowMentionsSwitch, allowMassSwitch, manageBlockedButton, doneButton);
    }

    private record UIRefs(
            ModularUI modularUI,
            ScrollerView mentionTypeList,
            Switch allowMentionsSwitch,
            Switch allowMassMentionsSwitch,
            Button manageBlockedButton,
            Button doneButton
    ) {
    }
}
