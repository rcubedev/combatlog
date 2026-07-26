package com.github.sirblobman.combatlogx.api.language;

import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import com.github.rcubedev.example.event.api.Identity;
import com.github.rcubedev.example.event.api.buses.MainBus;
import com.github.rcubedev.example.permission.node.PermissionNode;
import com.github.rcubedev.example.platform.IAdventure;
import com.github.rcubedev.example.platform.IPlatformHelper;
import com.github.sirblobman.combatlogx.VersionUtil;
import com.github.sirblobman.combatlogx.api.language.listener.LanguageListener;
import com.github.sirblobman.combatlogx.platform.IPlaceholderAPI;
import net.kyori.adventure.audience.ForwardingAudience;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import com.github.sirblobman.combatlogx.CombatLogX;
import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.language.replacer.Replacer;
import com.github.sirblobman.combatlogx.api.utility.Validate;
import folk.sisby.kaleido.api.WrappedConfig;
// import net.kyori.adventure.platform.modcommon.MinecraftServerAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * API wrapper for language management.
 * Supports multiple localized language files (en_us.toml, fr_fr.toml, etc).
 * Delegates to the actual LanguageManager implementation in the manager package.
 */
public final class LanguageManager<T extends WrappedConfig & ILanguage> {
    private static final String[] KNOWN_LANGUAGE_ARRAY = new String[]{"af_za", "ar_sa", "ast_es", "az_az", "ba_ru", "bar", "be_by", "bg_bg", "br_fr", "brb", "bs_ba", "ca_es", "cs_cz", "cy_gb", "da_dk", "de_at", "de_ch", "de_de", "el_gr", "en_au", "en_ca", "en_gb", "en_nz", "en_pt", "en_ud", "en_us", "enp", "enws", "eo_uy", "es_ar", "es_cl", "es_ec", "es_es", "es_mx", "es_uy", "es_ve", "esan", "et_ee", "eu_es", "fa_ir", "fi_fi", "fil_ph", "fo_fo", "fr_ca", "fr_fr", "fra_de", "fy_nl", "ga_ie", "gd_gb", "gl_es", "got_de", "gv_im", "haw_us", "he_il", "hi_in", "hr_hr", "hu_hu", "hy_am", "id_id", "ig_ng", "io_en", "is_is", "isv", "it_it", "ja_jp", "jbo_en", "ka_ge", "kab_kab", "kk_kz", "kn_in", "ko_kr", "ksh", "kw_gb", "la_la", "lb_lu", "li_li", "lol_us", "lt_lt", "lv_lv", "mi_nz", "mk_mk", "mn_mn", "moh_ca", "ms_my", "mt_mt", "nds_de", "nl_be", "nl_nl", "nn_no", "no_no", "nb_no", "nuk", "oc_fr", "oj_ca", "ovd", "pl_pl", "pt_br", "pt_pt", "qya_aa", "ro_ro", "rpr", "ru_ru", "scn", "se_no", "sk_sk", "sl_si", "so_so", "sq_al", "sr_sp", "sv_se", "swg", "sxu", "szl", "ta_in", "th_th", "tl_ph", "tlh_aa", "tr_tr", "tt_ru", "tzl_tzl", "uk_ua", "val_es", "vec_it", "vi_vn", "yi_de", "yo_ng", "zh_cn", "zh_hk", "zh_tw"};
    private final ICombatLogX mod;
    private final Class<T> clazz;
    private final ILanguageConfiguration config;
    private final String prefixPath;
    private final ConfigGetter<T, String> prefixValue;
    private final Map<UUID, String> localeMap = new HashMap<>();
    private final Map<String, Language<T>> languageMap = new HashMap<>();
    private final MiniMessage miniMessage;
    private String defaultLanguageName;
    private String consoleLanguageName;
    private boolean forceDefaultLanguage;
    private boolean usePlaceholderAPI;
    private boolean debugLanguage;
    private Language<T> defaultLanguage;
    private Language<T> consoleLanguage;

    public LanguageManager(@NotNull ICombatLogX mod, @NotNull Class<T> clazz, @NotNull ILanguageConfiguration config,
                           @NotNull String prefixPath, @NotNull ConfigGetter<T, String> prefixValue) {
        this.mod = mod;
        this.clazz = clazz;
        this.config = config;
        this.prefixPath = Validate.notEmpty(prefixPath, "prefixPath must not be empty!");
        this.prefixValue = prefixValue;
        MiniMessage.Builder builder = MiniMessage.builder();
        builder.strict(false);
        builder.debug(this::printMiniMessageDebug);
        this.miniMessage = builder.build();
        this.defaultLanguageName = config.defaultLocale();
        this.consoleLanguageName = config.consoleLocale();
        this.forceDefaultLanguage = config.enforceDefaultLocale();
        this.debugLanguage = config.debugMode();
    }

