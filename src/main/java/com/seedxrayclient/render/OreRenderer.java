package com.seedxrayclient.render;

import com.seedxrayclient.scan.OreHighlighter;
import com.seedxrayclient.scan.OreHighlighter.OreMarker;
import com.seedxrayclient.scan.OreHighlighter.OreType;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public final class OreRenderer {
    private OreRenderer() {}

    public static void render(WorldRenderContext context) {
        List<OreMarker> markers = OreHighlighter.getMarkersSnapshot();
        if (markers.isEmpty()) return;

        MatrixStack matrices = context.matrixStack();
        VertexConsumerProvider consumers = context.consumers();
        if (consumers == null) return;
        VertexConsumer lines = consumers.getBuffer(RenderLayer.getLines());

        Vec3d camera = context.camera().getPos();

        matrices.push();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        for (OreMarker marker : markers) {
            BlockPos pos = marker.pos;
            float a = 0.9f;
            int rgb = marker.type.rgb;
            float r = ((rgb >> 16) & 0xFF) / 255.0f;
            float g = ((rgb >> 8) & 0xFF) / 255.0f;
            float b = (rgb & 0xFF) / 255.0f;

            double x1 = pos.getX();
            double y1 = pos.getY();
            double z1 = pos.getZ();
            double x2 = x1 + 1.0;
            double y2 = y1 + 1.0;
            double z2 = z1 + 1.0;

            // Draw a thin wireframe box around the block
            WorldRenderer.drawBox(matrices, lines, x1, y1, z1, x2, y2, z2, r, g, b, a);
        }

        matrices.pop();
    }
}
