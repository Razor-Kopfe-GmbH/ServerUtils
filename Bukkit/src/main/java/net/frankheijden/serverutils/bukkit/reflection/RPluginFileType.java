package net.frankheijden.serverutils.bukkit.reflection;

import dev.frankheijden.minecraftreflection.MinecraftReflection;
import io.papermc.paper.plugin.configuration.PluginMeta;
import net.frankheijden.serverutils.common.entities.exceptions.InvalidPluginDescriptionException;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.jar.JarFile;

public class RPluginFileType {

    private static final Class<?> PLUGIN_FILE_TYPE_CLASS;
    private static final Object SPIGOT_PLUGIN_FILE_TYPE;
    private static final Method GUESS_TYPE;
    private static final Method GET_CONFIG;

    static {
        try {
            PLUGIN_FILE_TYPE_CLASS = Class.forName("io.papermc.paper.plugin.provider.type.PluginFileType");
            SPIGOT_PLUGIN_FILE_TYPE = PLUGIN_FILE_TYPE_CLASS.getField("SPIGOT").get(null);
            GUESS_TYPE = PLUGIN_FILE_TYPE_CLASS.getMethod("guessType", JarFile.class);
            GET_CONFIG = PLUGIN_FILE_TYPE_CLASS.getMethod("getConfig", JarFile.class);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private RPluginFileType() {

    }

    public static boolean isSpigotPlugin(JarFile jarFile) throws InvalidPluginDescriptionException {
        try {
            Object pluginFileType = GUESS_TYPE.invoke(null, jarFile);
            return pluginFileType == SPIGOT_PLUGIN_FILE_TYPE;
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Failed to parse plugin type!", ex);
        }
    }

    @Nullable
    public static PluginMeta getPluginMeta(JarFile jarFile) throws InvalidPluginDescriptionException {
        try {
            Object pluginFileType = GUESS_TYPE.invoke(null, jarFile);
            if (pluginFileType != SPIGOT_PLUGIN_FILE_TYPE) {
                throw new UnsupportedOperationException("Only spigot plugins are supported");
            }
            return (PluginMeta) GET_CONFIG.invoke(pluginFileType, jarFile);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Failed to parse plugin meta!");
        }
    }

}
