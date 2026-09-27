package dev.airun.golemysmlink.compat;

import com.elfmcys.yesstevemodel.OO0ooO00OoO00o0OO0OOooO0;
import com.elfmcys.yesstevemodel.YesSteveModel;
import com.mojang.logging.LogUtils;
import dev.airun.golemysmlink.common.SelectionData;
import dev.xkmc.modulargolems.content.entity.humanoid.HumanoidGolemEntity;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.world.entity.projectile.Projectile;

public final class YsmProjectiles {
    private YsmProjectiles() {}
    public static void initialize(Projectile projectile) {
        if (projectile.level().isClientSide || !(projectile.getOwner() instanceof HumanoidGolemEntity golem)) return;
        var selected = SelectionData.get(golem);
        if (!selected.enabled()) return;
        try {
            if (!YesSteveModel.isAvailable()) return;
            projectile.getCapability(OO0ooO00OoO00o0OO0OOooO0.Oo0Oo0o00O00Oo0OOoOOoooo).ifPresent(cap -> {
                if (!cap.o0OOooo0o0OO00OoOOOo0o0O())
                    cap.Oo0Oo0o00O00Oo0OOoOOoooo(selected.modelId(), new Object2FloatOpenHashMap<>());
            });
        } catch (RuntimeException | LinkageError failure) {
            LogUtils.getLogger().warn("Could not inherit YSM projectile appearance for {}", projectile.getUUID(), failure);
        }
    }
}
