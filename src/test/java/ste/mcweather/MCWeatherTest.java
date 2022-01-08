package ste.mcweather;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import static org.assertj.core.api.BDDAssertions.then;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.PlainPluginFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.rules.TemporaryFolder;
import ste.craft.ServerStub;
import ste.craft.WorldStub;
import ste.xtest.logging.ListLogHandler;
import ste.xtest.net.StubStreamHandler;
import ste.xtest.net.StubURLConnection;
import ste.xtest.reflect.PrivateAccess;

/**
 *
 */
public class MCWeatherTest {

    public static final String URL_OK =
        "https://api.openweathermap.org/data/2.5/weather?lat=51.506420&lon=-0.127210&appid=akey";
    public static final String URL_IOERROR =
        "https://api.openweathermap.org/data/2.5/weather?lat=45.678000&lon=1.123000&appid=akey";


    @Rule
    public TemporaryFolder DATA= new TemporaryFolder();

     @Rule
    public final ProvideSystemProperty PROTOCOL_PATH_PROP
	 = new ProvideSystemProperty("java.protocol.handler.pkgs", "ste.xtest.net.protocol");

    private Server server = null;

    @Before
    public void before() throws Exception {
        StubURLConnection stub = new StubURLConnection(new URL(URL_IOERROR));
        stub.error(new IOException("simulated IO error"));
        StubStreamHandler.URLMap.add(stub);

        stub = new StubURLConnection(new URL(URL_OK));
        stub.file("src/test/data/weather-01.json");
        StubStreamHandler.URLMap.add(stub);

        server = new ServerStub(new WorldStub("world"));

        PrivateAccess.setStaticValue(Bukkit.class, "server", null);
        Bukkit.setServer(server);
    }

    @After
    public void after() throws IOException {
        DATA.delete(); DATA.create();
    }

    /**
     * Test of onEnable method, of class MCWeather.
     */

    @Test
    public void read_default_configuration_at_OnEnable() throws Exception {
        MCWeather instance = newMCWeatherInstance();

        instance.onEnable();
        then(instance.apiKey).isEmpty();
        then(instance.verbose).isFalse();
    }

    @Test
    public void read_given_configuration_at_OnEnable() throws Exception {
        givenConfiguration("config-01.yml");
        MCWeather instance = newMCWeatherInstance();

        instance.onEnable();
        then(instance.apiKey).isEqualTo("akey");
        then(instance.verbose).isTrue();
    }

    @Test
    public void log_configuration_at_OnEnable_if_verbose() throws Exception {
        givenConfiguration("config-01.yml");
        MCWeather instance = newMCWeatherInstance();

        instance.onEnable();

        then(findListLogHandler(instance).getMessages(Level.INFO)).contains(
            "[MCWeather] Configuration: {api_key=akey, interval=5, worlds=[{name=world, latitude=51.50642, longitude=-0.12721}], verbose=true}"
        );
    }

    @Test
    public void log_openweather_request_only_if_verbose() throws Exception {
        givenConfiguration("config-01.yml");
        MCWeather plugin = newMCWeatherInstance(true);

        fireFetching(plugin);

        ListLogHandler logger = findListLogHandler(plugin);

        then(logger.getMessages(Level.INFO)).contains(
            "[MCWeather] [world] Fetching weather " + URL_OK
        );

        plugin.verbose = false; logger.flush();
        fireFetching(plugin);

        then(logger.getMessages(Level.INFO)).doesNotContain(
            "[MCWeather] [world] Fetching weather " + URL_OK
        );
    }

    @Test
    public void log_openweather_error() throws Exception {
        givenConfiguration("config-03.yml");
        MCWeather plugin = newMCWeatherInstance(true);

        fireFetching(plugin);

        ListLogHandler logger = findListLogHandler(plugin);

        then(logger.getMessages(Level.WARNING))
            .contains(
                "[MCWeather] [world] Error downloading weather data from " + URL_IOERROR
            );
        then(logger.getMessages(Level.INFO))
            .doesNotContain("[MCWeather] [world] Error: java.io.IOException: simulated IO error");

        plugin.verbose = true;
        fireFetching(plugin);
        then(logger.getMessages(Level.INFO))
            .contains("[MCWeather] [world] Error: java.io.IOException: simulated IO error");
    }

    @Test
    public void log_weather_only_if_verbose() throws Exception {
        givenConfiguration("config-01.yml");
        MCWeather plugin = newMCWeatherInstance(true);

        fireFetching(plugin);  // daat read from src/test/data/weather-01.json

        ListLogHandler logger = findListLogHandler(plugin);

        String weather = FileUtils.readFileToString(new File("src/test/data/weather-01.json"), "UTF-8");

        then(logger.getMessages(Level.INFO)).contains(
            "[MCWeather] [world] Weather: " + weather.replaceAll("\n|\r", "").replaceAll(" ", ""),
            "[MCWeather] [world] Weather at 6295630 is 804",
            "[MCWeather] [world] Storm: false, Thundering: false"
        );

        plugin.verbose = false; logger.flush();
        fireFetching(plugin);

        then(logger.getMessages(Level.INFO)).doesNotContain(
            "[MCWeather] [world] Weather: " + weather.replaceAll("\n|\r", "").replaceAll(" ", ""),
            "[MCWeather] [world] Weather at 6295630 is 804",
            "[MCWeather] [world] Storm: false, Thundering: false"
        );
    }

    // --------------------------------------------------------- private methods

    private MCWeather newMCWeatherInstance(boolean enable) throws Exception {
        MCWeather plugin =  (MCWeather)PlainPluginFactory.newPluginInstance(DATA.getRoot(), server, "MCWeather", "1.0", MCWeather.class.getName()
        );

        Logger l = plugin.getLogger();
        l.addHandler(new ListLogHandler());

        if (enable) {
            plugin.onEnable();
        }

        return plugin;
    }

    private MCWeather newMCWeatherInstance() throws Exception {
        return newMCWeatherInstance(false);
    }

    private void givenConfiguration(String configFile) throws IOException {
        FileUtils.copyFile(new File("src/test/config", configFile), DATA.newFile("config.yml"), true);
    }

    private ListLogHandler findListLogHandler(JavaPlugin p) throws Exception {
        for (Handler h: p.getLogger().getHandlers()) {
            if (h instanceof ListLogHandler) {
                return (ListLogHandler)h;
            }
        }

        throw new Exception("no ListLogHandler found in ");
    }

    private void fireFetching(final MCWeather plugin) {
        WorldWeather ww = new WorldWeather(
            plugin,
            plugin.getServer().getWorld("world"),
            Double.parseDouble(String.valueOf(plugin.getConfig().getMapList("worlds").get(0).get("latitude"))),
            Double.parseDouble(String.valueOf(plugin.getConfig().getMapList("worlds").get(0).get("longitude")))
        );

        ww.run();  // this already fire the task once
    }
}
