package com.github.sirblobman.combatlogx.configuration;

public class GetBars {
    private GetBars() {}

    public static String getBars(long timeLeftMillis) {
        // MainConfiguration config = mod.getConfiguration();
        // double timerMax = config.timer.defaultTimer * 1000; // todo this aint right but wtv
        // int totalBars = config.combatLog.barCount;
        // String leftSymbol = config.combatLog.barLeftSymbol;
        // String rightSymbol = config.combatLog.barRightSymbol;
        //
        // double percent = Math.clamp(timeLeftMillis / timerMax, 0.0D, 1.0D); // java 21
        // long leftBarsCount = (long) Math.ceil((double) totalBars * percent);
        // long rightBarsCount = totalBars - leftBarsCount;
        //
        // StringBuilder builder = new StringBuilder();
        //
        // for (long i = 0; i < leftBarsCount; i++) {
        //     builder.append(leftSymbol);
        // }
        //
        // for (long i = 0; i < rightBarsCount; i++) {
        //     builder.append(rightSymbol);
        // }
        //
        // return builder.toString();
        return "";
    }
}
