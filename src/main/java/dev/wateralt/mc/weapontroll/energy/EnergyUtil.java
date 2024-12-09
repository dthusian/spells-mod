package dev.wateralt.mc.weapontroll.energy;

import dev.wateralt.mc.weapontroll.Weapontroll;
import dev.wateralt.mc.weapontroll.asm.AsmError;
import dev.wateralt.mc.weapontroll.asm.std20.EnergyCosts;
import net.minecraft.server.network.ServerPlayerEntity;

public class EnergyUtil {
  public static void useEnergy(ServerPlayerEntity spl, double amount) {
    boolean stop = false;
    TrackedPlayer pl = Weapontroll.PLAYER_TRACKER.get(spl);
    int newEnergy = pl.getEnergy() - (int)Math.ceil(amount);
    if(newEnergy < 0) {
      int deficit = -newEnergy;
      newEnergy = 0;
      double damage = deficit * EnergyCosts.HP_PER_ENERGY_DEPLETED;
      if(!spl.isCreative()) {
        if(damage > spl.getHealth()) {
          spl.kill(spl.getServerWorld());
          stop = true;
        } else {
          spl.setHealth((float) (spl.getHealth() - damage));
          spl.markHealthDirty();
        }
      }
    }
    pl.setEnergy(newEnergy);
    if(stop) {
      throw new AsmError("Out of energy!");
    }
  }
}
