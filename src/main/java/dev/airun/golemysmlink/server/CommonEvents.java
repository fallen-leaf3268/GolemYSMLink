package dev.airun.golemysmlink.server;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.airun.golemysmlink.common.ModelSelection;
import dev.airun.golemysmlink.common.SelectionData;
import dev.airun.golemysmlink.compat.YsmServerCatalog;
import dev.airun.golemysmlink.compat.YsmProjectiles;
import dev.airun.golemysmlink.network.LinkNetwork;
import dev.xkmc.modulargolems.content.entity.humanoid.HumanoidGolemEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "golemysm_link")
public final class CommonEvents {
    private CommonEvents() {}
    @SubscribeEvent(priority = EventPriority.HIGHEST) public static void tracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getTarget() instanceof HumanoidGolemEntity golem) LinkNetwork.sendState(player,golem);
        if (event.getTarget() instanceof Projectile projectile) YsmProjectiles.initialize(projectile);
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST) public static void joined(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide && event.getEntity() instanceof Projectile projectile) YsmProjectiles.initialize(projectile);
        if (!event.getLevel().isClientSide && event.getEntity() instanceof HumanoidGolemEntity golem) {
            ModelSelection selection = SelectionData.get(golem);
            if (selection.enabled()) SelectionData.set(golem, selection);
        }
    }
    @SubscribeEvent public static void skinTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof HumanoidGolemEntity golem && !golem.level().isClientSide
                && (!golem.getMaidModelId().isEmpty() || !golem.getPlayerSkin().isEmpty())
                && SelectionData.get(golem).enabled()) {
            SelectionData.set(golem, ModelSelection.NONE);
            LinkNetwork.sync(golem);
        }
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) { LinkNetwork.forget(event.getEntity().getUUID()); }
    @SubscribeEvent public static void commands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("golemysm").requires(source -> source.hasPermission(2))
            .then(Commands.literal("set").then(Commands.argument("golem",EntityArgument.entity())
                .then(Commands.argument("model",StringArgumentType.string()).then(Commands.argument("texture",StringArgumentType.string())
                    .executes(context -> set(context.getSource(),EntityArgument.getEntity(context,"golem"),StringArgumentType.getString(context,"model"),StringArgumentType.getString(context,"texture")))))))
            .then(Commands.literal("clear").then(Commands.argument("golem",EntityArgument.entity())
                .executes(context -> set(context.getSource(),EntityArgument.getEntity(context,"golem"),"",""))))
            .then(Commands.literal("inspect").then(Commands.argument("golem",EntityArgument.entity())
                .executes(context -> inspect(context.getSource(),EntityArgument.getEntity(context,"golem"))))));
    }
    private static HumanoidGolemEntity golem(Entity entity) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        if (!(entity instanceof HumanoidGolemEntity golem) || !golem.isAlive()) throw error("invalid_target");
        return golem;
    }
    private static int set(CommandSourceStack source, Entity entity, String model, String texture) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        HumanoidGolemEntity golem = golem(entity);
        ModelSelection selection;
        try { selection = new ModelSelection(model,texture); }
        catch (IllegalArgumentException e) { throw error("invalid_selection"); }
        String error = YsmServerCatalog.validate(source.getEntity() instanceof ServerPlayer player ? player : null,selection);
        if (!error.isEmpty()) throw error(error);
        SelectionData.set(golem,selection);
        LinkNetwork.sync(golem);
        source.sendSuccess(() -> Component.literal("golemysm: " + model + " / " + texture),true);
        return 1;
    }
    private static int inspect(CommandSourceStack source, Entity entity) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ModelSelection selection = SelectionData.get(golem(entity));
        source.sendSuccess(() -> Component.literal("golemysm: " + selection.modelId() + " / " + selection.textureId()),false);
        return 1;
    }
    private static com.mojang.brigadier.exceptions.CommandSyntaxException error(String code) { return new SimpleCommandExceptionType(Component.literal(code)).create(); }
}
