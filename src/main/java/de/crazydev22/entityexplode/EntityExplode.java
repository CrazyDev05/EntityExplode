package de.crazydev22.entityexplode;

import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Explosion;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Random;

@Mod(EntityExplode.MODID)
public class EntityExplode {

    public static final String MODID = "entityexplode";
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final RegistryObject<Item> IGNITER = ITEMS.register("igniter", () -> new Igniter());
    public static final RegistryObject<Item> IGNITER_RAW = ITEMS.register("igniter_raw", () -> new Item(properties().stacksTo(1)));
    public static final RegistryObject<Item> IGNITER_EXPLOSIVE = ITEMS.register("igniter_explosive", () -> new Item(properties().stacksTo(16)));

    public EntityExplode() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ITEMS.register(modEventBus);

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
            if (new Random().nextInt(0, 5)==0)
                entity.level.explode(entity, entity.getX(), entity.getY(), entity.getZ(), 20.0F, Explosion.BlockInteraction.NONE);
        }
    }

    static Item.Properties properties() {
        return new Item.Properties().tab(IgniterTab.instance);
    }
}
