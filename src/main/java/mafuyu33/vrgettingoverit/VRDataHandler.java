package mafuyu33.vrgettingoverit;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix3f;

import static mafuyu33.vrgettingoverit.VRPlugin.getVRAPI;
import static mafuyu33.vrgettingoverit.VRPlugin.hasVRAPI;

public class VRDataHandler {

    public static Vec3d getMainhandControllerPosition(PlayerEntity player){
        return getControllerPosition(player, 0);
    }
    public static Vec3d getOffhandControllerPosition(PlayerEntity player){
        return getControllerPosition(player, 1);
    }
    public static Vec3d getControllerPosition(PlayerEntity player, int controllerIndex) {
        if (hasVRAPI()){
            return getVRAPI().getVRPlayer(player).getController(controllerIndex).position();
        }
        return null;
    }
    public static Vec3d getControllerLookAngle(PlayerEntity player, int controllerIndex) {
        if (hasVRAPI()){
            return getVRAPI().getVRPlayer(player).getController(controllerIndex).getLookAngle();
        }
        return null;
    }
    public static float getControllerRoll(PlayerEntity player, int controllerIndex) {
        if (hasVRAPI()){
            return getVRAPI().getVRPlayer(player).getController(controllerIndex).getRoll();
        }
        return 0;
    }
    public static Vec3d getHMDLookAngle(PlayerEntity player){
        if (hasVRAPI()){
            return getVRAPI().getVRPlayer(player).getHMD().getLookAngle();
        }
        return null;
    }
    public static float getHMDRoll(PlayerEntity player){
        if (hasVRAPI()){
            return getVRAPI().getVRPlayer(player).getHMD().getRoll();
        }
        return 0;
    }
    public static Vec3d getHMDPosition(PlayerEntity player){
        if (hasVRAPI()){
            return getVRAPI().getVRPlayer(player).getHMD().position();
        }
        return null;
    }
    public static Vec3d rotateVec3d(Vec3d vec, Matrix3f rotationMatrix) {
        return new Vec3d(
                vec.x * rotationMatrix.m00 + vec.y * rotationMatrix.m01 + vec.z * rotationMatrix.m02,
                vec.x * rotationMatrix.m10 + vec.y * rotationMatrix.m11 + vec.z * rotationMatrix.m12,
                vec.x * rotationMatrix.m20 + vec.y * rotationMatrix.m21 + vec.z * rotationMatrix.m22
        );
    }
}
