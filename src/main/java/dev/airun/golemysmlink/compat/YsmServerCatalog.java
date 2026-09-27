package dev.airun.golemysmlink.compat;

import com.elfmcys.yesstevemodel.OoOoOoooO0O00oOoO00OOo00;
import com.elfmcys.yesstevemodel.OOOOo0O0oO0OOo0O0O0Oo0O0;
import dev.airun.golemysmlink.common.ModelEntry;
import dev.airun.golemysmlink.common.ModelSelection;
import dev.airun.golemysmlink.server.ServerRules;
import net.minecraft.server.level.ServerPlayer;
import java.util.*;

public final class YsmServerCatalog {
    private record Snapshot(Map<String, List<String>> models, Set<String> restricted) {}
    private YsmServerCatalog() {}
    public static ServerRules.Page page(ServerPlayer player, String filter, int page) {
        try {
            Snapshot snapshot = snapshot();
            if (snapshot == null) return new ServerRules.Page(0, List.of(), "unavailable");
            List<ModelEntry> models = snapshot.models.entrySet().stream().map(e -> new ModelEntry(e.getKey(), e.getValue().stream().limit(128).toList())).toList();
            return ServerRules.page(models, snapshot.restricted, id -> authorized(player, id), filter, page);
        } catch (RuntimeException | LinkageError e) { return new ServerRules.Page(0, List.of(), "unavailable"); }
    }
    public static String validate(ServerPlayer player, ModelSelection selection) {
        if (selection == null) return "invalid_selection";
        if (!selection.enabled()) return "";
        try {
            Snapshot snapshot = snapshot();
            return snapshot == null ? "unavailable" : ServerRules.selectionError(selection, snapshot.models, snapshot.restricted, id -> authorized(player, id));
        } catch (RuntimeException | LinkageError e) { return "unavailable"; }
    }
    private static Snapshot snapshot() {
        var catalog = OoOoOoooO0O00oOoO00OOo00.Oo0Oo0o00O00Oo0OOoOOoooo();
        var restricted = OoOoOoooO0O00oOoO00OOo00.o0OOooo0o0OO00OoOOOo0o0O();
        if (catalog == null || restricted == null) return null;
        Map<String, List<String>> models = new HashMap<>();
        catalog.forEach((id, model) -> {
            if (id != null && !id.isEmpty() && id.length() <= 512 && model != null) {
                List<String> textures = model.o0OOooo0o0OO00OoOOOo0o0O().o0OOooo0o0OO00OoOOOo0o0O();
                if (textures != null) models.put(id, textures.stream().filter(value -> value != null && value.length() <= 512).toList());
            }
        });
        return new Snapshot(models, Set.copyOf(restricted));
    }
    private static boolean authorized(ServerPlayer player, String id) {
        return player != null && player.getCapability(OOOOo0O0oO0OOo0O0O0Oo0O0.Oo0Oo0o00O00Oo0OOoOOoooo)
            .map(capability -> capability.O00OOOooOoooOoo0o0o0oO0O(id)).orElse(false);
    }
}
