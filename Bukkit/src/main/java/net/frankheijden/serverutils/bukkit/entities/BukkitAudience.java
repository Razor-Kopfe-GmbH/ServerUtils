package net.frankheijden.serverutils.bukkit.entities;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.frankheijden.serverutils.common.entities.ServerUtilsAudience;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BukkitAudience extends ServerUtilsAudience<CommandSender> {

    private final CommandSourceStack sourceStack;

    public BukkitAudience(Audience audience, CommandSender source) {
        super(audience, source);
        this.sourceStack = null;
    }

    public BukkitAudience(Audience audience, CommandSourceStack source) {
        super(audience, source.getSender());
        this.sourceStack = source;
    }

    @Override
    public boolean isPlayer() {
        return source instanceof Player;
    }

    @Override
    public boolean hasPermission(String permission) {
        return source.hasPermission(permission);
    }

    public CommandSourceStack sourceStack() {
        return this.sourceStack;
    }
}
