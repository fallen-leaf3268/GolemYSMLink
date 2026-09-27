package dev.airun.golemysmlink.network;

import dev.airun.golemysmlink.common.ModelEntry;
import dev.airun.golemysmlink.common.ModelSelection;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;
import java.util.ArrayList;
import java.util.List;

final class PacketCodec {
    private PacketCodec() {}
    static ModelSelection readSelection(FriendlyByteBuf buf) { return new ModelSelection(buf.readUtf(512), buf.readUtf(512)); }
    static void writeSelection(FriendlyByteBuf buf, ModelSelection value) { buf.writeUtf(value.modelId(), 512); buf.writeUtf(value.textureId(), 512); }
    static List<ModelEntry> readEntries(FriendlyByteBuf buf) {
        int size = count(buf, 8);
        List<ModelEntry> entries = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            String model = buf.readUtf(512);
            int textures = count(buf, 128);
            List<String> ids = new ArrayList<>(textures);
            for (int j = 0; j < textures; j++) ids.add(buf.readUtf(512));
            entries.add(new ModelEntry(model, ids));
        }
        return List.copyOf(entries);
    }
    static void writeEntries(FriendlyByteBuf buf, List<ModelEntry> entries) {
        if (entries.size() > 8) throw new IllegalArgumentException("invalid_selection");
        buf.writeVarInt(entries.size());
        for (ModelEntry entry : entries) {
            if (entry.textures().size() > 128) throw new IllegalArgumentException("invalid_selection");
            buf.writeUtf(entry.modelId(), 512);
            buf.writeVarInt(entry.textures().size());
            entry.textures().forEach(id -> buf.writeUtf(id, 512));
        }
    }
    private static int count(FriendlyByteBuf buf, int maximum) {
        int count = buf.readVarInt();
        if (count < 0 || count > maximum) throw new DecoderException("invalid_selection");
        return count;
    }
}
