//package mafuyu33.vrgettingoverit.mixin;
//
//import com.llamalad7.mixinextras.sugar.Local;
//import mafuyu33.vrgettingoverit.VRDataHandler;
//import mafuyu33.vrgettingoverit.item.Moditems;
//import net.minecraft.client.network.AbstractClientPlayerEntity;
//import net.minecraft.client.util.math.MatrixStack;
//import net.minecraft.entity.player.PlayerEntity;
//import net.minecraft.item.ItemStack;
//import net.minecraft.particle.ParticleTypes;
//import net.minecraft.util.Hand;
//import net.minecraft.util.Util;
//import net.minecraft.util.math.RotationAxis;
//import net.minecraft.util.math.Vec3d;
//import org.joml.Quaternionf;
//import org.spongepowered.asm.mixin.Final;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Shadow;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//import org.vivecraft.client_vr.ClientDataHolderVR;
//import org.vivecraft.client_vr.render.VivecraftItemRendering;
//import org.vivecraft.common.utils.math.Quaternion;
//
//@Mixin(VivecraftItemRendering.class)
//public class VivecraftItemRenderingMixin {
//	@Final
//	@Shadow
//	private static ClientDataHolderVR dh;
//
//	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;multiply(Lorg/joml/Quaternionf;)V",ordinal = 3), method = "applyFirstPersonItemTransforms",cancellable = true)
//	private static void init(MatrixStack pMatrixStack, VivecraftItemRendering.VivecraftItemTransformType rendertype, boolean mainHand, AbstractClientPlayerEntity pPlayer, float pEquippedProgress, float pPartialTicks, ItemStack pStack, Hand pHand, CallbackInfo ci) {
//		//特定物品的位置、旋转和缩放调整
//		if(pStack.isOf(Moditems.VR_GETTING_OVER_IT)){
//			//初始化位置和大小
//			Vec3d vec3;
//			pMatrixStack.translate(-0.07999999821186066, -0.07999999821186066, -0.07999999821186066);
//			pMatrixStack.scale(1f,1.5f,1f);
//
//			Vec3d mainPos = VRDataHandler.getMainhandControllerPosition(pPlayer);
//			Vec3d offPos = VRDataHandler.getOffhandControllerPosition(pPlayer);
//
//			Quaternionf rotation = RotationAxis.POSITIVE_Y.rotationDegrees(0.0F);
//			if(!mainHand) {
//				vec3 = new Vec3d(offPos.x - mainPos.x, offPos.y - mainPos.y, offPos.z - mainPos.z).normalize();
//			}else {
//				vec3 = new Vec3d(mainPos.x - offPos.x, mainPos.y - offPos.y, mainPos.z - offPos.z).normalize();
//			}
//			Vec3d vec31 = new Vec3d(vec3.x, vec3.y, vec3.z);
//			Vec3d vec32 = dh.vrPlayer.vrdata_world_render.getHand(1).getCustomVector(new Vec3d(0.0, -1.0, 0.0));
//			Vec3d vec33 = dh.vrPlayer.vrdata_world_render.getHand(1).getCustomVector(new Vec3d(0.0, 0.0, -1.0));
//			vec31.crossProduct(vec32);
//			double d4 = 57.29577951308232 * Math.acos(vec31.dotProduct(vec32));
//			float f = (float)Math.toDegrees(Math.asin(vec31.y / vec31.length()));
//			float f1 = (float)Math.toDegrees(Math.atan2(vec31.x, vec31.z));
//			Vec3d vec34 = new Vec3d(0.0, 1.0, 0.0);
//			Vec3d vec35 = new Vec3d(vec31.x, 0.0, vec31.z);
//			Vec3d vec36 = Vec3d.ZERO;
//			double d5 = vec33.dotProduct(vec35);
//			if (d5 != 0.0) {
//				vec36 = vec35.multiply(d5);
//			}
//
//			double d6 = 0.0;
//			Vec3d vec37 = vec33.subtract(vec36).normalize();
//			d6 = vec37.dotProduct(vec34);
//			double d7 = vec35.dotProduct(vec37.crossProduct(vec34));
//			float f2;
//			if (d7 < 0.0) {
//				f2 = -((float)Math.acos(d6));
//			} else {
//				f2 = (float)Math.acos(d6);
//			}
//
//			float f3 = (float)(57.29577951308232 * (double)f2);
//
//			pMatrixStack.peek().getPositionMatrix().mul(dh.vrPlayer.vrdata_world_render.getController(1).getMatrix().transposed().toMCMatrix());
//			rotation.mul(RotationAxis.POSITIVE_Y.rotationDegrees(f1));
//			rotation.mul(RotationAxis.POSITIVE_X.rotationDegrees(-f));
//			rotation.mul(RotationAxis.POSITIVE_Z.rotationDegrees(-f3));
//			rotation.mul(RotationAxis.POSITIVE_Z.rotationDegrees(180.0F));
//			pMatrixStack.peek().getPositionMatrix().rotate(rotation);
//			rotation = RotationAxis.POSITIVE_Y.rotationDegrees(0.0F);
//			rotation.mul(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
//			rotation.mul(RotationAxis.POSITIVE_X.rotationDegrees(160.0F));
//
//			//应用
//			pMatrixStack.multiply(rotation);
//
//			ci.cancel();
//		}
//
////		if(stack.getHolder()!=null && stack.getHolder() instanceof PlayerEntity player) {
////            System.out.println(player);
////		}
////
////		if (stack.getHolder()!=null && stack.getHolder() instanceof PlayerEntity player
////				&& stack.isOf(Moditems.VR_GETTING_OVER_IT) && VRPlugin.canRetrieveData(player)) {
////
////
//////			matrices.translate(0.2F, -0.1F, 0.3F); // 示例值，x, y, z 需要替换为实际的位移量
//////
////////			// 自定义的旋转操作
////////			matrices.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(30.0F)); // 示例值，angleX 需要替换为实际的旋转角度
////////			matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(45.0F)); // 示例值，angleY 需要替换为实际的旋转角度
////////			matrices.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(60.0F)); // 示例值，angleZ 需要替换为实际的旋转角度
//////
//////			// 自定义的缩放操作
//////			matrices.scale(1.5F, 1.5F, 1.5F); // 示例值，scaleX, scaleY, scaleZ 需要替换为实际的缩放因子
////		}
//	}
//}