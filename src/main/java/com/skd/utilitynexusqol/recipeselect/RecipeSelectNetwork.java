package com.skd.utilitynexusqol.recipeselect;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.Optional;

public final class RecipeSelectNetwork {

    private static final String TOMS_STORAGE_MENU_CLASS =
            "com.tom.storagemod.menu.CraftingTerminalMenu";

    public record SetPreferredRecipePayload(Optional<ResourceLocation> recipeId) implements CustomPacketPayload {
        public static final Type<SetPreferredRecipePayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath("utility_nexus_qol", "set_preferred_recipe"));

        public static final StreamCodec<RegistryFriendlyByteBuf, SetPreferredRecipePayload> STREAM_CODEC =
                StreamCodec.composite(
                        ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs::optional), SetPreferredRecipePayload::recipeId,
                        SetPreferredRecipePayload::new);

        @Override
        public Type<SetPreferredRecipePayload> type() {
            return TYPE;
        }
    }

    private RecipeSelectNetwork() {
    }

    public static void register(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
                SetPreferredRecipePayload.TYPE,
                SetPreferredRecipePayload.STREAM_CODEC,
                RecipeSelectNetwork::handleSetPreferred
        );
    }

    public static void sendToServer(SetPreferredRecipePayload payload) {
        var mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.getConnection() != null) {
            mc.getConnection().send(payload);
        }
    }

    private static void handleSetPreferred(SetPreferredRecipePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            if (payload.recipeId().isPresent()) {
                ServerRecipePreferences.set(player.getUUID(), payload.recipeId().get());
            } else {
                ServerRecipePreferences.clear(player.getUUID());
            }
            AbstractContainerMenu menu = player.containerMenu;
            if (menu instanceof CraftingMenu cm) {
                cm.slotsChanged(cm.getSlot(1).container);
            } else if (menu instanceof InventoryMenu im) {
                im.slotsChanged(im.getSlot(1).container);
            } else if (TOMS_STORAGE_MENU_CLASS.equals(menu.getClass().getName())) {
                // Tom's Storage caches its resolved recipe and only re-resolves it via
                // CraftingTerminalMenu#clickMenuButton(id=1) (its own Polymorph hook, which
                // our mixin piggybacks on) - a plain slot-change notification is a no-op here
                // because the previously matching recipe still matches the unchanged grid.
                menu.clickMenuButton(player, 1);
            }
        });
    }
}
