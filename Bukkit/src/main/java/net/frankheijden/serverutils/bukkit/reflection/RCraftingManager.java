package net.frankheijden.serverutils.bukkit.reflection;

import dev.frankheijden.minecraftreflection.MinecraftReflection;
import dev.frankheijden.minecraftreflection.exceptions.MinecraftReflectionException;
import net.frankheijden.serverutils.bukkit.utils.version.MinecraftVersions;
import net.frankheijden.serverutils.common.utils.MapUtils;
import net.kyori.adventure.key.Keyed;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

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

    private RCraftingManager() {
    }

    /**
     * Removes all associated recipes of a plugin.
     *
     * @param plugin The plugin to remove recipes for.
     */
    public static void removeRecipesFor(Plugin plugin) {
        // Cleaning up recipes before MC 1.12 is not possible,
        // as recipes are not associated to plugins.
        if (MinecraftVersions.CURRENT.minor() < 12) {
            return;
        }
        if (MinecraftVersions.CURRENT.minor() == 12) {
            RRegistryMaterials.removeKeysFor(reflection.get(null, "recipes"), plugin);
            return;
        }
        Object server = RMinecraftServer.getReflection().invoke(null, "getServer");
        Object craftingManager;
        try {
            craftingManager = getCraftingManagerMethod.invoke(server);
        } catch (ReflectiveOperationException ex) {
            throw new MinecraftReflectionException(ex);
        }
        if (MinecraftVersions.CURRENT.isOlderThan(MinecraftVersions.V1_21)) {
            removeRecipesPre121(plugin, craftingManager);
            return;
        }
        Iterator<Recipe> recipeIterator = plugin.getServer().recipeIterator();
        String pluginName = plugin.getName().toLowerCase(Locale.ROOT);
        while (recipeIterator.hasNext()) {
            Recipe recipe = recipeIterator.next();
            if (recipe instanceof Keyed keyed && keyed.key().namespace().equals(pluginName)) {
                recipeIterator.remove();
            }
        }
    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void removeRecipesPre121(Plugin plugin, Object craftingManager) {
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
