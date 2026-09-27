package dev.airun.golemysmlink.server;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class RequestLimiter {
    private final Map<UUID, Long> catalogs = new HashMap<>();
    private final Map<UUID, Long> selections = new HashMap<>();
    public boolean allowCatalog(UUID player, long tick) { return allow(catalogs, player, tick, 5); }
    public boolean allowSelection(UUID player, long tick) { return allow(selections, player, tick, 2); }
    private boolean allow(Map<UUID, Long> values, UUID player, long tick, int interval) {
        Long previous = values.get(player);
        if (previous != null && tick >= previous && tick - previous < interval) return false;
        values.put(player, tick);
        return true;
    }
    public void remove(UUID player) { catalogs.remove(player); selections.remove(player); }
}
