package com.skd.utilitynexusqol.recipeselect;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ServerRecipePreferences {

    private static final Map<UUID, ResourceLocation> PREFERENCES = new ConcurrentHashMap<>();

    private ServerRecipePreferences() {
    }

    public static ResourceLocation get(UUID playerId) {
        return PREFERENCES.get(playerId);
    }

    public static void set(UUID playerId, ResourceLocation recipeId) {
        PREFERENCES.put(playerId, recipeId);
    }

    public static void clear(UUID playerId) {
        PREFERENCES.remove(playerId);
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        clear(event.getEntity().getUUID());
    }
}
