package de.crazydev22.entityexplode;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class IgniterTab extends CreativeModeTab {
    public static final IgniterTab instance = new IgniterTab(CreativeModeTab.TABS.length, "entityexplode");

    private IgniterTab(int index, String label) {
        super(index, label);
    }

    @Override
    @NotNull
    public ItemStack makeIcon() {
        return new ItemStack(EntityExplode.IGNITER.get());
    }
}
