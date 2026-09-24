package com.skd.utilitynexusqol.recipeselect;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class RecipeSelectConfig {

    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.BooleanValue ENABLED;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("Recipe Conflict Selector - lets you cycle among alternative crafting results when multiple recipes match the same grid.")
                .push("recipeSelect");

        ENABLED = builder
                .comment("Enable the Recipe Conflict Selector for vanilla crafting grids (crafting table + 2x2 inventory grid).")
                .define("enabled", true);

        builder.pop();

        SPEC = builder.build();
    }

    private RecipeSelectConfig() {
    }

    public static boolean enabled() {
        return SPEC.isLoaded() ? ENABLED.get() : true;
    }
}
