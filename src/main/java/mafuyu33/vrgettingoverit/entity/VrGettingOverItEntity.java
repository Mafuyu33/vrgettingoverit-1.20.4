//package mafuyu33.vrgettingoverit.entity;
//
//import mafuyu33.vrgettingoverit.VRDataHandler;
//import mafuyu33.vrgettingoverit.VRPlugin;
//import mafuyu33.vrgettingoverit.item.Moditems;
//import net.minecraft.client.util.math.MatrixStack;
//import net.minecraft.entity.Entity;
//import net.minecraft.entity.EntityPose;
//import net.minecraft.entity.EntityType;
//import net.minecraft.entity.player.PlayerEntity;
//import net.minecraft.item.ItemStack;
//import net.minecraft.nbt.NbtCompound;
//import net.minecraft.util.math.Vec3d;
//import net.minecraft.world.World;
//
//import java.security.PublicKey;
//
//public class VrGettingOverItEntity extends Entity{
//    public VrGettingOverItEntity(EntityType<?> type, World world) {
//        super(type, world);
//    }
//    public static Vec3d lastPos;
//
//    @Override
//    protected void initDataTracker() {
//    }
//
//    @Override
//    protected void readCustomDataFromNbt(NbtCompound nbt) {
//
//    }
//
//    @Override
//    protected void writeCustomDataToNbt(NbtCompound nbt) {
//    }
//
//
//    @Override
//    public void tick() {
//        super.tick();
//        PlayerEntity player = getWorld().getClosestPlayer(this,10);
//        if(player!=null && player.isHolding(Moditems.VR_GETTING_OVER_IT)) {
//            if (VRPlugin.canRetrieveData(player)) {
//                // 获取主手和副手控制器的位置
//                Vec3d mainPos = VRDataHandler.getMainhandControllerPosition(player);
//                Vec3d offPos = VRDataHandler.getOffhandControllerPosition(player);
//
//                // 计算两个位置的中点
//                Vec3d midPos = new Vec3d(
//                        (mainPos.x + offPos.x) / 2,
//                        (mainPos.y + offPos.y) / 2,
//                        (mainPos.z + offPos.z) / 2
//                );
//
//                // 设置实体的位置为中点
//                this.setPos(midPos.x, midPos.y, midPos.z);
//                // 使用线性插值平滑位置更新
//                if (lastPos != null) {
//                    Vec3d smoothPos = lastPos.lerp(midPos, 0.1); // 0.1 表示插值比例，可以调整
//                    this.setPos(smoothPos.x, smoothPos.y, smoothPos.z);
//                    lastPos = smoothPos;
//                } else {
//                    this.setPos(midPos.x, midPos.y, midPos.z);
//                    lastPos = midPos;
//                }
//                // 设置实体的旋转角度，使其面朝玩家正对方向
//                // 获取玩家的朝向角度
//                float playerYaw = player.getYaw();
//                // 设置实体的Y轴旋转角度为玩家的朝向角度
//                this.setYaw(playerYaw);
//
//                // 设置实体的俯仰角度（pitch）为 mainPos 的俯仰角度
//                // 计算 mainPos 的俯仰角度
//                float mainPosPitch = (float) Math.toDegrees(Math.atan2(mainPos.y - this.getY(), Math.sqrt(Math.pow(mainPos.x - this.getX(), 2) + Math.pow(mainPos.z - this.getZ(), 2))));
//                this.setPitch(mainPosPitch);
//
//            }
//        }else {
//            this.discard();
//        }
//    }
//}
