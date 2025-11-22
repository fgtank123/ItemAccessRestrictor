package net.fgtank123.itemaccessrestrictor.datagen;

import net.fgtank123.itemaccessrestrictor.definitions.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(@Nonnull RecipeOutput recipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.ITEM_ACCESS_RESTRICTOR)
            .define('a', Items.COPPER_INGOT)
            .define('b', Items.GLASS)
            .define('c', Items.QUARTZ)
            .define('d', Items.REDSTONE)
            .pattern("bab")
            .pattern("aca")
            .pattern("dad")
            .unlockedBy(getHasName(Items.COPPER_INGOT), has(Items.COPPER_INGOT))
            .unlockedBy(getHasName(Items.HOPPER), has(Items.HOPPER))
            .save(recipeOutput);
    }
}
