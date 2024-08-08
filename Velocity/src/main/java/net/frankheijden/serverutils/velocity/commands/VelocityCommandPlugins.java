package net.frankheijden.serverutils.velocity.commands;

import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import com.velocitypowered.api.plugin.PluginContainer;
import net.frankheijden.serverutils.common.commands.CommandPlugins;
import net.frankheijden.serverutils.velocity.entities.VelocityAudience;
import net.frankheijden.serverutils.velocity.entities.VelocityPlugin;
import net.frankheijden.serverutils.velocity.entities.VelocityPluginDescription;

@SuppressWarnings("LineLength")
public class VelocityCommandPlugins extends CommandPlugins<VelocityPlugin, PluginContainer, VelocityAudience, VelocityPluginDescription> {

    public VelocityCommandPlugins(VelocityPlugin plugin) {
        super(plugin);
    }

    @Override
    protected void register(
            CommandManager<VelocityAudience> manager,
            Command.Builder<VelocityAudience> builder
    ) {
        manager.command(builder
                .flag(parseFlag("version"))
                .handler(this::handlePlugins));
    }

    @Override
    protected void handlePlugins(CommandContext<VelocityAudience> context) {
        VelocityAudience sender = context.sender();
        boolean hasVersionFlag = context.flags().contains("version");

        handlePlugins(sender, plugin.getPluginManager().getPluginsSorted(), hasVersionFlag);
    }
}
