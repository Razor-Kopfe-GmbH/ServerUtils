package net.frankheijden.serverutils.bukkit.reflection;

import dev.frankheijden.minecraftreflection.MinecraftReflection;
import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import net.frankheijden.serverutils.bukkit.utils.version.MinecraftVersions;
import net.frankheijden.serverutils.bukkit.utils.version.VersionUtil;
import org.bukkit.Bukkit;
import org.bukkit.Warning;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class RCraftServer {

    private static final MinecraftReflection reflection = MinecraftReflection
            .of("org.bukkit.craftbukkit.CraftServer");

    public static MinecraftReflection getReflection() {
        return reflection;
    }

    public static File getConfigFile() {
        return reflection.invoke(Bukkit.getServer(), "getConfigFile");
    }

    /**
     * Retrieves the options file from a key.
     * @param option The option key.
     * @return The associated file.
     */
    public static File getOptionsFile(String option) {
        Object options = reflection.get(getConsole(), "options");
        return reflection.invoke(options, "valueOf", option);
    }

    public static File getCommandsConfigFile() {
        return reflection.invoke(Bukkit.getServer(), "getCommandsConfigFile");
    }

    public static SimpleCommandMap getCommandMap() {
        return reflection.get(Bukkit.getServer(), "commandMap");
    }

    /**
     * Syncs and registers all commands, but keeping the old values that haven't been added.
     */
    @SuppressWarnings({"rawtypes"})
    public static void syncCommands(Set<String> removedCommands) {
        if (MinecraftVersions.CURRENT.minor() < 13) return;

        Collection children = RCommandDispatcher.getDispatcher().getRoot().getChildren();
        reflection.invoke(Bukkit.getServer(), "syncCommands");
        Object root = RCommandDispatcher.getDispatcher().getRoot();

        for (Object child : children) {
            String name = RCommandNode.getName(child);
            RCommandNode.removeCommand(root, name);

            if (!removedCommands.contains(name)) {
                RCommandNode.addChild(root, child);
            }
        }
        updateCommands();
    }

    /**
     * Updates commands for all online players.
     */
    public static void updateCommands() {
        if (MinecraftVersions.CURRENT.minor() < 13) return;
        Bukkit.getOnlinePlayers().forEach(RCraftServer::updateCommands);
    }

    public static void updateCommands(Player player) {
        if (MinecraftVersions.CURRENT.minor() < 13) return;
        player.updateCommands();
    }

    public static Object getConsole() {
        return reflection.get(Bukkit.getServer(), "console");
    }

    /**
     * Reloads the bukkit configuration.
     */
    public static void reloadBukkitConfiguration() {
        YamlConfiguration bukkit = YamlConfiguration.loadConfiguration(getConfigFile());
        reflection.set(Bukkit.getServer(), "configuration", bukkit);

        RDedicatedServer.reload(getConsole());

        reflection.set(Bukkit.getServer(), "monsterSpawn", bukkit.getInt("spawn-limits.monsters"));
        reflection.set(Bukkit.getServer(), "animalSpawn", bukkit.getInt("spawn-limits.animals"));
        reflection.set(Bukkit.getServer(), "waterAnimalSpawn", bukkit.getInt("spawn-limits.water-animals"));
        reflection.set(Bukkit.getServer(), "ambientSpawn", bukkit.getInt("spawn-limits.ambient"));
        reflection.set(Bukkit.getServer(), "warningState",
                Warning.WarningState.value(bukkit.getString("settings.deprecated-verbose")));
        if (MinecraftVersions.CURRENT.minor() >= 14)
            reflection.set(Bukkit.getServer(), "minimumAPI", bukkit.getString("settings.minimum-api"));
        reflection.set(Bukkit.getServer(), "printSaveWarning", false);
        reflection.set(Bukkit.getServer(), "monsterSpawn", bukkit.getInt("spawn-limits.monsters"));
        reflection.set(Bukkit.getServer(), "monsterSpawn", bukkit.getInt("spawn-limits.monsters"));
        reflection.set(Bukkit.getServer(), "monsterSpawn", bukkit.getInt("spawn-limits.monsters"));
        if (MinecraftVersions.CURRENT.minor() <= 12) {
            reflection.set(Bukkit.getServer(), "chunkGCPeriod", bukkit.getInt("chunk-gc.period-in-ticks"));
            reflection.set(Bukkit.getServer(), "chunkGCLoadThresh", bukkit.getInt("chunk-gc.load-threshold"));
        }

        RDedicatedServer.getReflection().set(getConsole(), "autosavePeriod", bukkit.getInt("ticks-per.autosave"));
    }

    public static void loadIcon() {
        reflection.invoke(Bukkit.getServer(), "loadIcon");
    }

    /**
     * Reloads the commands.yml file.
     */
    public static void reloadCommandsConfiguration() {
        SimpleCommandMap commandMap = getCommandMap();
        Map<String, Command> map = RCommandMap.getKnownCommands(commandMap);

        Set<String> commandNames = Bukkit.getCommandAliases().keySet();
        RCommandDispatcher.removeCommands(commandNames);
        for (String alias : commandNames) {
            Command aliasCommand = map.remove(alias);
            if (aliasCommand == null) continue;

            aliasCommand.unregister(commandMap);
        }

        YamlConfiguration commands = YamlConfiguration.loadConfiguration(getCommandsConfigFile());
        reflection.set(Bukkit.getServer(), "commandsConfiguration", commands);
        reflection.set(Bukkit.getServer(), "overrideAllCommandBlockCommands",
                commands.getStringList("command-block-overrides").contains("*"));
        if (MinecraftVersions.CURRENT.minor() >= 13) reflection.set(
                Bukkit.getServer(),
                "ignoreVanillaPermissions",
                commands.getBoolean("ignore-vanilla-permissions")
        );
        if (MinecraftVersions.CURRENT.minor() == 12) reflection.set(
                Bukkit.getServer(),
                "unrestrictedAdvancements",
                commands.getBoolean("unrestricted-advancements")
        );

        commandMap.registerServerAliases();
        RCraftServer.syncCommands(commandNames);
    }

    /**
     * Reloads the ip-bans file.
     */
    public static void reloadIpBans() {
        Object playerList = reflection.get(Bukkit.getServer(), "playerList");
        Object jsonList = RPlayerList.getReflection().invoke(playerList, "getIPBans");
        RJsonList.load(jsonList);
    }

    /**
     * Reloads the profile bans file.
     */
    public static void reloadProfileBans() {
        Object playerList = reflection.get(Bukkit.getServer(), "playerList");
        Object jsonList = RPlayerList.getReflection().invoke(playerList, "getProfileBans");
        RJsonList.load(jsonList);
    }
}
