package dev.wateralt.mc.weapontroll.asm.amagus;

import dev.wateralt.mc.weapontroll.Util;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;

public class Functions {
  public record Def(String name, AmagusFunc metadata, Method method) { }

  @AmagusFunc(format = "set %e on fire")
  public static void setFireTo(AmagusProgramState state) {
    state.useEnergy(EnergyCosts.SET_FIRE);
    if(state.target() == null) {
      BlockPos blockPos = Util.vecToPos(state.targetPos());
      if(state.world().canPlace(Blocks.FIRE.getDefaultState(), blockPos, ShapeContext.absent())) {
        state.world().setBlockState(blockPos, Blocks.FIRE.getDefaultState(), Block.NOTIFY_ALL);
      }
    } else {
      state.target().setOnFireFor(5);
    }
  }
  
  public static final HashMap<String, Def> FUNCTIONS = new HashMap<>();
  
  static {
    Arrays.stream(Functions.class.getMethods()).forEach(v -> FUNCTIONS.put(v.getName(), new Def(v.getName(), v.getAnnotation(AmagusFunc.class), v)));
  }
}
