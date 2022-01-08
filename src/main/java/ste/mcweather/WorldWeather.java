package ste.mcweather;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

class WorldWeather extends BukkitRunnable {

    private static final String API = "https://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&appid=%s";

    private final MCWeather plugin;
    private final World world;
    private final URL url;

    private final Logger LOG;

    WorldWeather(MCWeather plugin, World world, double latitude, double longitude)
            throws IllegalArgumentException {

        if (plugin == null) {
            throw new IllegalArgumentException("plugin can not be null");
        }
        this.plugin = plugin;

        if (world == null) {
            throw new IllegalArgumentException("world can not be null");
        }
        this.world = world;
        try {
            this.url = new URL(String.format(API, latitude, longitude, plugin.apiKey));
        } catch (MalformedURLException x) {
            throw new IllegalArgumentException(x);
        }

        LOG = plugin.getLogger();
    }

    @Override
    public void run() {
        final boolean verbose = isVerbose();

        final WeatherState state;
        try {
            state = fetchWeather();
            world.setStorm(state.isRain());
            world.setThundering(state.isThunderstorm());
            world.setWeatherDuration(plugin.interval);
            if (verbose) {
                log(String.format("Storm: %b, Thundering: %b", state.isRain(), state.isThunderstorm()));
            }
        } catch (IOException x) {
            LOG.warning(
                String.format("[%s] Error downloading weather data from %s", world.getName(), url)
            );
            if (verbose) {
                log("Error: " + x);
            }
        } catch (InvalidConfigurationException x) {
            LOG.warning(
                String.format("[%s] Invalid weather data received", world.getName())
            );
        }
    }

    @Override
    public synchronized void cancel() throws IllegalStateException {
        // Allow normal game weather patterns to resume
        world.setWeatherDuration(0);
        super.cancel();
    }

    private WeatherState fetchWeather() throws IOException, InvalidConfigurationException {
        final boolean verbose = isVerbose();

        // Download
        final URLConnection request = url.openConnection();
        request.setConnectTimeout(500);
        request.setReadTimeout(500);
        request.connect();

        // Get weather data from API
        if (verbose) {
            log("Fetching weather " + url);
        }

        String weather = IOUtils.toString(request.getInputStream(), "UTF8");

        if (verbose) {
            log("Weather: " + weather.replaceAll("\n|\r", "").replaceAll(" ", ""));
        }

        // Parse
        final YamlConfiguration yaml = new YamlConfiguration();
        yaml.loadFromString(weather);

        // Extract
        final int weatherCode = Integer.parseInt(yaml.getMapList("weather").get(0).get("id").toString());

        if (verbose) {
            log(String.format("Weather at %d is %d", yaml.getInt("id", 0), weatherCode));
        }
        return new WeatherState(weatherCode);
    }

    /**
     * See https://openweathermap.org/weather-conditions for a description of
     * the conditions returned by Open Weather
     *
     */
    public static class WeatherState {

        private final int group;

        WeatherState(int condition) {
            this.group = condition / 100;
        }

        boolean isRain() {
            switch (group) {
                case 2:
                case 3:
                case 5:
                    return true;
                default:
                    return false;
            }
        }

        boolean isThunderstorm() {
            return group == 2;
        }

        boolean isSnow() {
            return group == 6;
        }
    }

    // --------------------------------------------------------- private methods

    private boolean isVerbose() {
        //return plugin.getConfig().getBoolean("verbose");
        return plugin.verbose;
    }

    private void log(String msg) {
        LOG.info(String.format("[%s] %s", world.getName(), msg));
    }
}
