package com.github.rcubedev.example.compat.luckperms;

import com.github.rcubedev.example.permission.context.PermissionDynamicContext;
import com.github.rcubedev.example.permission.node.PermissionNode;
import com.github.rcubedev.example.util.TriState;
import com.mojang.authlib.GameProfile;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.platform.PlayerAdapter;
import net.luckperms.api.util.Tristate;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Utility class for interacting with the LuckPerms API in a safe and isolated manner.
 *
 * <p>This class provides helper methods to query permission values for users using
 * LuckPerms, while abstracting away direct API usage and handling asynchronous
 * user loading where required.</p>
 *
 * <p>All methods which accept a {@link UUID} or {@link GameProfile} will load the
 * corresponding {@link User} asynchronously using LuckPerms' {@code UserManager}.
 * These methods return {@link CompletableFuture} instances and should not be blocked
 * on the main server thread.</p>
 *
 * <p>LuckPerms uses {@link Tristate} to represent permission states, which is mapped
 * to our {@link TriState} as follows:</p>
 * <ul>
 *     <li>{@code TRUE → TRUE}</li>
 *     <li>{@code FALSE → FALSE}</li>
 *     <li>{@code UNDEFINED → DEFAULT}</li>
 * </ul>
 *
 * <p>{@code DEFAULT} (formerly {@code UNDEFINED}) indicates that no explicit permission
 * is set and that a fallback value should be used.</p>
 */
public final class LPIntegration {

    /**
     * Gets the LuckPerms API instance.
     *
     * @return the LuckPerms API
     * @throws IllegalStateException if LuckPerms is not yet loaded
     */
    @Contract(pure = true)
    private static @NotNull LuckPerms getAPI() {
        return LuckPermsProvider.get();
    }

    /**
     * Gets the PlayerAdaptor.
     *
     * @return the LuckPerms API
     * @throws IllegalStateException if LuckPerms is not yet loaded
     */
    private static @NotNull PlayerAdapter<ServerPlayer> getPlayerAdaptor() {
        return getAPI().getPlayerAdapter(ServerPlayer.class);
    }

    /**
     * Loads a user.
     *
     * @param uuid the uuid of the user
     * @param username the username, if known
     * @return the resultant user
     */
    private static @NotNull CompletableFuture<User> loadUser(@NotNull UUID uuid, @Nullable String username) {
        return getAPI().getUserManager().loadUser(uuid, username);
    }

    /**
     * Loads a user.
     *
     * @param uuid the uuid of the user
     * @return the resultant user
     */
    private static @NotNull CompletableFuture<User> loadUser(@NotNull UUID uuid) {
        return loadUser(uuid, null);
    }

    /**
     * Gets a user by the player.
     * @param player the player
     * @return the resultant user
     */
    private static @NotNull User getUser(@NotNull ServerPlayer player) {
        return getPlayerAdaptor().getUser(player);
    }

    /**
     * Ensures that a user is loaded.
     * @param uuid the uuid of the user
     * @param username the username, if known
     */
    public static void ensureUserLoaded(@NotNull UUID uuid, @Nullable String username) {
        loadUser(uuid, username);
    }

    /**
     * Ensures that a user is loaded.
     * @param uuid the uuid of the user
     */
    public static void ensureUserLoaded(@NotNull UUID uuid) {
        loadUser(uuid);
    }

    /**
     * Checks whether the given {@link User} has the specified permission.
     *
     * <p>This method uses cached permission data and does not perform any asynchronous
     * operations.</p>
     *
     * @param user the LuckPerms user
     * @param permission the permission node to check
     * @param defaultValue the value to return if the permission is undefined
     * @return {@code true} if the user has the permission, otherwise {@code false}
     */
    private static boolean check(User user, String permission, boolean defaultValue) {
        return getPermissionValue(user, permission).toBoolean(defaultValue);
    }

    /**
     * Checks whether the user represented by the given {@link ServerPlayer} has the specified permission.
     *
     * <p>This method uses cached permission data and does not perform any asynchronous
     * operations.</p>
     *
     * @param player the player
     * @param permission the permission node to check
     * @param defaultValue the value to return if the permission is undefined
     * @return {@code true} if the user has the permission, otherwise {@code false}
     */
    public static boolean check(ServerPlayer player, String permission, boolean defaultValue) {
        return getPermissionValue(player, permission).toBoolean(defaultValue);
    }

