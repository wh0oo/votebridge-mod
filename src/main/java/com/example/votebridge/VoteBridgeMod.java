package com.example.votebridge;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.ServerPlaceholderContext;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class VoteBridgeMod implements ModInitializer {

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(this::register);
    }

    private void register(
            CommandDispatcher<CommandSourceStack> dispatcher,
            CommandBuildContext access,
            Commands.CommandSelection env
    ) {
        dispatcher.register(
            Commands.literal("voteannounce")
                .then(Commands.argument("player", EntityArgument.player())
                .then(Commands.argument("service", StringArgumentType.greedyString())
                .executes(ctx -> {

                    ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                    String service = StringArgumentType.getString(ctx, "service");

                    String count = Placeholders.SERVER_PLACEHOLDER_PARSER
                        .parseComponent(
                            "%votelistener:vote_count%",
                            ServerPlaceholderContext.of(player).asParserContext()
                        )
                        .getString();

                    String playerName = player.getName().getString();

                    String tellraw = String.format(
                        "/tellraw @a [" +
                            "{\"text\":\"%s\",\"color\":\"blue\"}," +
                            "{\"text\":\" voted on \",\"color\":\"aqua\"}," +
                            "{\"text\":\"%s\",\"color\":\"blue\"}," +
                            "{\"text\":\" (\",\"color\":\"aqua\"}," +
                            "{\"text\":\"%s\",\"color\":\"yellow\"}," +
                            "{\"text\":\" total votes)\",\"color\":\"aqua\"}" +
                        "]",
                        escape(playerName),
                        escape(service),
                        escape(count)
                    );

                    MinecraftServer server = ctx.getSource().getServer();
                    CommandSourceStack console = server.createCommandSourceStack();

                    server.getCommands().performPrefixedCommand(console, tellraw);

                    return 1;
                })))
        );
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}