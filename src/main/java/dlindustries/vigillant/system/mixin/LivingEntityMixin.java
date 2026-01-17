package dlindustries.vigillant.system.mixin;

import dlindustries.vigillant.system.module.modules.optimizer.ShieldOptimizer;
import dlindustries.vigillant.system.system;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.UseAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Shadow public abstract ItemStack getActiveItem();

    @Shadow public abstract boolean isUsingItem();

    @Inject(method = "isBlocking", at = @At("HEAD"), cancellable = true)
    private void modifyShieldDelay(CallbackInfoReturnable<Boolean> cir) {
        if (system.INSTANCE.getModuleManager().getModule(ShieldOptimizer.class).isEnabled()) {
            cir.setReturnValue(this.isUsingItem() && this.getActiveItem().getUseAction() == UseAction.BLOCK);
        }
    }
}