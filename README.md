![Java CI with Gradle](https://github.com/ModProg/BlockMeter/workflows/Java%20CI%20with%20Gradle/badge.svg) [![](http://cf.way2muchnoise.eu/full_blockmeterfabric_downloads.svg)](https://www.curseforge.com/minecraft/mc-mods/blockmeterfabric)

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
| Java (OpenJDK) | 8, 11                |
| Gradle         | 4.10.2, 5.6.4, 6.5.1 |

You need a gradle installation which does not come with the mod. At the time of this writing, the version of gradle used is 4.10.2. Gradle 6.5 has been tested to work too, so versions between those _should_ as well.

You might already have gradle installed, especially when you're running Linux - if so, make sure it's new enough. For example, Ubuntu 18.04 has gradle 4.4.1 which is not. Run `gradle -version` to check.

If you have the Fabric example mod installed, you can use the gradle installation from there. Else, download a release from https://gradle.org/releases/ (binary only is sufficient) and unpack it somewhere.

## Versionfiles submodule

All my mods use the same repository of files that match MineCraft, Fabric, and common libraries versions. This is included in the mod repository as a Versionfiles submodule, and you should get it when cloning the repo. Run `git submodule init`, then `git submodule update` to get the current version of the files. Do this after selecting your branch, see below.

## Compiling the mod

There are branches for the various versions of MineCraft that are supported by the mod. Run `git branch -r` to see which branches there are, then `git checkout branchname` without the `origin/` part, for example, `git checkout fabric_1_16`.
