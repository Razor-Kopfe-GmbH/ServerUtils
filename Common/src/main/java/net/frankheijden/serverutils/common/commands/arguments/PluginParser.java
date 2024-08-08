package net.frankheijden.serverutils.common.commands.arguments;

import net.frankheijden.serverutils.common.entities.ServerUtilsAudience;
import net.frankheijden.serverutils.common.entities.ServerUtilsPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;

import java.util.Optional;

public class PluginParser<C extends ServerUtilsAudience<?>, P> implements ArgumentParser<C, P>, BlockingSuggestionProvider.Strings<C> {

    private final ServerUtilsPlugin<P, ?, C, ?, ?> plugin;

    public PluginParser(ServerUtilsPlugin<P, ?, C, ?, ?> plugin) {
        this.plugin = plugin;
    }

    public static <C extends ServerUtilsAudience<?>, P> ParserDescriptor<C, P> pluginParser(
            ServerUtilsPlugin<P, ?, C, ?, ?> plugin,
            Class<P> pluginType
    ) {
        return ParserDescriptor.of(new PluginParser<>(plugin), pluginType);
    }

    @Override
    @NonNull
    public ArgumentParseResult<P> parse(@NonNull CommandContext<C> context, @NonNull CommandInput commandInput) {
        if (!commandInput.hasRemainingInput()) {
            return ArgumentParseResult.failure(new IllegalArgumentException("Missing input for plugin!"));
        }
        String pluginName = commandInput.peekString();
        Optional<P> pluginOptional = plugin.getPluginManager().getPlugin(pluginName);
        if (pluginOptional.isEmpty()) {
            return ArgumentParseResult.failure(new IllegalArgumentException("Plugin '" + pluginName + "' does not exist!"));
        }

        commandInput.readString();
        return ArgumentParseResult.success(pluginOptional.get());
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(@NonNull CommandContext<C> commandContext,
                                                                @NonNull CommandInput input) {
        return this.plugin.getPluginManager().getPluginNames();
    }
}
