package com.skd.utilitynexusqol.waystonebeam;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WaystoneBeamClientData {
    private static final Set<BlockPos> WAYSTONE_POSITIONS = ConcurrentHashMap.newKeySet();

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {
        if (!event.getLevel().isClientSide()) return;
        ChunkAccess chunk = event.getChunk();
        for (BlockPos bePos : chunk.getBlockEntitiesPos()) {
            BlockEntity be = chunk.getBlockEntity(bePos);
            if (be == null) continue;
            Block block = be.getBlockState().getBlock();
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
            if ("waystones".equals(id.getNamespace()) && id.getPath().contains("waystone")) {
                WAYSTONE_POSITIONS.add(be.getBlockPos().immutable());
            }
        }
    }

    @SubscribeEvent
    public void onChunkUnload(ChunkEvent.Unload event) {
        if (!event.getLevel().isClientSide()) return;
        ChunkAccess chunk = event.getChunk();
        for (BlockPos bePos : chunk.getBlockEntitiesPos()) {
            BlockEntity be = chunk.getBlockEntity(bePos);
            if (be == null) continue;
            Block block = be.getBlockState().getBlock();
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
            if ("waystones".equals(id.getNamespace()) && id.getPath().contains("waystone")) {
                WAYSTONE_POSITIONS.remove(be.getBlockPos().immutable());
            }
        }
    }

    private static int tickCounter = 0;

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        if (!WaystoneBeamConfig.enabled()) {
            WAYSTONE_POSITIONS.clear();
            return;
        }

        if (tickCounter++ % 20 != 0) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        int renderDistance = mc.options.getEffectiveRenderDistance();
        int playerX = mc.player.getBlockX();
        int playerZ = mc.player.getBlockZ();
        int playerChunkX = Math.floorDiv(playerX, 16);
        int playerChunkZ = Math.floorDiv(playerZ, 16);
        Set<BlockPos> newSet = ConcurrentHashMap.newKeySet();
        for (int dx = -renderDistance; dx <= renderDistance; dx++) {
            for (int dz = -renderDistance; dz <= renderDistance; dz++) {
                int chunkX = playerChunkX + dx;
                int chunkZ = playerChunkZ + dz;
                if (mc.level.getChunkSource().hasChunk(chunkX, chunkZ)) {
                    LevelChunk chunk = mc.level.getChunkSource().getChunk(chunkX, chunkZ, true);
                    if (chunk != null) {
                        for (BlockEntity be : chunk.getBlockEntities().values()) {
                            Block block = be.getBlockState().getBlock();
                            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
                            if ("waystones".equals(id.getNamespace()) && id.getPath().contains("waystone")) {
                                newSet.add(be.getBlockPos().immutable());
                            }
                        }
                    }
                }
            }
        }
        WAYSTONE_POSITIONS.clear();
        WAYSTONE_POSITIONS.addAll(newSet);
    }

    public static Set<BlockPos> getWaystonePositions() {
        return WAYSTONE_POSITIONS;
    }
}
