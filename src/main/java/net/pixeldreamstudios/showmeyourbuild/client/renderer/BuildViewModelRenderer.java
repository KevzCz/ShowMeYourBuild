package net.pixeldreamstudios.showmeyourbuild.client.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.RotationAxis;
@Environment(EnvType.CLIENT)
public class BuildViewModelRenderer {
    public static void drawEntity(int x, int y, int scale, float yaw, PlayerEntity player) {
        MinecraftClient client = MinecraftClient.getInstance();
        EntityRenderDispatcher dispatcher = client.getEntityRenderDispatcher();

        MatrixStack matrices = new MatrixStack();
        matrices.translate(x, y, 100.0);
        matrices.scale(-scale, scale, scale);

        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0f));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw));

        dispatcher.setRenderShadows(false);
        var immediate = client.getBufferBuilders().getEntityVertexConsumers();

        dispatcher.render(player, 0.0, 0.0, 0.0, 0.0f, 1.0f, matrices, immediate, 15728880);
        immediate.draw();
        dispatcher.setRenderShadows(true);
    }
}
