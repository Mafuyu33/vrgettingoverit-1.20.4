package mafuyu33.vrgettingoverit.mixin;

import com.mojang.authlib.GameProfile;
import mafuyu33.vrgettingoverit.VRDataHandler;
import mafuyu33.vrgettingoverit.VRPlugin;
import mafuyu33.vrgettingoverit.item.Moditems;
import mafuyu33.vrgettingoverit.util.Vec3History;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ClientDataHolderVR;


@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    @Shadow public abstract void sendMessage(Text message, boolean overlay);
    @Unique
    public Vec3History[] controllerHistory = new Vec3History[]{new Vec3History(), new Vec3History()};
    @Unique
    final double extendDistance=2.0;
    @Unique
    private static Vec3d lastPos;
    @Unique
    private static Vec3d predictPos;
    @Unique
    private static Vec3d currentPos;
    @Unique
    private boolean hasSpawn=false;
    @Unique
    Box[] blockbox = new Box[1];//方块的碰撞箱
    @Unique
    private static boolean leftHanded;
    @Unique
    private static Vec3d lastMainPos;
    @Unique
    private static Vec3d lastOffPos;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }



    @Inject(at = @At("HEAD"), method = "tick")
    private void init(CallbackInfo info) {
        if(this.isHolding(Moditems.VR_GETTING_OVER_IT)){//手持vr锤子的时候
            //传递手柄参数给


            //强制双手持有


            World world=this.getWorld();
            if (VRPlugin.canRetrieveData((PlayerEntity) (Object)this)) {//vr

                //禁止玩家使用方向键移动

                //获取vr玩家左右手的位置坐标
                Vec3d mainPos = VRDataHandler.getMainhandControllerPosition((PlayerEntity) (Object)this);
                Vec3d offPos = VRDataHandler.getOffhandControllerPosition((PlayerEntity) (Object)this);
                // 获取玩家当前活跃的手
                ItemStack mainHandStack = this.getMainHandStack();
                ItemStack offHandStack = this.getOffHandStack();

                // 判断活跃的手并计算扩展位置
                if (mainHandStack.isOf(Moditems.VR_GETTING_OVER_IT)) {
                    //如果活跃的是右手，从offPos向mainPos扩展，计算预测的坐标
                    predictPos = VrGettingOverIt$extendPosition(offPos, mainPos, extendDistance);
                    leftHanded=false;
                } else if(offHandStack.isOf(Moditems.VR_GETTING_OVER_IT)) {
                    // 如果活跃的是左手，从mainPos向offPos扩展，计算预测的坐标
                    predictPos = VrGettingOverIt$extendPosition(mainPos, offPos, extendDistance);
                    leftHanded=true;
                }


                if(VrGettingOverIt$isInsideBlock(world, predictPos) && !VrGettingOverIt$isInsideBlock(world, lastPos)){
                    //如果预测坐标在方块内，上次坐标不在方块内，表明是第一次碰到方块。更新坐标，不更新玩家位置。
                    currentPos=predictPos;
                    this.sendMessage(Text.literal("第一次碰到方块"), true);
                }
                if (VrGettingOverIt$isInsideBlock(world, predictPos) && VrGettingOverIt$isInsideBlock(world, lastPos)) {
                    // 如果预测坐标在方块内，上次坐标也在方块内，表明是卡在方块中了。为了防止移动，不更新坐标，但是更新玩家位置。
                    currentPos=lastPos;
                    this.sendMessage(Text.literal("卡在方块中了"), true);
                    // 然后移动玩家位置，让主手，副手，和现在坐标的位置三点连线是一条直线（这个怎么实现？）（用旋转角度检测？）

                    //让玩家浮空
                    if (this.isOnGround()) {
                        this.setOnGround(false);
                    }
                    this.setNoGravity(true);

                    // 获取当前玩家位置
                    Vec3d playerPos = this.getPos();

                    //将predictPos（预测位置）和currentPos（实际位置）进行比较，计算它们之间的位移向量
                    Vec3d displacement = predictPos.subtract(currentPos);
                    //将这个位移向量同步在玩家身上
                    this.setPos(playerPos.x-displacement.x,playerPos.y-displacement.y,playerPos.z-displacement.z);
                    if(world.isClient) {
                        ClientDataHolderVR dh = ClientDataHolderVR.getInstance();
                        dh.vrPlayer.snapRoomOriginToPlayerEntity((ClientPlayerEntity)(Object) this, false, false);
                    }
                }
                if(!VrGettingOverIt$isInsideBlock(world, predictPos) && VrGettingOverIt$isInsideBlock(world, lastPos)){
                    //如果预测坐标不在方块内，上次坐标在方块内，表明锤子脱离卡住状态了。更新坐标，不更新玩家位置。
                    this.fallDistance = 0.0F;
                    currentPos = predictPos;
                    this.setNoGravity(false);
                    this.sendMessage(Text.literal("脱离卡住状态了"), true);
                }
                if(!VrGettingOverIt$isInsideBlock(world, predictPos) && !VrGettingOverIt$isInsideBlock(world, lastPos)){
                    //都不在方块内，正常更新
                    currentPos = predictPos;
                    this.sendMessage(Text.literal("都不在方块内，正常更新"), true);
                }

                //渲染锤头粒子
                if(this.getWorld().isClient){
                    world.addParticle(ParticleTypes.BUBBLE, currentPos.x, currentPos.y, currentPos.z, 0, 0, 0);
                }

                //存储上一次的位置
                lastPos = currentPos;
            }else {//非vr
                if(!world.isClient) {
                    this.sendMessage(Text.literal("sorry, this item currently only working with VR Mode :("), true);

                }
            }
        }else if(currentPos!=null&&lastPos!=null&&predictPos!=null){//没有手持vr锤子,且之前的值不为空的时候,重置为初始状态
            this.setNoGravity(false);
            currentPos=null;
            lastPos=null;
            predictPos=null;
            hasSpawn=false;
        }
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
