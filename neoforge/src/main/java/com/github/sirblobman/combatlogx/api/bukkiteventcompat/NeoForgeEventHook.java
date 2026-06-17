package com.github.sirblobman.combatlogx.api.bukkiteventcompat;

import com.github.rcubedev.example.task.api.TaskType;
import com.github.rcubedev.example.task.impl.ModdedTaskScheduler;
import com.github.rcubedev.example.task.impl.TickContext;
import com.github.sirblobman.combatlogx.CombatLogX;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@SuppressWarnings("UnstableApiUsage")
@EventBusSubscriber(modid = CombatLogX.MOD_ID, value = Dist.DEDICATED_SERVER)
public class NeoForgeEventHook {

    @SubscribeEvent
    public static void fireJoinEvent(PlayerEvent.PlayerLoggedInEvent event) {
        PlayerJoinEvent e = new PlayerJoinEvent((ServerPlayer) event.getEntity());
        e.dispatch();

        /*LegacyComponentSerializer serializer = LegacyComponentSerializer.legacyAmpersand();
        Component comp = serializer.deserialize("&lOMG!&r Utilising &6LegacyComponentSerializer&r! &7String -> Component");
        String str = serializer.serialize(
                Component.text("OMG!").decorate(TextDecoration.BOLD)
                        .append(Component.text(" Utilising ").decoration(TextDecoration.BOLD, false))
                        .append(Component.text("LegacyComponentSerializer").color(NamedTextColor.GOLD))
                        .append(Component.text("! "))
                        .append(Component.text("Component -> String").color(NamedTextColor.GRAY))
        );
        ServerPlayer player = (ServerPlayer) event.getEntity();
        Audience audience = MinecraftServerAudiences.of(player.server).audience(player);
        audience.sendMessage(comp);
        audience.sendMessage(Component.text("Serialized: ").append(Component.text(str)));
        audience.sendMessage(Component.text("Reserialized: ").append(serializer.deserialize(str)));*/
    }

    @SubscribeEvent
    public static void fireQuitEvent(PlayerEvent.PlayerLoggedOutEvent event) {
        PlayerQuitEvent e = new PlayerQuitEvent((ServerPlayer) event.getEntity());
        e.dispatch();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void fireDeathEvent(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof ServerPlayer) return;
        EntityDeathEvent e = new EntityDeathEvent(entity, event.getSource());
        e.dispatch();
    }

    @SubscribeEvent
    public static void fireRespawnEvent(PlayerEvent.PlayerRespawnEvent event) {
        PlayerRespawnEvent e = new PlayerRespawnEvent((ServerPlayer) event.getEntity());
        e.dispatch();
    }

    @SubscribeEvent
    public static void handleSchedulerServerPreTick(ServerTickEvent.Pre event) {
        ModdedTaskScheduler scheduler = ModdedTaskScheduler.getScheduler();
        scheduler.fireTasks(TaskType.START_TICK, TickContext.ofServer(event.getServer()));
    }

    @SubscribeEvent
    public static void handleSchedulerServerPostTick(ServerTickEvent.Post event) {
        ModdedTaskScheduler scheduler = ModdedTaskScheduler.getScheduler();
        scheduler.fireTasks(TaskType.END_TICK, TickContext.ofServer(event.getServer()));
    }

    /*@SubscribeEvent
    public static void fireEntityExplodeEvent(ExplosionEvent.Detonate event) {
        Explosion explosion = event.getExplosion();
        Entity source = explosion.getDirectSourceEntity();
        Event e;
        if (source != null) e = new EntityExplodeEvent(source, source.position(), explosion.getBlockInteraction());
        else return; // for now no-op; if needed in future impl block explosion event
        e.dispatch();
    }*/

    /*@SubscribeEvent
    public static void fireEntityDamageEvent(LivingDamageEvent.Post event) {
        LivingEntity entity = event.getEntity();
        DamageSource source = event.getSource();
        Entity directEntity = source.getDirectEntity();
        EntityDamageEvent e = directEntity != null ? new EntityDamageByEntityEvent(directEntity, entity, source) : new EntityDamageEvent(entity, source);
        e.dispatch();
    }*/
}
