package de.crazydev22.entityexplode;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

public class Igniter extends Item {
    public Igniter() {
        super(EntityExplode.properties().stacksTo(1));
    }

    @Override
    @NotNull
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        HitResult hit =  player.pick(10.0D, 0.0F, false);

        BlockPos pos = new BlockPos(hit.getLocation());
        BlockState state = player.level.getBlockState(pos);

        if (String.valueOf(state.getBlock()).equals("Block{minecraft:air}")) {
            IgnitionArrowEntity arrow = new IgnitionArrowEntity(EntityExplode.IGNITION_ARROW.get(), player, level);
            arrow.setDeltaMovement(player.getLookAngle().multiply(1.5, 1.5, 1.5));
            level.addFreshEntity(arrow);

            level.playSound(null, player, EntityExplode.IGNITER_SHOOT.get(), SoundSource.PLAYERS,  4.0F, (1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F) * 0.7F);
            player.setItemInHand(hand, new ItemStack(EntityExplode.IGNITER_RAW.get()));
        }
        return super.use(level, player, hand);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (entity instanceof Player)
            return false;
        if (!(entity instanceof LivingEntity))
            return false;
        entity.hurt(DamageSource.GENERIC, 50.0F);

        EntityExplode.protectedEntites.add((LivingEntity) entity);

        float power = EntityExplode.getPower((LivingEntity) entity);

        NuclearExplosion.create(entity.level, entity, entity.getX(), entity.getY(), entity.getZ(), power, false, Explosion.BlockInteraction.NONE, EntityExplode.IGNITION_ARROW_HIT.get());
        var raw = new ItemStack(EntityExplode.IGNITER_RAW.get());
        if (player.getMainHandItem().equals(stack))
            player.setItemInHand(InteractionHand.MAIN_HAND, raw);
        else
            player.setItemInHand(InteractionHand.OFF_HAND, raw);
        return true;
    }
}
