//package mafuyu33.vrgettingoverit.renderer;
//
//import mafuyu33.vrgettingoverit.entity.VrGettingOverItEntity;
//import mafuyu33.vrgettingoverit.item.Moditems;
//import net.minecraft.client.MinecraftClient;
//import net.minecraft.client.render.VertexConsumerProvider;
//import net.minecraft.client.render.entity.EntityRenderer;
//import net.minecraft.client.render.entity.EntityRendererFactory;
//import net.minecraft.client.render.model.BakedModel;
//import net.minecraft.client.render.model.json.ModelTransformationMode;
//import net.minecraft.client.util.math.MatrixStack;
//import net.minecraft.data.client.TextureMap;
//import net.minecraft.entity.Entity;
//import net.minecraft.util.Identifier;
//
//public class VrGettingOverItRenderer extends EntityRenderer<Entity> {
//
//    public VrGettingOverItRenderer(EntityRendererFactory.Context ctx) {
//        super(ctx);
//    }
//
//    @Override
//    public void render(Entity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
//        if(entity instanceof VrGettingOverItEntity vrGettingOverItEntity){
//            matrices.push();
//            BakedModel model = MinecraftClient.getInstance().getItemRenderer().getModels().getModel(Moditems.VR_GETTING_OVER_IT);
//            MinecraftClient.getInstance().getItemRenderer().renderItem(Moditems.VR_GETTING_OVER_IT.getDefaultStack(), ModelTransformationMode.FIXED,false,matrices,vertexConsumers, light,1, model);
//            matrices.pop();
//        }
//
//        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
//    }
//
//    /**
//     * 调用物品渲染方法，渲染实体绑定的物品。
//     * 原理是拦截渲染实体的一些参数，然后用于渲染物品。
//     * 调整姿势和找参数调了好久awa
//     */
//
//
//    @Override
//    public Identifier getTexture(Entity entity) {
//        if (entity instanceof VrGettingOverItEntity){
//            return TextureMap.getId(Moditems.VR_GETTING_OVER_IT);
//        }
//        return null;
//    }
//}
