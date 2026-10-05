package com.skd.utilitynexusqol.recipeselect;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;

import java.util.ArrayList;
import java.util.List;

/**
 * Internal registry of {@link RecipeSelectorScreenAdapter}s. The two vanilla adapters
 * (crafting table + 2×2 inventory grid) are registered at class-load time. Third-party
 * adapters can be added later via {@link #register}.
 */
public final class RecipeSelectorScreens {

    private static final List<RecipeSelectorScreenAdapter> ADAPTERS = new ArrayList<>();

    static {
        ADAPTERS.add(new RecipeSelectorScreenAdapter() {
            @Override
            public boolean supports(AbstractContainerScreen<?> screen) {
                return screen instanceof CraftingScreen;
            }

            @Override
            public CraftingInput craftingInput(AbstractContainerMenu menu) {
                if (!(menu instanceof CraftingMenu)) {
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
        });

        ADAPTERS.add(new RecipeSelectorScreenAdapter() {
            @Override
            public boolean supports(AbstractContainerScreen<?> screen) {
                return screen instanceof InventoryScreen;
            }

            @Override
            public CraftingInput craftingInput(AbstractContainerMenu menu) {
                if (!(menu instanceof InventoryMenu)) {
                    return null;
                }
                List<ItemStack> items = new ArrayList<>(4);
                for (int i = 0; i < 4; i++) {
                    items.add(menu.getSlot(1 + i).getItem());
                }
                while (items.size() < 4) {
                    items.add(ItemStack.EMPTY);
                }
                return CraftingInput.of(2, 2, items);
            }

            @Override
            public int resultSlotIndex(AbstractContainerMenu menu) {
                return 0;
            }
        });
    }

    private RecipeSelectorScreens() {
    }

    /**
     * Registers a new screen adapter. Call this during mod init if you want to extend the
     * recipe conflict selector to a third-party crafting screen.
     */
    public static void register(RecipeSelectorScreenAdapter adapter) {
        ADAPTERS.add(adapter);
    }

    /**
     * Returns the first adapter that {@link RecipeSelectorScreenAdapter#supports} the given
     * screen, or {@code null} if none match.
     */
    public static RecipeSelectorScreenAdapter getAdapterForScreen(AbstractContainerScreen<?> screen) {
        for (RecipeSelectorScreenAdapter adapter : ADAPTERS) {
            if (adapter.supports(screen)) {
                return adapter;
            }
        }
        return null;
    }
}
