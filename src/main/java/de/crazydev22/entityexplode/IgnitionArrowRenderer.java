package de.crazydev22.entityexplode;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class IgnitionArrowRenderer extends ArrowRenderer<IgnitionArrowEntity> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(EntityExplode.MODID, "textures/entity/ignition_arrow.png");

    public IgnitionArrowRenderer(EntityRendererProvider.Context manager) {
        super(manager);
    }

    @Override
    @NotNull
    public ResourceLocation getTextureLocation(IgnitionArrowEntity p_114482_) {
        return TEXTURE;
    }
}
