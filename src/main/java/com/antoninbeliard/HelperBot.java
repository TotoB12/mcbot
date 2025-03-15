package com.antoninbeliard;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

public class HelperBot implements DedicatedServerModInitializer {
    // Global reference for our bot armor stand
    public static ArmorStandEntity bot;

    @Override
    public void onInitializeServer() {
        System.out.println("Helper Bot Initialized!");

        // Register the "/bot come" command
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(CommandManager.literal("bot")
                .then(CommandManager.literal("come")
                    .executes(context -> {
                        if (context.getSource().getEntity() instanceof ServerPlayerEntity) {
                            ServerPlayerEntity player = (ServerPlayerEntity) context.getSource().getEntity();
                            ServerWorld world = context.getSource().getWorld();

                            // If the bot is null or no longer alive, spawn it
                            if (bot == null || !bot.isAlive()) {
                                double x = player.getX();
                                double y = player.getY();
                                double z = player.getZ();

                                bot = new ArmorStandEntity(EntityType.ARMOR_STAND, world);
                                bot.refreshPositionAndAngles(x, y, z, 0, 0);
                                bot.setCustomName(Text.literal("bot"));
                                bot.setCustomNameVisible(true);
                                bot.setNoGravity(true);
                                world.spawnEntity(bot);
                                context.getSource().sendFeedback(() -> Text.literal("Bot spawned and teleported to you!"), false);
                            } else {
                                // Teleport the existing bot to the player's location
                                bot.requestTeleport(player.getX(), player.getY(), player.getZ());
                                context.getSource().sendFeedback(() -> Text.literal("Bot teleported to you!"), false);
                            }
                            return 1;
                        } else {
                            context.getSource().sendError(Text.literal("This command can only be executed by a player."));
                            return 0;
                        }
                    })
                )
            );
        });
    }
}