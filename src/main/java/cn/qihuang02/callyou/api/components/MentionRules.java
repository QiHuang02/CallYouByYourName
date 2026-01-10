package cn.qihuang02.callyou.api.components;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.mojang.serialization.Codec;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class MentionRules implements IPersistedSerializable {
    public static final MentionRules DEFAULT = new MentionRules(0, false);
    public static final Codec<MentionRules> CODEC = PersistedParser.createCodec(MentionRules::new);

    @Persisted(key = "min_op_level")
    private int minOpLevel;
    @Persisted(key = "is_mass")
    private boolean isMass;

    public MentionRules() {
        this(0, false);
    }

    public MentionRules(int minOpLevel, boolean isMass) {
        this.minOpLevel = minOpLevel;
        this.isMass = isMass;
    }

    public int minOpLevel() {
        return minOpLevel;
    }

    public boolean isMass() {
        return isMass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MentionRules that = (MentionRules) o;
        return minOpLevel == that.minOpLevel && isMass == that.isMass;
    }

    @Override
    public int hashCode() {
        return Objects.hash(minOpLevel, isMass);
    }

    @Override
    public @NotNull String toString() {
        return "MentionRules{" +
                "minOpLevel=" + minOpLevel +
                ", isMass=" + isMass +
                '}';
    }
}
