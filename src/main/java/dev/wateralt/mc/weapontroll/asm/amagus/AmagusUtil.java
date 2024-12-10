package dev.wateralt.mc.weapontroll.asm.amagus;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class AmagusUtil {
  public static List<Vec3d> entityToPos(List<Entity> entities) {
    return entities.stream().map(Entity::getPos).toList();
  }
}
