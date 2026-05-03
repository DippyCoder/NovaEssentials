package com.dippycoder.novaEssentials.util;

public final class DurationUtil {

    private DurationUtil() {}

    /**
     * Parses a duration string like "30s", "10m", "2h", "7d", "1w" into milliseconds.
     * Returns -1 for "perm" / "permanent", or throws NumberFormatException on bad input.
     */
    public static long parseMillis(String input) {
        if (input == null) throw new NumberFormatException("null");
        String s = input.trim().toLowerCase();
        if (s.equals("perm") || s.equals("permanent")) return -1L;
        if (s.isEmpty()) throw new NumberFormatException("empty");

        char unit = s.charAt(s.length() - 1);
        String num = s.substring(0, s.length() - 1);
        long amount;
        try {
            amount = Long.parseLong(num);
        } catch (NumberFormatException e) {
            // Maybe no unit given — treat as seconds
            amount = Long.parseLong(s);
            return amount * 1000L;
        }
        return switch (unit) {
            case 's' -> amount * 1_000L;
            case 'm' -> amount * 60_000L;
            case 'h' -> amount * 3_600_000L;
            case 'd' -> amount * 86_400_000L;
            case 'w' -> amount * 604_800_000L;
            default  -> Long.parseLong(s) * 1_000L;
        };
    }

    /** Formats a remaining-time millisecond value into a human-readable string. */
    public static String format(long millis) {
        if (millis < 0) return "permanent";
        long secs  = millis / 1_000;
        long mins  = secs  / 60;
        long hours = mins  / 60;
        long days  = hours / 24;

        if (days > 0)        return days  + "d " + (hours % 24) + "h";
        if (hours > 0)       return hours + "h " + (mins % 60)  + "m";
        if (mins > 0)        return mins  + "m " + (secs % 60)  + "s";
        return secs + "s";
    }
}
