package com.skd.utilitynexusqol.recipeselect;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.CraftingInput;

/**
 * Extension point for adding recipe-conflict selector support to third-party crafting screens.
 * Each adapter maps a screen+menu pair to the {@link CraftingInput} the recipe system needs.
 */
public interface RecipeSelectorScreenAdapter {

    /**
     * Returns {@code true} when this adapter handles the given screen (and its menu).
     */
    boolean supports(AbstractContainerScreen<?> screen);

    /**
     * Builds the {@link CraftingInput} from the given menu, or returns {@code null} if the
     * menu's grid is empty / cannot be represented as a crafting input.
     */
    CraftingInput craftingInput(AbstractContainerMenu menu);

    /**
     * The slot index of the result slot inside the menu. Most vanilla menus use {@code 0};
     * a third-party menu may place it elsewhere.
     */
    int resultSlotIndex(AbstractContainerMenu menu);

    /**
     * Called once when a matching screen is first opened (after Polymorph suppression and config
     * checks). Override to perform one-time client→server handshaking needed by the target menu
     * (e.g. sending an inventory-button-click so the server knows which player is interacting).
     * The default implementation does nothing.
     */
    default void onScreenOpened(AbstractContainerScreen<?> screen) {
    }
}
