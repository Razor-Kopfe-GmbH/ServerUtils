package net.frankheijden.serverutils.common.entities;

import net.frankheijden.serverutils.common.ServerUtilsApp;
import net.frankheijden.serverutils.common.commands.brigadier.BrigadierHandler;
import net.frankheijden.serverutils.common.config.CommandsResource;
import net.frankheijden.serverutils.common.config.ConfigResource;
import net.frankheijden.serverutils.common.config.MessageKey;
import net.frankheijden.serverutils.common.config.MessagesResource;
import net.frankheijden.serverutils.common.entities.results.CloseablePluginResults;
import net.frankheijden.serverutils.common.entities.results.PluginResults;
import net.frankheijden.serverutils.common.managers.AbstractPluginManager;
import net.frankheijden.serverutils.common.managers.AbstractTaskManager;
import net.frankheijden.serverutils.common.managers.UpdateManager;
import net.frankheijden.serverutils.common.managers.WatchManager;
import net.frankheijden.serverutils.common.providers.ResourceProvider;
import net.frankheijden.serverutils.common.providers.ServerUtilsAudienceProvider;
import net.frankheijden.serverutils.common.utils.FileUtils;
import org.incendo.cloud.CloudCapability;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.brigadier.CloudBrigadierManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public abstract class ServerUtilsPlugin<P, T, C extends ServerUtilsAudience<S>, S, D extends ServerUtilsPluginDescription> {

    private final UpdateManager updateManager = new UpdateManager();
    private final WatchManager<P, T> watchManager = new WatchManager<>(this);
    private CommandsResource commandsResource;
    private ConfigResource configResource;
    protected MessagesResource messagesResource;
    protected CommandManager<C> commandManager;

    public abstract Platform getPlatform();

    public abstract P getPlugin();

    public CommandsResource getCommandsResource() {
        return commandsResource;
    }

    public ConfigResource getConfigResource() {
        return configResource;
    }

    public MessagesResource getMessagesResource() {
        return messagesResource;
    }

    public abstract AbstractPluginManager<P, D> getPluginManager();

    public abstract AbstractTaskManager<T> getTaskManager();

    public abstract ResourceProvider getResourceProvider();

    public abstract ServerUtilsAudienceProvider<S> getChatProvider();

    public UpdateManager getUpdateManager() {
        return updateManager;
    }

    public WatchManager<P, T> getWatchManager() {
        return watchManager;
    }

    public abstract Logger getLogger();

    public abstract File getDataFolder();

    public Collection<Command<C>> getCommands() {
        return commandManager.commands();
    }

    public void createDataFolderIfNotExists() {
        if (getDataFolder().exists()) return;
        getDataFolder().mkdirs();
    }

    /**
     * Copies a resource from the jar to the specified target file name under the datafolder.
     * @param targetName The target file under the datafolder.
     * @param resource The resource from the jar file to copy.
     * @return The target file.
     */
    public File copyResourceIfNotExists(String targetName, String resource) {
        createDataFolderIfNotExists();

        File file = new File(getDataFolder(), targetName);
        if (!file.exists()) {
            getLogger().info("'" + targetName + "' not found, creating!");
            try {
                FileUtils.saveResource(getResourceProvider().getResource(resource), file);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return file;
    }

    private void unloadConfiguredPlugins() {
        List<String> pluginIds = configResource.getConfig().getStringList("unload-after-startup.plugins");
        List<P> plugins = new ArrayList<>(pluginIds.size());
        for (String pluginId : pluginIds) {
            Optional<P> pluginOptional = getPluginManager().getPlugin(pluginId);
            if (!pluginOptional.isPresent()) {
                getLogger().warning(
                        "Plugin '" + pluginId + "' defined in config.yml 'unload-after-startup' is not loaded!"
                );
                continue;
            }
            plugins.add(pluginOptional.get());
        }

        if (plugins.isEmpty()) return;

        PluginResults<P> disableResults = getPluginManager().disablePlugins(plugins);
        if (!disableResults.isSuccess()) {
            disableResults.sendTo(getChatProvider().getConsoleServerAudience(), null);
            return;
        }

        CloseablePluginResults<P> unloadResults = getPluginManager().unloadPlugins(plugins);
        unloadResults.tryClose();
        unloadResults.sendTo(getChatProvider().getConsoleServerAudience(), MessageKey.UNLOADPLUGIN);
    }

    protected abstract CommandManager<C> newCommandManager();

    protected abstract void registerCommands(CommandManager<C> commandManager);

    protected void handleBrigadier(CloudBrigadierManager<C, ?> brigadierManager) {
        BrigadierHandler<C, P> handler = new BrigadierHandler<>(brigadierManager);
        handler.registerTypes();
    }

    /**
     * Enables the plugin.
     */
    public final void enable() {
        Path dataFolder = getDataFolder().toPath();
        if (Files.notExists(dataFolder)) {
            try {
                Files.createDirectories(dataFolder);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        reload();
        enablePlugin();
        getTaskManager().runTaskLater(
                this::unloadConfiguredPlugins,
                configResource.getConfig().getInt("unload-after-startup.delay-ticks")
        );
        ServerUtilsApp.tryCheckForUpdates();
        ServerUtilsApp.unloadServerUtilsUpdater();
    }

    protected void enablePlugin() {

    }

    public final void disable() {
        disablePlugin();
        getTaskManager().cancelAllTasks();
    }

    protected void disablePlugin() {

    }

    /**
     * Reloads the plugin's configurations.
     */
    public final void reload() {
        this.commandsResource = new CommandsResource(this);
        this.configResource = new ConfigResource(this);
        this.messagesResource = new MessagesResource(this);
        this.messagesResource.load(Arrays.asList(MessageKey.values()));
        if (this.commandManager == null) {
            this.commandManager = newCommandManager();
            registerCommands(this.commandManager);
        }
        reloadPlugin();
    }

    protected void reloadPlugin() {

    }

    public enum Platform {
        BUKKIT,
        BUNGEE,
        VELOCITY,
    }
}
