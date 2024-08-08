package net.frankheijden.serverutils.bukkit.reflection;

import dev.frankheijden.minecraftreflection.MinecraftReflection;
import net.frankheijden.serverutils.bukkit.utils.version.MinecraftVersions;

public class RMinecraftServer {

    private static final MinecraftReflection reflection;

    static {
        if (MinecraftVersions.CURRENT.minor() >= 17) {
            reflection = MinecraftReflection.of("net.minecraft.server.MinecraftServer");
        } else {
            reflection = MinecraftReflection.of("net.minecraft.server.%s.MinecraftServer");
        }
    }

    private RMinecraftServer() {}

    public static MinecraftReflection getReflection() {
        return reflection;
    }
}
