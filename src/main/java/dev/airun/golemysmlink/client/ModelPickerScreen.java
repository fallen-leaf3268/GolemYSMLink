package dev.airun.golemysmlink.client;

import com.elfmcys.yesstevemodel.O00000oOo0o0o00OOoo0O0oo;
import com.elfmcys.yesstevemodel.O0o0oOO0OOO0O0OooOooOo0o;
import com.elfmcys.yesstevemodel.OOoO00ooO00OOO00O0o0000O;
import com.elfmcys.yesstevemodel.Oo0o0OOoOo00O0OOOoOOooo0;
import com.elfmcys.yesstevemodel.OoO000oOOOoO00OoOoOOo0OO;
import com.elfmcys.yesstevemodel.OoO00Oo00Ooo0OoOoo00o000;
import com.elfmcys.yesstevemodel.o0OooO00ooo0OO000O0OoOoO;
import com.elfmcys.yesstevemodel.o0o0oO0O0oo00o000000OOOO;
import com.elfmcys.yesstevemodel.oOo0O0oOOoooo0O00OoOoO00;
import com.elfmcys.yesstevemodel.oOo0oO0OOo0ooOoo0oOo0oOo;
import com.elfmcys.yesstevemodel.oooo0Ooo0O0OoOOO0OoOOoO0;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.airun.golemysmlink.common.ModelSelection;
import dev.airun.golemysmlink.network.ClientCallbacks;
import dev.airun.golemysmlink.network.LinkNetwork;
import dev.xkmc.modulargolems.content.entity.humanoid.HumanoidGolemEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public final class ModelPickerScreen extends O0o0oOO0OOO0O0OooOooOo0o {
    private static final AtomicInteger REQUESTS = new AtomicInteger();
    private final HumanoidGolemEntity golem;
    private final Screen parent;
    private ModelSelection selected;
    private MaidRenderProxy preview;
    private TextureScreen textures;
    private Component status = Component.translatable("golemysm_link.choose");
    private int selectionRequest = -1, waitTicks;
    private boolean pending, previewFailed;

    public ModelPickerScreen(Screen parent, HumanoidGolemEntity golem) {
        this.parent = parent;
        this.golem = golem;
        selected = ClientEvents.selection(golem);
    }

    @Override protected void init() {
        previewFailed = false;
        o0OooO00ooo0OO000O0OoOoO.Oo0Oo0o00O00Oo0OOoOOoooo(this);
        if (textures != null && (!textures.modelId.equals(selected.modelId()) || currentData() != textures.data)) {
            textures.removed();
            textures = null;
            status = Component.translatable("golemysm_link.preview_wait");
        }
        if (textures != null) {
            textures.init(minecraft, width, height);
            return;
        }
        super.init();
        int left = Oo0Oo0o00O00Oo0OOoOOoooo, top = o0OOooo0o0OO00OoOOOo0o0O;
        for (var child : List.copyOf(children())) {
            if (child instanceof AbstractWidget widget && widget.getY() == top + 5
                    && (widget.getX() < left + 138 || widget.getX() >= left + 357)) removeWidget(child);
        }
        addRenderableWidget(new oooo0Ooo0O0OoOOO0OoOOoO0(left + 5, top + 5, 20, 20, 80, 16,
                button -> openInformation()).Oo0Oo0o00O00Oo0OOoOOoooo("gui.yes_steve_model.model.info"));
        addRenderableWidget(new oooo0Ooo0O0OoOOO0OoOOoO0(left + 28, top + 5, 79, 20, 32, 16,
                button -> openTextures()).Oo0Oo0o00O00Oo0OOoOOoooo("gui.yes_steve_model.model.texture"));
        addRenderableWidget(Button.builder(Component.literal("↺"), button -> apply(ModelSelection.NONE))
                .bounds(left + 110, top + 5, 20, 20)
                .tooltip(Tooltip.create(Component.translatable("golemysm_link.clear"))).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.back"), button -> onClose())
                .bounds(left + 357, top + 5, 58, 18).build());
    }

    @Override protected o0o0oO0O0oo00o000000OOOO Oo0Oo0o00O00Oo0OOoOOoooo(int x, int y, boolean locked,
            O00000oOo0o0o00OOoo0O0oo model, oOo0oO0OOo0ooOoo0oOo0oOo data) {
        return new o0o0oO0O0oo00o000000OOOO(x, y, locked, model, data) {
            @Override public void onPress() {
                if (!locked) apply(new ModelSelection(model.ooooO0o00oO0Oo0OOo0O0O0o(), model.oOoO0Oo0oO0o00O0oO0ooooo()));
            }
        };
    }

    @Override protected Oo0o0OOoOo00O0OOOoOOooo0 Oo0Oo0o00O00Oo0OOoOOoooo(O0o0oOO0OOO0O0OooOooOo0o screen,
            String modelId, oOo0oO0OOo0ooOoo0oOo0oOo data) {
        return new TextureScreen(selected.modelId(), currentData());
    }

    @Override protected oOo0O0oOOoooo0O00OoOoO00 Oo0Oo0o00O00Oo0OOoOOoooo(O0o0oOO0OOO0O0OooOooOo0o screen,
            oOo0oO0OOo0ooOoo0oOo0oOo data) {
        return new InformationScreen(currentData());
    }

    private oOo0oO0OOo0ooOoo0oOo0oOo currentData() {
        return selected.enabled() ? o0OooO00ooo0OO000O0OoOoO.Oo0Oo0o00O00Oo0OOoOOoooo(selected.modelId()).orElse(null) : null;
    }

    private void openTextures() {
        if (pending || !validTarget()) return;
        var data = currentData();
        if (data == null) { status = Component.translatable("golemysm_link.preview_wait"); return; }
        textures = new TextureScreen(selected.modelId(), data);
        clearWidgets();
        textures.init(minecraft, width, height);
    }

    private void openInformation() {
        if (pending || !validTarget()) return;
        var data = currentData();
        if (data == null) { status = Component.translatable("golemysm_link.preview_wait"); return; }
        if (data.Ooooo0oooO0oooOOOoO0000O().Oo0Oo0o00O00Oo0OOoOOoooo() == null) {
            status = Component.translatable("golemysm_link.no_information");
            return;
        }
        minecraft.setScreen(new InformationScreen(data));
    }

    private void closeTextures() {
        if (textures != null) textures.removed();
        textures = null;
        init();
    }

    private void apply(ModelSelection value) {
        if (pending || !validTarget()) return;
        pending = true;
        waitTicks = 0;
        selectionRequest = REQUESTS.incrementAndGet();
        status = Component.translatable("golemysm_link.saving");
        LinkNetwork.requestSelection(golem.getId(), golem.getUUID(), selectionRequest, value);
    }

    public void receive(ClientCallbacks.CatalogReply reply) {}

    public void receive(ClientCallbacks.SelectionReply reply) {
        if (reply.requestId() != selectionRequest || !reply.entityUuid().equals(golem.getUUID())) return;
        pending = false;
        if (reply.accepted()) {
            selected = reply.selection();
            previewFailed = false;
            status = Component.translatable("golemysm_link.saved");
        } else status = Component.translatable("golemysm_link.error." + reply.error());
    }

    private boolean validTarget() {
        Minecraft client = Minecraft.getInstance();
        return !golem.isRemoved() && golem.isAlive() && client.player != null && golem.level() == client.level;
    }

    private void updateSelection() {
        if (pending && ++waitTicks > 200) {
            pending = false;
            status = Component.translatable("golemysm_link.timeout");
        }
        if (!pending) {
            ModelSelection current = ClientEvents.selection(golem);
            if (!selected.equals(current)) { selected = current; previewFailed = false; }
        }
    }

    @Override public void tick() {
        if (!validTarget()) { closePicker(); return; }
        updateSelection();
        if (textures != null) {
            if (!textures.modelId.equals(selected.modelId()) || currentData() != textures.data) {
                closeTextures();
                status = Component.translatable("golemysm_link.preview_wait");
            } else textures.tick();
        } else super.tick();
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        if (!validTarget()) return;
        if (textures == null) super.render(graphics, mouseX, mouseY, partial);
        else textures.render(graphics, mouseX, mouseY, partial);
        int left = (width - 420) / 2, top = (height - 235) / 2;
        graphics.drawString(font, Component.translatable("golemysm_link.title"), left, Math.max(2, top - 35), 0xFFFFFF);
        graphics.drawString(font, font.plainSubstrByWidth(status.getString(), 420), left,
                Math.min(height - 10, top + 240), 0xE5DCAD);
    }

    @Override protected void Oo0Oo0o00O00Oo0OOoOOoooo(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        int left = Oo0Oo0o00O00Oo0OOoOOoooo, top = o0OOooo0o0OO00OoOOOo0o0O;
        boolean ready = false;
        if (selected.enabled() && currentData() != null && !previewFailed) {
            graphics.enableScissor(left + 5, top + 29, left + 130, top + 200);
            try {
                if (preview == null) preview = new MaidRenderProxy(golem.level());
                preview.project(golem, selected);
                ready = YsmClientAccess.ready(preview, selected);
                if (ready) PreviewRender.draw(graphics, left + 67, top + 190, 70,
                        left + 67 - mouseX, top + 85 - mouseY, preview);
            } catch (RuntimeException | LinkageError failure) { previewFailed = true; }
            finally { graphics.disableScissor(); }
        }
        if (!ready) graphics.drawCenteredString(font, Component.translatable(!selected.enabled()
                ? "golemysm_link.no_selection" : previewFailed ? "golemysm_link.preview_failed" : "golemysm_link.preview_wait"),
                left + 67, top + 110, 0xAAAAAA);
        if (selected.enabled()) graphics.drawCenteredString(font, font.plainSubstrByWidth(selected.modelId(), 125),
                left + 67, top + 205, 0xF3EF60);
    }

    @Override public boolean mouseClicked(double x, double y, int button) {
        return textures == null ? super.mouseClicked(x, y, button) : textures.mouseClicked(x, y, button);
    }
    @Override public List<? extends GuiEventListener> children() {
        return textures == null ? super.children() : textures.children();
    }
    @Override public boolean mouseReleased(double x, double y, int button) {
        return textures == null ? super.mouseReleased(x, y, button) : textures.mouseReleased(x, y, button);
    }
    @Override public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
        return textures == null ? super.mouseDragged(x, y, button, dx, dy) : textures.mouseDragged(x, y, button, dx, dy);
    }
    @Override public boolean mouseScrolled(double x, double y, double delta) {
        return textures == null ? super.mouseScrolled(x, y, delta) : textures.mouseScrolled(x, y, delta);
    }
    @Override public boolean keyPressed(int key, int scan, int modifiers) {
        return textures == null ? super.keyPressed(key, scan, modifiers) : textures.keyPressed(key, scan, modifiers);
    }
    @Override public boolean keyReleased(int key, int scan, int modifiers) {
        return textures == null ? super.keyReleased(key, scan, modifiers) : textures.keyReleased(key, scan, modifiers);
    }
    @Override public boolean charTyped(char character, int modifiers) {
        return textures == null ? super.charTyped(character, modifiers) : textures.charTyped(character, modifiers);
    }
    @Override public void mouseMoved(double x, double y) {
        if (textures == null) super.mouseMoved(x, y); else textures.mouseMoved(x, y);
    }
    @Override public void resize(Minecraft client, int width, int height) {
        if (textures == null) super.resize(client, width, height);
        else init(client, width, height);
    }

    @Override public void removed() {
        o0OooO00ooo0OO000O0OoOoO.o0OOooo0o0OO00OoOOOo0o0O(this);
        if (preview != null) { preview.release(); preview = null; }
        if (textures != null) { textures.removed(); textures = null; }
    }
    @Override public void onClose() {
        if (textures != null) closeTextures(); else closePicker();
    }
    private void closePicker() {
        Minecraft client = Minecraft.getInstance();
        client.setScreen(validTarget() && parent instanceof AbstractContainerScreen<?> container
                && client.player.containerMenu == container.getMenu() ? parent : null);
    }

    private final class TextureScreen extends Oo0o0OOoOo00O0OOOoOOooo0 {
        private final String modelId;
        private final oOo0oO0OOo0ooOoo0oOo0oOo data;

        private TextureScreen(String modelId, oOo0oO0OOo0ooOoo0oOo0oOo data) {
            super(ModelPickerScreen.this, modelId, data);
            this.modelId = modelId;
            this.data = data;
        }

        @Override protected void init() {
            super.init();
            for (var child : List.copyOf(children())) {
                if (child instanceof AbstractWidget widget && widget.getX() == O00OOOooOoooOoo0o0o0oO0O + 5
                        && widget.getY() == oOOOo0OOO0ooooo0O00OO0o0) removeWidget(child);
            }
            addRenderableWidget(Button.builder(Component.translatable("gui.yes_steve_model.model.return"), button -> closeTextures())
                    .bounds(O00OOOooOoooOoo0o0o0oO0O + 5, oOOOo0OOO0ooooo0O00OO0o0, 80, 18).build());
        }

        @Override protected OoO000oOOOoO00OoOoOOo0OO Oo0Oo0o00O00Oo0OOoOOoooo(int x, int y,
                O00000oOo0o0o00OOoo0O0oo model, int index) {
            return new OoO000oOOOoO00OoOoOOo0OO(x, y, model, data) {
                @Override public void onPress() {
                    apply(new ModelSelection(modelId, model.oOoO0Oo0oO0o00O0oO0ooooo()));
                }
            };
        }

        @Override protected void Oo0Oo0o00O00Oo0OOoOOoooo(GuiGraphics graphics, int x, int y, int width, int height, float partial) {
            RenderSystem.enableScissor(x, y, width, height);
            try {
                Oo0Oo0o00O00Oo0OOoOOoooo.Oo0Oo0o00O00Oo0OOoOOoooo(modelId, selected.textureId());
                OoO00Oo00Ooo0OoOoo00o000.Oo0Oo0o00O00Oo0OOoOOoooo(
                        O00OOOooOoooOoo0o0o0oO0O + 189.5f + OOOOo0O0oO0OOo0O0O0Oo0O0,
                        oOOOo0OOO0ooooo0O00OO0o0 + 197.5f + Ooooo0oooO0oooOOOoO0000O,
                        oo0OoO00oOoo000O0000o0oo, Oo00o0OooOOo0ooOoo0oO0o0, oooooooOOoOOoO00OooOo00O, partial,
                        Oo0Oo0o00O00Oo0OOoOOoooo, OOoO00ooO00OOO00O0o0000O.Oo0Oo0o00O00Oo0OOoOOoooo(), o0OOO0o0o0OOo000oO00o00O);
            } finally { RenderSystem.disableScissor(); }
        }

        @Override public void onClose() { closeTextures(); }
        @Override public void removed() { Oo0Oo0o00O00Oo0OOoOOoooo.Ooo0O0000OO0OOO0o0Oo0oOO(); }
    }

    private final class InformationScreen extends oOo0O0oOOoooo0O00OoOoO00 {
        private final oOo0oO0OOo0ooOoo0oOo0oOo data;

        private InformationScreen(oOo0oO0OOo0ooOoo0oOo0oOo data) {
            super(ModelPickerScreen.this, data);
            this.data = data;
        }

        @Override public void tick() {
            if (!validTarget()) { closePicker(); return; }
            updateSelection();
            if (currentData() != data) onClose();
        }
        @Override public void onClose() { Minecraft.getInstance().setScreen(ModelPickerScreen.this); }
    }
}
