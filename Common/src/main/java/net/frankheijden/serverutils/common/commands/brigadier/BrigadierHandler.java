package net.frankheijden.serverutils.common.commands.brigadier;

import net.frankheijden.serverutils.common.commands.arguments.JarFilesParser;
import net.frankheijden.serverutils.common.commands.arguments.PluginsParser;
import org.incendo.cloud.brigadier.CloudBrigadierManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.leangen.geantyref.TypeToken;
import net.frankheijden.serverutils.common.entities.ServerUtilsAudience;

public class BrigadierHandler<C extends ServerUtilsAudience<?>, P> {

    private final CloudBrigadierManager<C, ?> brigadierManager;

    public BrigadierHandler(CloudBrigadierManager<C, ?> brigadierManager) {
        this.brigadierManager = brigadierManager;
    }

    /**
     * Registers types with the cloud brigadier manager.
     */
    public void registerTypes() {
        brigadierManager.registerMapping(
                new TypeToken<JarFilesParser<C>>() {},
                builder -> builder
                        .cloudSuggestions()
                        .toConstant(StringArgumentType.greedyString())
        );
        brigadierManager.registerMapping(
                new TypeToken<PluginsParser<C, P>>() {},
                builder -> builder
                        .cloudSuggestions()
                        .toConstant(StringArgumentType.greedyString())
        );
    }
}
