package net.unmined.biometreeextractor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(net.minecraft.world.level.biome.Climate.RTree.Leaf.class)
public interface TreeLeafNodeAccessor {
    @Accessor
    Object getValue();

}