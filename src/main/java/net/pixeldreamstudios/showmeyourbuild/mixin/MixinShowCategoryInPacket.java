package net.pixeldreamstudios.showmeyourbuild.mixin;

import net.puffish.skillsmod.client.data.ClientCategoryData;
import net.puffish.skillsmod.client.network.packets.in.ShowCategoryInPacket;
import net.pixeldreamstudios.showmeyourbuild.network.CategoryCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShowCategoryInPacket.class)
public abstract class MixinShowCategoryInPacket {

    @Inject(method = "read", at = @At("RETURN"))
    private static void onReadReturn(net.minecraft.network.RegistryByteBuf buf, CallbackInfoReturnable<ShowCategoryInPacket> cir) {
        ClientCategoryData data = cir.getReturnValue().getCategory();
        if (data != null) {
            CategoryCache.put(data);
        }
    }
}
