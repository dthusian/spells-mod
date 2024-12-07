package dev.wateralt.mc.weapontroll.asm;


import java.lang.reflect.Constructor;

public abstract class Dialect {
  public abstract short parseSlot(String str);
  public abstract double parseDouble(String str);
  public abstract Class<? extends Instructions.Instr> lookupMnemonic(String name);

  public Instructions.Instr parse(String x) {
    String[] parts = x.split(" ");
    String op = parts[0];
    Class<? extends Instructions.Instr> cls = this.lookupMnemonic(op);
    if(cls == null) {
      throw new AsmError("Invalid instruction: %s".formatted(op));
    }
    Constructor<?> ctor = cls.getConstructors()[0];
    Class<?>[] paramTypes = ctor.getParameterTypes();
    if(paramTypes.length != parts.length - 1) {
      throw new AsmError("Wrong number of params: expected %d, found %d".formatted(paramTypes.length, parts.length - 1));
    }
    Object[] params = new Object[paramTypes.length];
    for(int i = 0; i < paramTypes.length; i++) {
      if(paramTypes[i] == String.class) {
        params[i] = parts[i + 1];
      } else if(paramTypes[i] == short.class) {
        params[i] = parseSlot(parts[i + 1]);
      } else if(paramTypes[i] == double.class) {
        params[i] = parseDouble(parts[i + 1]);
      } else {
        throw new RuntimeException("Invalid type in instruction ctor");
      }
    }
    try {
      return (Instructions.Instr) ctor.newInstance(params);
    } catch(Exception err) {
      throw new RuntimeException(err);
    }
  }
}
