package dlindustries.vigillant.system.mixin;

import dlindustries.vigillant.system.module.modules.render.NameTags;
import dlindustries.vigillant.system.system;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

    @Inject(
            method = "hasLabel(Lnet/minecraft/entity/LivingEntity;D)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onHasLabel(LivingEntity entity, double distanceSq, CallbackInfoReturnable<Boolean> cir) {
        NameTags nameTags = system.INSTANCE.getModuleManager().getModule(NameTags.class);
        if (nameTags != null && nameTags.isEnabled()) {
            if (nameTags.isUnlimitedRange()) {
                cir.setReturnValue(true);
            } else if (nameTags.shouldForcePlayerNametags() && entity instanceof net.minecraft.entity.player.PlayerEntity) {
                cir.setReturnValue(true);
            }
        }
    }
}