# TL;DR

- Get a version of gradle that's at least 4.10.2 or use `./gradlew`
- `git clone <repo>`
- `git branch -r`  to see available branches
- `git checkout fabric_1_16` to select your branch
- `git submodule init`
- `git submodule update`
- `/path/to/gradle build`

# How to compile this mod

Because I created several mods, which have some things in common, the structure of my mods is a bit different from the example mod that Fabric or Forge provide.


# Versionfiles submodule

All my mods use the same repository of files that match MineCraft, Fabric, and common libraries versions. This is included in the mod repository as a Versionfiles submodule, and you should get it when cloning the repo. Run `git submodule init`, then `git submodule update` to get the current version of the files. Do this after selecting your branch, see below.

# Compiling the mod

There are branches for the various versions of MineCraft that are supported by the mod. Run `git branch -r` to see which branches there are, then `git checkout branchname` without the `origin/` part, for example, `git checkout fabric_1_16`.

