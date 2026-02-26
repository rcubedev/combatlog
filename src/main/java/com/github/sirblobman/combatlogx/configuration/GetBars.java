package com.github.sirblobman.combatlogx.config;

import static com.github.sirblobman.combatlogx.CombatLogX.config;

public class GetBars {
    private GetBars() {}

    public static String getBars(long timeLeftMillis) {
        double timerMax = Math.round(config.combatLog.combatTimeout * 1000);
        int totalBars = config.combatLog.barCount;
        String leftSymbol = config.combatLog.barLeftSymbol;
        String rightSymbol = config.combatLog.barRightSymbol;

        double percent = Math.clamp(timeLeftMillis / timerMax, 0.0D, 1.0D); // java 21
        long leftBarsCount = (long) Math.ceil((double) totalBars * percent);
        long rightBarsCount = totalBars - leftBarsCount;

        StringBuilder builder = new StringBuilder();

        for (long i = 0; i < leftBarsCount; i++) {
            builder.append(leftSymbol);
        }

        for (long i = 0; i < rightBarsCount; i++) {
            builder.append(rightSymbol);
        }

        return builder.toString();
    }
}
