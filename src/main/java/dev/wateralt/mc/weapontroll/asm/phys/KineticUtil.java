package dev.wateralt.mc.weapontroll.asm.phys;

import net.minecraft.util.math.Vec3d;

public class KineticUtil {
  public double getKineticEnergy(Vec3d v) {
    return v.lengthSquared();
  }
  
  public double getDifferenceInKineticEnergy(Vec3d vInitial, Vec3d vFinal) {
    return vFinal.lengthSquared() - vInitial.lengthSquared();
  }
}
