package dlindustries.vigillant.system.mixin;

import dlindustries.vigillant.system.module.modules.render.NameTags;
import dlindustries.vigillant.system.system;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAttachmentType;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {
    @Shadow @Final protected EntityRenderDispatcher dispatcher;
    @Shadow public abstract TextRenderer getTextRenderer();

    @Inject(
            method = "renderLabelIfPresent",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onRenderLabel(S state, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        renderCustomLabel(state, text, matrices, vertexConsumers, light);
        ci.cancel();
    }

    private void renderCustomLabel(S state, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        NameTags nameTags = system.INSTANCE.getModuleManager().getModule(NameTags.class);
        if (nameTags == null) return;

        double distanceSq = state.squaredDistanceToCamera;
        if (distanceSq > 4096 && !nameTags.isUnlimitedRange()) return;
        Vec3d attVec = state.nameLabelPos;
        if (attVec == null) return;

        matrices.push();
        matrices.translate(attVec.x, attVec.y + 0.5, attVec.z);
        matrices.multiply(dispatcher.getRotation());
        float scale = 0.025F;
        if (nameTags.isEnabled()) {
            scale *= nameTags.getScale();
            double distance = Math.sqrt(distanceSq);
            if (distance > 10) scale *= (float) (distance / 10);
        }
        matrices.scale(scale, -scale, scale);
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float bgOpacity = 0.25F;
        int bgColor = (int)(bgOpacity * 255F) << 24;
        TextRenderer tr = getTextRenderer();
        float labelX = -tr.getWidth(text) / 2f;
        int labelY = 0;
        TextLayerType layerType = nameTags.isEnabled() && nameTags.isSeeThrough() ?
                TextLayerType.SEE_THROUGH : TextLayerType.NORMAL;
        tr.draw(text, labelX, labelY, 0x20FFFFFF, false, matrix,
                vertexConsumers, layerType, bgColor, light);
        if (!state.sneaking || nameTags.isEnabled()) {
            tr.draw(text, labelX, labelY, 0xFFFFFFFF, false, matrix,
                    vertexConsumers, layerType, 0, light);
        }

        matrices.pop();
    }
}