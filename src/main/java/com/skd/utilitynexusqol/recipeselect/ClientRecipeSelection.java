package com.skd.utilitynexusqol.recipeselect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ClientRecipeSelection {

    private static final Map<List<ResourceLocation>, Integer> SELECTIONS = new HashMap<>();

    private ClientRecipeSelection() {
    }

    @SubscribeEvent
    public static void onClientLoggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        SELECTIONS.clear();
    }

    public static List<RecipeHolder<CraftingRecipe>> getCandidates(CraftingInput input, Level level) {
        List<RecipeHolder<CraftingRecipe>> recipes = level.getRecipeManager()
                .getRecipesFor(RecipeType.CRAFTING, input, level);
        List<RecipeHolder<CraftingRecipe>> matching = new ArrayList<>();
        for (RecipeHolder<CraftingRecipe> holder : recipes) {
            if (holder.value().matches(input, level)) {
                matching.add(holder);
            }
        }
        return matching;
    }

    public static List<ItemStack> getDistinctResults(List<RecipeHolder<CraftingRecipe>> candidates, CraftingInput input, Level level) {
        List<ItemStack> distinct = new ArrayList<>();
        for (RecipeHolder<CraftingRecipe> holder : candidates) {
            ItemStack result = holder.value().assemble(input, level.registryAccess());
            boolean found = false;
            for (ItemStack existing : distinct) {
                if (ItemStack.isSameItemSameComponents(existing, result) && existing.getCount() == result.getCount()) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                distinct.add(result);
            }
        }
        return distinct;
    }

    public static int getSelectedIndex(List<ResourceLocation> sortedIds) {
        return SELECTIONS.getOrDefault(sortedIds, 0);
    }

    public static void setSelectedIndex(List<ResourceLocation> sortedIds, int index) {
        SELECTIONS.put(sortedIds, index);
    }

    public static List<ResourceLocation> getSortedIds(List<RecipeHolder<CraftingRecipe>> candidates) {
        List<ResourceLocation> ids = new ArrayList<>();
        for (RecipeHolder<CraftingRecipe> holder : candidates) {
            ids.add(holder.id());
        }
        ids.sort(Comparator.naturalOrder());
        return ids;
    }

    public static RecipeHolder<CraftingRecipe> getRecipeForDistinctIndex(
            List<RecipeHolder<CraftingRecipe>> candidates,
            List<ItemStack> distinctResults,
            int distinctIndex,
            CraftingInput input,
            Level level) {
        ItemStack target = distinctResults.get(distinctIndex);
        for (RecipeHolder<CraftingRecipe> holder : candidates) {
            ItemStack result = holder.value().assemble(input, level.registryAccess());
            if (ItemStack.isSameItemSameComponents(target, result) && target.getCount() == result.getCount()) {
                return holder;
            }
        }
        return candidates.isEmpty() ? null : candidates.get(0);
    }
}
