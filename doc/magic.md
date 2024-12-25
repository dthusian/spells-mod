# Magic Language Reference

All books must begin with `#lang magic` on one line.
Each line after contains one incantation each.

## Targeting

- `self` - Targets the caster

## Effects

- `fire` (30) - Creates fire at the target
- `heal` (60) - Heals the target for 2 hearts
- `mine` (5 x hardness) - Mines the block at the target
- `explode` (160) - Explode at the target
- `pull` (50) - Pulls the target to the caster
- `push` (50) - Pushes the target away
- `lift` (50) - Lifts the target
- `lightning` (100) - Spawns lightning at the target
- `milk` (50) - Removes status effects on the target
- `teleport` (100) - Teleports to the target
- `teleswap` (20) - Swaps with the target
- `infuse` (50) - Throws the first potion in your inventory at the target

## Projectile Spawning

- `projectile` - Fires a projectile
- `multiprojectile` - Fires many projectiles
- `wait` - Waits for a second