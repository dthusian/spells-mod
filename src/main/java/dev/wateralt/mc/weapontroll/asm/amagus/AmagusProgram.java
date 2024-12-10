package dev.wateralt.mc.weapontroll.asm.amagus;

import dev.wateralt.mc.weapontroll.asm.Program;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class AmagusProgram implements Program {
  private Object[] sexpr;
  
  public AmagusProgram(Object[] expr) {
    this.sexpr = expr;
  }
  
  public Object[] getSexpr() {
    return sexpr;
  }

  @Override
  public AmagusProgramState prepareRun(ServerWorld world, Vec3d origin, LivingEntity user, LivingEntity target) {
    return new AmagusProgramState(this, world, user, target, origin);
  }
}
