package mafuyu33.vrgettingoverit.event;

import com.google.common.collect.Multimap;
import mafuyu33.vrgettingoverit.item.Moditems;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class onAttack implements AttackEntityCallback {//继承副手的武器数值并损坏耐久
    @Override
    public ActionResult interact(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult hitResult) {
        if (player.isHolding(Moditems.VR_GETTING_OVER_IT) && !player.getOffHandStack().isOf(Moditems.VR_GETTING_OVER_IT)
                && !player.getOffHandStack().isEmpty() && !world.isClient) {
            double damageData = getItemAttackDamage(player);
            entity.damage(entity.getDamageSources().playerAttack(player), (float) damageData);
            if(!player.isCreative()) {
                ItemStack itemStack = player.getOffHandStack();
                itemStack.damage(1, player, (p) -> p.sendToolBreakStatus(Hand.OFF_HAND));
            }
        }
        return ActionResult.PASS;
    }

    public double getItemAttackDamage(PlayerEntity player) {
        double attackDamage = 0.0;
            ItemStack itemStack = player.getOffHandStack();
            Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers = itemStack.getItem().getAttributeModifiers(itemStack, EquipmentSlot.MAINHAND);

            Collection<EntityAttributeModifier> attackDamageModifiers = attributeModifiers.get(EntityAttributes.GENERIC_ATTACK_DAMAGE);

            for (EntityAttributeModifier modifier : attackDamageModifiers) {
                attackDamage += modifier.getValue();
            }
        return attackDamage;
    }
}