package me.tague.pseudochickens;

import org.bukkit.plugin.java.JavaPlugin;

public final class Pseudochickens extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager()
                .registerEvents(new InteractionHandler(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

}
