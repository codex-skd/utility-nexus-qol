package com.skd.utilitynexusqol.recipeselect;

import net.neoforged.fml.ModList;

/**
 * Runtime detection for the Polymorph mod. When Polymorph is present it ships its own
 * recipe-conflict selector for vanilla crafting screens, so we suppress our duplicate overlay
 * on those screens to avoid two competing buttons over the same result slot.
 */
public final class PolymorphCompat {

    private static final boolean LOADED = ModList.get().isLoaded("polymorph");

    private PolymorphCompat() {
    }

    public static boolean isLoaded() {
        return LOADED;
    }
}
