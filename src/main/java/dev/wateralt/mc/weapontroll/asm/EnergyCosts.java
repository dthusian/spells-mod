package dev.wateralt.mc.weapontroll.asm;

public class EnergyCosts {
  // Instruction energy costs
  public static double ACCEL_COST_FACTOR = 10;
  public static double DAMAGE_COST_POWER = 2;
  public static double EXPLODE_COST_FACTOR = 10;
  public static double EXPLODE_COST_BASE = 2;
  public static double PLACE_COST_FACTOR = 1;
  public static double DESTROY_COST_FACTOR = 1;
  public static double LIGHTNING_COST = 20;
  public static double FIREBALL_COST = 20;
  
  // Energy depletion hp cost
  public static double HP_PER_ENERGY_DEPLETED = 0.04;
  
  // Natural energy regen
  public static int ENERGY_REGEN = 10;
}
