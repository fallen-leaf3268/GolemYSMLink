package dev.airun.golemysmlink.client;

import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.EntityMaidRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import dev.airun.golemysmlink.common.ModelSelection;
import dev.xkmc.modulargolems.content.entity.humanoid.HumanoidGolemEntity;
import dev.xkmc.modulargolems.content.entity.humanoid.HumanoidGolemRenderer;
import dev.xkmc.modulargolems.content.entity.humanoid.skin.SpecialRenderSkin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import java.util.IdentityHashMap;
import java.util.Map;

public final class ProxyRender implements SpecialRenderSkin {
    public static final ProxyRender INSTANCE = new ProxyRender();
    private final Map<HumanoidGolemEntity, Entry> entries = new IdentityHashMap<>();
    private final Map<HumanoidGolemEntity, Entry> previews = new IdentityHashMap<>();
    private boolean screenRendering;
    private static final class Entry {
        final HumanoidGolemEntity source;
        final MaidRenderProxy proxy;
        int retryTick;
        long lastSeen;
        Entry(HumanoidGolemEntity source) { this.source = source; proxy = new MaidRenderProxy(source.level()); }
    }
    private ProxyRender() {}
    public void screenRendering(boolean rendering) { screenRendering = rendering; }
    public void clear() {
        entries.values().forEach(e -> e.proxy.release()); entries.clear();
        previews.values().forEach(e -> e.proxy.release()); previews.clear();
        screenRendering = false;
    }
    public void prune() {
        prune(entries); prune(previews);
    }
    private static void prune(Map<HumanoidGolemEntity, Entry> cache) {
        cache.values().removeIf(e -> {
            if (e.source.isRemoved() || e.source.level().getGameTime() - e.lastSeen > 200) { e.proxy.release(); return true; }
            return false;
        });
    }
    @Override public void render(HumanoidGolemEntity golem, float yaw, float partial,
                                 PoseStack pose, MultiBufferSource buffers, int light) {
        ModelSelection selected = ClientEvents.selection(golem);
        var cache = screenRendering ? previews : entries;
        Entry entry = cache.get(golem);
        if (entry == null) {
            if (cache.size() >= 256) {
                var oldest = cache.values().stream().min(java.util.Comparator.comparingLong(e -> e.lastSeen)).orElseThrow();
                oldest.proxy.release(); cache.remove(oldest.source);
            }
            entry = new Entry(golem); cache.put(golem, entry);
        }
        entry.lastSeen = golem.level().getGameTime();
        boolean rendered = false;
        if (golem.tickCount >= entry.retryTick) {
            PoseStack isolated = new PoseStack();
            isolated.last().pose().set(pose.last().pose());
            isolated.last().normal().set(pose.last().normal());
            try {
                entry.proxy.project(golem, selected);
                if (screenRendering) entry.proxy.stabilizePreviewRotation();
                if (YsmClientAccess.ready(entry.proxy, selected)
                        && Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entry.proxy) instanceof EntityMaidRenderer renderer) {
                    float scale = golem.getScale();
                    isolated.scale(scale, scale, scale);
                    renderer.render(entry.proxy, yaw, partial, isolated, buffers, light);
                    rendered = true;
                }
            } catch (RuntimeException | LinkageError error) {
                entry.retryTick = golem.tickCount + 100;
                LogUtils.getLogger().warn("YSM golem rendering failed; restoring golem renderer for {}", golem.getUUID(), error);
            }
        }
        if (!rendered && Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(golem) instanceof HumanoidGolemRenderer renderer)
            renderer.renderImpl(golem, yaw, partial, pose, buffers, light);
    }
}
