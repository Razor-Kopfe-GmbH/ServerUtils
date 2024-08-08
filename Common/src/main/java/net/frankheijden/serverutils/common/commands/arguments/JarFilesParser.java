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

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JarFilesParser<C extends ServerUtilsAudience<?>> implements ArgumentParser<C, File[]>, BlockingSuggestionProvider.Strings<C> {

    private static final File[] EMPTY = new File[0];


    private final ServerUtilsPlugin<?, ?, C, ?, ?> plugin;

    public JarFilesParser(ServerUtilsPlugin<?, ?, C, ?, ?> plugin) {
        this.plugin = plugin;
    }

    public static <C extends ServerUtilsAudience<?>> ParserDescriptor<C, File[]> jarFilesParser(ServerUtilsPlugin<?, ?, C, ?, ?> plugin) {
        return ParserDescriptor.of(new JarFilesParser<>(plugin), File[].class);
    }


    @Override
    public @NonNull ArgumentParseResult<File @NonNull []> parse(@NonNull CommandContext<@NonNull C> commandContext,
                                                                @NonNull CommandInput commandInput) {
        Set<String> pluginFiles = new HashSet<>(plugin.getPluginManager().getPluginFileNames());
        List<File> files = new ArrayList<>();
        while (!commandInput.isEmpty()) {
            String input = commandInput.peekString();
            if (input.isEmpty()) {
                return ArgumentParseResult.success(EMPTY);
            }
            StringBuilder builder = new StringBuilder(commandInput.peekString());

            final String pluginFileName;
            if (builder.charAt(0) == '"') {
                while (commandInput.hasRemainingInput()) {
                    if (builder.length() > 1 && builder.charAt(builder.length() - 1) == '"') {
                        break;
                    }
                    builder.append(" ").append(commandInput.readString());
                }

                if (builder.charAt(builder.length() - 1) != '"') {
                    return ArgumentParseResult.failure(new IllegalArgumentException(
                            "Could not find closing '\"' character"
                    ));
                }
                pluginFileName = builder.substring(1, builder.length() - 1);
            } else {
                if (!builder.isEmpty()) {
                    int lastChar;
                    while (builder.charAt((lastChar = builder.length() - 1)) == '\\' && commandInput.hasRemainingInput()) {
                        builder.setCharAt(lastChar, ' ');
                        builder.append(commandInput.readString());
                    }
                }
                pluginFileName = builder.toString();
            }

            if (!pluginFiles.contains(pluginFileName)) {
                return ArgumentParseResult.failure(new IllegalArgumentException(
                        "Plugin '" + pluginFileName + "' does not exist!"
                ));
            }
            files.add(new File(plugin.getPluginManager().getPluginsFolder(), pluginFileName));
        }

        return ArgumentParseResult.success(files.toArray(File[]::new));
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(@NonNull CommandContext<C> commandContext,
                                                                @NonNull CommandInput input) {
        return this.plugin.getPluginManager().getPluginFileNames();
    }


}
