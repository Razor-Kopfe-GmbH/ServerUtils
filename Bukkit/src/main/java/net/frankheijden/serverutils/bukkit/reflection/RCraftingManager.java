package net.frankheijden.serverutils.bukkit.reflection;

import dev.frankheijden.minecraftreflection.exceptions.MinecraftReflectionException;
import dev.frankheijden.minecraftreflection.MinecraftReflection;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import net.frankheijden.serverutils.bukkit.utils.version.MinecraftVersions;
import net.frankheijden.serverutils.bukkit.utils.version.VersionUtil;
import net.frankheijden.serverutils.common.utils.MapUtils;
import org.bukkit.plugin.Plugin;

public class RCraftingManager {

    private static final MinecraftReflection reflection;
    private static final Method getCraftingManagerMethod;

    static {
        if (MinecraftVersions.CURRENT.minor() >= 17) {
            reflection = MinecraftReflection.of("net.minecraft.world.item.crafting.CraftingManager");
        } else if (MinecraftVersions.CURRENT.minor() >= 12) {
            reflection = MinecraftReflection.of("net.minecraft.server.%s.CraftingManager");
        } else {
            reflection = null;
        }

        if (MinecraftVersions.CURRENT.minor() > 12) {
            getCraftingManagerMethod = Arrays.stream(RMinecraftServer.getReflection().getClazz().getDeclaredMethods())
                    .filter(m -> m.getReturnType().equals(reflection.getClazz()))
                    .findAny()
                    .get();
        } else {
            getCraftingManagerMethod = null;
        }
    }

    private RCraftingManager() {}

    /**
     * Removes all associated recipes of a plugin.
     * @param plugin The plugin to remove recipes for.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void removeRecipesFor(Plugin plugin) {
        // Cleaning up recipes before MC 1.12 is not possible,
        // as recipes are not associated to plugins.
        if (MinecraftVersions.CURRENT.minor() == 12) {
            RRegistryMaterials.removeKeysFor(reflection.get(null, "recipes"), plugin);
        } else if (MinecraftVersions.CURRENT.minor() > 12) {
            Object server = RMinecraftServer.getReflection().invoke(null, "getServer");
            Object craftingManager;
            try {
                craftingManager = getCraftingManagerMethod.invoke(server);
            } catch (ReflectiveOperationException ex) {
                throw new MinecraftReflectionException(ex);
            }

            Map recipes;
            if (MinecraftVersions.CURRENT.minor() >= 17) {
                recipes = reflection.get(craftingManager, "c");
            } else {
                recipes = reflection.get(craftingManager, "recipes");
            }

            Predicate<Object> predicate = RMinecraftKey.matchingPluginPredicate(new AtomicBoolean(false), plugin);
            if (MinecraftVersions.CURRENT.minor() == 13) {
                MapUtils.removeKeys(recipes, predicate);
            } else {
                Collection<Map> list = (Collection<Map>) recipes.values();
                list.forEach(map -> MapUtils.removeKeys(map, predicate));
            }

            if (MinecraftVersions.CURRENT.minor() >= 18) {
                Map byName = reflection.get(craftingManager, "d");
                MapUtils.removeKeys(byName, predicate);
            }
        }
    }
}
