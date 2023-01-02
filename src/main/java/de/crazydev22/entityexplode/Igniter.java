package de.crazydev22.entityexplode;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;

public class Igniter extends Item {
    public Igniter() {
        super(EntityExplode.properties().stacksTo(1));
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (entity instanceof Player)
            return false;
        entity.hurt(DamageSource.GENERIC, 50.0F);
        entity.level.explode(player, entity.getX(), entity.getY(), entity.getZ(), 10.0F, Explosion.BlockInteraction.NONE);
        var raw = new ItemStack(EntityExplode.IGNITER_RAW.get());
        if (player.getMainHandItem().equals(stack))
            player.setItemInHand(InteractionHand.MAIN_HAND, raw);
        else
            player.setItemInHand(InteractionHand.OFF_HAND, raw);
        return true;
    }

}
