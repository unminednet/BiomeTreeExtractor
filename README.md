# BiomeTreeExtractor

BiomeTreeExtractor is a Fabric mod __for developers__ to extract biome search trees from Minecraft in JSON format for use with [cubiomes](https://github.com/Cubitect/cubiomes) or other seed tools.

Both Minecraft and cubiomes use this search tree to determine biomes based on climate parameters. 

The climate parameter ranges are extracted as arrays. The order of parameters in the array are:

0. Temperature
1. Humidity
2. Continentalness
3. Erosion
4. Depth
5. Weirdness
6. Offset

Requires the [Fabric API](https://modrinth.com/mod/fabric-api) mod to be installed.

This version supports __Minecraft 1.21.2 snapshot 24w40a__.

## Usage

1. Build the mod (using JDK 21, "gradlew build", the output is placed in build/libs)
2. Install the mod 
3. Start Minecraft and load a world.
4. A biome search tree for each dimension will be automatically extracted to `biometreeextract_{dimension_name}.json` files in the world folder on world load.
5. Use the [BiomeTreeGenerator](https://github.com/unminednet/BiomeTreeGenerator) tool to convert these json files to `btree*.h` files for use with cubiomes.

## Notes

I'm neither a Java guy nor a Minecraft modder, so there may be some wtf moments in the code.

## License

MIT
