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

import static org.joml.Math.lerp;

@Mixin(value = HeldItemRenderer.class, priority = 998)
public abstract class ItemInHandRendererVRMixin {
	@Shadow
	private float equipProgressMainHand;
	@Shadow
	private float prevEquipProgressMainHand;
	@Shadow
	private float equipProgressOffHand;
	@Shadow
	private float prevEquipProgressOffHand;


	protected ItemInHandRendererVRMixin() {
	}

	@Shadow
	protected abstract void renderArmHoldingItem(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float equipProgress, float swingProgress, Arm arm);

	@Inject(at = @At("HEAD"), method = "renderFirstPersonItem", cancellable = true)
	private void init(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
		if (item.isOf(Moditems.VR_GETTING_OVER_IT) && player != null && VRPlugin.canRetrieveData(player)) {//如果是VR锤子的话
			boolean mainHand = hand == Hand.MAIN_HAND;
			ClientDataHolderVR dh = ClientDataHolderVR.getInstance();
			Arm humanoidarm = mainHand ? player.getMainArm() : player.getMainArm().getOpposite();
			equipProgressMainHand = this.gettingoverit$getEquipProgress(hand, tickDelta);
			boolean renderArm = dh.currentPass != RenderPass.THIRD || dh.vrSettings.mixedRealityRenderHands;

			if (dh.currentPass == RenderPass.CAMERA) {
				renderArm = false;
			}
			if (renderArm && !player.isInvisible()) {
				this.renderArmHoldingItem(matrices, vertexConsumers, light, equipProgress, swingProgress, humanoidarm);//渲染手臂
			}
			// 获取两个手柄
			Vec3d rightHandPos = VRPlugin.getVRAPI().getRenderVRPlayer().getController(0).position();
			Vec3d leftHandPos = VRPlugin.getVRAPI().getRenderVRPlayer().getController(1).position();

			//开始矩阵操作
			matrices.push();


			//清除旋转矩阵
			gettingoverit$clearRotate(matrices);
			//让他的坐标系从玩家变成世界
			float yaw1 = VRPlugin.getVRAPI().getRenderVRPlayer().getHMD().getYaw();
			float pitch1 = -VRPlugin.getVRAPI().getRenderVRPlayer().getHMD().getPitch();
			float roll1 = VRPlugin.getVRAPI().getRenderVRPlayer().getHMD().getRoll();

			// 计算逆旋转四元数
			Quaternionf inverseRotation = gettingoverit$getInverseQuaternionFromPitchYaw(yaw1, pitch1, roll1);
			// 应用逆旋转，使物体相对于世界坐标系保持静止
			matrices.multiply(inverseRotation);



			//旋转绑定另外一只手
			Vec3d direction;
			if (mainHand) {
				direction = leftHandPos.subtract(rightHandPos).normalize();
			} else {
				direction = rightHandPos.subtract(leftHandPos).normalize();
			}

			Vec3d Y_axis = new Vec3d(0, -1, 0);
			Vec3d rotationAxis = Y_axis.crossProduct(direction);

			double theta = -Math.acos(Y_axis.dotProduct(direction));

			Quaternionf rotationQuaternion = new Quaternionf().fromAxisAngleRad(
					new org.joml.Vector3f((float) rotationAxis.x, (float) rotationAxis.y,
							(float) rotationAxis.z), (float) theta);
			// 应用变换矩阵
			matrices.multiply(rotationQuaternion);

			matrices.scale(1, 1.7f, 1);//原始长度

			// 渲染
			if (mainHand) {
				BakedModel model = MinecraftClient.getInstance().getItemRenderer().getModels().getModel(Moditems.VR_GETTING_OVER_IT);
				MinecraftClient.getInstance().getItemRenderer().renderItem(Moditems.VR_GETTING_OVER_IT.getDefaultStack(), ModelTransformationMode.FIXED, false, matrices, vertexConsumers, light, 1, model);
			} else {
				BakedModel model = MinecraftClient.getInstance().getItemRenderer().getModels().getModel(Moditems.VR_GETTING_OVER_IT);
				MinecraftClient.getInstance().getItemRenderer().renderItem(Moditems.VR_GETTING_OVER_IT.getDefaultStack(), ModelTransformationMode.FIXED, true, matrices, vertexConsumers, light, 1, model);
			}
			matrices.pop();
			ci.cancel();//取消正常的渲染
		}
	}

	@Unique
	private float gettingoverit$getEquipProgress(Hand hand, float partialTicks) {
		return hand == Hand.MAIN_HAND ? 1.0F - (this.prevEquipProgressMainHand + (this.equipProgressMainHand - this.prevEquipProgressMainHand) * partialTicks) : 1.0F - (this.prevEquipProgressOffHand + (this.equipProgressOffHand - this.prevEquipProgressOffHand) * partialTicks);
	}
	@Unique
	private Quaternionf gettingoverit$getInverseQuaternionFromPitchYaw(float yaw,float pitch, float roll) {
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
}