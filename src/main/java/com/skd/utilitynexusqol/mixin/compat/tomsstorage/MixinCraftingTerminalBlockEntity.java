package com.skd.utilitynexusqol.mixin.compat.tomsstorage;

import com.skd.utilitynexusqol.recipeselect.RecipeSelectConfig;
import com.skd.utilitynexusqol.recipeselect.ServerRecipePreferences;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.ref.WeakReference;
import java.util.Optional;

/**
 * Server-side override of Tom's Storage recipe resolution inside
 * {@code CraftingTerminalBlockEntity#getRecipe}.
 *
 * <p>When the player has chosen a specific result via the Recipe Conflict Selector,
 * this injection substitutes that recipe (if it still matches the current grid) and
 * cancels Tom's Storage's own logic. If no preference is stored, Tom's Storage's
 * default behaviour (first matching recipe, or Polymorph if installed) runs unmodified.
 */
@Mixin(targets = "com.tom.storagemod.block.entity.CraftingTerminalBlockEntity")
public abstract class MixinCraftingTerminalBlockEntity {

    @Shadow
    private WeakReference<Player> polymorphPlayer;

    @Inject(method = "getRecipe", at = @At("HEAD"), cancellable = true)
    private void uqol$applyPreferredRecipe(CraftingInput input,
            CallbackInfoReturnable<Optional<RecipeHolder<CraftingRecipe>>> cir) {
        if (!RecipeSelectConfig.enabled()) {
            return;
        }
        if (polymorphPlayer == null) {
            return;
        }
        Player player = polymorphPlayer.get();
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        Level level = ((BlockEntity) (Object) this).getLevel();
        if (level == null || level.isClientSide() || level.getServer() == null) {
            return;
        }
        ResourceLocation preferred = ServerRecipePreferences.get(serverPlayer.getUUID());
        if (preferred == null) {
            return;
        }
        RecipeManager recipeManager = level.getServer().getRecipeManager();
        for (RecipeHolder<CraftingRecipe> holder :
                recipeManager.getRecipesFor(RecipeType.CRAFTING, input, level)) {
            if (holder.id().equals(preferred) && holder.value().matches(input, level)) {
                cir.setReturnValue(Optional.of(holder));
                return;
            }
        }
    }
}