    public @NotNull ICombatLogX getCombatLogX() {
        return this.mod;
    }

    public @NotNull Logger getLogger() {
        ICombatLogX mod = this.getCombatLogX();
        return mod.getLogger();
    }

    public @NotNull MiniMessage getMiniMessage() {
        return this.miniMessage;
    }

    public @NotNull String getCachedLocale(@NotNull ServerPlayer player) {
        UUID playerId = player.getUUID();
        return this.localeMap.get(playerId);
    }

    public void setLocale(@NotNull ServerPlayer player, @NotNull String locale) {
        this.printDebug("Detected setLocale for player '" + VersionUtil.getName(player.getGameProfile()) + "' and locale '" + locale + "'.");
        UUID playerId = player.getUUID();
        this.localeMap.put(playerId, locale);
    }

    public void removeLocale(@NotNull ServerPlayer player) {
        this.printDebug("Detected removeLocale for player '" + VersionUtil.getName(player.getGameProfile()) + "'.");
        UUID playerId = player.getUUID();
        this.localeMap.remove(playerId);
    }

    public @Nullable Language<T> getDefaultLanguage() {
        if (this.defaultLanguage != null) {
            return this.defaultLanguage;
        } else if (this.defaultLanguageName == null) {
            Logger logger = this.getLogger();
            logger.warn("The default language name is not properly defined.");
            return null;
        } else {
            this.defaultLanguage = this.languageMap.get(this.defaultLanguageName);
            return this.defaultLanguage;
        }
    }

    public @Nullable Language<T> getConsoleLanguage() {
        if (this.forceDefaultLanguage) {
            return this.getDefaultLanguage();
        } else if (this.consoleLanguage != null) {
            return this.consoleLanguage;
        } else {
            Logger logger = this.getLogger();
            if (this.consoleLanguageName == null) {
                logger.warn("The console language name is not properly defined, using default.");
                return this.getDefaultLanguage();
            } else {
                this.consoleLanguage = this.getLanguage(this.consoleLanguageName);
                if (this.consoleLanguage == null) {
                    logger.warn("The console language name '{}' is not valid, using default.", this.consoleLanguageName);
                    return this.getDefaultLanguage();
                } else {
                    return this.consoleLanguage;
                }
            }
        }
    }

    public @Nullable Language<T> getLanguage(@Nullable String name) {
        this.printDebug("Detected getLanguage for name '" + name + "'...");
        Language<T> defaultLanguage = this.getDefaultLanguage();
        if (name == null || name.isEmpty()) {
            this.printDebug("Name is not valid, using default language.");
            return defaultLanguage;
        } else if (name.equals("default")) {
            this.printDebug("Using default language.");
            return defaultLanguage;
        }
        this.printDebug("Getting name from language map.");
        return this.languageMap.getOrDefault(name, defaultLanguage);
    }

    private @Nullable Language<T> getPlayerLanguage(@NotNull ServerPlayer player) {
        this.printDebug("Detected getPlayerLanguage for player '" + VersionUtil.getName(player.getGameProfile()) + "'.");
        String cachedLocale = this.getCachedLocale(player);
        this.printDebug("Cached Locale Name: " + cachedLocale);
        return this.getLanguage(cachedLocale);
    }

    // todo should prob also add a CommandSource variant
    public @Nullable Language<T> getLanguage(@Nullable CommandSourceStack sender) {
        if (this.forceDefaultLanguage) {
            return this.getDefaultLanguage();
        } else if (sender != null && sender.getEntity() != null) {
            ServerPlayer playerSender = sender.getPlayer();
            if (playerSender != null) {
                return this.getPlayerLanguage(playerSender);
            } else {
                return this.getDefaultLanguage();
            }
        } else {
            return this.getConsoleLanguage();
        }
    }

    public void onEnable() {
        this.printDebug("Detected onInitialize...");
        Identity id = Identity.of(MethodHandles.lookup());
        MainBus.BUS.register(new LanguageListener(mod, this), id);
    }

    public void loadDefaultLanguageFiles(Path configDir, String family, String languageDir) {
        Path langDir = configDir.resolve(family).resolve(languageDir);
        if (!Files.isDirectory(langDir) && Files.exists(langDir)) throw new IllegalStateException(langDir + "' is not a directory.");
        for (String languageName : KNOWN_LANGUAGE_ARRAY) {
            if (languageName.equals("en_us")) getLogger().info("Loading language files for en_us...");
            loadLanguageFile(configDir, family, languageDir, languageName);
        }
    }

    public void printDebug(@NotNull String message) {
        if (this.debugLanguage) {
            Logger logger = this.getLogger();
            logger.info("[Debug] [Language] {}", message);
        }
    }

    public void printMiniMessageDebug(@NotNull String message) {
        this.printDebug("[MiniMessage] " + message);
    }

