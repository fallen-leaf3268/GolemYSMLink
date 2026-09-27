package dev.airun.golemysmlink.client;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.OO0oo0O00O0Oo0O00000o0oO;
import com.elfmcys.yesstevemodel.o0OooO00ooo0OO000O0OoOoO;
import com.elfmcys.yesstevemodel.O00OOo00o0O00ooo000OOo0O;
import com.elfmcys.yesstevemodel.oO0OoO0O0OoO0oo0oo0OOooo;
import com.elfmcys.yesstevemodel.oo00O000000OoOo0O0O00oOO;
import com.elfmcys.yesstevemodel.oo0oOO0000o0Ooooo0OoOo0O;
import dev.airun.golemysmlink.common.ModelSelection;

public final class YsmClientAccess {
    private YsmClientAccess() {}
    public static net.minecraft.network.chat.Component modelName(String modelId) {
        String name = modelId;
        if (YesSteveModel.isAvailable()) {
            var data = o0OooO00ooo0OO000O0OoOoO.Oo0Oo0o00O00Oo0OOoOOoooo(modelId).orElse(null);
            if (data != null && data.Ooooo0oooO0oooOOOoO0000O().Oo0Oo0o00O00Oo0OOoOOoooo() != null) {
                var metadata = data.Ooooo0oooO0oooOOOoO0000O().Oo0Oo0o00O00Oo0OOoOOoooo();
                name = com.elfmcys.yesstevemodel.OOOOooO0O0o0OOoOOo00oo00
                        .Oo0Oo0o00O00Oo0OOoOOoooo(data, "metadata.name", metadata.Oo0Oo0o00O00Oo0OOoOOoooo());
            }
        }
        return net.minecraft.network.chat.Component.literal(name == null || name.isBlank() ? modelId : name);
    }

    public static void setupAnimationCompatibility() {
        try {
            var binding = oo00O000000OoOo0O0O00oOO.Oo0Oo0o00O00Oo0OOoOOoooo.get();
            var original = (O00OOo00o0O00ooo000OOo0O) binding.getProperty("mod_version");
            binding.Oo0Oo0o00O00Oo0OOoOOoooo("mod_version", new O00OOo00o0O00ooo000OOo0O() {
                @Override public Object evaluate(oO0OoO0O0OoO0oo0oo0OOooo<?> context, Oo0Oo0o00O00Oo0OOoOOoooo arguments) {
                    if (!(context.Oo0Oo0o00O00Oo0OOoOOoooo() instanceof oo0oOO0000o0Ooooo0OoOo0O<?> modelContext)
                            || !(modelContext.Oo0Oo0o00O00Oo0OOoOOoooo() instanceof MaidRenderProxy))
                        return original.evaluate(context, arguments);
                    String modId = arguments.Oo0Oo0o00O00Oo0OOoOOoooo(context, 0);
                    if ("bettercombat".equals(modId)) return null;
                    return original.evaluate(context, new Oo0Oo0o00O00Oo0OOoOOoooo(java.util.List.of(arguments.Oo0Oo0o00O00Oo0OOoOOoooo(0))) {
                        @Override public String Oo0Oo0o00O00Oo0OOoOOoooo(oO0OoO0O0OoO0oo0oo0OOooo<?> ignored, int index) {
                            return modId;
                        }
                    });
                }
                @Override public boolean Oo0Oo0o00O00Oo0OOoOOoooo(int size) {
                    return original.Oo0Oo0o00O00Oo0OOoOOoooo(size);
                }
            });
        } catch (org.apache.commons.lang3.concurrent.ConcurrentException error) {
            throw new IllegalStateException("Cannot initialize YSM golem animation compatibility", error);
        }
    }

    public static boolean ready(MaidRenderProxy proxy, ModelSelection selection) {
        if (!selection.enabled() || !YesSteveModel.isAvailable()) return false;
        var expected = o0OooO00ooo0OO000O0OoOoO.Oo0Oo0o00O00Oo0OOoOOoooo(selection.modelId());
        if (expected.isEmpty()) return false;
        return proxy.getCapability(OO0oo0O00O0Oo0O00000o0oO.Oo0Oo0o00O00Oo0OOoOOoooo).map(geo -> {
            geo.setYsmModel(selection.modelId(), selection.textureId());
            return geo.O0ooooOO0oOo000O0Oo00OOO() == expected.get() && geo.getGeoModel() != null;
        }).orElse(false);
    }
}
