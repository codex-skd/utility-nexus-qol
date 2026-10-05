package com.skd.utilitynexusqol.mixin;

import com.skd.utilitynexusqol.recipeselect.RecipeSelectConfig;
import com.skd.utilitynexusqol.recipeselect.ServerRecipePreferences;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

/**
 * Server-side override of vanilla crafting-grid recipe resolution.
 *
 * <p>{@code CraftingMenu#slotChangedCraftingGrid} is the shared static helper called by both
 * {@link CraftingMenu} and {@code InventoryMenu} to recompute the result slot. Vanilla always
 * picks the first matching recipe. When the player has chosen a specific result via the Recipe
 * Conflict Selector, this injection substitutes that recipe (if it still matches the current
 * grid) and cancels the vanilla body.
 */
@Mixin(CraftingMenu.class)
public class MixinCraftingMenu {

    @Inject(method = "slotChangedCraftingGrid", at = @At("HEAD"), cancellable = true)
    private static void uqol$applyPreferredRecipe(
            AbstractContainerMenu menu,
            Level level,
            Player player,
            CraftingContainer craftSlots,
            ResultContainer resultSlots,
            @Nullable RecipeHolder<CraftingRecipe> recipe,
            CallbackInfo ci) {
        if (level.isClientSide()) {
            return;
        }
        if (!RecipeSelectConfig.enabled()) {
            return;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        ResourceLocation preferred = ServerRecipePreferences.get(serverPlayer.getUUID());
        if (preferred == null || level.getServer() == null) {
            return;
        }

        RecipeManager recipeManager = level.getServer().getRecipeManager();
        CraftingInput input = craftSlots.asCraftInput();

        RecipeHolder<CraftingRecipe> chosen = null;
        for (RecipeHolder<CraftingRecipe> holder : recipeManager.getRecipesFor(RecipeType.CRAFTING, input, level)) {
            if (holder.id().equals(preferred) && holder.value().matches(input, level)) {
                chosen = holder;
                break;
            }
        }
        if (chosen == null) {
            // Preferred recipe is not applicable to the current grid: let vanilla handle it.
            return;
        }

        ItemStack result = ItemStack.EMPTY;
        if (resultSlots.setRecipeUsed(level, serverPlayer, chosen)) {
            ItemStack assembled = chosen.value().assemble(input, level.registryAccess());
            if (assembled.isItemEnabled(level.enabledFeatures())) {
                result = assembled;
            }
        }

        resultSlots.setItem(0, result);
        menu.setRemoteSlot(0, result);
        serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(
                menu.containerId, menu.incrementStateId(), 0, result));
        ci.cancel();
    }
}
