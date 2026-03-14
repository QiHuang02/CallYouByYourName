package cn.qihuang02.callyou.core.mention.lifecycle;

import cn.qihuang02.callyou.api.MentionContext;

public interface MentionLifeCycle {
    void process(MentionContext context);
}
