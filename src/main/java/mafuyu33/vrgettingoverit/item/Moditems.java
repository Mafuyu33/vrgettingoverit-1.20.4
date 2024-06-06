package mafuyu33.vrgettingoverit.item;

import mafuyu33.vrgettingoverit.VRGettingOverIt;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class Moditems {
    public static final Item VR_GETTING_OVER_IT = registerItem("vr_getting_over_it", new Item(new FabricItemSettings().maxCount(1)));

    private static void addItemsToIngredientItemGroup(FabricItemGroupEntries entries){
        entries.add(VR_GETTING_OVER_IT);
    }

    public static Item registerItem(String name, Item item){
        return Registry.register(Registries.ITEM,new Identifier(VRGettingOverIt.MOD_ID,name),item);
    }

    public static void registerModItems(){
        VRGettingOverIt.LOGGER.info("注册MOD物品"+VRGettingOverIt.MOD_ID);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(Moditems::addItemsToIngredientItemGroup);
    }
}
