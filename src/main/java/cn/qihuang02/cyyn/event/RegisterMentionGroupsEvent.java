package cn.qihuang02.cyyn.event;

import cn.qihuang02.cyyn.mention.MentionGroup;
import cn.qihuang02.cyyn.mention.MentionGroupRegistry;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.NotNull;

public class RegisterMentionGroupsEvent extends Event {
    public void register(@NotNull MentionGroup mentionGroup) {
        MentionGroupRegistry.register(mentionGroup);
    }
}
