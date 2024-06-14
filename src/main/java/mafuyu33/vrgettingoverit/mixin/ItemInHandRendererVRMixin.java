package mafuyu33.vrgettingoverit.mixin;

import mafuyu33.vrgettingoverit.VRDataHandler;
import mafuyu33.vrgettingoverit.item.Moditems;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.common.utils.math.Quaternion;

@Mixin(value = HeldItemRenderer.class, priority = 998)
public abstract class ItemInHandRendererVRMixin {//让手上不渲染锤子物品
	@Shadow
	protected abstract void renderArmHoldingItem(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float equipProgress, float swingProgress, Arm arm);
	@Inject(at = @At("HEAD"), method = "renderFirstPersonItem",cancellable = true)
	private void init(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {

		if(item.isOf(Moditems.VR_GETTING_OVER_IT)){//如果是VR锤子的话
			boolean mainHand = hand == Hand.MAIN_HAND;
			ClientDataHolderVR dh = ClientDataHolderVR.getInstance();
			Arm humanoidarm = mainHand ? player.getMainArm() : player.getMainArm().getOpposite();
			boolean renderArm = dh.currentPass != RenderPass.THIRD || dh.vrSettings.mixedRealityRenderHands;

			if (dh.currentPass == RenderPass.CAMERA) {
				renderArm = false;
			}
			if (renderArm && !player.isInvisible()) {
				this.renderArmHoldingItem(matrices, vertexConsumers, light, equipProgress, swingProgress, humanoidarm);//渲染手臂
			}

			matrices.push();
			// 获取两个手柄

			Vec3d rightHandPos = VRDataHandler.getMainhandControllerPosition(player);
			Vec3d leftHandPos  = VRDataHandler.getOffhandControllerPosition(player);

			// 计算中间位置
			Vec3d midPoint = gettingoverit$getMidPoint(leftHandPos, rightHandPos);
			Vec3d translationVector;

				//测试
				player.getWorld().addParticle(ParticleTypes.BUBBLE,leftHandPos.x,leftHandPos.y,leftHandPos.z,0,0,0);
				player.getWorld().addParticle(ParticleTypes.BUBBLE,rightHandPos.x,rightHandPos.y,rightHandPos.z,0,0,0);
				player.getWorld().addParticle(ParticleTypes.BUBBLE,midPoint.x,midPoint.y,midPoint.z,0,0,0);

			//在另外一只手上为原点进行渲染？怎么做
			if(mainHand) {
//				translationVector = midPoint.subtract(rightHandPos);
//				matrices.translate(translationVector.x, translationVector.y, translationVector.z);
			}else {
//				translationVector = midPoint.subtract(leftHandPos);
//				matrices.translate(translationVector.x, translationVector.y, translationVector.z);
			}

			//清除旋转矩阵
			gettingoverit$clearRotate(matrices);
			//让他的坐标系从玩家变成世界
			float yaw = player.getYaw();
			float pitch1 = player.getPitch();
			float roll = VRDataHandler.getHMDRoll(player);
			// 计算逆旋转四元数
			Quaternionf inverseRotation = gettingoverit$getInverseQuaternionFromPitchYaw(pitch1, yaw, roll);
			// 应用逆旋转，使物体相对于世界坐标系保持静止
			matrices.multiply(inverseRotation);










			// 旋转矩阵，让他跟着玩家的手转



			// 将中间位置转换为渲染坐标
//			matrices.translate(translationVector.x, translationVector.y, translationVector.z);
			matrices.scale(1,1,1);

			// 渲染
			if(mainHand) {
				BakedModel model = MinecraftClient.getInstance().getItemRenderer().getModels().getModel(Moditems.VR_GETTING_OVER_IT);
				MinecraftClient.getInstance().getItemRenderer().renderItem(Moditems.VR_GETTING_OVER_IT.getDefaultStack(), ModelTransformationMode.FIXED, false, matrices, vertexConsumers, light, 1, model);
			}else {
				BakedModel model = MinecraftClient.getInstance().getItemRenderer().getModels().getModel(Moditems.VR_GETTING_OVER_IT);
				MinecraftClient.getInstance().getItemRenderer().renderItem(Moditems.VR_GETTING_OVER_IT.getDefaultStack(), ModelTransformationMode.FIXED, true, matrices, vertexConsumers, light, 1, model);
			}
			matrices.pop();
			ci.cancel();//取消正常的渲染
		}
	}
	@Unique
	private Quaternionf gettingoverit$getInverseQuaternionFromPitchYaw(float pitch, float yaw, float roll) {
		// 创建四元数
		Quaternionf quaternion = new Quaternionf();

		// 计算 pitch 和 yaw 和 roll的旋转四元数
		Quaternionf quaternionYaw = new Quaternionf().rotateY((float) Math.toRadians(-yaw));
		Quaternionf quaternionPitch = new Quaternionf().rotateX((float) Math.toRadians(-pitch));
		Quaternionf quaternionRoll = new Quaternionf().rotateZ((float) Math.toRadians(-roll));

		// 合并旋转
		quaternion.set(quaternionYaw);
		quaternion.mul(quaternionPitch);
		quaternion.mul(quaternionRoll);

		// 计算逆四元数
		quaternion.conjugate();

		return quaternion;
	}

	// 旋转向量的方法
	@Unique
	private void gettingoverit$clearRotate(MatrixStack matrices) {
		Matrix4f matrix4f = matrices.peek().getPositionMatrix();
		// 保留平移部分
		float m03 = matrix4f.m30();
		float m13 = matrix4f.m31();
		float m23 = matrix4f.m32();
		float m33 = matrix4f.m33();
		// 清除旋转部分（将旋转部分设置为单位矩阵）
		matrix4f.identity();
		// 重新设置平移部分
		matrix4f.m30(m03);
		matrix4f.m31(m13);
		matrix4f.m32(m23);
		matrix4f.m33(m33);
	}
	@Unique
	public Vec3d gettingoverit$getMidPoint(Vec3d pos1, Vec3d pos2) {
		double midX = pos1.x + (pos2.x - pos1.x) / 2.0;
		double midY = pos1.y + (pos2.y - pos1.y) / 2.0;
		double midZ = pos1.z + (pos2.z - pos1.z) / 2.0;
		return new Vec3d(midX, midY, midZ);
	}

}
//			String rightHandPosStr = String.format("(%.2f, %.2f, %.2f)", rightHandPos.x, rightHandPos.y, rightHandPos.z);
//			String leftHandPosStr = String.format("(%.2f, %.2f, %.2f)", leftHandPos.x, leftHandPos.y, leftHandPos.z);
//
//			String message = rightHandPosStr + " " + leftHandPosStr;
//			player.sendMessage(Text.literal((message)),true);