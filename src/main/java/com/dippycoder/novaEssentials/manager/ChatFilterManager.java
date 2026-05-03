package com.dippycoder.novaEssentials.manager;

import com.dippycoder.novaEssentials.NovaEssentials;

import java.util.List;
import java.util.regex.Pattern;

public class ChatFilterManager {

    private final NovaEssentials plugin;
    private Pattern pattern;
    private String replacement;

    public ChatFilterManager(NovaEssentials plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        List<String> words = plugin.getConfigManager().getBlockedWords();
        replacement = plugin.getConfigManager().getFilterReplacement();
        if (words.isEmpty()) {
            pattern = null;
            return;
        }
        // Case-insensitive word boundary match
        StringBuilder sb = new StringBuilder("(?i)(");
        for (int i = 0; i < words.size(); i++) {
            if (i > 0) sb.append('|');
            sb.append(Pattern.quote(words.get(i)));
        }
        sb.append(')');
        pattern = Pattern.compile(sb.toString());
    }

    /**
     * Returns true if the message contains any filtered words.
     * Does not modify the message; use {@link #filter(String)} for that.
     */
    public boolean contains(String message) {
        if (!plugin.getConfigManager().isChatFilterEnabled() || pattern == null) return false;
        return pattern.matcher(message).find();
    }

    /** Replace all filtered words with the replacement string. */
    public String filter(String message) {
        if (!plugin.getConfigManager().isChatFilterEnabled() || pattern == null) return message;
        return pattern.matcher(message).replaceAll(replacement);
    }
}
