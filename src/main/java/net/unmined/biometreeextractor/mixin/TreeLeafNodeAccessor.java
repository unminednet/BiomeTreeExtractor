package net.unmined.biometreeextractor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(net.minecraft.world.biome.source.util.MultiNoiseUtil.SearchTree.TreeLeafNode.class)
public interface TreeLeafNodeAccessor {
    @Accessor
    Object getValue();

}