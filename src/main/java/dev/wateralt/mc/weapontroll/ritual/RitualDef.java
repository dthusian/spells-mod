package dev.wateralt.mc.weapontroll.ritual;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

import java.util.List;

public record RitualDef(
  int enchantLevel,
  List<ItemRequirement> materials,
  List<BlockRequirement> blocks,
  int time,
  int initialChaos,
  int maxChaos,
  int chaosReductionCost, // per 100 chaos units
  double errorRate,
  int errorCooldown
) {
  public record ItemRequirement(Item item, int count) {}
  public record BlockRequirement(Block block, int count) {}
}
