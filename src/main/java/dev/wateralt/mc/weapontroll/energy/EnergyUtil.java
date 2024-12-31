package dev.wateralt.mc.weapontroll.energy;

import dev.wateralt.mc.weapontroll.asm.AsmError;
import dev.wateralt.mc.weapontroll.asm.std20.EnergyCosts;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.number.StyledNumberFormat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class EnergyUtil {
  public static void useEnergy(ServerPlayerEntity spl, double amount) {
    boolean stop = false;
    int newEnergy = getEnergy(spl) - (int)Math.ceil(amount);
    if(newEnergy < 0) {
      int deficit = -newEnergy;
      newEnergy = 0;
      double damage = deficit * EnergyCosts.HP_PER_ENERGY_DEPLETED;
      if(!spl.isCreative() && spl.isAlive()) {
        if(damage + 2 >= spl.getHealth()) {
          spl.damage(
            spl.getServerWorld(),
            spl.getRegistryManager()
              .getOptional(RegistryKeys.DAMAGE_TYPE)
              .flatMap(v -> v.getEntry(Identifier.of("weapontroll", "mana_depletion")))
              .map(DamageSource::new)
              .orElseGet(() -> spl.getDamageSources().magic()),
            50.0f);
          stop = true;
        } else {
          spl.setHealth((float) (spl.getHealth() - damage));
          spl.markHealthDirty();
        }
      }
    }
    setEnergy(spl, newEnergy);
    if(stop) {
      throw new AsmError("Out of energy!");
    }
  }
  
  public static void addEnergy(ServerPlayerEntity spl, int amount) {
    addEnergyIgnoringLimit(spl, amount);
  }
  
  public static void addEnergyIgnoringLimit(ServerPlayerEntity spl, int amount) {
    setEnergy(spl, getEnergy(spl) + amount);
  }
  
  public static void setEnergy(ServerPlayerEntity spl, int amount) {
    MinecraftServer server = spl.getServer();
    if(server != null) {
      ServerScoreboard scoreboard = server.getScoreboard();
      ScoreboardObjective obj = getOrCreate(scoreboard);
      scoreboard.getOrCreateScore(spl, obj, true).setScore(amount);
    }
  }
  
  public static int getEnergy(ServerPlayerEntity spl) {
    MinecraftServer server = spl.getServer();
    if(server != null) {
      ServerScoreboard scoreboard = server.getScoreboard();
      ScoreboardObjective obj = getOrCreate(scoreboard);
      ReadableScoreboardScore score = scoreboard.getScore(spl, obj);
      if(score != null) {
        return score.getScore();
      }
    }
    return 0;
  }
  
  private static ScoreboardObjective getOrCreate(ServerScoreboard scoreboard) {
    ScoreboardObjective obj = scoreboard.getNullableObjective("weapontroll_energy");
    if(obj == null) {
      obj = scoreboard.addObjective(
        "weapontroll_energy",
        ScoreboardCriterion.DUMMY,
        Text.of("Energy Values"),
        ScoreboardCriterion.RenderType.INTEGER,
        true,
        StyledNumberFormat.YELLOW
      );
    }
    return obj;
  }
}
