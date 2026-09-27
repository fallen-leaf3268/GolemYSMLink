package dev.airun.golemysmlink.server;

import dev.airun.golemysmlink.common.ModelEntry;
import dev.airun.golemysmlink.common.ModelSelection;
import java.util.*;
import java.util.function.Predicate;

public final class ServerRules {
    public record Page(int total, List<ModelEntry> entries, String error) {
        public Page { entries = List.copyOf(entries); }
    }
    private ServerRules() {}
    public static String targetError(boolean matches, boolean alive, double distanceSquared, boolean canModify) {
        if (!matches || !alive) return "invalid_target";
        if (!Double.isFinite(distanceSquared) || distanceSquared > 64) return "too_far";
        return canModify ? "" : "denied";
    }
    public static Page page(Collection<ModelEntry> models, Set<String> restricted, Predicate<String> authorized, String filter, int page) {
        if (filter == null || filter.length() > 64 || page < 0 || page > 100000) return new Page(0, List.of(), "invalid_selection");
        String query = filter.toLowerCase(Locale.ROOT);
        List<ModelEntry> visible = models.stream().filter(e -> !e.textures().isEmpty())
            .filter(e -> !restricted.contains(e.modelId()) || authorized.test(e.modelId()))
            .filter(e -> e.modelId().toLowerCase(Locale.ROOT).contains(query)).sorted(Comparator.comparing(ModelEntry::modelId)).toList();
        int start = Math.min(page * 8, visible.size());
        return new Page(visible.size(), visible.subList(start, Math.min(start + 8, visible.size())), "");
    }
    public static String selectionError(ModelSelection selection, Map<String, List<String>> models, Set<String> restricted, Predicate<String> authorized) {
        if (selection == null) return "invalid_selection";
        if (!selection.enabled()) return "";
        if (models == null) return "unavailable";
        List<String> textures = models.get(selection.modelId());
        if (textures == null) return "model_missing";
        if (restricted.contains(selection.modelId()) && !authorized.test(selection.modelId())) return "model_locked";
        return textures.contains(selection.textureId()) ? "" : "texture_missing";
    }
}
