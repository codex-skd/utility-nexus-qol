package com.skd.utilitynexusqol.recipeselect;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Client-side UI for the Recipe Conflict Selector: a fixed swap-arrows button above the crafting
 * result slot that toggles a single bordered panel of result icons, mirroring the Polymorph mod's
 * selection pattern (independent reimplementation). Picking an icon sends the chosen recipe id to
 * the server; the server-side mixin ({@code MixinCraftingMenu}) resolves the result to that recipe.
 */
public final class RecipeSelectClient {

    private static final ResourceLocation BUTTON_SPRITE =
            ResourceLocation.withDefaultNamespace("widget/button");
    private static final ResourceLocation BUTTON_HIGHLIGHT_SPRITE =
            ResourceLocation.withDefaultNamespace("widget/button_highlighted");

    private static final int TOGGLE_SIZE = 16;

    /** Panel geometry. */
    private static final int CELL = 18;
    private static final int CELL_GAP = 2;
    private static final int PANEL_PAD = 3;
    private static final int PANEL_GAP = 3;

    private static final int PANEL_BG = 0xF0100010;
    private static final int PANEL_BORDER = 0xFFA0A0A0;
    private static final int SELECTED_BORDER = 0xFFFFFFFF;
    private static final int HOVER_TINT = 0x33FFFFFF;

    private static int toggleX, toggleY;
    private static boolean stripOpen;

    private static List<ItemStack> cachedGrid;
    private static List<RecipeHolder<CraftingRecipe>> cachedCandidates = List.of();
    private static List<ItemStack> cachedDistinct = List.of();
    private static ResourceLocation lastSentId;

    private RecipeSelectClient() {
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) {
            return;
        }
        RecipeSelectorScreenAdapter adapter = RecipeSelectorScreens.getAdapterForScreen(screen);
        if (adapter == null) {
            return;
        }

        if (PolymorphCompat.isLoaded()) {
            return;
        }

        adapter.onScreenOpened(screen);

        cachedGrid = null;
        lastSentId = null;
        stripOpen = false;

        if (!RecipeSelectConfig.enabled()) {
            return;
        }

