# AreWePatchworkYet?
**AreWePatchworkYet?** is a tool that takes a Forge mod jar file, and lists unimplemented methods based on an inputted Patchwork API jar file. It's designed to see which methods need to be implemented in Patchwork API for a mod to work.

Note that results provided by this tool are not guaranteed - there may be unimplemented methods it may miss, or other things a mod will require such as AT/AWs, coremods, or specific field access.

## Setup
At the moment, prebuilt binaries are not available. You'll need to clone this repo and build it manually, assuming you have a JDK installed. If you don't have a JDK, grab one from [AdoptOpenJDK](https://adoptopenjdk.net/).
```
git clone https://github.com/stairman06/arewepatchworkyet.git
cd arewepatchworkyet
gradlew shadowJar
```

Once you have built it, run the jar: 
```
java -jar build/libs/AreWePatchworkYet-1.0-SNAPSHOT-all.jar
```
Make sure to include the `-all`, that means its the fat jar with all the libraries.

## Usage
The leftmost panel is called the **Configuration Panel**. It's where you configure settings.

### Minecraft Version
Self explanatory, the version of Minecraft you're using. This is required as Minecraft is automatically downloaded and remapped.

### Input mod
This is the input mod, or the one you want to test and see all the unimplemented methods it references. This is a path relative to the current directory.

The input mod needs to be patched through [patchwork-patcher](https://github.com/patchworkmc/patchwork-patcher). Eventually, this will be done automatically.

### Patchwork API jar
A relative path to the Patchwork API jar you're using. To get Patchwork API, compile it [from source](https://github.com/patchworkmc/patchwork-api).

### Mappings to display in
Items displayed will be displayed in the chosen mappings. By default, this is set to intermediary. Support for SRG/MCP will be added soon.

When you've configured all settings, press *Analyze*, and the mod jar will be analyzed and compared with the Minecraft and Patchwork API jars to see which methods are missing.

## Technical Details
AreWePatchworkYet? works using the [ASM](https://asm.ow2.io/) bytecode analysis library.
 
First, it looks through the Minecraft and Patchwork API jars to see which methods are defined, and stores them in an internal map.

Second, it looks through the input mod jar to see which methods are referenced. If a method is referenced that isn't defined, its stored to be displayed for later.

AreWePatchworkYet? also handles Mixin classes that add methods, by looking for the `@org/spongepowered/asm/mixin/Mixin` annotation, and storing the added methods.

## License
This tool is licensed under the [GNU Lesser General Public License version 3.0](https://www.gnu.org/licenses/lgpl-3.0.html).