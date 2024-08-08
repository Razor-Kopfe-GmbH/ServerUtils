package net.frankheijden.serverutils.bukkit.commands;

import net.frankheijden.serverutils.bukkit.config.BukkitMessageKey;
import net.frankheijden.serverutils.bukkit.entities.BukkitAudience;
import net.frankheijden.serverutils.bukkit.entities.BukkitPlugin;
import net.frankheijden.serverutils.bukkit.managers.BukkitPluginManager;
import net.frankheijden.serverutils.bukkit.reflection.RCraftServer;
import net.frankheijden.serverutils.bukkit.reflection.RDedicatedServer;
import net.frankheijden.serverutils.bukkit.utils.ReloadHandler;
import net.frankheijden.serverutils.bukkit.utils.VersionReloadHandler;
import net.frankheijden.serverutils.bukkit.utils.version.MinecraftVersions;
import net.frankheijden.serverutils.bukkit.utils.version.VersionUtil;
import net.frankheijden.serverutils.common.commands.CommandServerUtils;
import net.frankheijden.serverutils.common.config.MessageKey;
import net.frankheijden.serverutils.common.config.MessagesResource;
import net.frankheijden.serverutils.common.entities.results.PluginResults;
import net.frankheijden.serverutils.common.utils.ForwardFilter;
import net.frankheijden.serverutils.common.utils.KeyValueComponentBuilder;
import net.frankheijden.serverutils.common.utils.ListComponentBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BukkitCommandServerUtils extends CommandServerUtils<BukkitPlugin, Plugin, BukkitAudience> {

    private static final Map<String, ReloadHandler> supportedConfigs;

    static {
        supportedConfigs = new HashMap<>();
        supportedConfigs.put("bukkit", new VersionReloadHandler(16, RCraftServer::reloadBukkitConfiguration));
        supportedConfigs.put("commands.yml", RCraftServer::reloadCommandsConfiguration);
        supportedConfigs.put("server-icon.png", new VersionReloadHandler(16, RCraftServer::loadIcon));
        supportedConfigs.put("banned-ips.json", new VersionReloadHandler(16, RCraftServer::reloadIpBans));
        supportedConfigs.put("banned-players.json", new VersionReloadHandler(16, RCraftServer::reloadProfileBans));
        supportedConfigs.put("server.properties", new VersionReloadHandler(
                16,
                RDedicatedServer::reloadServerProperties
        ));
    }

    public BukkitCommandServerUtils(BukkitPlugin plugin) {
        super(plugin, Plugin[]::new, Plugin.class);
    }

    @Override
    public void register(
            CommandManager<BukkitAudience> manager,
            org.incendo.cloud.Command.Builder<BukkitAudience> builder
    ) {
        super.register(manager, builder);

        final List<String> supportedConfigNames = supportedConfigs.entrySet().stream()
                .filter(r -> {
                    if (r instanceof VersionReloadHandler) {
                        VersionReloadHandler reloadHandler = ((VersionReloadHandler) r);
                        return reloadHandler.supportsVersion(MinecraftVersions.CURRENT);
                    }
                    return true;
                })
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        CommandComponent<BukkitAudience> configComponent = CommandComponent.<BukkitAudience, String>builder()
                .name("config")
                .suggestionProvider((BlockingSuggestionProvider.Strings<BukkitAudience>) (context, s) -> supportedConfigNames)
                .valueType(String.class)
                .build();
        addComponent(configComponent);

        registerSubcommand(manager, builder, "enableplugin", subcommandBuilder -> subcommandBuilder
                .argument(getComponent("plugins"))
                .handler(this::handleEnablePlugin));
        registerSubcommand(manager, builder, "disableplugin", subcommandBuilder -> subcommandBuilder
                .required("plugins", pluginsParser(getRawPath("disableplugin")))
                .handler(this::handleDisablePlugin));
        registerSubcommand(manager, builder, "reloadconfig", subcommandBuilder -> subcommandBuilder
                .argument(getComponent("config"))
                .handler(this::handleReloadConfig));
    }

    private void handleEnablePlugin(CommandContext<BukkitAudience> context) {
        BukkitAudience sender = context.sender();
        List<Plugin> plugins = Arrays.asList(context.get("plugins"));

        PluginResults<Plugin> enableResults = plugin.getPluginManager().enablePlugins(plugins);
        enableResults.sendTo(sender, BukkitMessageKey.ENABLEPLUGIN);
    }

    private void handleDisablePlugin(CommandContext<BukkitAudience> context) {
        BukkitAudience sender = context.sender();
        List<Plugin> plugins = Arrays.asList(context.get("plugins"));

        if (checkProtectedPlugins(sender, plugins)) {
            return;
        }

        if (checkDependingPlugins(context, sender, plugins, "disableplugin")) {
            return;
        }

        PluginResults<Plugin> disableResults = plugin.getPluginManager().disablePlugins(plugins);
        disableResults.sendTo(sender, BukkitMessageKey.DISABLEPLUGIN);
    }

    private void handleReloadConfig(CommandContext<BukkitAudience> context) {
        BukkitAudience sender = context.sender();
        String config = context.get("config");

        MessagesResource messages = plugin.getMessagesResource();
        ReloadHandler handler = supportedConfigs.get(config);
        if (handler == null) {
            messages.get(BukkitMessageKey.RELOADCONFIG_NOT_EXISTS).sendTo(
                    sender,
                    Placeholder.unparsed("config", config)
            );
            return;
        }

        if (handler instanceof VersionReloadHandler) {
            VersionReloadHandler versionReloadHandler = (VersionReloadHandler) handler;
            int max = versionReloadHandler.getMinecraftVersionMaximum();

            if (MinecraftVersions.CURRENT.minor() > max) {
                messages.get(BukkitMessageKey.RELOADCONFIG_NOT_SUPPORTED).sendTo(
                        sender,
                        Placeholder.unparsed("config", config)
                );
                return;
            }
        }

        ForwardFilter filter = new ForwardFilter(sender);
        filter.start(Bukkit.getLogger());
        try {
            handler.handle();
            filter.stop(Bukkit.getLogger());

            BukkitMessageKey key = filter.hasWarnings()
                    ? BukkitMessageKey.RELOADCONFIG_WARNINGS
                    : BukkitMessageKey.RELOADCONFIG_SUCCESS;
            TagResolver.builder().tag("config", Tag.selfClosingInserting(Component.text(config)));
            plugin.getMessagesResource().get(key).sendTo(sender, Placeholder.unparsed("config", config));
        } catch (Exception ex) {
            filter.stop(Bukkit.getLogger());

            ex.printStackTrace();
            plugin.getMessagesResource().get(MessageKey.GENERIC_ERROR).sendTo(sender);
        }
    }

    @Override
    protected KeyValueComponentBuilder createPluginInfo(
            KeyValueComponentBuilder builder,
            Function<Consumer<ListComponentBuilder<String>>, Component> listBuilderFunction,
            Plugin bukkitPlugin
    ) {
        PluginDescriptionFile description = bukkitPlugin.getDescription();

        builder.key("Name").value(bukkitPlugin.getName())
                .key("Full Name").value(description.getFullName())
                .key("Version").value(description.getVersion())
                .key("Website").value(description.getWebsite())
                .key("Authors").value(listBuilderFunction.apply(b -> b.addAll(description.getAuthors())))
                .key("Description").value(description.getDescription())
                .key("Main").value(description.getMain())
                .key("Prefix").value(description.getPrefix())
                .key("Load Order").value(description.getLoad().name())
                .key("Load Before").value(listBuilderFunction.apply(b -> b.addAll(description.getLoadBefore())))
                .key("Depend").value(listBuilderFunction.apply(b -> b.addAll(description.getDepend())))
                .key("Soft Depend").value(listBuilderFunction.apply(b -> b.addAll(description.getSoftDepend())));

        if (MinecraftVersions.CURRENT.minor() >= 13) {
            builder.key("API Version").value(description.getAPIVersion());
        }

        if (MinecraftVersions.CURRENT.minor() >= 15) {
            builder.key("Provides").value(listBuilderFunction.apply(b -> b.addAll(description.getProvides())));
        }

        return builder;
    }

    @Override
    protected KeyValueComponentBuilder createCommandInfo(
            KeyValueComponentBuilder builder,
            Function<Consumer<ListComponentBuilder<String>>, Component> listBuilderFunction,
            String commandName
    ) {
        Command cmd = BukkitPluginManager.getCommand(commandName);
        builder.key("Name").value(cmd.getName());

        if (cmd instanceof PluginIdentifiableCommand) {
            builder.key("Plugin").value(((PluginIdentifiableCommand) cmd).getPlugin().getName());
        }

        return builder.key("Aliases").value(listBuilderFunction.apply(b -> b.addAll(cmd.getAliases())))
                .key("Usage").value(cmd.getUsage())
                .key("Description").value(cmd.getDescription())
                .key("Label").value(cmd.getLabel())
                .key("Permission").value(cmd.getPermission())
                .key("Permission Message").value(cmd.getPermissionMessage());
    }
}