        Slot resultSlot = screen.getMenu().getSlot(adapter.resultSlotIndex(screen.getMenu()));
        toggleX = screen.getGuiLeft() + resultSlot.x + 1;
        toggleY = screen.getGuiTop() + resultSlot.y - TOGGLE_SIZE - 6;
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        if (!RecipeSelectConfig.enabled()) {
            return;
        }

        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) {
            return;
        }
        RecipeSelectorScreenAdapter adapter = RecipeSelectorScreens.getAdapterForScreen(screen);
        if (adapter == null) {
            return;
        }

        if (PolymorphCompat.isLoaded()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) {
            return;
        }

        CraftingInput input = adapter.craftingInput(screen.getMenu());
        if (input == null) {
            hideAndClearPreference();
            return;
        }

        refreshCache(input, level);
        if (cachedDistinct.size() <= 1) {
            hideAndClearPreference();
            return;
        }

        List<ResourceLocation> sortedIds = ClientRecipeSelection.getSortedIds(cachedCandidates);
        int selected = ClientRecipeSelection.getSelectedIndex(sortedIds);
        if (selected >= cachedDistinct.size()) {
            selected = 0;
            ClientRecipeSelection.setSelectedIndex(sortedIds, selected);
        }

        RecipeHolder<CraftingRecipe> recipe = ClientRecipeSelection.getRecipeForDistinctIndex(
                cachedCandidates, cachedDistinct, selected, input, level);
        if (recipe != null && !recipe.id().equals(lastSentId)) {
            RecipeSelectNetwork.sendToServer(
                    new RecipeSelectNetwork.SetPreferredRecipePayload(Optional.of(recipe.id())));
            lastSentId = recipe.id();
        }

        GuiGraphics gg = event.getGuiGraphics();
        double mx = event.getMouseX();
        double my = event.getMouseY();

        gg.pose().pushPose();
        gg.pose().translate(0, 0, 300);
        drawToggle(gg, mx, my);
        if (stripOpen) {
            drawPanel(gg, mc, selected, mx, my);
        }
        gg.pose().popPose();

        if (stripOpen) {
            int hovered = findHoveredCell(mx, my);
            if (hovered >= 0 && hovered < cachedDistinct.size()) {
                gg.pose().pushPose();
                gg.pose().translate(0, 0, 400);
                gg.renderTooltip(mc.font, cachedDistinct.get(hovered), (int) mx, (int) my);
                gg.pose().popPose();
            }
        } else if (isInside(toggleX, toggleY, TOGGLE_SIZE, TOGGLE_SIZE, mx, my)) {
            gg.pose().pushPose();
            gg.pose().translate(0, 0, 400);
            gg.renderTooltip(mc.font,
                    Component.translatable("utility_nexus_qol.recipeselect.button"),
                    (int) mx, (int) my);
            gg.pose().popPose();
        }
    }

    @SubscribeEvent
    public static void onMouseClick(ScreenEvent.MouseButtonPressed.Pre event) {
        if (!RecipeSelectConfig.enabled()) {
            return;
        }

        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) {
            return;
        }
        RecipeSelectorScreenAdapter adapter = RecipeSelectorScreens.getAdapterForScreen(screen);
        if (adapter == null) {
            return;
        }

        if (PolymorphCompat.isLoaded()) {
            return;
        }

        if (event.getButton() != 0) {
            return;
        }

        double mx = event.getMouseX();
        double my = event.getMouseY();

        if (isInside(toggleX, toggleY, TOGGLE_SIZE, TOGGLE_SIZE, mx, my)) {
            stripOpen = !stripOpen;
            event.setCanceled(true);
            return;
        }

        if (stripOpen) {
            int idx = findHoveredCell(mx, my);
            if (idx >= 0 && idx < cachedDistinct.size()) {
                selectResult(idx, screen, adapter);
            }
            stripOpen = false;
            event.setCanceled(true);
        }
    }

    private static void selectResult(int distinctIndex, AbstractContainerScreen<?> screen,
                                     RecipeSelectorScreenAdapter adapter) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) {
            return;
        }

        CraftingInput input = adapter.craftingInput(screen.getMenu());
        if (input == null) {
            return;
        }

        List<ResourceLocation> sortedIds = ClientRecipeSelection.getSortedIds(cachedCandidates);
        ClientRecipeSelection.setSelectedIndex(sortedIds, distinctIndex);

        RecipeHolder<CraftingRecipe> recipe = ClientRecipeSelection.getRecipeForDistinctIndex(
                cachedCandidates, cachedDistinct, distinctIndex, input, level);
        if (recipe != null) {
            RecipeSelectNetwork.sendToServer(
                    new RecipeSelectNetwork.SetPreferredRecipePayload(Optional.of(recipe.id())));
            lastSentId = recipe.id();
        }

        cachedGrid = null;
    }

    private static void hideAndClearPreference() {
        stripOpen = false;
        if (lastSentId != null) {
            RecipeSelectNetwork.sendToServer(
                    new RecipeSelectNetwork.SetPreferredRecipePayload(Optional.empty()));
            lastSentId = null;
        }
    }

    private static void refreshCache(CraftingInput input, Level level) {
        List<ItemStack> grid = new ArrayList<>(input.items().size());
        for (ItemStack stack : input.items()) {
            grid.add(stack.copy());
        }
        if (cachedGrid != null && sameGrid(cachedGrid, grid)) {
            return;
        }
        cachedGrid = grid;
        cachedCandidates = ClientRecipeSelection.getCandidates(input, level);
        cachedDistinct = ClientRecipeSelection.getDistinctResults(cachedCandidates, input, level);
    }

    private static boolean sameGrid(List<ItemStack> a, List<ItemStack> b) {
        if (a.size() != b.size()) {
            return false;
        }
        for (int i = 0; i < a.size(); i++) {
            if (!ItemStack.matches(a.get(i), b.get(i))) {
                return false;
            }
        }
        return true;
    }

    // ------------------------------------------------------------------ rendering

    private static void drawToggle(GuiGraphics gg, double mx, double my) {
        boolean hovered = isInside(toggleX, toggleY, TOGGLE_SIZE, TOGGLE_SIZE, mx, my);
        gg.blitSprite(hovered ? BUTTON_HIGHLIGHT_SPRITE : BUTTON_SPRITE,
                toggleX, toggleY, TOGGLE_SIZE, TOGGLE_SIZE);
        drawSwapIcon(gg, toggleX, toggleY);
    }

    /** Fixed two-arrows "swap" glyph, drawn from flat fills (no textures). */
    private static void drawSwapIcon(GuiGraphics gg, int ox, int oy) {
        drawSwapGlyph(gg, ox + 1, oy + 1, 0xFF202020);
        drawSwapGlyph(gg, ox, oy, 0xFFFFFFFF);
    }

    private static void drawSwapGlyph(GuiGraphics gg, int ox, int oy, int c) {
        // left arrow, pointing up
        gg.fill(ox + 3, oy + 2, ox + 4, oy + 3, c);
        gg.fill(ox + 2, oy + 3, ox + 5, oy + 4, c);
        gg.fill(ox + 1, oy + 4, ox + 6, oy + 5, c);
        gg.fill(ox + 3, oy + 4, ox + 4, oy + 12, c);
        // right arrow, pointing down
        gg.fill(ox + 10, oy + 4, ox + 11, oy + 12, c);
        gg.fill(ox + 8, oy + 9, ox + 13, oy + 10, c);
        gg.fill(ox + 9, oy + 10, ox + 12, oy + 11, c);
        gg.fill(ox + 10, oy + 11, ox + 11, oy + 12, c);
    }

    private static int[] panelBounds(int count) {
        int panelW = PANEL_PAD * 2 + count * CELL + (count - 1) * CELL_GAP;
        int panelH = PANEL_PAD * 2 + CELL;
        int panelX = toggleX + TOGGLE_SIZE / 2 - panelW / 2;
        int panelY = toggleY - PANEL_GAP - panelH;
        return new int[] {panelX, panelY, panelW, panelH};
    }

    private static int[] cellPos(int i, int count) {
        int[] p = panelBounds(count);
        return new int[] {p[0] + PANEL_PAD + i * (CELL + CELL_GAP), p[1] + PANEL_PAD};
    }

    private static void drawPanel(GuiGraphics gg, Minecraft mc, int selected, double mx, double my) {
        int count = cachedDistinct.size();
        int[] p = panelBounds(count);
        int x0 = p[0];
        int y0 = p[1];
        int x1 = p[0] + p[2];
        int y1 = p[1] + p[3];

        gg.fill(x0, y0, x1, y1, PANEL_BG);
        gg.fill(x0, y0, x1, y0 + 1, PANEL_BORDER);
        gg.fill(x0, y1 - 1, x1, y1, PANEL_BORDER);
        gg.fill(x0, y0, x0 + 1, y1, PANEL_BORDER);
        gg.fill(x1 - 1, y0, x1, y1, PANEL_BORDER);

        int hovered = findHoveredCell(mx, my);
        for (int i = 0; i < count; i++) {
            int[] c = cellPos(i, count);
            int cx = c[0];
            int cy = c[1];
            ItemStack stack = cachedDistinct.get(i);

            if (i == hovered) {
                gg.fill(cx, cy, cx + CELL, cy + CELL, HOVER_TINT);
            }
            gg.renderItem(stack, cx + 1, cy + 1);
            gg.renderItemDecorations(mc.font, stack, cx + 1, cy + 1);
            if (i == selected) {
                gg.fill(cx, cy, cx + CELL, cy + 1, SELECTED_BORDER);
                gg.fill(cx, cy + CELL - 1, cx + CELL, cy + CELL, SELECTED_BORDER);
                gg.fill(cx, cy, cx + 1, cy + CELL, SELECTED_BORDER);
                gg.fill(cx + CELL - 1, cy, cx + CELL, cy + CELL, SELECTED_BORDER);
            }
        }
    }

    private static int findHoveredCell(double mx, double my) {
        int count = cachedDistinct.size();
        for (int i = 0; i < count; i++) {
            int[] c = cellPos(i, count);
            if (isInside(c[0], c[1], CELL, CELL, mx, my)) {
                return i;
            }
        }
        return -1;
    }

    private static boolean isInside(int x, int y, int w, int h, double mx, double my) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }
}
