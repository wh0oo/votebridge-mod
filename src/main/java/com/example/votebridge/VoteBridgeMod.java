package com.example.votebridge;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.CommandDispatcher;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.PlaceholderContext;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class VoteBridgeMod implements ModInitializer {

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(this::register);
    }

    private void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess access, CommandManager.RegistrationEnvironment env) {
        dispatcher.register(CommandManager.literal("voteannounce")
            .requires(src -> src.hasPermissionLevel(2))
            .then(CommandManager.argument("player", EntityArgumentType.player())
            .then(CommandManager.argument("service", StringArgumentType.greedyString())
            .executes(ctx -> {
                ServerCommandSource src = ctx.getSource();
                ServerPlayerEntity player = EntityArgumentType.getPlayer(ctx, "player");
                String service = StringArgumentType.getString(ctx, "service");

                // Resolve %votelistener:vote_count% in the player's context
                Text parsed = Placeholders.parseText(Text.literal("%votelistener:vote_count%"), PlaceholderContext.of(player));
                String count = parsed.getString();

                // Use display name text -> string (works across mappings)
                String playerName = player.getName().getString();

                String tellraw = String.format(
                    "tellraw @a [{\\\"text\\\":\\\"%s\\\",\\\"color\\\":\\\"blue\\\"},{\\\"text\\\":\\\" voted on \\\",\\\"color\\\":\\\"aqua\\\"},{\\\"text\\\":\\\"%s\\\",\\\"color\\\":\\\"blue\\\"},{\\\"text\\\":\\\" (\\\",\\\"color\\\":\\\"aqua\\\"},{\\\"text\\\":\\\"%s\\\",\\\"color\\\":\\\"yellow\\\"},{\\\"text\\\":\\\" total votes)\\\",\\\"color\\\":\\\"aqua\\\"}]",
                    escape(playerName), escape(service), escape(count)
                );

                MinecraftServer server = src.getServer();
                server.getCommandManager().executeWithPrefix(server.getCommandSource(), tellraw);
                return 1;
            }))));
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
