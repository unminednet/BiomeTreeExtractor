package net.unmined.biometreeextractor;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;

import net.minecraft.core.Holder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;

import net.minecraft.world.level.storage.LevelResource;
import net.unmined.biometreeextractor.mixin.TreeLeafNodeAccessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;


public class BiomeTreeExtractorMod implements ModInitializer {

    public static final String MOD_ID = "biometreeextractor";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ServerLevelEvents.LOAD.register(this::onWorldLoad);
        LOGGER.info("BiomeTreeExtractor is initialized");
    }

    private void onWorldLoad(MinecraftServer minecraftServer, ServerLevel serverLevel) {

        var dimensionName = serverLevel.dimension().identifier().toString();
        LOGGER.info(String.format("Extracting biome search tree for dimension %s", dimensionName));

        // Access the world's chunk generator to get the BiomeSource
        ChunkGenerator chunkGenerator = serverLevel.getChunkSource().getGenerator();
        if (!(chunkGenerator instanceof NoiseBasedChunkGenerator noiseBasedChunkGenerator)) {
            LOGGER.info("Extraction skipped: dimension does not use NoiseBasedChunkGenerator");
            return;
        }

        // Check if the biome source is an instance of MultiNoiseBiomeSource
        if (!(noiseBasedChunkGenerator.getBiomeSource() instanceof MultiNoiseBiomeSource multiNoiseBiomeSource)) {
            LOGGER.info("Extraction skipped: dimension does not use MultiNoiseBiomeSource");
            return;
        }

        try {

            var worldPath = serverLevel.getServer().getWorldPath(LevelResource.ROOT);
            var sanitizedDimensionName = dimensionName.replaceAll("[^a-zA-Z0-9._]+", "_");
            var fileName = Paths.get(
                            worldPath.toAbsolutePath().toString(),
                            String.format("biometreeextract_%s.json", sanitizedDimensionName))
                    .toString();

            var parameterList = multiNoiseBiomeSource.parameters.map(
                    direct -> direct,
                    preset -> preset.value().parameters()
            );
            var rootNode = parameterList.index.root;

            try (var writer = new BufferedWriter(new FileWriter(fileName))) {
                DumpTreeNode(rootNode, writer);
            }

            LOGGER.info("Extraction was successful");

        } catch (Exception e) {
            LOGGER.error("Extraction failed", e);
        }
    }

    private static void DumpTreeNode(Climate.RTree.Node<Holder<Biome>> node, BufferedWriter writer) throws IOException {
        writer.write("{");

        var parameters = node.parameterSpace;

        writer.write("\"parameters\":[");
        writer.write(String.join(",", Arrays.stream(parameters).map((Climate.Parameter p) -> "{\"min\":" + p.min() + ",\"max\":" + p.max() + "}").toList()));
        writer.write("],");

        if (node instanceof Climate.RTree.SubTree<Holder<Biome>> n) {
            writer.write("\"subTree\":[");
            var subTree = n.children;
            for (var i = 0; i < subTree.length; i++) {
                DumpTreeNode(subTree[i], writer);
                if (i < subTree.length - 1) writer.write(",");
            }
            writer.write("]");
        } else if (node.getClass().getName().contains("Leaf")) {
            var biome = ((Holder.Reference<Biome>) ((TreeLeafNodeAccessor) (Object) node).getValue()).key().identifier().toString();
            writer.write("\"biome\":\"" + biome + "\"");
        }

        writer.write("}");
    }

}