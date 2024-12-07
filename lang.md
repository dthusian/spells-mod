# WeaponTroll Language

## Slots

The program has access to 8 "slots", these are storage for
program variables. They can hold one of the following types:
- Number (called Double internally)
- Vec3 (vector of 3 numbers)
- Entity

At the start of your program, slot 0 will contain the entity executing the program,
and slot 1 will contain the target of the program. This depends on the nature of the program,
for example a weapon will have slot 1 as the entity being hit.

## Energy

Some instructions (mostly world-manipulation ones) cost energy. Additionally,
world-manipulation energy costs grow as the distance from the user increases.
Actions within 8 blocks radius have no additional cost. After that, the cost
is scaled by `1+0.001*(r-8)^2`, where `r` is the distance to user.

If there is insufficient energy to execute an instruction in a program, it will stop
and print a message.

TBD

## Instructions

Instructions are listed as `name` followed by parameters.
`<x: T>` indicates a parameter named `x`, with type `T`.
Here are the available types:
- `str`: A literal string
- `num`: A literal number
- `slot`: Any slot
- `slot.num`: A slot containing a number
- `slot.vec`: A slot containing a vector
- `slot.entity`: A slot containing an entity
- `slot=num`: Any slot; will be overwritten with a number
- `slot=vec`: Any slot; will be overwritten with a vector
- `slot=entity`: Any slot; will be overwritten with an entity

### General Instructions

TODO
- `isnull <dst: slot=num> <a: slot>` Sets `dst` to 1 if `a` is null, 0 otherwise.
- `copy <dst: slot> <src: slot>` Copies `src` into `dst`.

### Scalar Instructions

- `loadnum <dst: slot=num> <x: num>` Sets `dst` to the number `x`.
- `add <dst: slot=num> <a: slot.num> <b: slot.num>` Adds two numbers.
- `sub <dst: slot=num> <a: slot.num> <b: slot.num>` Subtracts two numbers (a - b).
- `mul <dst: slot=num> <a: slot.num> <b: slot.num>` Multiplies two numbers.
- `div <dst: slot=num> <a: slot.num> <b: slot.num>` Divides two numbers (a / b).
- `round <dst: slot=num> <a: slot.num>` Rounds to nearest integer.

### Vector Instructions

- `loadvec <dst: slot=vec> <x: num> <y: num> <z: num>` Sets `dst` to the vector defined by `[x, y, z]`.
- `makevec <dst: slot=vec> <x: slot.num> <y: slot.num> <z: slot.num>` Forms a vector from three number slots.
- `splitvec <x: slot=num> <y: slot=num> <z: slot=num> <src: slot.vec>` Splits a vector into its 3 components. This writes to `x`, `y`, and `z`.
- `vadd <dst: slot=vec> <a: slot.vec> <b: slot.vec>` Adds two vectors.
- `vsub <dst: slot=vec> <a: slot.vec> <b: slot.vec>` Subtracts two vectors (a - b).
- `vmul <dst: slot=vec> <a: slot.vec> <b: slot.num>` Multiplies a vector (a) by a scalar (b).
- `vdiv <dst: slot=vec> <a: slot.vec> <b: slot.num>` Divides a vector (a) by a scalar (b).
- `vdist <dst: slot=num> <a: slot.vec>` Computes the length of a vector.
- `vnorm <dst: slot=vec> <a: slot.vec>` Normalizes a vector to length = 1.

### Jump Instructions

- `label <name: str>` Does nothing, but is used to refer to locations in the program for jumping.
- `jmpl <label: str> <a: slot.num> <b: slot.num>` Jumps to the label named `label` if `a` is less than `b`.
- `jmpe <label: str> <a: slot> <b: slot>` Jumps to the label named `label` if `a` is equal to `b`. Works on
  numbers, vectors, and entities.

### Entity Querying Instructions

- `nearestent <dst: slot=ent> <pos: slot.vec> <n: num>` Finds the `n`th nearest entity from the position `pos`.
  `n` is zero-indexed, so `n=0` means the closest entity, `n=1` is the second closest, etc.
  `n` will be truncated (i.e. rounded towards zero).
- `entpos <dst: slot=vec> <ent: slot.ent>` Gets the position of the entity, in blocks.
- `entvel <dst: slot=vec> <ent: slot.ent>` Gets the velocity of the entity, in blocks per tick.
- `entfacing <dst: slot=vec> <ent: slot.ent>` Gets the direction the entity is facing.

### World Manipulation Instructions

- `accelent <ent: slot.ent> <vel: slot.vel>` Adds `vel` (blocks per tick) to `ent`'s velocity.
  - Costs 10 energy per m/s accelerated.
- `damageent <ent: slot.ent> <dmg: slot.num>` Hits `ent` for `dmg` damage.
  - Costs `dmg^2` energy, rounded up.
- `explode <pos: slot.vec> <power: slot.num>` Creates an explosion at `pos` with power `power`.
  - Costs `10*2^power` energy, rounded up.
  - `power` is capped to 6.0, the power of a charged creeper or end crystal.
- `placeblock <pos: slot.vec> <block: str>` Places a block from your inventory to the position `pos`.
  - Costs energy equal to hardness of the block, rounded up, minimum of 1.
- `destroyblock <pos: slot.vec> <block: str>` Destroys a block at position `pos`.
  - Costs energy equal to hardness of the block, rounded up, minimum of 1.
- `summonlightning <pos: slot.vec>` Summons lightning at position `pos`.
  - Costs 20 energy
- `summonfireball <ent: slot=ent> <pos: slot.num>` Summons a fireball at position `pos`, and returns it in `ent`.
  - Costs 20 energy