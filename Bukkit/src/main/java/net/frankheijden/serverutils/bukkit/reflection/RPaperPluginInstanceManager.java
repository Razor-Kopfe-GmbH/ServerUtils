package net.frankheijden.serverutils.bukkit.reflection;

import dev.frankheijden.minecraftreflection.MinecraftReflection;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Map;

public class RPaperPluginInstanceManager {

    private static final MinecraftReflection INSTANCE_MANAGER = MinecraftReflection.of("io.papermc.paper.plugin.manager.PaperPluginInstanceManager");


    private RPaperPluginInstanceManager() {}

    public static void removePlugin(Object pluginInstanceManager, Plugin plugin) {
        String name = plugin.getPluginMeta().getName().toLowerCase(java.util.Locale.ENGLISH);
        List<Plugin> plugins = INSTANCE_MANAGER.get(pluginInstanceManager, "plugins");
        plugins.remove(plugin);
        Map<String, Plugin> lookup = INSTANCE_MANAGER.get(pluginInstanceManager, "lookupNames");
        lookup.remove(name);
    }


}
