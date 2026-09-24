package com.skd.utilitynexusqol.waystonebeam;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.FastColor;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.Set;

public class WaystoneBeamRenderer {

    @SubscribeEvent
    public void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        if (!WaystoneBeamConfig.enabled()) return;
        if (!ModList.get().isLoaded("waystones")) return;

        Set<BlockPos> positions = WaystoneBeamClientData.getWaystonePositions();
        if (positions.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        PoseStack poseStack = event.getPoseStack();
        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        long gameTime = mc.level.getGameTime();
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);

        int r = WaystoneBeamConfig.colorRed();
        int g = WaystoneBeamConfig.colorGreen();
        int b = WaystoneBeamConfig.colorBlue();
        int color = FastColor.ARGB32.color(255, r, g, b);

        int height = WaystoneBeamConfig.beamHeightBlocks();

        for (BlockPos pos : positions) {
            poseStack.pushPose();
            poseStack.translate(pos.getX() - cam.x, pos.getY() + 2 - cam.y, pos.getZ() - cam.z);
            BeaconRenderer.renderBeaconBeam(
                    poseStack,
                    bufferSource,
                    BeaconRenderer.BEAM_LOCATION,
                    partialTick,
                    1.0F,
                    gameTime,
                    0,
                    height,
                    color,
                    0.2F,
                    0.25F
            );
            poseStack.popPose();
        }

        bufferSource.endBatch();
    }
}
