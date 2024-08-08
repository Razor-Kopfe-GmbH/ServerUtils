package net.frankheijden.serverutils.velocity.entities;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import net.frankheijden.serverutils.common.entities.ServerUtilsPlugin;
import net.frankheijden.serverutils.velocity.ServerUtils;
import net.frankheijden.serverutils.velocity.commands.VelocityCommandPlugins;
import net.frankheijden.serverutils.velocity.commands.VelocityCommandServerUtils;
import net.frankheijden.serverutils.velocity.listeners.VelocityPlayerListener;
import net.frankheijden.serverutils.velocity.managers.VelocityPluginManager;
import net.frankheijden.serverutils.velocity.managers.VelocityTaskManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.velocity.VelocityCommandManager;

import java.io.File;
import java.util.logging.Logger;

public class VelocityPlugin extends ServerUtilsPlugin<PluginContainer, ScheduledTask, VelocityAudience, CommandSource, VelocityPluginDescription> {

    private final ServerUtils plugin;
    private final VelocityPluginManager pluginManager;
    private final VelocityTaskManager taskManager;
    private final VelocityResourceProvider resourceProvider;
    private final VelocityAudienceProvider chatProvider;

    /**
     * Creates a new BungeePlugin instance of ServerUtils.
     * @param plugin The ServerUtils plugin.
     */
    public VelocityPlugin(ServerUtils plugin) {
        this.plugin = plugin;
        this.pluginManager = new VelocityPluginManager(
                plugin.getProxy(),
                plugin.getLogger(),
                plugin.getPluginCommandManager()
        );
        this.taskManager = new VelocityTaskManager(plugin);
        this.resourceProvider = new VelocityResourceProvider(plugin);
        this.chatProvider = new VelocityAudienceProvider(plugin);
    }

    @Override
    protected VelocityCommandManager<VelocityAudience> newCommandManager() {
        VelocityCommandManager<VelocityAudience> commandManager = new VelocityCommandManager<>(
                plugin.getPluginContainer(),
                plugin.getProxy(),
                ExecutionCoordinator.asyncCoordinator(),
                SenderMapper.create(chatProvider::get, VelocityAudience::getSource)
        );
        handleBrigadier(commandManager.brigadierManager());
        return commandManager;
    }

    @Override
    public VelocityPluginManager getPluginManager() {
        return this.pluginManager;
    }

    @Override
    public VelocityTaskManager getTaskManager() {
        return this.taskManager;
    }

    @Override
    public Platform getPlatform() {
        return Platform.VELOCITY;
    }

    @Override
    public PluginContainer getPlugin() {
        return plugin.getPluginContainer();
    }

    @Override
    public VelocityResourceProvider getResourceProvider() {
        return this.resourceProvider;
    }

    @Override
    public VelocityAudienceProvider getChatProvider() {
        return this.chatProvider;
    }

    @Override
    public Logger getLogger() {
        return Logger.getLogger(plugin.getLogger().getName());
    }

    @Override
    public File getDataFolder() {
        return this.plugin.getDataDirectory().toFile();
    }

    @Override
    protected void enablePlugin() {
        plugin.getProxy().getEventManager().register(plugin, new VelocityPlayerListener(this));
    }

    @Override
    protected void reloadPlugin() {
        new VelocityCommandPlugins(this).register(commandManager);
        new VelocityCommandServerUtils(this).register(commandManager);
    }
}
