package mafuyu33.vrgettingoverit.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import mafuyu33.vrgettingoverit.item.Moditems;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client.Xplat;
import org.vivecraft.client_vr.gameplay.trackers.SwingTracker;
import org.vivecraft.mod_compat_vr.bettercombat.BetterCombatHelper;

@Mixin(SwingTracker.class)
public class SwingTrackerMixin {
	@Unique
	private boolean isLooped=false;
//	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec3d;add(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;",ordinal = 0), method = "doProcess")
//	private void init(CallbackInfo info, @Local(ordinal = 1) LocalRef<float> weaponLength, @Local(ordinal = 1)float entityReachAdd, @Local(ordinal = 0) ItemStack itemstack, @Local(ordinal = 0) int c) {
//		if(itemstack.getItem() == Moditems.VR_GETTING_OVER_IT){
////			if(c==0){
////				c=1;
////			}else {
////				c=0;
////			}
//			weaponLength.set(weaponLength.get()+1f);
//			entityReachAdd = (float)playerEntityReach - weaponLength;
//		}
//	}

	@ModifyVariable(method = "doProcess", at = @At("STORE"), ordinal = 0)
	private float injected1(float weaponLength, @Local(ordinal = 0) ItemStack itemstack) {
		if(itemstack.getItem() == Moditems.VR_GETTING_OVER_IT) {
			return 1.2F;
		}
		else return weaponLength;
    }
	@ModifyVariable(method = "doProcess", at = @At("STORE"), ordinal = 1)
	private float injected2(float entityReachAdd, @Local(ordinal = 0) ItemStack itemstack) {
		if(itemstack.getItem() == Moditems.VR_GETTING_OVER_IT) {

			return 3.8F;
		}
		else return entityReachAdd;
	}
	@ModifyVariable(method = "doProcess", at = @At("STORE"), ordinal = 0)
	private ItemStack injected3(ItemStack itemStack,@Local(argsOnly = true) ClientPlayerEntity player,@Local(ordinal = 0) int c) {
		ItemStack anotherHanditemstack = player.getStackInHand(c == 0 ? Hand.OFF_HAND : Hand.MAIN_HAND);
		if(anotherHanditemstack.getItem() == Moditems.VR_GETTING_OVER_IT || itemStack.getItem() == Moditems.VR_GETTING_OVER_IT) {
			return anotherHanditemstack;
		} else {
			return itemStack;
		}

	}
}