    /**
     * Checks whether the user represented by the given {@link GameProfile}
     * has the specified permission.
     *
     * <p>The user will be loaded via LuckPerms if not already cached.</p>
     *
     * @param profile the game profile of the user
     * @param permission the permission node to check
     * @param defaultValue the value to return if the permission is undefined
     * @return a future containing {@code true} if the user has the permission,
     *         otherwise {@code false}
     */
    public static @NotNull CompletableFuture<Boolean> check(@NotNull GameProfile profile, String permission, boolean defaultValue) {
        return getPermissionValue(getUUID(profile), permission).thenApply(tristate -> tristate.toBoolean(defaultValue));
    }

    /**
     * Checks whether the user with the given {@link UUID} has the specified permission.
     *
     * <p>The user will be loaded via LuckPerms if not already cached.</p>
     *
     * @param uuid the UUID of the user
     * @param permission the permission node to check
     * @param defaultValue the value to return if the permission is undefined
     * @return a future containing {@code true} if the user has the permission,
     *         otherwise {@code false}
     */
    public static @NotNull CompletableFuture<Boolean> check(UUID uuid, String permission, boolean defaultValue) {
        return getPermissionValue(uuid, permission).thenApply(tristate -> tristate.toBoolean(defaultValue));
    }

    /**
     * Gets the {@link TriState} permission value for the given {@link User}.
     *
     * <p>This method uses cached permission data and does not perform any asynchronous
     * operations.</p>
     *
     * @param user the LuckPerms user
     * @param permission the permission node to query
     * @return the resolved {@link TriState} value
     */
    public static @NotNull TriState getPermissionValue(@NotNull User user, String permission) {
        return fromLuckPerms(user.getCachedData().getPermissionData().checkPermission(permission));
    }

    /**
     * Gets the {@link TriState} permission value for the user represented by the given {@link ServerPlayer}.
     *
     * <p>This method uses cached permission data and does not perform any asynchronous
     * operations.</p>
     *
     * @param user the player
     * @param permission the permission node to query
     * @return the resolved {@link TriState} value
     */
    public static @NotNull TriState getPermissionValue(@NotNull ServerPlayer player, String permission) {
        return fromLuckPerms(getPlayerAdaptor().getPermissionData(player).checkPermission(permission));
    }

    /**
     * Gets the {@link TriState} permission value for the user
     * represented by the given {@link GameProfile}.
     *
     * @param profile the game profile of the user
     * @param permission the permission node to query
     * @return a future containing the resolved {@link TriState} value
     */
    public static @NotNull CompletableFuture<TriState> getPermissionValue(@NotNull GameProfile profile, String permission) {
        return getPermissionValue(getUUID(profile), permission);
    }

    /**
     * Gets the {@link TriState} permission value for the user with the given {@link UUID}.
     *
     * <p>The user will be loaded via LuckPerms if not already cached.</p>
     *
     * @param uuid the UUID of the user
     * @param permission the permission node to query
     * @return a future containing the resolved {@link TriState} value
     */
    public static @NotNull CompletableFuture<TriState> getPermissionValue(UUID uuid, String permission) {
        return loadUser(uuid).thenApply(u -> getPermissionValue(u, permission));
    }

    /**
     * Converts a LuckPerms {@link Tristate} value to our {@link TriState}.
     *
     * @param tristate the LuckPerms tristate value
     * @return the equivalent tristate value
     */
    @Contract(pure = true)
    private static TriState fromLuckPerms(@NotNull Tristate tristate) {
        return switch (tristate) {
            case TRUE -> TriState.TRUE;
            case FALSE -> TriState.FALSE;
            case UNDEFINED -> TriState.DEFAULT;
        };
    }

    @Contract(pure = true)
    private static UUID getUUID(@NotNull GameProfile profile) {
        return profile./*? if >=1.21.10 {*/ /*id()*/ /*?} else {*/ getId()/*?}*/;
    }

    public static <T> T getPermissionValue(User user, PermissionNode<T> permission, PermissionDynamicContext<?>... context) {
        return permission.resolveFallback(user.getUniqueId(), context).join(); // user already loaded, fallback cannot be async
//        if (permission instanceof NeoForgePermissionNode<T> permissionNodeHolder) {
//            return PermissionAPI.getOfflinePermission(user.getUniqueId(), permissionNodeHolder.getHeldNode(), context);
//        }
//        throw new IllegalArgumentException("Incorrect permission node class type for loader provided");
    }
}
