package net.frankheijden.serverutils.bungee.commands;

import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import net.frankheijden.serverutils.bungee.entities.BungeeAudience;
import net.frankheijden.serverutils.bungee.entities.BungeePlugin;
import net.frankheijden.serverutils.bungee.entities.BungeePluginDescription;
import net.frankheijden.serverutils.common.commands.CommandPlugins;
import net.md_5.bungee.api.plugin.Plugin;

@SuppressWarnings("LineLength")
public class BungeeCommandPlugins extends CommandPlugins<BungeePlugin, Plugin, BungeeAudience, BungeePluginDescription> {

    public BungeeCommandPlugins(BungeePlugin plugin) {
        super(plugin);
    }

    @Override
    protected void register(
            CommandManager<BungeeAudience> manager,
            Command.Builder<BungeeAudience> builder
    ) {
        manager.command(builder
                .flag(parseFlag("version"))
                .flag(parseFlag("modules"))
                .handler(this::handlePlugins));
    }

    @Override
    protected void handlePlugins(CommandContext<BungeeAudience> context) {
        BungeeAudience sender = context.sender();
        boolean hasVersionFlag = context.flags().contains("version");
        boolean hasModulesFlag = context.flags().contains("modules");

        handlePlugins(sender, plugin.getPluginManager().getPluginsSorted(hasModulesFlag), hasVersionFlag);
    }
}
