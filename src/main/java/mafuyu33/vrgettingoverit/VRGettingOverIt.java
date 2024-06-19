package mafuyu33.vrgettingoverit;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.PlayerEvent;
import mafuyu33.vrgettingoverit.item.Moditems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.TypedActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VRGettingOverIt implements ModInitializer {

	public static final String MOD_ID="vrgettingoverit";
    public static final Logger LOGGER = LoggerFactory.getLogger("vrgettingoverit");

	@Override
	public void onInitialize() {
		Moditems.registerModItems();
		//VR
		VRPlugin.init();
//		ModEntities.init();
//		EntityRendererRegistry.register(ModEntities.VR_GETTING_OVER_IT_ENTITY, VrGettingOverItRenderer::new);
		LOGGER.info("Getting over it!");

		// 注册玩家复活事件
		ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
			if (!alive) { // 检测到玩家重生
				checkAndGiveHammer(newPlayer);
			}
		});

		// 注册玩家加入事件
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity player = handler.getPlayer();
			checkAndGiveHammer(player);
		});

		//注册玩家死亡事件
		ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, damageAmount) -> {
			onPlayerDeath(entity);
			return true; //允许玩家死亡
		});
	}
	public void onPlayerDeath(Entity entity) {
		// 移除玩家背包中的锤子
		if(entity instanceof ServerPlayerEntity player) {
			for (int i = 0; i < player.getInventory().main.size(); i++) {
				ItemStack itemStack = player.getInventory().main.get(i);
				if (itemStack.getItem() == Moditems.VR_GETTING_OVER_IT) {
					player.getInventory().removeStack(i);
					break;
				}
			}
		}
	}
	public void checkAndGiveHammer(ServerPlayerEntity player) {
		boolean hasHammer = false;

		// 检查玩家背包中是否有锤子
		for (int i = 0; i < player.getInventory().main.size(); i++) {
			ItemStack itemStack = player.getInventory().main.get(i);
			if (itemStack.getItem() == Moditems.VR_GETTING_OVER_IT) {
				hasHammer = true;
				break;
			}
		}

		// 如果玩家没有锤子，则给予一个
		if (!hasHammer) {
			ItemStack hammer = new ItemStack(Moditems.VR_GETTING_OVER_IT);

			// 将锤子添加到玩家的背包中
			if (!player.getInventory().insertStack(hammer)) {
				// 如果玩家的背包已满，将锤子掉落在玩家所在位置
				player.dropItem(hammer, false);
			}

			// 给玩家发送一个信息
			player.sendMessage(Text.literal("看着手中的锤子，你充满了决心"), true);
		}
	}
}