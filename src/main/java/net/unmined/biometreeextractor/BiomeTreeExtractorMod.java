package net.unmined.biometreeextractor;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;

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
        ServerWorldEvents.LOAD.register(this::onWorldLoad);
        LOGGER.info("BiomeTreeExtractor is initialized");
    }

    private void onWorldLoad(MinecraftServer minecraftServer, ServerWorld serverWorld) {

        var dimensionName = serverWorld.getRegistryKey().getValue().toString();
        LOGGER.info(String.format("Extracting biome search tree for dimension %s", dimensionName));

        // Access the world's chunk generator to get the BiomeSource
        if (!(serverWorld.getChunkManager().getChunkGenerator() instanceof NoiseChunkGenerator chunkGenerator)) {
            LOGGER.info("Extraction skipped: dimension does not use NoiseChunkGenerator");
            return;
        }

        // Check if the biome source is an instance of MultiNoiseBiomeSource
        if (!(chunkGenerator.getBiomeSource() instanceof MultiNoiseBiomeSource multiNoiseBiomeSource)) {
            LOGGER.info("Extraction skipped: dimension does not use MultiNoiseBiomeSource");
            return;
        }

        try {

            var worldPath = serverWorld.getServer().getSavePath(WorldSavePath.ROOT);
            var sanitizedDimensionName = dimensionName.replaceAll("[^a-zA-Z0-9._]+", "_");
            var fileName = Paths.get(
                            worldPath.toAbsolutePath().toString(),
                            String.format("biometreeextract_%s.json", sanitizedDimensionName))
                    .toString();

            var rootNode = multiNoiseBiomeSource.biomeEntries.right().isPresent()
                    ? multiNoiseBiomeSource.biomeEntries.right().get().value().getEntries().tree.firstNode
                    : multiNoiseBiomeSource.biomeEntries.left().get().tree.firstNode;

            try (var writer = new BufferedWriter(new FileWriter(fileName))) {
                DumpTreeNode(rootNode, writer);
            }

            LOGGER.info("Extraction was successful");

        } catch (Exception e) {
            LOGGER.error("Extraction failed", e);
        }
    }

    private static void DumpTreeNode(MultiNoiseUtil.SearchTree.TreeNode<RegistryEntry<Biome>> node, BufferedWriter writer) throws IOException {
        writer.write("{");

        var parameters = node.parameters;

        writer.write("\"parameters\":[");
        writer.write(String.join(",", Arrays.stream(parameters).map((MultiNoiseUtil.ParameterRange p) -> "{\"min\":" + p.min() + ",\"max\":" + p.max() + "}").toList()));
        writer.write("],");

        if (node instanceof net.minecraft.world.biome.source.util.MultiNoiseUtil.SearchTree.TreeBranchNode<RegistryEntry<Biome>> n) {
            writer.write("\"subTree\":[");
            var subTree = n.subTree;
            for (var i = 0; i < subTree.length; i++) {
                DumpTreeNode(subTree[i], writer);
                if (i < subTree.length - 1) writer.write(",");
            }
            writer.write("]");
        } else if (node instanceof net.minecraft.world.biome.source.util.MultiNoiseUtil.SearchTree.TreeLeafNode<RegistryEntry<Biome>> l) {
            var biome = ((RegistryEntry.Reference<Biome>) ((TreeLeafNodeAccessor) (Object) l).getValue()).getKey().get().getValue().toString();
            writer.write("\"biome\":\"" + biome + "\"");
        }

        writer.write("}");
    }

}