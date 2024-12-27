package dev.wateralt.mc.weapontroll.ritual;

import java.util.List;

public class Rituals {
  public static final RitualDef ATTUNE1 = new RitualDef(
    1,
    List.of(),
    List.of(),
    15*20,
    200,
    1000,
    100,
    0.005,
    4*20
  );
  public static final RitualDef ATTUNE2 = new RitualDef(
    2,
    List.of(),
    List.of(),
    30*20,
    500,
    1000,
    150,
    0.008,
    5*20
  );
  public static final RitualDef ATTUNE3 = new RitualDef(
    3,
    List.of(),
    List.of(),
    45*20,
    800,
    1000,
    180,
    0.01,
    6*20
  );
}
