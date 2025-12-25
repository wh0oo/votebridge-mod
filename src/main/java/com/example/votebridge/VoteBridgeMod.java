package com.example.votebridge;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class VoteBridgeMod implements ModInitializer {

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                CommandManager.literal("votebridge")
                    // FIXED FOR 1.21.11
                    .requires(source -> source.hasPermission(2))
                    .executes(context -> {
                        ServerCommandSource source = context.getSource();
                        source.sendFeedback(
                            () -> Text.literal("VoteBridge command executed."),
                            false
                        );
                        return 1;
                    })
            );
        });
    }
}
