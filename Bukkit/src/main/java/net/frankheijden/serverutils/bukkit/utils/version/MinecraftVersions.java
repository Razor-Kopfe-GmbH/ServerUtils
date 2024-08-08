package net.frankheijden.serverutils.bukkit.utils.version;

import org.bukkit.Bukkit;

public class MinecraftVersions {

    public static final VersionData CURRENT = VersionUtil.parseMinecraftVersion(Bukkit.getBukkitVersion()).versionData();

    public static final VersionData V1_16_1 = new VersionData(1, 16, 1);
    public static final VersionData V1_16_2 = new VersionData(1, 16, 2);
    public static final VersionData V1_16_3 = new VersionData(1, 16, 3);

    private MinecraftVersions() {
    }

}
