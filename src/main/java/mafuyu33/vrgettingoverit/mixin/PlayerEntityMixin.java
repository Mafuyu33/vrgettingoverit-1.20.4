package mafuyu33.vrgettingoverit.mixin;

import com.google.common.collect.Multimap;
import mafuyu33.vrgettingoverit.VRDataHandler;
import mafuyu33.vrgettingoverit.VRPlugin;
import mafuyu33.vrgettingoverit.item.Moditems;
import mafuyu33.vrgettingoverit.util.Vec3History;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeModifierCreator;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathConstants;
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

import java.util.Collection;
import java.util.Collections;
import java.util.List;


@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    @Shadow public abstract void sendMessage(Text message, boolean overlay);

    @Shadow public abstract float getMovementSpeed();

    @Unique
    public Vec3History positionHistory  = new Vec3History();
    @Unique
    final double extendDistance=2.0;
    @Unique
    private static Vec3d beforeTouchPos;
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

//实时检测锤柄移动速度？还是手柄移动速度？然后超过一个阈值之后给一个速度并禁用锤子检测一段时间，实现跳跃效果。

    @Inject(at = @At("HEAD"), method = "tick")
    private void init(CallbackInfo info) {
        if(this.isHolding(Moditems.VR_GETTING_OVER_IT)){//手持vr锤子的时候

            World world=this.getWorld();
            if (VRPlugin.canRetrieveData((PlayerEntity) (Object)this)) {//vr
                //禁止玩家使用方向键移动？

                ClientDataHolderVR dh = ClientDataHolderVR.getInstance();
                //获取vr玩家左右手的位置坐标
                Vec3d mainPos = VRDataHandler.getMainhandControllerPosition((PlayerEntity) (Object)this);
                Vec3d offPos = VRDataHandler.getOffhandControllerPosition((PlayerEntity) (Object)this);
                // 获取玩家当前活跃的手
                ItemStack mainHandStack = this.getMainHandStack();
                ItemStack offHandStack = this.getOffHandStack();

                // 判断活跃的手并计算扩展位置
                if (mainHandStack.isOf(Moditems.VR_GETTING_OVER_IT)) {
                    //如果活跃的是右手，从offPos向mainPos扩展，计算预测的坐标
                    predictPos = VrGettingOverIt$extendPosition(mainPos, offPos, extendDistance);
                    leftHanded=false;
                } else if(offHandStack.isOf(Moditems.VR_GETTING_OVER_IT)) {
                    // 如果活跃的是左手，从mainPos向offPos扩展，计算预测的坐标
                    predictPos = VrGettingOverIt$extendPosition(offPos, mainPos, extendDistance);
                    leftHanded=true;
                }


                if(VrGettingOverIt$isInsideBlock(world, predictPos) && !VrGettingOverIt$isInsideBlock(world, lastPos)){
                    //如果预测坐标在方块内，上次坐标不在方块内，表明是第一次碰到方块。更新坐标，不更新玩家位置。把玩家速度设置成0
                    currentPos=predictPos;
                    this.setVelocity(0,0,0);
                    dh.vr.triggerHapticPulse(0, 100);
                    dh.vr.triggerHapticPulse(1, 100);
                    //this.sendMessage(Text.literal("第一次碰到方块"), true);
                }
                if (VrGettingOverIt$isInsideBlock(world, predictPos) && VrGettingOverIt$isInsideBlock(world, lastPos)) {
                    // 如果预测坐标在方块内，上次坐标也在方块内，表明是卡在方块中了。为了防止移动，不更新坐标，但是更新玩家位置。
                    currentPos = lastPos;
                    //this.sendMessage(Text.literal("卡在方块中了"), true);
                    // 然后移动玩家位置，让主手，副手，和现在坐标的位置三点连线是一条直线
                    gettingoverit$hammerMovePlayer(world, dh);
                }
                if(!VrGettingOverIt$isInsideBlock(world, predictPos) && VrGettingOverIt$isInsideBlock(world, lastPos)){
                    //如果预测坐标不在方块内，上次坐标在方块内(但是还要加一个判断！)
                    //并且，锤子距离方块的哪一个面近，就不让锤头从对面的面出去。
                    //实现：加一个beforeTouchPos，可以和lastPos计算出向量，进而如果predictPos在beforeTouchPos指向lastPos的向量底下，也不更新。
                    if(gettingoverit$isAbovePlane(lastPos,beforeTouchPos,predictPos) && lastPos.distanceTo(predictPos)< extendDistance* MathConstants.PI/3){
                        //此时虽然预测点在方块外，但是不符合上面的要求，继续移动玩家位置，不更新坐标。
                        currentPos=lastPos;
                        gettingoverit$hammerMovePlayer(world, dh);
                        //this.sendMessage(Text.literal("接着卡在方块中"), true);
                    }else {//此时表明锤子脱离卡住状态了。更新坐标，停止更新玩家位置。
                        this.fallDistance = 0.0F;
                        currentPos = predictPos;
                        this.setNoGravity(false);

                        gettingoverit$addVelocity();

                        //this.sendMessage(Text.literal("脱离卡住状态了"), true);
                    }
                }
                if(!VrGettingOverIt$isInsideBlock(world, predictPos) && !VrGettingOverIt$isInsideBlock(world, lastPos)){
                    //都不在方块内，正常更新
                    currentPos = predictPos;
                    beforeTouchPos = lastPos;
                    //this.sendMessage(Text.literal("都不在方块内，正常更新"), true);
                }

//                //渲染锤头粒子
//                if(this.getWorld().isClient&&beforeTouchPos!=null&&lastPos!=null&&currentPos!=null){
//                world.addParticle(ParticleTypes.END_ROD, beforeTouchPos.x, beforeTouchPos.y, beforeTouchPos.z, 0, 0, 0);
//                world.addParticle(ParticleTypes.BUBBLE, currentPos.x, currentPos.y, currentPos.z, 0, 0, 0);
//                }

                //存储上一次的位置
                lastPos = currentPos;
                // 在更新周期中记录玩家的位置
                positionHistory.add(this.getPos());

            }else {//非vr
                if(!world.isClient) {
                    this.sendMessage(Text.literal("sorry, this item currently only working with VR Mode :("), true);
                    if(this.hasNoGravity()){
                        this.setNoGravity(false);
                    }
                }
            }
        }else if(currentPos!=null&&lastPos!=null&&predictPos!=null){//没有手持vr锤子,且之前的值不为空的时候,重置为初始状态
            if(this.hasNoGravity()){
                this.setNoGravity(false);
            }
            currentPos=null;
            lastPos=null;
            predictPos=null;
            hasSpawn=false;
        }
    }
    @Unique
    private void gettingoverit$addVelocity() {
        // 获取0.3秒内的净移动量和平均速度
        Vec3d netMovement = positionHistory.netMovement(0.3D);
        double averageSpeed = positionHistory.averageSpeed(0.3F);
        float amp = 0.06f;
        if(this.hasStatusEffect(StatusEffects.STRENGTH)){
           amp = amp * (this.getStatusEffect(StatusEffects.STRENGTH).getAmplifier()+1f);
        }
        // 计算新的速度并施加
        Vec3d newVelocity = netMovement.normalize().multiply(averageSpeed*amp);
        this.setVelocity(newVelocity);
    }

    @Unique
    private boolean gettingoverit$isAbovePlane(Vec3d lastPos, Vec3d beforeTouchPos, Vec3d predictPos) {
        // 第一步：计算 beforeTouchPos 指向 lastPos 的单位方向向量
        if(lastPos!=null&& beforeTouchPos!=null) {
            Vec3d direction = lastPos.subtract(beforeTouchPos).normalize();

            // 使用 direction 作为平面的法向量

            // 第三步：计算平面方程 ax + by + cz + d = 0 的常数项 d
            double d = -(direction.x * lastPos.x + direction.y * lastPos.y + direction.z * lastPos.z);

            // 第四步：判断 predictPos 在平面的哪一侧
            double result = direction.x * predictPos.x + direction.y * predictPos.y + direction.z * predictPos.z + d;

//        // 渲染粒子来表示平面
//        gettingoverit$renderPlane(lastPos, normal, this.getWorld());

            // 如果结果为正，则 predictPos 在平面上方
            return result > 0;
        }else {
            return false;
        }
    }
    @Unique
    private void gettingoverit$hammerMovePlayer(World world, ClientDataHolderVR dh) {
        //让玩家浮空
        if (this.isOnGround()) {
            this.setOnGround(false);
        }
        this.setNoGravity(true);

        // 获取当前玩家位置
        Vec3d playerPos = this.getPos();

        //将predictPos（预测位置）和currentPos（实际位置）进行比较，计算它们之间的位移向量
        Vec3d displacement = predictPos.subtract(currentPos);

        // 计算新的位置
        Vec3d newPos = playerPos.subtract(displacement);
        if (world.isClient) {
        //将这个位移向量同步在玩家身上
        VrGettingOverIt$adjustPlayerPosition(dh,(ClientPlayerEntity) (Object)this,newPos.x,newPos.y,newPos.z);
//                    dh.vrPlayer.snapRoomOriginToPlayerEntity((ClientPlayerEntity) (Object) this, false, false);
        }
    }

    @Unique
    private void VrGettingOverIt$adjustPlayerPosition(ClientDataHolderVR dh, ClientPlayerEntity player, double x, double y, double z) {
        double d4 = player.getX();
        double d6 = player.getY();
        double d8 = player.getZ();
        boolean flag6 = false;

        for (int l = 0; l < 8; ++l) {
            double d13 = x;
            double d2 = y;
            double d3 = z;

            switch (l) {
                case 1:
                default:
                    break;
                case 2:
                    d2 = d6;
                    break;
                case 3:
                    d3 = d8;
                    break;
                case 4:
                    d13 = d4;
                    break;
                case 5:
                    d13 = d4;
                    d3 = d8;
                    break;
                case 6:
                    d13 = d4;
                    d2 = d6;
                    break;
                case 7:
                    d2 = d6;
                    d3 = d8;
            }

            player.setPosition(d13, d2, d3);
            Box aabb1 = player.getBoundingBox();
            flag6 = this.getWorld().isSpaceEmpty(player, aabb1);

            if (flag6) {
//                dh.vrPlayer.snapRoomOriginToPlayerEntity((ClientPlayerEntity) (Object) this, false, false);
//                if (l > 1) {
//                    dh.vr.triggerHapticPulse(0, 100);
//                    dh.vr.triggerHapticPulse(1, 100);
//                }
                break;
            }
        }

        if (!flag6) {
            player.setPosition(d4, d6, d8);
//            dh.vr.triggerHapticPulse(0, 100);
//            dh.vr.triggerHapticPulse(1, 100);
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
