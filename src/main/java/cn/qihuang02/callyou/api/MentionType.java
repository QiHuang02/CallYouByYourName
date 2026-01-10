package cn.qihuang02.callyou.api;

import cn.qihuang02.callyou.api.components.MentionRules;
import cn.qihuang02.callyou.api.components.Notifier;
import cn.qihuang02.callyou.api.components.TargetProvider;
import cn.qihuang02.callyou.api.components.TextFormatter;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.mojang.serialization.Codec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class MentionType implements IPersistedSerializable {
    public static final Codec<MentionType> MENTION_TYPE_CODEC = PersistedParser.createCodec(MentionType::new);

    @Persisted(key = "target")
    private TargetProvider targetProvider;

    @Persisted(key = "format")
    private TextFormatter textFormatter;

    @Persisted(key = "notifier")
    private Notifier notifier;

    @Persisted(key = "rules")
    private MentionRules rules;

    public MentionType() {
        this(null, null, null, MentionRules.DEFAULT);
    }

    public MentionType(
            @Nullable TargetProvider targetProvider,
            @Nullable TextFormatter textFormatter,
            @Nullable Notifier notifier,
            @Nullable MentionRules rules
    ) {
        this.targetProvider = targetProvider;
        this.textFormatter = textFormatter;
        this.notifier = notifier;
        this.rules = rules == null ? MentionRules.DEFAULT : rules;
    }

    public @Nullable TargetProvider targetProvider() {
        return targetProvider;
    }

    public @Nullable TextFormatter textFormatter() {
        return textFormatter;
    }

    public @Nullable Notifier notifier() {
        return notifier;
    }

    public @NotNull MentionRules rules() {
        return rules;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MentionType that = (MentionType) o;
        return Objects.equals(targetProvider, that.targetProvider)
                && Objects.equals(textFormatter, that.textFormatter)
                && Objects.equals(notifier, that.notifier)
                && Objects.equals(rules, that.rules);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetProvider, textFormatter, notifier, rules);
    }

    @Override
    public String toString() {
        return "MentionType{" +
                "targetProvider=" + targetProvider +
                ", textFormatter=" + textFormatter +
                ", notifier=" + notifier +
                ", rules=" + rules +
                '}';
    }
}
