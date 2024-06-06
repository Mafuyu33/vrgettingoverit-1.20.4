package mafuyu33.vrgettingoverit;

import mafuyu33.vrgettingoverit.item.Moditems;
import mafuyu33.vrgettingoverit.renderer.VrGettingOverItRenderer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
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
		LOGGER.info("Hello Fabric world!");
	}
}