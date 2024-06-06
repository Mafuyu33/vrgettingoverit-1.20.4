//package mafuyu33.vrgettingoverit.entity;
//
//import dev.architectury.registry.registries.DeferredRegister;
//import mafuyu33.vrgettingoverit.VRGettingOverIt;
//import mafuyu33.vrgettingoverit.util.VrGettingOverItEntityFactory;
//import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
//import net.minecraft.entity.Entity;
//import net.minecraft.entity.EntityDimensions;
//import net.minecraft.entity.EntityType;
//import net.minecraft.entity.SpawnGroup;
//import net.minecraft.entity.projectile.PersistentProjectileEntity;
//import net.minecraft.registry.Registries;
//import net.minecraft.registry.Registry;
//
//public class ModEntities {
//
////    public static final EntityType<VrGettingOverItEntity> VR_GETTING_OVER_IT_ENTITY = Registry.register(Registries.ENTITY_TYPE,
////            new Identifier(VRGettingOverIt.MOD_ID, "vr_getting_over_it_entity"),
////            EntityType.Builder.create(VrGettingOverItEntity::new,SpawnGroup.MISC)
////                    .setDimensions(0.25f, 0.25f).build());
//    public static EntityType<VrGettingOverItEntity> VR_GETTING_OVER_IT_ENTITY;
//
//    private static <T extends Entity> EntityType<T> register(EntityType<T> entityType) {
//        return Registry.register(Registries.ENTITY_TYPE, VRGettingOverIt.MOD_ID + ":" + "vr_getting_over_it_entity", entityType);
//    }
//
//    private static <T extends Entity> EntityType<T> createArrowEntityType(EntityType.EntityFactory<T> factory) {
//        return FabricEntityTypeBuilder.create(SpawnGroup.MISC, factory).dimensions(EntityDimensions.fixed(0.5f, 0.5f)).trackRangeBlocks(4).trackedUpdateRate(20).build();
//    }
//    public static void init() {
//        VR_GETTING_OVER_IT_ENTITY = register(createArrowEntityType(VrGettingOverItEntityFactory::create));
//    }
//
//
////    public static final DeferredRegister<EntityType<VrGettingOverItEntity>> VR_GETTING_OVER_IT_ENTITY =
////            DeferredRegister.create(VRGettingOverIt.MOD_ID, EntityType.Builder.create((type, world) -> VrGettingOverItEntityFactory.create((EntityType<? extends PersistentProjectileEntity>) type, world), SpawnGroup.MISC)
////            .setCustomClientFactory(VrGettingOverItEntity::new).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(1).sized(1f, 0.2f));
//}
