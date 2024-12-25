package dev.wateralt.mc.weapontroll.asm.std20;

import java.util.HashMap;

public class EnergyCosts {
  // Instruction energy costs
  public static final double ACCEL_COST_FACTOR = 40;
  public static final double DAMAGE_COST_POWER = 2;
  public static final double EXPLODE_COST_FACTOR = 10;
  public static final double EXPLODE_COST_BASE = 2;
  public static final double PLACE_COST_FACTOR = 3;
  public static final double DESTROY_COST_FACTOR = 3;
  public static final double LIGHTNING_COST = 100;
  public static final double MOUNT_DIST_COST_FACTOR = 1;
  
  // Cost for each entity
  public static final HashMap<String, Integer> SUMMON_ENTITY_COSTS = new HashMap<>();
  
  static {
    SUMMON_ENTITY_COSTS.put("pig", 200);
    SUMMON_ENTITY_COSTS.put("chicken", 200);
    SUMMON_ENTITY_COSTS.put("zombie", 300);
    SUMMON_ENTITY_COSTS.put("skeleton", 300);
    
    SUMMON_ENTITY_COSTS.put("arrow", 50);
    SUMMON_ENTITY_COSTS.put("fireball", 30);
    SUMMON_ENTITY_COSTS.put("snowball", 20);
  }
  
  // Energy depletion hp cost
  public static double HP_PER_ENERGY_DEPLETED = 0.2;
  
  // Threshold for "far" interactions that cost extra
  public static int LOCAL_RADIUS = 8;
}
