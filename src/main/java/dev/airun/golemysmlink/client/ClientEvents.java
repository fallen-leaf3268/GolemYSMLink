package dev.airun.golemysmlink.client;

import dev.airun.golemysmlink.GolemYsmLink;
import dev.airun.golemysmlink.common.ModelSelection;
import dev.airun.golemysmlink.common.SelectionData;
import dev.airun.golemysmlink.network.ClientCallbacks;
import dev.airun.golemysmlink.network.LinkNetwork;
import dev.xkmc.modulargolems.content.entity.humanoid.HumanoidGolemEntity;
import dev.xkmc.modulargolems.content.menu.equipment.EquipmentsScreen;
import dev.xkmc.modulargolems.events.event.HumanoidSkinEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = GolemYsmLink.MOD_ID, value = Dist.CLIENT)
public final class ClientEvents {
    private static final Map<UUID, ClientCallbacks.State> states = new HashMap<>();
    private static Level level;
    private ClientEvents() {}
    private static void checkLevel() {
        Level current = Minecraft.getInstance().level;
        if (level != current) { reset(); level = current; }
    }
    public static ModelSelection selection(HumanoidGolemEntity golem) {
        checkLevel();
        if (golem.getTags().contains("ClientOnly") && golem.level().getEntity(golem.getId()) != golem) return SelectionData.get(golem);
        var state = states.get(golem.getUUID());
        return state != null && state.entityId() == golem.getId() && state.dimension().equals(golem.level().dimension().location())
                ? state.selection() : ModelSelection.NONE;
    }
    private static void reset() { states.clear(); ProxyRender.INSTANCE.clear(); level = null; }
    private static void accept(ClientCallbacks.State state) {
        checkLevel();
        if (level != null && level.dimension().location().equals(state.dimension())) states.put(state.entityUuid(), state);
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void skin(HumanoidSkinEvent event) {
        if (selection(event.getGolem()).enabled()) event.setSkin(ProxyRender.INSTANCE);
    }
    @SubscribeEvent public static void screen(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof EquipmentsScreen screen && screen.getMenu().golem instanceof HumanoidGolemEntity golem)
            screen.addSkinWidget(new SkinButton(screen, golem));
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event) { reset(); }
    @SubscribeEvent public static void beforeScreen(ScreenEvent.Render.Pre event) {
        ProxyRender.INSTANCE.screenRendering(true);
        if (event.getScreen() instanceof dev.xkmc.modulargolems.compat.maid.GolemMaidModelGui) {
            var cached = com.github.tartaricacid.touhoulittlemaid.util.EntityCacheUtil.ENTITY_CACHE
                    .getIfPresent(com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid.TYPE);
            if (cached instanceof com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid maid) maid.setIsYsmModel(false);
        }
    }
    @SubscribeEvent public static void afterScreen(ScreenEvent.Render.Post event) { ProxyRender.INSTANCE.screenRendering(false); }
    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public static void beforeHud(RenderGuiEvent.Pre event) { ProxyRender.INSTANCE.hudRendering(true); }
    @SubscribeEvent public static void renderTick(TickEvent.RenderTickEvent event) {
        ProxyRender.INSTANCE.screenRendering(false);
        ProxyRender.INSTANCE.hudRendering(false);
    }
    @SubscribeEvent public static void removed(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide && event.getEntity() instanceof HumanoidGolemEntity golem) states.remove(golem.getUUID());
    }
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) { checkLevel(); ProxyRender.INSTANCE.prune(); }
    }
    private static final class SkinButton extends ImageButton {
        private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("modulargolems", "textures/gui/sprites/button/skin.png");
        private final HumanoidGolemEntity golem;
        private SkinButton(EquipmentsScreen screen, HumanoidGolemEntity golem) {
            super(screen.getGuiLeft() + 161, screen.getGuiTop() + 5, 9, 9, 0, 0, 9, TEXTURE, 9, 18,
                    button -> Minecraft.getInstance().setScreen(new ModelPickerScreen(screen, golem)));
            this.golem = golem;
            setMessage(Component.translatable("golemysm_link.open"));
            setTooltip(Tooltip.create(Component.translatable("golemysm_link.open")));
        }
        @Override public boolean mouseClicked(double x, double y, int button) {
            if (button == 1 && active && visible && clicked(x, y)) {
                playDownSound(Minecraft.getInstance().getSoundManager());
                LinkNetwork.requestSelection(golem.getId(), golem.getUUID(), 0, ModelSelection.NONE);
                return true;
            }
            return super.mouseClicked(x, y, button);
        }
    }
    @Mod.EventBusSubscriber(modid = GolemYsmLink.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class Setup {
        @SubscribeEvent public static void reload(EntityRenderersEvent.AddLayers event) { ProxyRender.INSTANCE.clear(); }
        @SubscribeEvent public static void setup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                YsmClientAccess.setupAnimationCompatibility();
                ClientCallbacks.STATE = ClientEvents::accept;
                ClientCallbacks.CATALOG = reply -> {
                    if (Minecraft.getInstance().screen instanceof ModelPickerScreen picker) picker.receive(reply);
                };
                ClientCallbacks.RESULT = reply -> {
                    if (Minecraft.getInstance().screen instanceof ModelPickerScreen picker) picker.receive(reply);
                };
            });
        }
    }
}
