package dev.wateralt.mc.weapontroll.test;

import dev.wateralt.mc.weapontroll.asm.Executor;
import dev.wateralt.mc.weapontroll.asm.Program;

import java.util.Objects;

public class Tests {
  public static <T> void assertEqual(T a, T b) {
    if(!Objects.equals(a, b)) {
      throw new RuntimeException("Assertion failed: %s != %s".formatted(a, b));
    }
  }
  
  public static void test1() {
    Program prog = new Program("""
loadnum 0 5
add 0 0 0
loadnum 3 4
loadnum 7 9
sub 0 3 0
mul 1 7 3
div 2 1 0
""", 8);
    Object[] slots = Executor.execute(prog, null, null, null, 32);
    assertEqual((Double)slots[2], -6.0);
  }
  
  public static void testAll() {
    test1();
  }
}
