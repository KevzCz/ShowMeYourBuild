package net.pixeldreamstudios.showmeyourbuild.client.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Unit;
import net.minecraft.util.profiler.Profiler;
import net.pixeldreamstudios.showmeyourbuild.client.renderer.stats.AttributeOverrideRegistry;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class AttributeOverrideLoader implements IdentifiableResourceReloadListener {
    private static final Gson GSON = new Gson();
    private static final Identifier OVERRIDE_JSON = Identifier.of("showmeyourbuild", "override.json");

    @Override
    public Identifier getFabricId() {
        return Identifier.of("showmeyourbuild", "attribute_override_loader");
    }

    @Override
    public CompletableFuture<Void> reload(
            ResourceReloader.Synchronizer synchronizer,
            ResourceManager manager,
            Profiler prepareProfiler,
            Profiler applyProfiler,
            Executor prepareExecutor,
            Executor applyExecutor
    ) {
        return synchronizer.whenPrepared(Unit.INSTANCE).thenRunAsync(() -> {
            applyProfiler.startTick();
            applyProfiler.push("showmeyourbuild:attribute_override_loader");

            try {
                var optional = manager.getResource(OVERRIDE_JSON);
                if (optional.isEmpty()) {
                    System.out.println("[OverrideLoader] override.json not found.");
                    return;
                }

                Resource resource = optional.get();
                JsonObject root = GSON.fromJson(
                        new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8),
                        JsonObject.class
                );


                if (root.has("attributes")) {
                    JsonObject attributes = root.getAsJsonObject("attributes");
                    for (Map.Entry<String, JsonElement> entry : attributes.entrySet()) {
                        Identifier id = Identifier.tryParse(entry.getKey());
                        if (id == null) continue;

                        JsonObject data = entry.getValue().getAsJsonObject();
                        Identifier icon = data.has("icon") ? Identifier.tryParse(data.get("icon").getAsString()) : null;
                        String label = JsonHelper.getString(data, "label", null);
                        String tooltip = JsonHelper.getString(data, "tooltip", null);
                        boolean hidden = JsonHelper.getBoolean(data, "hidden", false);

                        AttributeOverrideRegistry.registerAttributeOverride(id,
                                new AttributeOverrideRegistry.AttributeOverride(icon, label, tooltip, hidden));


                    }
                }

                if (root.has("groups")) {
                    JsonObject groups = root.getAsJsonObject("groups");
                    for (Map.Entry<String, JsonElement> entry : groups.entrySet()) {
                        String namespace = entry.getKey();
                        JsonObject data = entry.getValue().getAsJsonObject();
                        Identifier icon = data.has("icon") ? Identifier.tryParse(data.get("icon").getAsString()) : null;
                        String label = JsonHelper.getString(data, "label", null);
                        String tooltip = JsonHelper.getString(data, "tooltip", null);
                        boolean hidden = JsonHelper.getBoolean(data, "hidden", false);

                        AttributeOverrideRegistry.registerGroupOverride(namespace,
                                new AttributeOverrideRegistry.GroupOverride(icon, label, tooltip, hidden));


                    }
                }

            } catch (Exception e) {
                System.err.println("[OverrideLoader] Failed to load attribute overrides:");
                e.printStackTrace();
            }

            applyProfiler.pop();
            applyProfiler.endTick();
        }, applyExecutor);
    }

}
