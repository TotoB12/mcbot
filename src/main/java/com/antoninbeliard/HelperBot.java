package com.antoninbeliard;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class HelperBot implements DedicatedServerModInitializer {
    // Global reference for our bot Allay
    public static AllayEntity bot;

    @Override
    public void onInitializeServer() {
        System.out.println("Helper Bot Initialized!");

        // Register the server started event to spawn the bot at world spawn if it
        // doesn't exist
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            ServerWorld overworld = server.getWorld(ServerWorld.OVERWORLD);
            if (overworld != null) {
                // Check for existing Allays named "bot"
                List<? extends AllayEntity> existingBots = overworld.getEntitiesByType(EntityType.ALLAY,
                        entity -> entity.getCustomName() != null && entity.getCustomName().getString().equals("bot"));
                if (!existingBots.isEmpty()) {
                    bot = existingBots.get(0);
                    System.out.println("Existing bot found.");
                } else {
                    // Spawn a new Allay at the world spawn
                    BlockPos spawnPos = overworld.getSpawnPos();
                    double x = spawnPos.getX() + 0.5; // Center on block
                    double y = spawnPos.getY();
                    double z = spawnPos.getZ() + 0.5;
                    bot = new AllayEntity(EntityType.ALLAY, overworld);
                    bot.refreshPositionAndAngles(x, y, z, 0, 0);
                    bot.setCustomName(Text.literal("bot"));
                    bot.setCustomNameVisible(true);
                    overworld.spawnEntity(bot);
                    System.out.println("Bot spawned at world spawn.");
                }
            }
        });

        // Register the "/bot come" command
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(CommandManager.literal("bot")
                    .then(CommandManager.literal("come")
                            .executes(context -> {
                                if (context.getSource().getEntity() instanceof ServerPlayerEntity) {
                                    ServerPlayerEntity player = (ServerPlayerEntity) context.getSource().getEntity();
                                    ServerWorld world = context.getSource().getWorld();

                                    // If the bot is null or not alive, spawn a new one at the player's location
                                    if (bot == null || !bot.isAlive()) {
                                        double x = player.getX();
                                        double y = player.getY();
                                        double z = player.getZ();
                                        bot = new AllayEntity(EntityType.ALLAY, world);
                                        bot.refreshPositionAndAngles(x, y, z, 0, 0);
                                        bot.setCustomName(Text.literal("bot"));
                                        bot.setCustomNameVisible(true);
                                        world.spawnEntity(bot);
                                        context.getSource().sendFeedback(
                                                () -> Text.literal("Bot spawned and teleported to you!"), false);
                                    } else {
                                        // Teleport the existing bot to the player's location
                                        // Command the bot to navigate toward the player's location using its native
                                        // pathfinding.
                                        bot.getNavigation().startMovingTo(player, 1.0);
                                        context.getSource().sendFeedback(() -> Text.literal("Bot navigating to you!"),
                                                false);
                                    }
                                    return 1;
                                } else {
                                    context.getSource()
                                            .sendError(Text.literal("This command can only be executed by a player."));
                                    return 0;
                                }
                            })));
        });
    }
}