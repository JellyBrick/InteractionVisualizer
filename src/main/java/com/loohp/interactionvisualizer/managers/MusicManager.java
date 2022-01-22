package com.loohp.interactionvisualizer.managers;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.config.Config;
import org.bukkit.Bukkit;
import org.simpleyaml.configuration.file.FileConfiguration;
import org.simpleyaml.exceptions.InvalidConfigurationException;

import java.io.File;
import java.io.IOException;

public class MusicManager {

    public static final String MUSIC_CONFIG_ID = "music";

    public static void setup() {
        if (!InteractionVisualizer.plugin.getDataFolder().exists()) {
            InteractionVisualizer.plugin.getDataFolder().mkdir();
        }
        try {
            Config.loadConfig(MUSIC_CONFIG_ID, new File(InteractionVisualizer.plugin.getDataFolder(), "music.yml"), InteractionVisualizer.plugin.getClass().getClassLoader().getResourceAsStream("music.yml"), InteractionVisualizer.plugin.getClass().getClassLoader().getResourceAsStream("music.yml"), true);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(InteractionVisualizer.plugin);
            return;
        }
    }

    public static FileConfiguration getMusicConfig() {
        return Config.getConfig(MUSIC_CONFIG_ID).getConfiguration();
    }

    public static void saveConfig() {
        Config.getConfig(MUSIC_CONFIG_ID).save();
    }

    public static void reloadConfig() {
        Config.getConfig(MUSIC_CONFIG_ID).reload();
    }

}
