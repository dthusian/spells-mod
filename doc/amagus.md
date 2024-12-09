# Amagus Language Reference

Write `#lang amagus` at the top of a book to use the amagus language.

Each line (that does not start with `#`) has a effect followed by a target.

For example,
```
#lang amagus
fireball you
```
launches a fireball at the entity attacked.

`+` or `-` can be attached to a effect to amplify or weaken its power, respectively.
Up to 3 `+`s or `-`s can be applied. Amplifying costs (1.5x, 2.5x, 4x) more energy,
weakening costs (1x, 0.8x, 0.5x).

For example,
```
#lang amagus
heal++ me
```
heals the user amplified two levels.

## Targets

- `me` - Targets yourself
- `you` - Targets the entity (or position) you hit
- `everyone` - Targets everyone in an area around where you hit

Using `everyone` costs 1.2x more energy.

## Effects

The (number) next to each effect is the energy cost per target.

### Combat effects

- `setfire` - (20) Sets the target on fire
- `explode` - (250) Creates a TNT explosion at the target
- `lightning` - (100) Summon lightning on the target
- `darkness` - Apply darkness to the target
- `nausea` - Apply nausea to the target
- `poison` - Apply poison to the target
- `wither` - Apply wither to the target
- `freeze` - Apply freezing and slowness on the target
- `heal` - Heals the target
- `slowfall` - Apply slow falling to the target

### Manipulation effects

- `lift` - Lifts the target
- `push` - Push the target away from the user
- `pull` - Pull the target towards the user
- `swap` - Swap the target with the user
- `teleport` - Teleport to the target
- `evaporate` - Deletes fluids near the target
- `water` - Creates water near the target