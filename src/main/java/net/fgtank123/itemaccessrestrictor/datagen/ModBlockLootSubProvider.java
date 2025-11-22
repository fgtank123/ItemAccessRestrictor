package net.fgtank123.itemaccessrestrictor.datagen;

import net.fgtank123.itemaccessrestrictor.definitions.ModBlocks;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nonnull;
import java.util.Set;

public class ModBlockLootSubProvider extends BlockLootSubProvider {

    public ModBlockLootSubProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        dropSelf(ModBlocks.ITEM_ACCESS_RESTRICTOR.get());
    }

    @Nonnull
    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.DR.getEntries().stream().map(Holder::value)::iterator;
    }
}
