package cn.qihuang02.callyou.handler.command;

import cn.qihuang02.callyou.CallYouByYourName;
import cn.qihuang02.callyou.attachment.CallYouAttachments;
import cn.qihuang02.callyou.registry.CallYouMentionRegistries;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

@EventBusSubscriber(modid = CallYouByYourName.MODID)
public final class CallYouCommands {
    private static final DynamicCommandExceptionType UNKNOWN_MENTION_TYPE = new DynamicCommandExceptionType(
            id -> Component.translatable("command.callyou.error.unknown_type", id)
    );

    @SubscribeEvent
    public static void onRegisterCommands(@NotNull RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                literal("callyou")
                        .requires(source -> source.hasPermission(0))
                        .then(buildIgnoreSubtree())
                        .then(buildUnignoreSubtree())
                        .then(buildPrefsSubtree())
        );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildIgnoreSubtree() {
        return literal("ignore")
                // /callyou ignore all
                .then(literal("all")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            var prefs = player.getData(CallYouAttachments.MENTION_PREFERENCES.get());
                            prefs.setAllowMentions(false);
                            ctx.getSource().sendSuccess(
                                    () -> Component.translatable("command.callyou.ignore.all"),
                                    false
                            );
                            return 1;
                        }))
                // /callyou ignore mass
                .then(literal("mass")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            var prefs = player.getData(CallYouAttachments.MENTION_PREFERENCES.get());
                            prefs.setAllowMassMentions(false);
                            ctx.getSource().sendSuccess(
                                    () -> Component.translatable("command.callyou.ignore.mass"),
                                    false
                            );
                            return 1;
                        }))
                // /callyou ignore key <key>
                .then(literal("type")
                        .then(argument("type", ResourceLocationArgument.id())
                                .suggests(CallYouCommands::suggestMentionTypes)
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    var prefs = player.getData(CallYouAttachments.MENTION_PREFERENCES.get());
                                    var typeID = getMentionTypeID(ctx);
                                    prefs.blockMentionType(typeID);
                                    ctx.getSource().sendSuccess(
                                            () -> Component.translatable("command.callyou.ingore.type", typeID),
                                            false
                                    );
                                    return 1;
                                })))
                // /callyou ignore player <name>
                .then(literal("player")
                        .then(argument("target", EntityArgument.player())
                                .executes(ctx -> {
                                    ServerPlayer self = ctx.getSource().getPlayerOrException();
                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "target");

                                    UUID targetId = target.getUUID();
                                    var prefs = self.getData(CallYouAttachments.MENTION_PREFERENCES.get());
                                    prefs.blockSender(targetId);

                                    ctx.getSource().sendSuccess(
                                            () -> Component.translatable("command.callyou.ignore.player",
                                                    target.getName()),
                                            false
                                    );
                                    return 1;
                                })));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildUnignoreSubtree() {
        return literal("unignore")
                // /callyou unignore all
                .then(literal("all")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            var prefs = player.getData(CallYouAttachments.MENTION_PREFERENCES.get());
                            prefs.setAllowMentions(true);
                            ctx.getSource().sendSuccess(
                                    () -> Component.translatable("command.callyou.unignore.all"),
                                    false
                            );
                            return 1;
                        }))
                // /callyou unignore mass
                .then(literal("mass")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            var prefs = player.getData(CallYouAttachments.MENTION_PREFERENCES.get());
                            prefs.setAllowMassMentions(true);
                            ctx.getSource().sendSuccess(
                                    () -> Component.translatable("command.callyou.unignore.mass"),
                                    false
                            );
                            return 1;
                        }))
                // /callyou unignore key <key>
                .then(literal("type")
                        .then(argument("type", ResourceLocationArgument.id())
                                .suggests(CallYouCommands::suggestMentionTypes)
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    var prefs = player.getData(CallYouAttachments.MENTION_PREFERENCES.get());
                                    var typeID = getMentionTypeID(ctx);
                                    prefs.unblockMentionType(typeID);
                                    ctx.getSource().sendSuccess(
                                            () -> Component.translatable("command.callyou.unignore.type", typeID),
                                            false
                                    );
                                    return 1;
                                })))
                // /callyou unignore player <name>
                .then(literal("player")
                        .then(argument("target", EntityArgument.player())
                                .executes(ctx -> {
                                    ServerPlayer self = ctx.getSource().getPlayerOrException();
                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "target");

                                    UUID targetId = target.getUUID();
                                    var prefs = self.getData(CallYouAttachments.MENTION_PREFERENCES.get());
                                    prefs.unblockSender(targetId);

                                    ctx.getSource().sendSuccess(
                                            () -> Component.translatable("command.callyou.unignore.player",
                                                    target.getName()),
                                            false
                                    );
                                    return 1;
                                })));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildPrefsSubtree() {
        return literal("prefs")
                // /callyou prefs reset
                .then(literal("reset")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            var prefs = player.getData(CallYouAttachments.MENTION_PREFERENCES.get());
                            prefs.resetAll();
                            ctx.getSource().sendSuccess(
                                    () -> Component.translatable("command.callyou.prefs.reset"),
                                    false
                            );
                            return 1;
                        }))
                // /callyou prefs show
                .then(literal("show")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            var prefs = player.getData(CallYouAttachments.MENTION_PREFERENCES.get());

                            ctx.getSource().sendSuccess(
                                    () -> Component.translatable(
                                            "command.callyou.prefs.show.header"),
                                    false
                            );
                            ctx.getSource().sendSuccess(
                                    () -> Component.translatable(
                                            "command.callyou.prefs.show.allow_mentions",
                                            prefs.isAllowMentions()),
                                    false
                            );
                            ctx.getSource().sendSuccess(
                                    () -> Component.translatable(
                                            "command.callyou.prefs.show.allow_mass_mentions",
                                            prefs.isAllowMassMentions()),
                                    false
                            );
                            // 简单一点：只显示 key 的数量和 sender 的数量
                            ctx.getSource().sendSuccess(
                                    () -> Component.translatable(
                                            "command.callyou.prefs.show.blocked_types",
                                            prefs.getBlockedTypes().size()),
                                    false
                            );
                            ctx.getSource().sendSuccess(
                                    () -> Component.translatable(
                                            "command.callyou.prefs.show.blocked_senders",
                                            prefs.getBlockedSenders().size()),
                                    false
                            );
                            return 1;
                        }));
    }

    private static @NotNull ResourceLocation getMentionTypeID(
            CommandContext<CommandSourceStack> ctx
    ) throws CommandSyntaxException {
        ResourceLocation id = ResourceLocationArgument.getId(ctx, "type");
        Registry<?> registry = ctx.getSource().registryAccess()
                .registry(CallYouMentionRegistries.MENTION_TYPE_REGISTRY_KEY)
                .orElse(null);

        if (registry == null || !registry.containsKey(id)) {
            throw UNKNOWN_MENTION_TYPE.create(id);
        }

        return id;
    }

    private static CompletableFuture<Suggestions> suggestMentionTypes(
            @NotNull CommandContext<CommandSourceStack> ctx,
            com.mojang.brigadier.suggestion.@NotNull SuggestionsBuilder builder
    ) {
        return ctx.getSource().registryAccess()
                .registry(CallYouMentionRegistries.MENTION_TYPE_REGISTRY_KEY)
                .map(registry -> SharedSuggestionProvider.suggestResource(registry.keySet(), builder))
                .orElseGet(builder::buildFuture);
    }
}
