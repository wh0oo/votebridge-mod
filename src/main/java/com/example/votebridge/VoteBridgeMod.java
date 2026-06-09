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

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class VoteBridgeMod implements ModInitializer {
    private static final Path CONFIG_DIR = Path.of("config", "votebridge");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("config.json");
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    private static String webhookUrl = "";

    @Override
    public void onInitialize() {
        loadConfig();
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

                    sendDiscordWebhook(playerName, service, count);

                    return 1;
                })))
        );
    }

    private static void loadConfig() {
        try {
            Files.createDirectories(CONFIG_DIR);

            if (!Files.exists(CONFIG_FILE)) {
                String defaultConfig = "{\n" +
                    "  \"webhookUrl\": \"\"\n" +
                    "}\n";
                Files.writeString(CONFIG_FILE, defaultConfig, StandardCharsets.UTF_8);
                return;
            }

            String json = Files.readString(CONFIG_FILE, StandardCharsets.UTF_8);
            webhookUrl = extractJsonString(json, "webhookUrl").trim();

        } catch (IOException e) {
            System.err.println("[VoteBridge] Failed to load config: " + e.getMessage());
            webhookUrl = "";
        }
    }

    private static void sendDiscordWebhook(String playerName, String service, String count) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return;
        }

        String ansiLine =
            "\u001b[34m" + sanitizeAnsi(playerName) + "\u001b[0m " +
            "\u001b[36mvoted on\u001b[0m " +
            "\u001b[34m" + sanitizeAnsi(service) + "\u001b[0m " +
            "\u001b[36m(\u001b[0m" +
            "\u001b[33m" + sanitizeAnsi(count) + "\u001b[0m " +
            "\u001b[36mtotal votes)\u001b[0m";

        String description = "```ansi\n" + ansiLine + "\n```";

        String payload = "{"
            + "\"username\":\"VoteBridge\","
            + "\"embeds\":[{"
            + "\"title\":\"Vote received\","
            + "\"description\":\"" + escapeJson(description) + "\","
            + "\"color\":3447003"
            + "}]"
            + "}";

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(webhookUrl))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
            .build();

        HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenAccept(response -> {
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    System.err.println("[VoteBridge] Discord webhook failed: HTTP " + response.statusCode() + " " + response.body());
                }
            })
            .exceptionally(error -> {
                System.err.println("[VoteBridge] Discord webhook error: " + error.getMessage());
                return null;
            });
    }

    private static String extractJsonString(String json, String key) {
        String search = "\"" + key + "\"";
        int keyIndex = json.indexOf(search);
        if (keyIndex < 0) return "";

        int colonIndex = json.indexOf(":", keyIndex);
        if (colonIndex < 0) return "";

        int firstQuote = json.indexOf("\"", colonIndex + 1);
        if (firstQuote < 0) return "";

        int secondQuote = firstQuote + 1;
        while (secondQuote < json.length()) {
            if (json.charAt(secondQuote) == '"' && json.charAt(secondQuote - 1) != '\\') {
                break;
            }
            secondQuote++;
        }

        if (secondQuote >= json.length()) return "";

        return json.substring(firstQuote + 1, secondQuote)
            .replace("\\\"", "\"")
            .replace("\\\\", "\\");
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

 private static String escapeJson(String s) {
    if (s == null) return "";

    StringBuilder out = new StringBuilder();

    for (int i = 0; i < s.length(); i++) {
        char c = s.charAt(i);

        switch (c) {
            case '"' -> out.append("\\\"");
            case '\\' -> out.append("\\\\");
            case '\b' -> out.append("\\b");
            case '\f' -> out.append("\\f");
            case '\n' -> out.append("\\n");
            case '\r' -> out.append("\\r");
            case '\t' -> out.append("\\t");
            default -> {
                if (c < 0x20) {
                    out.append(String.format("\\u%04x", (int) c));
                } else {
                    out.append(c);
                }
            }
        }
    }

    return out.toString();
}

    private static String sanitizeAnsi(String s) {
        if (s == null) return "";
        return s
            .replace("\u001b", "")
            .replace("`", "'")
            .replace("@", "@\u200B");
    }
}