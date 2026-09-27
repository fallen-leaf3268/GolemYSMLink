package dev.airun.golemysmlink.network;

import dev.airun.golemysmlink.common.ModelSelection;
import dev.airun.golemysmlink.common.SelectionData;
import dev.airun.golemysmlink.compat.YsmServerCatalog;
import dev.airun.golemysmlink.server.ServerRules;
import dev.airun.golemysmlink.server.RequestLimiter;
import dev.xkmc.modulargolems.content.entity.humanoid.HumanoidGolemEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public final class LinkNetwork {
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(ResourceLocation.fromNamespaceAndPath("golemysm_link", "main"), () -> "1", "1"::equals, "1"::equals);
    private static final RequestLimiter LIMITER = new RequestLimiter();
    private static boolean registered;
    private record CatalogRequest(int entityId, UUID entityUuid, int requestId, String filter, int page) {}
    private record SelectionRequest(int entityId, UUID entityUuid, int requestId, ModelSelection selection) {}
    private LinkNetwork() {}
    public static void register() {
        if (registered) return;
        registered = true;
        CHANNEL.messageBuilder(CatalogRequest.class, 0, NetworkDirection.PLAY_TO_SERVER)
            .encoder((p,b) -> { target(b,p.entityId,p.entityUuid,p.requestId); b.writeUtf(p.filter,64); b.writeVarInt(p.page); })
            .decoder(b -> new CatalogRequest(b.readVarInt(),b.readUUID(),b.readVarInt(),b.readUtf(64),b.readVarInt()))
            .consumerNetworkThread(LinkNetwork::catalog).add();
        CHANNEL.messageBuilder(SelectionRequest.class, 1, NetworkDirection.PLAY_TO_SERVER)
            .encoder((p,b) -> { target(b,p.entityId,p.entityUuid,p.requestId); PacketCodec.writeSelection(b,p.selection); })
            .decoder(b -> new SelectionRequest(b.readVarInt(),b.readUUID(),b.readVarInt(),PacketCodec.readSelection(b)))
            .consumerNetworkThread(LinkNetwork::selection).add();
        CHANNEL.messageBuilder(ClientCallbacks.State.class, 2, NetworkDirection.PLAY_TO_CLIENT)
            .encoder((p,b) -> { b.writeResourceLocation(p.dimension()); b.writeVarInt(p.entityId()); b.writeUUID(p.entityUuid()); PacketCodec.writeSelection(b,p.selection()); })
            .decoder(b -> new ClientCallbacks.State(b.readResourceLocation(),b.readVarInt(),b.readUUID(),PacketCodec.readSelection(b)))
            .consumerNetworkThread((p,c) -> { receive(c, () -> ClientCallbacks.STATE.accept(p)); }).add();
        CHANNEL.messageBuilder(ClientCallbacks.CatalogReply.class, 3, NetworkDirection.PLAY_TO_CLIENT)
            .encoder((p,b) -> { b.writeVarInt(p.requestId()); b.writeUUID(p.entityUuid()); b.writeVarInt(p.page()); b.writeVarInt(p.total()); PacketCodec.writeEntries(b,p.entries()); b.writeUtf(p.error(),512); })
            .decoder(b -> new ClientCallbacks.CatalogReply(b.readVarInt(),b.readUUID(),b.readVarInt(),b.readVarInt(),PacketCodec.readEntries(b),b.readUtf(512)))
            .consumerNetworkThread((p,c) -> { receive(c, () -> ClientCallbacks.CATALOG.accept(p)); }).add();
        CHANNEL.messageBuilder(ClientCallbacks.SelectionReply.class, 4, NetworkDirection.PLAY_TO_CLIENT)
            .encoder((p,b) -> { b.writeVarInt(p.requestId()); b.writeUUID(p.entityUuid()); b.writeBoolean(p.accepted()); PacketCodec.writeSelection(b,p.selection()); b.writeUtf(p.error(),512); })
            .decoder(b -> new ClientCallbacks.SelectionReply(b.readVarInt(),b.readUUID(),b.readBoolean(),PacketCodec.readSelection(b),b.readUtf(512)))
            .consumerNetworkThread((p,c) -> { receive(c, () -> ClientCallbacks.RESULT.accept(p)); }).add();
    }
    private static void target(FriendlyByteBuf b,int entityId,UUID uuid,int requestId) { b.writeVarInt(entityId); b.writeUUID(uuid); b.writeVarInt(requestId); }
    private static void receive(Supplier<NetworkEvent.Context> supplied, Runnable task) {
        NetworkEvent.Context context = supplied.get();
        context.enqueueWork(task);
        context.setPacketHandled(true);
    }
    public static void requestCatalog(int entityId, UUID entityUuid, int requestId, String filter, int page) {
        if (filter == null || filter.length() > 64 || page < 0 || page > 100000) throw new IllegalArgumentException("invalid_selection");
        CHANNEL.sendToServer(new CatalogRequest(entityId,entityUuid,requestId,filter,page));
    }
    public static void requestSelection(int entityId, UUID entityUuid, int requestId, ModelSelection selection) {
        CHANNEL.sendToServer(new SelectionRequest(entityId,entityUuid,requestId,selection));
    }
    public static void sync(HumanoidGolemEntity golem) {
        if (!golem.level().isClientSide) CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> golem), state(golem));
    }
    public static void sendState(ServerPlayer player, HumanoidGolemEntity golem) { send(player, state(golem)); }
    private static ClientCallbacks.State state(HumanoidGolemEntity golem) {
        return new ClientCallbacks.State(golem.level().dimension().location(),golem.getId(),golem.getUUID(),SelectionData.get(golem));
    }
    public static void forget(UUID player) { LIMITER.remove(player); }
    private static HumanoidGolemEntity resolve(ServerPlayer player, int id, UUID uuid) {
        var entity = player.serverLevel().getEntity(id);
        return entity instanceof HumanoidGolemEntity golem && golem.getUUID().equals(uuid) ? golem : null;
    }
    private static String targetError(ServerPlayer player, HumanoidGolemEntity golem) {
        return ServerRules.targetError(golem != null,golem != null && golem.isAlive(),golem == null ? 0 : player.distanceToSqr(golem),golem != null && golem.canModify(player));
    }
    private static void catalog(CatalogRequest packet, Supplier<NetworkEvent.Context> supplied) {
        var context = supplied.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            HumanoidGolemEntity golem = resolve(player,packet.entityId,packet.entityUuid);
            String error = LIMITER.allowCatalog(player.getUUID(),player.server.getTickCount()) ? targetError(player,golem) : "rate_limited";
            ServerRules.Page page = error.isEmpty() ? YsmServerCatalog.page(player,packet.filter,packet.page) : new ServerRules.Page(0,List.of(),error);
            send(player,new ClientCallbacks.CatalogReply(packet.requestId,packet.entityUuid,packet.page,page.total(),page.entries(),page.error()));
        });
        context.setPacketHandled(true);
    }
    private static void selection(SelectionRequest packet, Supplier<NetworkEvent.Context> supplied) {
        var context = supplied.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            HumanoidGolemEntity golem = resolve(player,packet.entityId,packet.entityUuid);
            String error = LIMITER.allowSelection(player.getUUID(),player.server.getTickCount()) ? targetError(player,golem) : "rate_limited";
            if (error.isEmpty()) error = YsmServerCatalog.validate(player,packet.selection);
            boolean accepted = error.isEmpty();
            if (accepted) { SelectionData.set(golem,packet.selection); sync(golem); sendState(player,golem); }
            send(player,new ClientCallbacks.SelectionReply(packet.requestId,packet.entityUuid,accepted,golem == null ? ModelSelection.NONE : SelectionData.get(golem),error));
        });
        context.setPacketHandled(true);
    }
    private static void send(ServerPlayer player, Object packet) { CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),packet); }
}