    public void reloadLanguages() {
        this.reloadLanguageSettings();
        this.reloadLanguageFiles();
        int languageCount = this.languageMap.size();
        Logger logger = this.getLogger();
        logger.info("Successfully loaded {} language(s).", languageCount);
    }

    private boolean loadLanguageFile(Path configDir, String family, String languageDir, String languageName) {
        if (languageMap.containsKey(languageName)) return true; // already loaded
        Path path = configDir.resolve(family).resolve(languageDir).resolve(languageName + ".lang.toml"); // fixme kinda jank but works ig
        if (!Files.isRegularFile(path) && !languageName.equals(config.defaultLocale())) return false; // doesn't exist and not default locale; we don't want to create the file.
        T localeCfg = WrappedConfig.createToml(configDir, family + "/" + languageDir, languageName + ".lang", clazz);
        Language<T> language = new Language<>(languageName, localeCfg, getMiniMessage());
        this.languageMap.put(languageName, language);
        return true;
    }

    //todo
    private void reloadLanguageSettings() {
        this.defaultLanguageName = config.defaultLocale();
        this.consoleLanguageName = config.consoleLocale();
        this.forceDefaultLanguage = config.enforceDefaultLocale();
        this.debugLanguage = config.debugMode();
        this.usePlaceholderAPI = config.usePlaceholderAPI() && IPlatformHelper.getInstance().isModLoaded("placeholder-api");
    }

    private void reloadLanguageFiles() {
        this.defaultLanguage = null;
        this.consoleLanguage = null;

        this.config.reload();
        for (Map.Entry<String, Language<T>> entry : this.languageMap.entrySet()) {
            Language<T> language = entry.getValue();
            T localeCfg = language.getConfiguration();
            CombatLogX.reload(localeCfg);
            this.reloadLanguage(language);
        }
    }

    private void reloadLanguage(@NotNull Language<T> language) {
        MiniMessage miniMessage = this.getMiniMessage();
        String decimalFormatString = language.getConfiguration().decimalFormat();
        if (!decimalFormatString.isEmpty()) {
            DecimalFormat decimalFormat = new DecimalFormat(decimalFormatString);
            language.setDecimalFormat(decimalFormat);
        }
    }

    // todo should this deserialize into minimessage if the replacer doesn't
    private @NotNull String replacePlaceholderAPI(@Nullable CommandSourceStack audience, @NotNull String message) {
        if (message.isEmpty()) return "";
        MiniMessage miniMessage = getMiniMessage();
        Component component = replacePlaceholderAPI(audience, miniMessage.deserialize(message));
        return miniMessage.serialize(component);
    }

    private @NotNull Component replacePlaceholderAPI(@Nullable CommandSourceStack audience, @NotNull Component message) {
        if (audience == null) return message;
        ServerPlayer player = audience.getPlayer();
        if (player == null) return message;

        // MinecraftServerAudiences audiences = CombatLogX.createAudiences(player);
        return IAdventure.getInstance().asAdventure(IPlaceholderAPI.getInstance().format(player, IAdventure.getInstance().asNative(message)));
    }

    public @Nullable T getLanguageConfig(@Nullable CommandSourceStack audience) {
        Language<T> language = this.getLanguage(audience);
        if (language == null) {
            Logger logger = this.getLogger();
            logger.warn("There are no languages available.");
            return null;
        }
        return language.getConfiguration();
    }

    // fallback typically the langauge key. e.g. if it's located at placeholder -> status -> idle would typically be
    //  MOD_NAME.placeholder.status.idle (these are the serialized names)

    public @NotNull String getMessageRaw(@Nullable CommandSourceStack audience, @NotNull String path, @NotNull ConfigGetter<T, String> value) {
        Validate.notEmpty(path, "path must not be empty!");
        Language<T> language = this.getLanguage(audience);
        if (language == null) {
            Logger logger = this.getLogger();
            logger.warn("There are no languages available.");
            return String.format(Locale.US, "{%s}", path);
        }
        return language.getRawMessage(path, value);
    }

