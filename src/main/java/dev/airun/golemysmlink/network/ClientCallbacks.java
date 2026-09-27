package dev.airun.golemysmlink.network;

import dev.airun.golemysmlink.common.ModelEntry;
import dev.airun.golemysmlink.common.ModelSelection;
import net.minecraft.resources.ResourceLocation;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public final class ClientCallbacks {
    public static Consumer<State> STATE = value -> {};
    public static Consumer<CatalogReply> CATALOG = value -> {};
    public static Consumer<SelectionReply> RESULT = value -> {};
    public record State(ResourceLocation dimension, int entityId, UUID entityUuid, ModelSelection selection) {}
    public record CatalogReply(int requestId, UUID entityUuid, int page, int total, List<ModelEntry> entries, String error) {
        public CatalogReply { entries = List.copyOf(entries); }
    }
    public record SelectionReply(int requestId, UUID entityUuid, boolean accepted, ModelSelection selection, String error) {}
    private ClientCallbacks() {}
}
