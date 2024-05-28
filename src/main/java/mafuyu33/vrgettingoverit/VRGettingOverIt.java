package mafuyu33.vrgettingoverit;

import mafuyu33.vrgettingoverit.item.Moditems;
import net.fabricmc.api.ModInitializer;
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

		LOGGER.info("Hello Fabric world!");
	}
}