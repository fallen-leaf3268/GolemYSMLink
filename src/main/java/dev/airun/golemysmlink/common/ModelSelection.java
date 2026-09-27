package dev.airun.golemysmlink.common;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public record ModelSelection(String modelId, String textureId) {
    public static final ModelSelection NONE = new ModelSelection("", "");
    public ModelSelection {
        if (modelId == null || textureId == null || modelId.length() > 512 || textureId.length() > 512 || (modelId.isEmpty() && !textureId.isEmpty()))
            throw new IllegalArgumentException("invalid_selection");
    }
    public boolean enabled() { return !modelId.isEmpty(); }
    public static ModelSelection fromNbt(CompoundTag tag) {
        if (tag == null || !tag.contains("model", Tag.TAG_STRING) || !tag.contains("texture", Tag.TAG_STRING)) return NONE;
        try { return new ModelSelection(tag.getString("model"), tag.getString("texture")); }
        catch (IllegalArgumentException e) { return NONE; }
    }
    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putString("model", modelId);
        tag.putString("texture", textureId);
        return tag;
    }
}
