package mafuyu33.vrgettingoverit.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import mafuyu33.vrgettingoverit.VRDataHandler;
import mafuyu33.vrgettingoverit.VRPlugin;
import mafuyu33.vrgettingoverit.item.Moditems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
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
	@Unique
	private static Vec3d lastLeftHandPos;
	@Unique
	private static Vec3d lastRightHandPos;
	@Unique
	private static Vec3d lastPos;
	@Unique
	private static Vec3d predictPos;
	@Unique
	private static Vec3d currentPos;
	@Unique
	Box[] blockbox = new Box[1];//方块的碰撞箱
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
			Vec3d interpolatedRightHandPos;
			Vec3d interpolatedLeftHandPos;
			// 计算右手手柄的插值位置
			if(lastRightHandPos!=null&&lastLeftHandPos!=null) {
				interpolatedRightHandPos = new Vec3d(
						lastRightHandPos.x + tickDelta * (rightHandPos.x - lastRightHandPos.x),
						lastRightHandPos.y + tickDelta * (rightHandPos.y - lastRightHandPos.y),
						lastRightHandPos.z + tickDelta * (rightHandPos.z - lastRightHandPos.z)
				);
				// 计算左手手柄的插值位置
				interpolatedLeftHandPos = new Vec3d(
						lastLeftHandPos.x + tickDelta * (leftHandPos.x - lastLeftHandPos.x),
						lastLeftHandPos.y + tickDelta * (leftHandPos.y - lastLeftHandPos.y),
						lastLeftHandPos.z + tickDelta * (leftHandPos.z - lastLeftHandPos.z)
				);
			}else {
				interpolatedRightHandPos=rightHandPos;
				interpolatedLeftHandPos=leftHandPos;
			}

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

			//旋转绑定锤头判定点
			Vec3d direction = gettingoverit$getDirection(mainHand,player.getWorld(),interpolatedRightHandPos,interpolatedLeftHandPos,2.0f);

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

			lastPos = currentPos;//储存计算的位置
			lastRightHandPos=rightHandPos;
			lastLeftHandPos=leftHandPos;

			ci.cancel();//取消正常的渲染
		}
	}

	@Unique
	private Vec3d gettingoverit$getDirection(boolean mainHand, World world, Vec3d mainPos, Vec3d offPos, float extendDistance) {
		// 判断活跃的手并计算扩展位置
		if (mainHand) {
			//如果活跃的是右手，从offPos向mainPos扩展，计算预测的坐标
			predictPos = VrGettingOverIt$extendPosition(mainPos, offPos, extendDistance);
		} else{
			// 如果活跃的是左手，从mainPos向offPos扩展，计算预测的坐标
			predictPos = VrGettingOverIt$extendPosition(offPos, mainPos, extendDistance);
		}
		if(VrGettingOverIt$isInsideBlock(world, predictPos) && !VrGettingOverIt$isInsideBlock(world, lastPos)){
			//如果预测坐标在方块内，上次坐标不在方块内，表明是第一次碰到方块。更新坐标，不更新玩家位置。
			currentPos=predictPos;
		}
		if (VrGettingOverIt$isInsideBlock(world, predictPos) && VrGettingOverIt$isInsideBlock(world, lastPos)) {
			// 如果预测坐标在方块内，上次坐标也在方块内，表明是卡在方块中了。为了防止移动，不更新坐标，但是更新玩家位置。
			currentPos=lastPos;
			// 然后移动玩家位置，让主手，副手，和现在坐标的位置三点连线是一条直线（这个怎么实现？）（用旋转角度检测？）
		}
		if(!VrGettingOverIt$isInsideBlock(world, predictPos) && VrGettingOverIt$isInsideBlock(world, lastPos)){
			//如果预测坐标不在方块内，上次坐标在方块内，表明锤子脱离卡住状态了。更新坐标，不更新玩家位置。
			currentPos = predictPos;
		}
		if(!VrGettingOverIt$isInsideBlock(world, predictPos) && !VrGettingOverIt$isInsideBlock(world, lastPos)){
			//都不在方块内，正常更新
			currentPos = predictPos;
		}
		if(mainHand){
			return currentPos.subtract(mainPos).normalize();
		}else {
			return currentPos.subtract(offPos).normalize();
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
	@Unique
	public Vec3d VrGettingOverIt$extendPosition(Vec3d mainPos, Vec3d offPos, double distance) {
		// 从mainPos到offPos的方向向量
		Vec3d direction = new Vec3d(offPos.x - mainPos.x, offPos.y - mainPos.y, offPos.z - mainPos.z);

		// 计算方向向量的长度
		double length = Math.sqrt(direction.x * direction.x + direction.y * direction.y + direction.z * direction.z);

		// 标准化方向向量
		Vec3d normalizedDirection = new Vec3d(direction.x / length, direction.y / length, direction.z / length);

		// 将标准化的方向向量乘以所需延伸的距离
		Vec3d extendedVector = new Vec3d(normalizedDirection.x * distance, normalizedDirection.y * distance, normalizedDirection.z * distance);

		// 将延伸向量加到mainPos上，得到新的坐标位置
		return new Vec3d(mainPos.x + extendedVector.x, mainPos.y + extendedVector.y, mainPos.z + extendedVector.z);
	}
	@Unique
	public boolean VrGettingOverIt$isInsideBlock(World world, @Nullable Vec3d position) {
		if(position!=null) {
			int x = (int) Math.floor(position.x);
			int y = (int) Math.floor(position.y);
			int z = (int) Math.floor(position.z);
			// 将 Vec3d 坐标转换为 BlockPos
			BlockPos blockPos = new BlockPos(x, y, z);

			// 获取给定位置的方块状态
			BlockState blockState = world.getBlockState(blockPos);
			Block block = blockState.getBlock();
			VoxelShape voxelshape = blockState.getCollisionShape(world, blockPos);

			// 检查方块是否为整个的方块
			if (voxelshape.isEmpty()) {
				blockbox[0] = null;
			} else {
				blockbox[0] = voxelshape.getBoundingBox();
			}
			//判断是否在方块内部
			return blockbox[0] != null && blockbox[0].offset(blockPos).contains(position);
		}else {
			return false;
		}
	}
}