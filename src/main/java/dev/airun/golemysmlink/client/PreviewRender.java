package dev.airun.golemysmlink.client;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

final class PreviewRender {
    private PreviewRender() {}
    static void draw(GuiGraphics graphics, int x, int y, int size, float mouseX, float mouseY, MaidRenderProxy proxy) {
        float angleX = (float) Math.atan(mouseX / 40);
        float angleY = (float) Math.atan(mouseY / 40);
        var tilt = new Quaternionf().rotateX(angleY * (float) Math.PI / 9);
        var pose = new PoseStack();
        pose.last().pose().set(graphics.pose().last().pose());
        pose.last().normal().set(graphics.pose().last().normal());
        pose.translate(x, y, 50);
        pose.mulPoseMatrix(new Matrix4f().scaling(size, size, -size));
        pose.mulPose(new Quaternionf().rotateZ((float) Math.PI).mul(tilt));
        proxy.yBodyRot = 180 + angleX * 20;
        proxy.setYRot(180 + angleX * 40);
        proxy.setXRot(-angleY * 20);
        proxy.yHeadRot = proxy.getYRot();
        proxy.yHeadRotO = proxy.getYRot();
        proxy.stabilizePreviewRotation();
        var dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        var previous = new Quaternionf(dispatcher.cameraOrientation());
        graphics.flush();
        Lighting.setupForEntityInInventory();
        try {
            dispatcher.overrideCameraOrientation(new Quaternionf(tilt).conjugate());
            dispatcher.getRenderer(proxy).render(proxy, 0, 1, pose, graphics.bufferSource(), 0xF000F0);
        } finally {
            dispatcher.overrideCameraOrientation(previous);
            try { graphics.flush(); }
            finally { Lighting.setupFor3DItems(); }
        }
    }
}
