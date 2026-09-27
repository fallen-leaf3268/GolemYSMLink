package dev.airun.golemysmlink.common;

import java.util.List;

public record ModelEntry(String modelId, List<String> textures) {
    public ModelEntry {
        if (modelId == null || modelId.isEmpty() || modelId.length() > 512) throw new IllegalArgumentException("invalid_selection");
        textures = List.copyOf(textures);
        if (textures.stream().anyMatch(value -> value.length() > 512)) throw new IllegalArgumentException("invalid_selection");
    }
}
