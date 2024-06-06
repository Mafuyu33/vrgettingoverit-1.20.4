package mafuyu33.vrgettingoverit;

import mafuyu33.vrgettingoverit.item.Moditems;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Quaternionf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VRGettingOverItClient implements ClientModInitializer {


	@Override
	public void onInitializeClient() {

//		WorldRenderEvents.LAST.register(context -> {
//
//
//			// 确保在客户端线程中执行
//			MinecraftClient mc = MinecraftClient.getInstance();
//			mc.execute(() -> {
//				MatrixStack matrices = new MatrixStack();
//				VertexConsumerProvider.Immediate vertexConsumers = mc.getBufferBuilders().getEntityVertexConsumers();
//
//				// 获取 VR 控制器位置
//				Vec3d startPos;
//				Vec3d endPos = currentPos; // 假设 VRDataHandler 提供当前目标位置
//
//				// 根据持有手决定起始位置
//				if (leftHanded) {
//					startPos = VRDataHandler.getMainhandControllerPosition(mc.player);
//				} else {
//					startPos = VRDataHandler.getOffhandControllerPosition(mc.player);
//				}
//
//				// 计算中点和方向向量
//				Vec3d midPoint = startPos.add(endPos).multiply(0.5);
//				Vec3d direction = endPos.subtract(startPos).normalize();
//				double distance = startPos.distanceTo(endPos);
//
//				// 将物品移动到中点位置
//				matrices.translate(midPoint.x, midPoint.y, midPoint.z);
//
//				// 计算旋转角度
//				float yaw = (float) Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90.0F;
//				float pitch = (float) Math.toDegrees(Math.asin(direction.y));
//
//				// 应用旋转
//				matrices.multiply(new Quaternionf().rotateY((float) Math.toRadians(yaw)));
//				matrices.multiply(new Quaternionf().rotateX((float) Math.toRadians(pitch)));
//
//				// 应用缩放
//				matrices.scale((float) distance, 1.0F, 1.0F);
//
//				// 渲染物品
//				BakedModel model = mc.getItemRenderer().getModel(Moditems.VR_GETTING_OVER_IT.getDefaultStack(), MinecraftClient.getInstance().world, mc.player, 0);
//				mc.getItemRenderer().renderItem(Moditems.VR_GETTING_OVER_IT.getDefaultStack(), ModelTransformationMode.FIXED, false, matrices, vertexConsumers, 15728880, OverlayTexture.DEFAULT_UV, model);
//
//				// 提交渲染
//				vertexConsumers.draw();
//			});
//		});


	}
}