package cn.qihuang02.callyou.core.mention.executor;

import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.MentionType;
import cn.qihuang02.callyou.api.components.Notifier;
import cn.qihuang02.callyou.api.components.TargetProvider;
import cn.qihuang02.callyou.api.event.MentionEvent;
import cn.qihuang02.callyou.api.TargetCollection;
import cn.qihuang02.callyou.core.MentionGuard;
import cn.qihuang02.callyou.core.attachment.CallYouAttachments;
import cn.qihuang02.callyou.core.attachment.MentionPreferences;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class MentionDispatcher {
    private final MentionGuard guard;
    private final MentionHistoryRecorder historyRecorder;

    public MentionDispatcher(@NotNull MentionGuard guard, @NotNull MentionHistoryRecorder historyRecorder) {
        this.guard = guard;
        this.historyRecorder = historyRecorder;
    }

    public @NotNull List<ServerPlayer> dispatchAll(
            @NotNull List<MentionExecution> pendingMentions,
            long nowTick,
            @NotNull Component formattedMessage
    ) {
        List<ServerPlayer> allTargetsHit = new ArrayList<>();
        for (MentionExecution pending : pendingMentions) {
            List<ServerPlayer> hit = dispatchSingle(pending.type(), pending.context(), nowTick, formattedMessage);
            allTargetsHit.addAll(hit);
        }
        return allTargetsHit;
    }

    private @NotNull List<ServerPlayer> dispatchSingle(
            @NotNull MentionType type,
            @NotNull MentionContext context,
            long nowTick,
            @NotNull Component formattedMessage
    ) {
        TargetProvider targetProvider = type.targetProvider();
        Notifier notifier = type.notifier();

        TargetCollection rawTargets = targetProvider.resolveTargets(context);
        boolean hadRawTargets = !rawTargets.isEmpty();

        MentionEvent.Pre preEvent = new MentionEvent.Pre(context, type, rawTargets);
        NeoForge.EVENT_BUS.post(preEvent);

        if (preEvent.isCanceled()) {
            return List.of();
        }
        TargetCollection eventTargets = preEvent.getTargets();
        if (eventTargets.isEmpty() && hadRawTargets) {
            return List.of();
        }

        List<ServerPlayer> resolvedOnline = eventTargets.resolveOnline(context.server());

        List<ServerPlayer> filteredTargets =
                guard.filterTargets(context.sender(), type, context.typeId(), context, resolvedOnline, nowTick);

        if (filteredTargets.isEmpty()) {
            historyRecorder.record(context, eventTargets, resolvedOnline, List.of(), formattedMessage);
            return List.of();
        }

        List<ServerPlayer> finalTargets = new ArrayList<>();

        for (ServerPlayer target : filteredTargets) {
            MentionPreferences prefs = target.getData(CallYouAttachments.MENTION_PREFERENCES);
            ResourceLocation typeId = context.typeId();

            if (typeId != null && !prefs.isNotifierEnabled(typeId, target.server.registryAccess())) {
                continue;
            }
            finalTargets.add(target);
        }

        if (finalTargets.isEmpty()) {
            historyRecorder.record(context, eventTargets, resolvedOnline, List.of(), formattedMessage);
            return List.of();
        }

        notifier.apply(context, finalTargets);

        NeoForge.EVENT_BUS.post(new MentionEvent.Post(context, type, TargetCollection.ofPlayers(finalTargets)));

        historyRecorder.record(context, eventTargets, resolvedOnline, finalTargets, formattedMessage);

        return finalTargets;
    }
}
