package dev.airun.golemysmlink.compat;

import dev.airun.golemysmlink.client.ClientEvents;
import dev.airun.golemysmlink.client.YsmClientAccess;
import dev.xkmc.modulargolems.content.entity.humanoid.HumanoidGolemEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

@WailaPlugin
public final class GolemYsmJadePlugin implements IWailaPlugin {
    @Override public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityComponent(Provider.INSTANCE, HumanoidGolemEntity.class);
    }
    private enum Provider implements IEntityComponentProvider {
        INSTANCE;
        private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath("golemysm_link", "ysm_model");
        @Override public ResourceLocation getUid() { return UID; }
        @Override public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
            if (accessor.getEntity() instanceof HumanoidGolemEntity golem) {
                var selection = ClientEvents.selection(golem);
                if (selection.enabled()) tooltip.add(Component.translatable("golemysm_link.jade_model",
                        YsmClientAccess.modelName(selection.modelId())));
            }
        }
    }
}
