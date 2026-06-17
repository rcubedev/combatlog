package com.github.sirblobman.combatlogx.api.language;

public interface ILanguageConfiguration {

    void reload();
    boolean debugMode();
    boolean enforceDefaultLocale();
    String defaultLocale();
    String consoleLocale();
    boolean usePlaceholderAPI();
}
