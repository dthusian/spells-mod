# std20 Language Reference

Write `#lang std20` at the start of a book to use the std20 language.

## Slots

The program has access to 8 "slots", these are storage for
program variables. They can hold one of the following types:
- Number (Java double type)
- String
- Vector (vector of 3 numbers, for coordinates or directions)
- Entity

At the start of your program, slot 0 will contain the entity executing the program,
and slot 1 will contain the target of the program. This depends on the nature of the program,
for example a weapon will have slot 1 as the entity being hit.

## Energy

Some instructions (mostly world-manipulation ones) cost energy. Energy cost
will be listed under the instruction, and usually depends on the amount of action
that the instruction does.

Additionally, energy costs grow as the distance from the user increases.
Actions within 8 blocks radius have no additional cost. After that, the cost
is scaled by `1+0.001*(r-8)^2`, where `r` is the distance to user.

## Instructions

Instructions are of the form `[<slot> = ] <instruction> [<argument 1>] [<argument 2>] ...`.

Instructions are listed as `name` followed by parameters.
`<x: T>` indicates a parameter named `x`, with type `T`.
Here are the available types:
- `any`: Any type is valid
- `number`: Number
- `string`: String
- `vector`: Vector
- `entity`: Entity

The syntax `<T> = ` before an instruction indicates the instruction returns `T`.

### General Instructions

- `<number> = isnull <x: any>` Returns 1 if `x` is null, 0 otherwise.
- `<any> = mov <x: any>` Copies `x`.

### Scalar Instructions

- `<number> = add <a: number> <b: number>` Adds two numbers.
- `<number> = sub <a: number> <b: number>` Subtracts two numbers (a - b).
- `<number> = mul <a: number> <b: number>` Multiplies two numbers.
- `<number> = div <a: number> <b: number>` Divides two numbers (a / b).
- `<number> = round <a: number>` Rounds to nearest integer.
- `<number> = sqrt <a: number>` Takes the square root.
- `<number> = sin <a: number>` Computes sin, in radians
- `<number> = cos <a: number>` Computes cos, in radians

### Vector Instructions

- `<number> = makevec <x: number> <y: number> <z: number>` Forms a vector from three number slots.
- `<number> = vx <a: vector>` Gets the X component of the vector.
- `<number> = vy <a: vector>` Gets the Y component of the vector.
- `<number> = vz <a: vector>` Gets the Z component of the vector.
- `<vector> = vadd <a: vector> <b: vector>` Adds two vectors.
- `<vector> = vsub <a: vector> <b: vector>` Subtracts two vectors (a - b).
- `<vector> = vmul <a: vector> <b: number>` Multiplies a vector (a) by a scalar (b).
- `<vector> = vdiv <a: vector> <b: number>` Divides a vector (a) by a scalar (b).
- `<number> = vdist <a: vector>` Computes the length of a vector.
- `<vector> = vnorm <a: vector>` Normalizes a vector to length = 1.
- `<number> = vdot <a: vector> <b: vector>` Computes dot product of two vectors.
- `<vector> = vcross <a: vector> <b: vector>` Computes cross product of two vectors.

### Jump Instructions

- `label <name: string>` Does nothing, but is used to refer to locations in the program for jumping.
- `jmpl <label: string> <a: number> <b: number>` Jumps to the label named `label` if `a` is less than `b`.
- `jmple <label: string> <a: number> <b: number>` Jumps to the label named `label` if `a` is less than or equal to `b`.
- `jmpg <label: string> <a: number> <b: number>` Jumps to the label named `label` if `a` is less than `b`.
- `jmpge <label: string> <a: number> <b: number>` Jumps to the label named `label` if `a` is less than or equal to `b`.
- `jmpe <label: string> <a: slot> <b: slot>` Jumps to the label named `label` if `a` is equal to `b`. Works on
  numbers, vectors, and entities.

### World Querying Instructions

- `<entity> = findent <pos: vector> <n: number>` Finds the `n`th nearest entity from the position `pos`.
  `n` is zero-indexed, so `n=0` means the closest entity, `n=1` is the second closest, etc.
  `n` will be truncated (i.e. rounded towards zero).
- `<vector> = entpos <ent: entity>` Gets the position of the entity, in blocks.
- `<vector> = entvel <ent: entity>` Gets the velocity of the entity, in blocks per tick.
- `<vector> = entfacing <ent: entity>` Gets the direction the entity is facing.
- `<number> = checkblock <pos: vector> <block: string>` Returns 1 if the block at that position matches the block given, 0 otherwise

### World Manipulation Instructions

- `accelent <ent: entity> <vel: vector>` Adds `vel` (blocks per tick) to `ent`'s velocity.
    - Costs 30 energy per m/t accelerated.
- `damageent <ent: entity> <dmg: number>` Hits `ent` for `dmg` damage.
    - Costs `dmg^2` energy, rounded up.
- `mountent <bottom: entity> <top: entity>` Makes the `top` entity ride the `bottom` entity.
    - Costs 0 energy.
- `fireballpwr <fireball: entity> <power: double>` Adds explosion power to the fireball,
    - Costs `10*2^power` energy, rounded up.
    - `power` is capped to 6.0
- `explode <pos: vector> <power: number>` Creates an explosion at `pos` with power `power`.
    - Costs `10*2^power` energy, rounded up.
    - `power` is capped to 6.0, the power of a charged creeper or end crystal.
- `placeblock <pos: vector> <block: string>` Places a block from your inventory to the position `pos`.
    - Costs 3 * hardness of the block, rounded up.
- `destroyblock <pos: vector>` Destroys a block at position `pos`.
    - Costs 3 * hardness of the block, rounded up.
- `lightning <pos: vector>` Summons lightning at position `pos`.
    - Costs 100 energy
- `<entity> = summon <pos: vector> <type: string>` Summons an entity. You can only pick from the list below:
    - `pig`: Costs 200 energy
    - `chicken`: Costs 200 energy
    - `zombie`: Costs 300 energy
    - `skeleton`: Costs 300 energy
    - `arrow`: Costs 50 energy
    - `fireball`: Costs 30 energy
    - `snowball`: Costs 20 energy

### Control Flow Instructions

- `wait <ticks: number>` Waits for the specified number of ticks before continuing execution.