package net.pixeldreamstudios.showmeyourbuild.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.pixeldreamstudios.showmeyourbuild.client.SpyglassHighlighter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(Entity.class)
public abstract class MixinEntity {
    @ModifyReturnValue(method = "isGlowing", at = @At("RETURN"))
    private boolean forceGlowForSpyglass(boolean original) {
        return original || (Object) this == SpyglassHighlighter.getCurrentTarget();
    }
}
