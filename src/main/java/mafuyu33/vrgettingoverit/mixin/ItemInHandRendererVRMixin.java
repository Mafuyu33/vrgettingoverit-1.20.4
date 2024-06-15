package mafuyu33.vrgettingoverit.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import mafuyu33.vrgettingoverit.VRDataHandler;
import mafuyu33.vrgettingoverit.VRPlugin;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.common.utils.math.Quaternion;
import org.vivecraft.common.utils.math.Vector3;

@Mixin(value = HeldItemRenderer.class, priority = 998)
public abstract class ItemInHandRendererVRMixin {//让手上不渲染锤子物品
	@Unique
	boolean isInitialized = false;
	@Shadow private ItemStack mainHand;
	@Shadow private ItemStack offHand;
	@Shadow
	protected abstract void renderArmHoldingItem(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float equipProgress, float swingProgress, Arm arm);
	@Inject(at = @At("HEAD"), method = "renderFirstPersonItem",cancellable = true)
	private void init(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {

		if(item.isOf(Moditems.VR_GETTING_OVER_IT) && player!=null && VRPlugin.canRetrieveData(player)&& player.getWorld().isClient){//如果是VR锤子的话
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
			// 获取两个手柄
			Vec3d rightHandPos = VRDataHandler.getMainhandControllerPosition(player);
			Vec3d leftHandPos  = VRDataHandler.getOffhandControllerPosition(player);
			//开始矩阵操作
			matrices.push();

			//清除旋转矩阵
				gettingoverit$clearRotate(matrices);
				//让他的坐标系从玩家变成世界
				float yaw1 = player.getYaw();
				float pitch1 = player.getPitch();
				float roll1 = VRDataHandler.getHMDRoll(player);
				// 计算逆旋转四元数
				Quaternionf inverseRotation = gettingoverit$getInverseQuaternionFromPitchYaw(pitch1, yaw1, roll1);
				// 应用逆旋转，使物体相对于世界坐标系保持静止
				matrices.multiply(inverseRotation);

			//初始化位置
				Vec3d initialDirection;
				if (mainHand) {
					initialDirection = leftHandPos.subtract(rightHandPos).normalize();
				} else {
					initialDirection = rightHandPos.subtract(leftHandPos).normalize();
				}

				Vec3d Y_axis = new Vec3d(0, -1, 0);
				Vec3d initialRotationAxis = Y_axis.crossProduct(initialDirection);

				double initialTheta = -Math.acos(Y_axis.dotProduct(initialDirection));

				Quaternionf initialRotationQuaternion = new Quaternionf().fromAxisAngleRad(
						new org.joml.Vector3f((float) initialRotationAxis.x, (float) initialRotationAxis.y, 
								(float) initialRotationAxis.z), (float) initialTheta);
				// 应用变换矩
				matrices.multiply(initialRotationQuaternion);

			//旋转，让他跟着第二只手
//				// 计算旋转矩阵
//				float yaw2 = (float) Math.atan2(direction.z, direction.x);
//				float pitch2 = (float) Math.asin(direction.y);
//				// 计算roll2
//				Vec3d horizontalDirection = new Vec3d(direction.x, 0, direction.z).normalize();
//				Vec3d verticalComponent = direction.subtract(horizontalDirection.multiply(horizontalDirection.dotProduct(direction))).normalize();
//				float roll2 = (float) Math.atan2(verticalComponent.y, Math.sqrt(verticalComponent.x * verticalComponent.x + verticalComponent.z * verticalComponent.z));
//				// 创建旋转四元数
//				Quaternionf rotation = new Quaternionf();
//				rotation.rotateY(yaw2);
//				rotation.rotateX(pitch2);
//				rotation.rotateZ(roll2);
//				// 应用旋转四元数到矩阵堆栈
//				matrices.multiply(rotation.conjugate());


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

	@Inject(at = @At("TAIL"), method = "updateHeldItems")//检测是不是初始化的植入
	private void init(CallbackInfo info, @Local(ordinal = 0)ItemStack itemStack,@Local(ordinal = 1)ItemStack itemStack2) {
		if (!ItemStack.areEqual(this.mainHand, itemStack)) {
			this.isInitialized = false; // 重置初始化标志
		}

		if (!ItemStack.areEqual(this.offHand, itemStack2)) {
			this.isInitialized = false; // 重置初始化标志
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
//	@Unique
//	public Vec3d gettingoverit$getMidPoint(Vec3d pos1, Vec3d pos2) {
//		double midX = pos1.x + (pos2.x - pos1.x) / 2.0;
//		double midY = pos1.y + (pos2.y - pos1.y) / 2.0;
//		double midZ = pos1.z + (pos2.z - pos1.z) / 2.0;
//		return new Vec3d(midX, midY, midZ);
//	}

}
//			String rightHandPosStr = String.format("(%.2f, %.2f, %.2f)", rightHandPos.x, rightHandPos.y, rightHandPos.z);
//			String leftHandPosStr = String.format("(%.2f, %.2f, %.2f)", leftHandPos.x, leftHandPos.y, leftHandPos.z);
//
//			String message = rightHandPosStr + " " + leftHandPosStr;
//			player.sendMessage(Text.literal((message)),true);