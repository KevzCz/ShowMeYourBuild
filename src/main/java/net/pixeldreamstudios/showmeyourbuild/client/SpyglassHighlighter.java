package net.pixeldreamstudios.showmeyourbuild.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.Items;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;
@Environment(EnvType.CLIENT)
public class SpyglassHighlighter {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static Entity currentTarget = null;

    private static final double MAX_DISTANCE = 64.0;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(clientTick -> {
            if (client.player == null || client.world == null) {
                currentTarget = null;
                return;
            }

            ClientPlayerEntity player = client.player;

            boolean usingSpyglass = player.isUsingItem()
                    && player.getActiveItem().getItem() == Items.SPYGLASS;

            if (!usingSpyglass) {
                currentTarget = null;
                return;
            }

            currentTarget = raycastEntity(player, MAX_DISTANCE);
        });
    }

    public static Entity getCurrentTarget() {
        return currentTarget;
    }

    private static Entity raycastEntity(ClientPlayerEntity player, double maxDistance) {
        Vec3d start = player.getCameraPosVec(1.0F);
        Vec3d direction = player.getRotationVec(1.0F);
        Vec3d end = start.add(direction.multiply(maxDistance));

        Entity hitEntity = null;
        double closestDistance = maxDistance * maxDistance;

        List<Entity> entities = client.world.getOtherEntities(player, new Box(start, end).expand(1.0),
                e -> e instanceof net.minecraft.entity.player.PlayerEntity
                        && e.getId() != player.getId()
                        && e.isAlive());

        for (Entity entity : entities) {
            Box box = entity.getBoundingBox().expand(0.3);
            Vec3d hitPos = box.raycast(start, end).orElse(null);
            if (hitPos != null) {
                double distance = start.squaredDistanceTo(hitPos);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    hitEntity = entity;
                }
            }
        }

        return hitEntity;
    }
}
