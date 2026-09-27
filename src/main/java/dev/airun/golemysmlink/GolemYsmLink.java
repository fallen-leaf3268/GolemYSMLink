package dev.airun.golemysmlink;

import dev.airun.golemysmlink.network.LinkNetwork;
import net.minecraftforge.fml.common.Mod;

@Mod(GolemYsmLink.MOD_ID)
public final class GolemYsmLink {
    public static final String MOD_ID = "golemysm_link";
    public GolemYsmLink() {
        LinkNetwork.register();
    }
}
