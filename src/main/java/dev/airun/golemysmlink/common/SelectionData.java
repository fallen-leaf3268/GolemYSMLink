package dev.airun.golemysmlink.common;

import dev.xkmc.modulargolems.content.entity.humanoid.HumanoidGolemEntity;

public final class SelectionData {
    private SelectionData() {}
    public static ModelSelection get(HumanoidGolemEntity golem) {
        return ModelSelection.fromNbt(golem.getPersistentData().getCompound("golemysm_link"));
    }
    public static void set(HumanoidGolemEntity golem, ModelSelection selection) {
        if (selection.enabled()) {
            golem.setPlayerSkin("");
            golem.setMaidModelId("");
            golem.setSoundPackId("");
        }
        golem.getPersistentData().put("golemysm_link", selection.toNbt());
    }
}