    public @NotNull String getMessageString(@Nullable CommandSourceStack audience, @NotNull String path, @NotNull ConfigGetter<T, String> value, @NotNull Replacer... replacers) {
        String message = this.getMessageRaw(audience, path, value);
        if (this.usePlaceholderAPI) message = replacePlaceholderAPI(audience, message);

        for (Replacer replacer : replacers) {
            String target = replacer.getTarget();
            String replacement = replacer.getReplacementString();
            message = message.replace(target, replacement);
        }

        return message;
    }
    public @NotNull List<Component> getMessageList(@Nullable CommandSourceStack audience, @NotNull String path, @NotNull ConfigGetter<T, List<String>> value, @NotNull Replacer... replacers) {
        Validate.notEmpty(path, "path must not be empty!");
        Language<T> language = this.getLanguage(audience);
        if (language == null) {
            Logger logger = this.getLogger();
            logger.warn("There are no languages available.");
            return Collections.emptyList(); // todo maybe want to put sm text here?
        } else {
            List<Component> messages = language.getMessageList(path, value);
            if (this.usePlaceholderAPI) {
                List<Component> newMessages = new ArrayList<>();

                for (Component message : messages) {
                    message = this.replacePlaceholderAPI(audience, message);
                    newMessages.add(message);
                }

                messages = newMessages;
            }

            for (Replacer replacer : replacers) {
                List<Component> newMessages = new ArrayList<>();
                TextReplacementConfig replacementConfig = replacer.asReplacementConfig();

                for(Component message : messages) {
                    message = message.replaceText(replacementConfig);
                    newMessages.add(message);
                }

                messages = newMessages;
            }

            return messages;
        }
    }

    public @NotNull Component getMessage(@Nullable CommandSourceStack audience, @NotNull String path, @NotNull ConfigGetter<T, String> value, @NotNull Replacer... replacers) {
        Validate.notEmpty(path, "path must not be empty!");
        Language<T> language = this.getLanguage(audience);
        if (language == null) {
            Logger logger = this.getLogger();
            logger.warn("There are no languages available.");
            return Component.text(String.format(Locale.US, "{%s}", path));
        }
        Component message = language.getMessage(path, value);
        if (this.usePlaceholderAPI) message = replacePlaceholderAPI(audience, message);

        for (Replacer replacer : replacers) {
            TextReplacementConfig replacementConfig = replacer.asReplacementConfig();
            message = message.replaceText(replacementConfig);
        }

        return message;
    }

    public @NotNull Component getMessageWithPrefix(@Nullable CommandSourceStack audience, @NotNull String path, @NotNull ConfigGetter<T, String> value, @NotNull Replacer... replacers) {
        Component message = this.getMessage(audience, path, value, replacers);
        if (Component.empty().equals(message)) return Component.empty();

        Component prefix = this.getMessage(audience, this.prefixPath, this.prefixValue, replacers);
        if (Component.empty().equals(prefix)) return message;

        TextComponent.Builder builder = Component.text();
        builder.append(prefix);
        builder.append(Component.space());
        builder.append(message);
        return builder.build();
    }

    public void sendMessage(@NotNull CommandSourceStack audience, @NotNull String path, @NotNull ConfigGetter<T, String> value, @NotNull Replacer... replacers) {
        Component message = this.getMessage(audience, path, value, replacers);
        this.sendMessage(audience, message);
    }

    public void sendMessage(@NotNull CommandSourceStack audience, @NotNull Component message) {
        if (!Component.empty().equals(message)) ((ForwardingAudience.Single) audience).sendMessage(message);
    }

    public void sendMessageWithPrefix(@NotNull CommandSourceStack audience, @NotNull String path, @NotNull ConfigGetter<T, String> value, @NotNull Replacer... replacers) {
        Component message = this.getMessageWithPrefix(audience, path, value, replacers);
        this.sendMessage(audience, message);
    }

    public void sendActionBar(@NotNull CommandSourceStack audience, @NotNull String path, @NotNull ConfigGetter<T, String> value) {
        Component message = this.getMessage(audience, path, value);
        this.sendMessage(audience, message);
    }

    public void sendActionBar(@NotNull CommandSourceStack audience, @NotNull Component message) {
        if (!Component.empty().equals(message)) ((ForwardingAudience.Single) audience).sendActionBar(message);
    }

    public void broadcastMessage(@NotNull MinecraftServer server, @NotNull String path, @NotNull ConfigGetter<T, String> value, @Nullable PermissionNode<Boolean> permission, @NotNull Replacer... replacers) {
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        this.broadcastMessage(server, players, path, value, permission, replacers);
    }

    public void broadcastMessage(@NotNull MinecraftServer server, @NotNull Iterable<? extends ServerPlayer> players, @NotNull String path, @NotNull ConfigGetter<T, String> value, @Nullable PermissionNode<Boolean> permission, @NotNull Replacer... replacers) {
        this.sendMessage(server.createCommandSourceStack(), path, value, replacers);

        for (ServerPlayer player : players) {
            if (this.hasPermission(player, permission)) {
                this.sendMessage(player.createCommandSourceStack(), path, value, replacers);
            }
        }
    }

    private boolean hasPermission(@NotNull ServerPlayer player, @Nullable PermissionNode<Boolean> permission) {
        return permission == null || permission.resolveFallback(player);
    }

    public @NotNull DecimalFormat getDecimalFormat(@Nullable CommandSourceStack source) {
        Language<T> language = this.getLanguage(source);
        if (language != null) return language.getDecimalFormat();

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        return new DecimalFormat("0.00", symbols);
    }
}
