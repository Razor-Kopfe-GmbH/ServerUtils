package net.frankheijden.serverutils.bukkit.reflection;

import dev.frankheijden.minecraftreflection.MinecraftReflection;

public class RPaperPluginManagerImpl {

    private static final MinecraftReflection PAPER_PLUGIN_MANAGER = MinecraftReflection.of(
            "io.papermc.paper.plugin.manager.PaperPluginManagerImpl");


    private RPaperPluginManagerImpl() {}

    public static Object instanceManager() {
        return PAPER_PLUGIN_MANAGER.get(PAPER_PLUGIN_MANAGER.invoke(null, "getInstance"), "instanceManager");
    }

}
