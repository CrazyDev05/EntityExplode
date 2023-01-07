package de.crazydev22.entityexplode;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class NuclearExplosion extends Explosion {
    private final Level level;
    private final Entity source;
    private final double x;
    private final double y;
    private final double z;
    private final float radius;
    private final BlockInteraction blockInteraction;
    private final boolean fire;
    private final ObjectArrayList<BlockPos> toBlow = new ObjectArrayList<>();
    private final RandomSource random = RandomSource.create();
    private final SoundEvent soundEvent;

    public NuclearExplosion(Level level, @Nullable Entity source, double x, double y, double z, float radius, boolean fire, BlockInteraction blockInteraction, SoundEvent soundEvent) {
        super(level, source, x, y, z, radius, fire, blockInteraction);
        this.level = level;
        this.source = source;
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
        this.fire = fire;
        this.blockInteraction = blockInteraction;
        this.soundEvent = soundEvent;
    }

    @Override
    public void finalizeExplosion(boolean bool) {
        this.level.playSound(null, this.x, this.y, this.z, soundEvent, SoundSource.BLOCKS, 4.0F, (1.0F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F) * 0.7F);

        boolean flag = this.blockInteraction != Explosion.BlockInteraction.NONE;
        if (bool) {
            if (!(this.radius < 2.0F) && flag) {
                this.level.addParticle(ParticleTypes.EXPLOSION_EMITTER, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);
            } else {
                this.level.addParticle(ParticleTypes.EXPLOSION, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);
            }
        }

        if (flag) {
            ObjectArrayList<Pair<ItemStack, BlockPos>> objectarraylist = new ObjectArrayList<>();
            boolean flag1 = this.getSourceMob() instanceof Player;
            Util.shuffle(this.toBlow, this.level.random);

            for(BlockPos blockpos : this.toBlow) {
                BlockState blockstate = this.level.getBlockState(blockpos);
                if (!blockstate.isAir()) {
                    BlockPos blockpos1 = blockpos.immutable();
                    this.level.getProfiler().push("explosion_blocks");
                    if (blockstate.canDropFromExplosion(this.level, blockpos, this)) {
                        if (this.level instanceof ServerLevel serverlevel) {
                            BlockEntity blockentity = blockstate.hasBlockEntity() ? this.level.getBlockEntity(blockpos) : null;
                            LootContext.Builder lootcontext$builder = (new LootContext.Builder(serverlevel)).withRandom(this.level.random).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockpos)).withParameter(LootContextParams.TOOL, ItemStack.EMPTY).withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockentity).withOptionalParameter(LootContextParams.THIS_ENTITY, this.source);
                            if (this.blockInteraction == Explosion.BlockInteraction.DESTROY) {
                                lootcontext$builder.withParameter(LootContextParams.EXPLOSION_RADIUS, this.radius);
                            }

                            blockstate.spawnAfterBreak(serverlevel, blockpos, ItemStack.EMPTY, flag1);
                            blockstate.getDrops(lootcontext$builder).forEach((p_46074_) -> {
                                addBlockDrops(objectarraylist, p_46074_, blockpos1);
                            });
                        }
                    }

                    blockstate.onBlockExploded(this.level, blockpos, this);
                    this.level.getProfiler().pop();
                }
            }

            for(Pair<ItemStack, BlockPos> pair : objectarraylist) {
                Block.popResource(this.level, pair.getSecond(), pair.getFirst());
            }
        }

        if (this.fire) {
            for(BlockPos blockpos2 : this.toBlow) {
                if (this.random.nextInt(3) == 0 && this.level.getBlockState(blockpos2).isAir() && this.level.getBlockState(blockpos2.below()).isSolidRender(this.level, blockpos2.below())) {
                    this.level.setBlockAndUpdate(blockpos2, BaseFireBlock.getState(this.level, blockpos2));
                }
            }
        }
    }


    private static void addBlockDrops(ObjectArrayList<Pair<ItemStack, BlockPos>> list, ItemStack itemStack, BlockPos pos) {
        int i = list.size();

        for(int j = 0; j < i; ++j) {
            Pair<ItemStack, BlockPos> pair = list.get(j);
            ItemStack itemstack = pair.getFirst();
            if (ItemEntity.areMergable(itemstack, itemStack)) {
                ItemStack itemstack1 = ItemEntity.merge(itemstack, itemStack, 16);
                list.set(j, Pair.of(itemstack1, pair.getSecond()));
                if (itemStack.isEmpty()) {
                    return;
                }
            }
        }

        list.add(Pair.of(itemStack, pos));
    }

    public static void create(Level level, Entity source, double x, double y, double z, float radius, boolean fire, BlockInteraction blockInteraction, SoundEvent soundEvent) {
        NuclearExplosion explosion = new NuclearExplosion(level, source, x, y, z, radius, fire, blockInteraction, soundEvent);
        if (net.minecraftforge.event.ForgeEventFactory.onExplosionStart(level, explosion)) return;
        explosion.explode();
        explosion.finalizeExplosion(true);
    }
}