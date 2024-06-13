package mafuyu33.vrgettingoverit.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import mafuyu33.vrgettingoverit.VRDataHandler;
import mafuyu33.vrgettingoverit.item.Moditems;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.EntityRenderers;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.gameplay.trackers.BowTracker;
import org.vivecraft.client_vr.gameplay.trackers.TelescopeTracker;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_vr.render.VivecraftItemRendering;
import org.vivecraft.client_vr.render.helpers.VREffectsHelper;
import org.vivecraft.mod_compat_vr.optifine.OptifineHelper;

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
			// 获取两个手柄的坐标
			Vec3d rightHandPos = VRDataHandler.getMainhandControllerPosition(player);
			Vec3d leftHandPos  = VRDataHandler.getOffhandControllerPosition(player);

			// 计算中间位置
			Vec3d midPoint = gettingoverit$getMidPoint(leftHandPos, rightHandPos);
			Vec3d translationVector;
			Vec3d rotationAxe;
			// 计算从手柄到中间点的向量
			if(mainHand) {
				translationVector = midPoint.subtract(rightHandPos);
			}else {
				translationVector = midPoint.subtract(leftHandPos);
			}


			gettingoverit$rotateVector(matrices);



			// 将中间位置转换为渲染坐标
			matrices.translate(translationVector.x, translationVector.y, -translationVector.z);
			matrices.scale(1,1,1);

			//渲染
			BakedModel model = MinecraftClient.getInstance().getItemRenderer().getModels().getModel(Moditems.VR_GETTING_OVER_IT);
			MinecraftClient.getInstance().getItemRenderer().renderItem(Moditems.VR_GETTING_OVER_IT.getDefaultStack(), ModelTransformationMode.FIXED,false,matrices,vertexConsumers, light,1, model);

			matrices.pop();
			ci.cancel();//取消正常的渲染
		}

	}



	// 旋转向量的方法
	@Unique
	private void gettingoverit$rotateVector(MatrixStack matrices) {
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
//
//	@Unique
//	private void gettingoverit$renderCustomHammer(AbstractClientPlayerEntity player, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
//		// 获取两个手柄的坐标
//		Vec3d rightHandPos = VRDataHandler.getMainhandControllerPosition(player);
//		Vec3d leftHandPos  = VRDataHandler.getOffhandControllerPosition(player);
//
//		// 计算中间位置
//		Vec3d midPoint = gettingoverit$getMidPoint(leftHandPos, rightHandPos);
//
//		// 将中间位置转换为渲染坐标
//		matrices.push();
//		matrices.translate(midPoint.x, midPoint.y, midPoint.z);
//
//		// 渲染自定义的长柄锤
//		gettingoverit$renderHammerModel(matrices, vertexConsumers, light);
//
//		matrices.pop();
//	}
//
	@Unique
	public Vec3d gettingoverit$getMidPoint(Vec3d pos1, Vec3d pos2) {
		return new Vec3d((pos1.x + pos2.x) / 2, (pos1.y + pos2.y) / 2, (pos1.z + pos2.z) / 2);
	}
//	@Unique
//	private void gettingoverit$renderHammerModel(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
//		// 获取Minecraft客户端实例
//		MinecraftClient client = MinecraftClient.getInstance();
//		ItemRenderer itemRenderer = client.getItemRenderer();
//
//		// 创建一个ItemStack
//		ItemStack hammerStack = new ItemStack(Moditems.VR_GETTING_OVER_IT);
//
//		// 获取锤子的BakedModel
//		BakedModel model = itemRenderer.getModel(hammerStack, null, client.player, 0);
//
//		// 渲染锤子模型
//		itemRenderer.renderItem(hammerStack, ModelTransformationMode.NONE, false, matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV, model);
//	}
//
//	@Shadow private ItemStack offHand;
//	@Shadow
//	protected abstract void renderMapInBothHands(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float pitch, float equipProgress, float swingProgress);
//	@Shadow
//	protected abstract void renderMapInOneHand(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float equipProgress, Arm arm, float swingProgress, ItemStack stack);
//	@Shadow
//	protected abstract void applyEquipOffset(MatrixStack matrices, Arm arm, float equipProgress);
//
//	@Shadow @Final private MinecraftClient client;
//	@Shadow
//	protected abstract void applySwingOffset(MatrixStack matrices, Arm arm, float swingProgress);
//	@Shadow
//	public abstract void renderItem(LivingEntity entity, ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light);
//	@Shadow
//	protected abstract void applyEatOrDrinkTransformation(MatrixStack matrices, float tickDelta, Arm arm, ItemStack stack);
//	@Shadow
//	protected abstract void applyBrushTransformation(MatrixStack matrices, float tickDelta, Arm arm, ItemStack stack, float equipProgress);
//
//	@Unique
//	private void gettingoverit$renderFirstPersonItem(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
//		if (!player.isUsingSpyglass()) {
//			boolean bl = hand == Hand.MAIN_HAND;
//			Arm arm = bl ? player.getMainArm() : player.getMainArm().getOpposite();
//			matrices.push();
//			if (item.isEmpty()) {
//				if (bl && !player.isInvisible()) {
//					this.renderArmHoldingItem(matrices, vertexConsumers, light, equipProgress, swingProgress, arm);
//				}
//			} else if (item.isOf(Items.FILLED_MAP)) {
//				if (bl && this.offHand.isEmpty()) {
//					this.renderMapInBothHands(matrices, vertexConsumers, light, pitch, equipProgress, swingProgress);
//				} else {
//					this.renderMapInOneHand(matrices, vertexConsumers, light, equipProgress, arm, swingProgress, item);
//				}
//			} else {
//				boolean bl2;
//				float f;
//				float g;
//				float h;
//				float j;
//				if (item.isOf(Items.CROSSBOW)) {
//					bl2 = CrossbowItem.isCharged(item);
//					boolean bl3 = arm == Arm.RIGHT;
//					int i = bl3 ? 1 : -1;
//					if (player.isUsingItem() && player.getItemUseTimeLeft() > 0 && player.getActiveHand() == hand) {
//						this.applyEquipOffset(matrices, arm, equipProgress);
//						matrices.translate((float)i * -0.4785682F, -0.094387F, 0.05731531F);
//						matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-11.935F));
//						matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)i * 65.3F));
//						matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)i * -9.785F));
//						f = (float)item.getMaxUseTime() - ((float)this.client.player.getItemUseTimeLeft() - tickDelta + 1.0F);
//						g = f / (float)CrossbowItem.getPullTime(item);
//						if (g > 1.0F) {
//							g = 1.0F;
//						}
//
//						if (g > 0.1F) {
//							h = MathHelper.sin((f - 0.1F) * 1.3F);
//							j = g - 0.1F;
//							float k = h * j;
//							matrices.translate(k * 0.0F, k * 0.004F, k * 0.0F);
//						}
//
//						matrices.translate(g * 0.0F, g * 0.0F, g * 0.04F);
//						matrices.scale(1.0F, 1.0F, 1.0F + g * 0.2F);
//						matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees((float)i * 45.0F));
//					} else {
//						f = -0.4F * MathHelper.sin(MathHelper.sqrt(swingProgress) * 3.1415927F);
//						g = 0.2F * MathHelper.sin(MathHelper.sqrt(swingProgress) * 6.2831855F);
//						h = -0.2F * MathHelper.sin(swingProgress * 3.1415927F);
//						matrices.translate((float)i * f, g, h);
//						this.applyEquipOffset(matrices, arm, equipProgress);
//						this.applySwingOffset(matrices, arm, swingProgress);
//						if (bl2 && swingProgress < 0.001F && bl) {
//							matrices.translate((float)i * -0.641864F, 0.0F, 0.0F);
//							matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)i * 10.0F));
//						}
//					}
//
//					this.renderItem(player, item, bl3 ? ModelTransformationMode.FIRST_PERSON_RIGHT_HAND : ModelTransformationMode.FIRST_PERSON_LEFT_HAND, !bl3, matrices, vertexConsumers, light);
//				} else {
//					bl2 = arm == Arm.RIGHT;
//					int l;
//					float m;
//					if (player.isUsingItem() && player.getItemUseTimeLeft() > 0 && player.getActiveHand() == hand) {
//						l = bl2 ? 1 : -1;
//						switch (item.getUseAction()) {
//							case NONE:
//								this.applyEquipOffset(matrices, arm, equipProgress);
//								break;
//							case EAT:
//							case DRINK:
//								this.applyEatOrDrinkTransformation(matrices, tickDelta, arm, item);
//								this.applyEquipOffset(matrices, arm, equipProgress);
//								break;
//							case BLOCK:
//								this.applyEquipOffset(matrices, arm, equipProgress);
//								break;
//							case BOW:
//								this.applyEquipOffset(matrices, arm, equipProgress);
//								matrices.translate((float)l * -0.2785682F, 0.18344387F, 0.15731531F);
//								matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-13.935F));
//								matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)l * 35.3F));
//								matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)l * -9.785F));
//								m = (float)item.getMaxUseTime() - ((float)this.client.player.getItemUseTimeLeft() - tickDelta + 1.0F);
//								f = m / 20.0F;
//								f = (f * f + f * 2.0F) / 3.0F;
//								if (f > 1.0F) {
//									f = 1.0F;
//								}
//
//								if (f > 0.1F) {
//									g = MathHelper.sin((m - 0.1F) * 1.3F);
//									h = f - 0.1F;
//									j = g * h;
//									matrices.translate(j * 0.0F, j * 0.004F, j * 0.0F);
//								}
//
//								matrices.translate(f * 0.0F, f * 0.0F, f * 0.04F);
//								matrices.scale(1.0F, 1.0F, 1.0F + f * 0.2F);
//								matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees((float)l * 45.0F));
//								break;
//							case SPEAR:
//								this.applyEquipOffset(matrices, arm, equipProgress);
//								matrices.translate((float)l * -0.5F, 0.7F, 0.1F);
//								matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-55.0F));
//								matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)l * 35.3F));
//								matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)l * -9.785F));
//								m = (float)item.getMaxUseTime() - ((float)this.client.player.getItemUseTimeLeft() - tickDelta + 1.0F);
//								f = m / 10.0F;
//								if (f > 1.0F) {
//									f = 1.0F;
//								}
//
//								if (f > 0.1F) {
//									g = MathHelper.sin((m - 0.1F) * 1.3F);
//									h = f - 0.1F;
//									j = g * h;
//									matrices.translate(j * 0.0F, j * 0.004F, j * 0.0F);
//								}
//
//								matrices.translate(0.0F, 0.0F, f * 0.2F);
//								matrices.scale(1.0F, 1.0F, 1.0F + f * 0.2F);
//								matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees((float)l * 45.0F));
//								break;
//							case BRUSH:
//								this.applyBrushTransformation(matrices, tickDelta, arm, item, equipProgress);
//						}
//					} else if (player.isUsingRiptide()) {
//						this.applyEquipOffset(matrices, arm, equipProgress);
//						l = bl2 ? 1 : -1;
//						matrices.translate((float)l * -0.4F, 0.8F, 0.3F);
//						matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)l * 65.0F));
//						matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)l * -85.0F));
//					} else {
//						float n = -0.4F * MathHelper.sin(MathHelper.sqrt(swingProgress) * 3.1415927F);
//						m = 0.2F * MathHelper.sin(MathHelper.sqrt(swingProgress) * 6.2831855F);
//						f = -0.2F * MathHelper.sin(swingProgress * 3.1415927F);
//						int o = bl2 ? 1 : -1;
//						matrices.translate((float)o * n, m, f);
//						this.applyEquipOffset(matrices, arm, equipProgress);
//						this.applySwingOffset(matrices, arm, swingProgress);
//					}
//
//					this.renderItem(player, item, bl2 ? ModelTransformationMode.FIRST_PERSON_RIGHT_HAND : ModelTransformationMode.FIRST_PERSON_LEFT_HAND, !bl2, matrices, vertexConsumers, light);
//				}
//			}
//
//			matrices.pop();
//		}
//	}
}