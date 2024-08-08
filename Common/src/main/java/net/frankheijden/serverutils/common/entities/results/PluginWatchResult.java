package net.frankheijden.serverutils.common.entities.results;

import net.frankheijden.serverutils.common.config.ConfigKey;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class PluginWatchResult implements AbstractResult {

    private final WatchResult result;
    private final TagResolver[] placeholders;

    public PluginWatchResult(WatchResult result, TagResolver... placeholders) {
        this.result = result;
        this.placeholders = placeholders;
    }

    public WatchResult getResult() {
        return result;
    }

    public TagResolver[] getPlaceholders() {
        return placeholders;
    }

    @Override
    public ConfigKey getKey() {
        return result.getKey();
    }
}
