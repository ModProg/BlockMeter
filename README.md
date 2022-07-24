![Java CI with Gradle](https://github.com/ModProg/BlockMeter/workflows/Java%20CI%20with%20Gradle/badge.svg)

# BlockMeter

This is the public repository for the Block Meter a Fabric Mod to measure Boxes in Minecraft.

# Compile

- Get a version of gradle that's at least 4.10.2
- `git clone <repo>`
- `git branch -r` to see available branches
- `git checkout fabric_1_16` to select your branch
- `git submodule init`
- `git submodule update`
- `/path/to/gradle build`

## How to compile this mod

Because I created several mods, which have some things in common, the structure of my mods is a bit different from the example mod that Fabric or Forge provide.

In particular, I don't want the gradle files to be duplicated into every single mod repository, and some common files that contain version info for Fabric, its tools, and some library mods, have been moved to a (common) submodule.

## Prerequisites

### These Versions should work (are used in GitHub CI)

| Software       | Version              |
| -------------- | -------------------- |
| Java (OpenJDK) | 17, 18               |
| Gradle         | ./gradlew or current |

## Versionfiles submodule

All my mods use the same repository of files that match MineCraft, Fabric, and common libraries versions. This is included in the mod repository as a Versionfiles submodule, and you should get it when cloning the repo. Run `git submodule init`, then `git submodule update` to get the current version of the files. Do this after selecting your branch, see below.

## Compiling the mod

There are branches for the various versions of MineCraft that are supported by the mod. Run `git branch -r` to see which branches there are, then `git checkout branchname` without the `origin/` part, for example, `git checkout fabric_1_16`.
