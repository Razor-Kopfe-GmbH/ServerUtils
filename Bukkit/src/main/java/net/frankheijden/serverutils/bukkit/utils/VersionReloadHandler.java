package net.frankheijden.serverutils.bukkit.utils;

import net.frankheijden.serverutils.bukkit.utils.version.MinecraftVersions;
import net.frankheijden.serverutils.bukkit.utils.version.Version;
import net.frankheijden.serverutils.bukkit.utils.version.VersionData;
import net.frankheijden.serverutils.bukkit.utils.version.VersionUtil;

public class VersionReloadHandler implements ReloadHandler {

    private final int minecraftVersionMaximum;
    private final ReloadHandler handler;

    public VersionReloadHandler(int minecraftVersionMaximum, ReloadHandler handler) {
        this.minecraftVersionMaximum = minecraftVersionMaximum;
        this.handler = handler;
    }

    public int getMinecraftVersionMaximum() {
        return minecraftVersionMaximum;
    }

    @Override
    public void handle() throws Exception {
        if (!supportsVersion(MinecraftVersions.CURRENT)) {
            throw new Exception("ReloadHandler is incompatible with version " + MinecraftVersions.CURRENT.patch()
                    + ". Maximum version this handler supports is " + minecraftVersionMaximum + ".");
        }
        handler.handle();
    }


    public boolean supportsVersion(VersionData version) {
        return version.minor() <= minecraftVersionMaximum;
    }
}
