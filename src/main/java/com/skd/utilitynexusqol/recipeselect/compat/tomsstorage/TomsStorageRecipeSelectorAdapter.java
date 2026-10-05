package com.skd.utilitynexusqol.recipeselect.compat.tomsstorage;

import com.skd.utilitynexusqol.recipeselect.RecipeSelectorScreenAdapter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;

import java.util.ArrayList;
import java.util.List;

/**
 * Client-side adapter that enables the Recipe Conflict Selector overlay inside Tom's Storage
 * {@code CraftingTerminalScreen}. All class references are kept as plain strings to avoid any
 * compile-time dependency on Tom's Storage.
 */
public final class TomsStorageRecipeSelectorAdapter implements RecipeSelectorScreenAdapter {

    private static final String SCREEN_CLASS =
            "com.tom.storagemod.screen.CraftingTerminalScreen";
    private static final String MENU_CLASS =
            "com.tom.storagemod.menu.CraftingTerminalMenu";

    @Override
    public boolean supports(AbstractContainerScreen<?> screen) {
        return SCREEN_CLASS.equals(screen.getClass().getName());
    }

    @Override
    public CraftingInput craftingInput(AbstractContainerMenu menu) {
        if (!MENU_CLASS.equals(menu.getClass().getName())) {
            return null;
        }
        List<ItemStack> items = new ArrayList<>(9);
        for (int i = 0; i < 9; i++) {
            items.add(menu.getSlot(1 + i).getItem());
        }
        return CraftingInput.of(3, 3, items);
    }

    @Override
    public int resultSlotIndex(AbstractContainerMenu menu) {
        return 0;
    }

    @Override
    public void onScreenOpened(AbstractContainerScreen<?> screen) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameMode == null) {
            return;
        }
        mc.gameMode.handleInventoryButtonClick(screen.getMenu().containerId, 1);
    }
}
