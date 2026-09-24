package com.skd.utilitynexusqol;

import com.mojang.logging.LogUtils;
import com.skd.utilitynexusqol.recipeselect.RecipeSelectClient;
import com.skd.utilitynexusqol.recipeselect.RecipeSelectConfig;
import com.skd.utilitynexusqol.recipeselect.RecipeSelectNetwork;
import com.skd.utilitynexusqol.recipeselect.RecipeSelectorScreens;
import com.skd.utilitynexusqol.recipeselect.ServerRecipePreferences;
import com.skd.utilitynexusqol.recipeselect.compat.tomsstorage.TomsStorageRecipeSelectorAdapter;
import com.skd.utilitynexusqol.waystonebeam.WaystoneBeamClientData;
import com.skd.utilitynexusqol.waystonebeam.WaystoneBeamConfig;
import com.skd.utilitynexusqol.waystonebeam.WaystoneBeamRenderer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slf4j.Logger;

@Mod(UtilityNexusQol.MODID)
public class UtilityNexusQol {
    public static final String MODID = "utility_nexus_qol";
    public static final String VERSION = "1.1.0";
    public static final Logger LOGGER = LogUtils.getLogger();

    public UtilityNexusQol(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(
                net.neoforged.fml.config.ModConfig.Type.CLIENT,
                WaystoneBeamConfig.SPEC,
                "utility_nexus/qol/config.toml"
        );

        // Separate file: NeoForge does not allow two mod configs to share a filename,
        // and this spec is COMMON (server honours it) while the beam spec is CLIENT.
        modContainer.registerConfig(
                net.neoforged.fml.config.ModConfig.Type.COMMON,
                RecipeSelectConfig.SPEC,
                "utility_nexus/qol/recipe-select.toml"
        );

        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(RecipeSelectNetwork::register);

        NeoForge.EVENT_BUS.register(ServerRecipePreferences.class);

        LOGGER.info("Utility Nexus QoL loaded! v{}", VERSION);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.register(RecipeSelectClient.class);
        NeoForge.EVENT_BUS.register(com.skd.utilitynexusqol.recipeselect.ClientRecipeSelection.class);

        if (ModList.get().isLoaded("toms_storage")) {
            RecipeSelectorScreens.register(new TomsStorageRecipeSelectorAdapter());
        } else {
            LOGGER.info("Tom's Storage not present - Crafting Terminal recipe selector compat disabled");
        }

        if (!ModList.get().isLoaded("waystones")) {
            LOGGER.info("Waystones not present - Waystone Beacon Beam disabled");
            return;
        }

        NeoForge.EVENT_BUS.register(new WaystoneBeamClientData());
        NeoForge.EVENT_BUS.register(new WaystoneBeamRenderer());
    }
}
