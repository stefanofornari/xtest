package ste.mcweather;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class MCWeather extends JavaPlugin {
    private static final int TICKS_PER_SECOND = 20;
    final ArrayList<WorldWeather> confWorlds = new ArrayList<>();
    String apiKey;
    int interval;
    boolean verbose;

    @Override
    public void onEnable() {
        final Logger LOG = getLogger();

        saveDefaultConfig();

        FileConfiguration config = getConfig();

        interval = config.getInt("interval", 10) * 60 * TICKS_PER_SECOND;
        apiKey = config.getString("api_key");
        verbose = config.getBoolean("verbose");

        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("Configuration: " + String.valueOf(config.getValues(true)));
        }

        confWorlds.clear();

        final List<Map<?, ?>> list = config.getMapList("worlds");

        for (Map<?, ?> map : list) {
            // Get the configuration data for this world
            String name = (String) map.get("name");
            double latitude = Double.parseDouble(map.get("latitude").toString());
            double longitude = Double.parseDouble(map.get("longitude").toString());

            // Get a reference to this world on the server
            final World world = getServer().getWorld(name);

            if (world == null) {
                getLogger().warning(String.format("World %s is configured but not loaded", name));
                continue;
            }

            // Schedule updates for this world
            WorldWeather worldWeather = new WorldWeather(this, world, latitude, longitude);
            confWorlds.add(worldWeather);
            worldWeather.runTaskTimer(this, 0, interval);
        }
    }

    @Override
    public void onDisable() {
        for (WorldWeather world : confWorlds) {
            world.cancel();
        }

        confWorlds.clear();
    }
}
