package mafuyu33.vrgettingoverit.mixin;

import mafuyu33.vrgettingoverit.VRDataHandler;
import mafuyu33.vrgettingoverit.VRPlugin;
import mafuyu33.vrgettingoverit.item.Moditems;
import mafuyu33.vrgettingoverit.item.VrGettingOverItItem;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V"), method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V")
	private void init(ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, CallbackInfo ci) {
		// 特定物品的位置、旋转和缩放调整

		if(stack.getHolder()!=null && stack.getHolder() instanceof PlayerEntity player) {
            System.out.println(player);
		}

		if (stack.getHolder()!=null && stack.getHolder() instanceof PlayerEntity player
				&& stack.isOf(Moditems.VR_GETTING_OVER_IT) && VRPlugin.canRetrieveData(player)) {


//			matrices.translate(0.2F, -0.1F, 0.3F); // 示例值，x, y, z 需要替换为实际的位移量
//
////			// 自定义的旋转操作
////			matrices.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(30.0F)); // 示例值，angleX 需要替换为实际的旋转角度
////			matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(45.0F)); // 示例值，angleY 需要替换为实际的旋转角度
////			matrices.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(60.0F)); // 示例值，angleZ 需要替换为实际的旋转角度
//
//			// 自定义的缩放操作
//			matrices.scale(1.5F, 1.5F, 1.5F); // 示例值，scaleX, scaleY, scaleZ 需要替换为实际的缩放因子
		}
	}
}