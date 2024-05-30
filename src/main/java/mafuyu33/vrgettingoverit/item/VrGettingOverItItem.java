package mafuyu33.vrgettingoverit.item;

import mafuyu33.vrgettingoverit.VRDataHandler;
import mafuyu33.vrgettingoverit.VRPlugin;
import mafuyu33.vrgettingoverit.util.Vec3History;
import net.blf02.vrapi.data.VRData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.joml.Quaternionf;
import org.vivecraft.client_vr.ClientDataHolderVR;


public class VrGettingOverItItem extends Item {
    public VrGettingOverItItem(Settings settings) {
        super(settings);
    }
    public Vec3History[] controllerHistory = new Vec3History[]{new Vec3History(), new Vec3History()};
    final double extendDistance=2.0;
    public static Vec3d lastPos=new Vec3d (0,100,0);
    public static Vec3d lastMainPos=new Vec3d (0,100,0);
    public static Vec3d lastOffPos=new Vec3d (0,100,0);
    public static Vec3d predictPos=new Vec3d (0,100,0);
    public static Vec3d currentPos = new Vec3d (0,100,0);
    Box[] blockbox = new Box[1];//方块的碰撞箱
    public static boolean leftHanded;

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        if(entity instanceof PlayerEntity player){
            if(player.isHolding(stack.getItem())){
                //强制双手持有
                if (VRPlugin.canRetrieveData(player)) {//vr


                    //初始化位置阶段

                    //禁止玩家使用方向键移动

                    //生成锤头的位置
                    Vec3d mainPos = VRDataHandler.getMainhandControllerPosition(player);
                    Vec3d offPos = VRDataHandler.getOffhandControllerPosition(player);
                    // 获取玩家当前活跃的手
                    ItemStack mainHandStack = player.getMainHandStack();
                    ItemStack offHandStack = player.getOffHandStack();

                    // 判断活跃的手并计算扩展位置
                    if (mainHandStack == stack) {
                        //如果活跃的是右手，从offPos向mainPos扩展，计算预测的坐标
                        predictPos = extendPosition(offPos, mainPos, extendDistance);
                        leftHanded=false;
                    } else if(offHandStack == stack) {
                        // 如果活跃的是左手，从mainPos向offPos扩展，计算预测的坐标
                        predictPos = extendPosition(mainPos, offPos, extendDistance);
                        leftHanded=true;
                    }


                    if(isInsideBlock(world, predictPos) && !isInsideBlock(world, lastPos)){
                        //如果预测坐标在方块内，上次坐标不在方块内，表明是第一次碰到方块。更新坐标，不更新玩家位置。
                        currentPos=predictPos;
                        player.sendMessage(Text.literal("第一次碰到方块"), true);
                    }
                    if (isInsideBlock(world, predictPos) && isInsideBlock(world, lastPos)) {
                        // 如果预测坐标在方块内，上次坐标也在方块内，表明是卡在方块中了。为了防止移动，不更新坐标，但是更新玩家位置。
                        currentPos=lastPos;
                        player.sendMessage(Text.literal("卡在方块中了"), true);
                        // 然后移动玩家位置，让主手，副手，和现在坐标的位置三点连线是一条直线（这个怎么实现？）（用旋转角度检测？）

                        //让玩家浮空
                        if (player.isOnGround()) {
                            player.setOnGround(false);
                        }
                        player.setNoGravity(true);

                        // 获取当前玩家位置
                        Vec3d playerPos = player.getPos();

                        //将predictPos（预测位置）和currentPos（实际位置）进行比较，计算它们之间的位移向量
                        Vec3d displacement = predictPos.subtract(currentPos);
                        //将这个位移向量同步在玩家身上
                        player.setPos(playerPos.x-displacement.x,playerPos.y-displacement.y,playerPos.z-displacement.z);
                        if(world.isClient) {
                            ClientDataHolderVR dh = ClientDataHolderVR.getInstance();
                            dh.vrPlayer.snapRoomOriginToPlayerEntity((ClientPlayerEntity) player, false, false);
                        }
                    }
                    if(!isInsideBlock(world, predictPos) && isInsideBlock(world, lastPos)){
                        //如果预测坐标不在方块内，上次坐标在方块内，表明锤子脱离卡住状态了。更新坐标，不更新玩家位置。
                        player.fallDistance = 0.0F;
                        currentPos = predictPos;
                        player.setNoGravity(false);
                        player.sendMessage(Text.literal("脱离卡住状态了"), true);
                    }
                    if(!isInsideBlock(world, predictPos) && !isInsideBlock(world, lastPos)){
                        //都不在方块内，正常更新
                        currentPos = predictPos;
                        player.sendMessage(Text.literal("都不在方块内，正常更新"), true);
                    }


                    //渲染
                    if(world.isClient){
                        //锤头的位置模拟（目前用粒子代替）
                        int numParticles = 10;  // 粒子数量
                        Vec3d startPos;

                        if(leftHanded) {//左右手
                            startPos = mainPos;
                        }else {
                            startPos= offPos;
                        }

                        // 计算每个粒子的位置增量
                        Vec3d increment = currentPos.subtract(startPos).multiply(1.0 / numParticles);

                        for (int i = 0; i <= numParticles; i++) {
                            Vec3d particlePos = startPos.add(increment.multiply(i));
                            world.addParticle(ParticleTypes.BUBBLE, particlePos.x, particlePos.y, particlePos.z, 0, 0, 0);
                        }
                    }

                    //存储上一次的位置
                    lastPos = currentPos;
                    lastMainPos = mainPos;
                    lastOffPos = offPos;
                }else {
                    player.sendMessage(Text.literal("sorry, this item currently only working with VR Mode :("), true);
                }
            }
        }
    }



    // 旋转向量 aroundAxis 轴的 angle 角度
    private Vec3d rotateAroundAxis(Vec3d vector, Vec3d axis, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return vector.multiply(cos)
                .add(axis.crossProduct(vector).multiply(sin))
                .add(axis.multiply(axis.dotProduct(vector)).multiply(1 - cos));
    }

    public Vec3d extendPosition(Vec3d mainPos, Vec3d offPos, double distance) {
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

    public boolean isInsideBlock(World world, Vec3d position) {
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
    }
}
/*
    物理碰撞部分，当锤头在方块内部的时候，判定为卡住（攀爬爪代码），并且把他的位置限制在方块的最外面（可以用掉落物被推出方块的代码）
    此时以锤头的位置作为基准，与玩家两个手柄的位置的变化来进行计算，来改变玩家的位置（而且要有惯性，所以不能单纯
    地通过改变坐标位置来实现，而要给玩家施加速度的方式，所以还要储存上一次的位置来计算双手的速度）。

    还要有一个锤头能拿出来的判定，通过检测两只手的变化，如果判定到锤头往方块外面的方向移动了就切换状态，判定为没卡住，让锤头可以自由移动。

    渲染部分是拿对应手控制器和extendPosition这两个点来做渲染锤子。生成一个实体，然后调用itementity的渲染方法来渲染。记得添加实体在服务端。
*/