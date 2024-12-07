package dev.wateralt.mc.weapontroll.playertracker;

import net.minecraft.server.network.ServerPlayerEntity;

public record TrackedPlayer(ServerPlayerEntity entity, int energy, int maxEnergy) { }
