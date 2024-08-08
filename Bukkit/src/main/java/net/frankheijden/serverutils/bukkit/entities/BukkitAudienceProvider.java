package net.frankheijden.serverutils.bukkit.entities;

import net.frankheijden.serverutils.bukkit.ServerUtils;
import net.frankheijden.serverutils.common.providers.ServerUtilsAudienceProvider;
import net.kyori.adventure.text.Component;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.permissions.Permissible;

public class BukkitAudienceProvider implements ServerUtilsAudienceProvider<CommandSender> {

    private final Server audience;
    private final BukkitAudience consoleServerAudience;

    /**
     * Constructs a new BukkitAudienceProvider.
     */
    public BukkitAudienceProvider(ServerUtils plugin) {
        this.audience = plugin.getServer();
        ConsoleCommandSender consoleCommandSender = plugin.getServer().getConsoleSender();
        this.consoleServerAudience = new BukkitAudience(consoleCommandSender, consoleCommandSender);
    }

    @Override
    public BukkitAudience getConsoleServerAudience() {
        return this.consoleServerAudience;
    }

    @Override
    public BukkitAudience get(CommandSender source) {
        return new BukkitAudience(source, source);
    }

    @Override
    public void broadcast(Component component, String permission) {
        audience.filterAudience(sender -> sender instanceof Permissible permissible && permissible.hasPermission(
                        permission))
                .sendMessage(component);
    }
}
