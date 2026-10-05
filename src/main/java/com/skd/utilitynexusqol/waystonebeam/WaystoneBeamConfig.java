package com.skd.utilitynexusqol.waystonebeam;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class WaystoneBeamConfig {

    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.BooleanValue ENABLED;
    private static final ModConfigSpec.IntValue BEAM_HEIGHT_BLOCKS;
    private static final ModConfigSpec.IntValue COLOR_RED;
    private static final ModConfigSpec.IntValue COLOR_GREEN;
    private static final ModConfigSpec.IntValue COLOR_BLUE;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("Waystone Beacon Beam - a vanilla beacon-style light beam over Waystones blocks.")
                .push("waystoneBeam");

        ENABLED = builder
                .comment("Master switch for the Waystone Beacon Beam. Requires the Waystones mod; does nothing without it.")
                .define("enabled", true);

        BEAM_HEIGHT_BLOCKS = builder
                .comment("How many blocks tall the beam is.")
                .defineInRange("beamHeightBlocks", 100, 1, 1024);

        COLOR_RED = builder
                .comment("Beam colour - red channel (0-255).")
                .defineInRange("colorRed", 80, 0, 255);

        COLOR_GREEN = builder
                .comment("Beam colour - green channel (0-255).")
                .defineInRange("colorGreen", 200, 0, 255);

        COLOR_BLUE = builder
                .comment("Beam colour - blue channel (0-255).")
                .defineInRange("colorBlue", 255, 0, 255);

        builder.pop();

        SPEC = builder.build();
    }

    private WaystoneBeamConfig() {
    }

    public static boolean enabled() {
        return SPEC.isLoaded() ? ENABLED.get() : true;
    }

    public static int beamHeightBlocks() {
        return SPEC.isLoaded() ? BEAM_HEIGHT_BLOCKS.get() : 100;
    }

    public static int colorRed() {
        return SPEC.isLoaded() ? COLOR_RED.get() : 80;
    }

    public static int colorGreen() {
        return SPEC.isLoaded() ? COLOR_GREEN.get() : 200;
    }

    public static int colorBlue() {
        return SPEC.isLoaded() ? COLOR_BLUE.get() : 255;
    }
}
