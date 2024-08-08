package net.frankheijden.serverutils.bukkit.entities;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

import io.papermc.paper.plugin.configuration.PluginMeta;
import net.frankheijden.serverutils.common.entities.ServerUtilsPluginDescription;

public class BukkitPluginDescription implements ServerUtilsPluginDescription {

    private final PluginMeta descriptionFile;
    private final File file;
    private final String author;
    private final Set<String> dependencies;

    /**
     * Constructs a new BukkitPluginDescription.
     */
    public BukkitPluginDescription(PluginMeta descriptionFile, File file) {
        this.descriptionFile = descriptionFile;
        this.file = file;
        this.author = String.join(", ", this.descriptionFile.getAuthors());
        this.dependencies = new LinkedHashSet<>(descriptionFile.getPluginDependencies());
    }

    @Override
    public String getId() {
        return this.descriptionFile.getName();
    }

    @Override
    public String getName() {
        return this.descriptionFile.getName();
    }

    @Override
    public String getVersion() {
        return this.descriptionFile.getVersion();
    }

    @Override
    public String getAuthor() {
        return this.author;
    }

    @Override
    public File getFile() {
        return this.file;
    }

    @Override
    public Set<String> getDependencies() {
        return this.dependencies;
    }

    public PluginMeta getPluginMeta() {
        return descriptionFile;
    }
}
