package ste.mcweather;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Handler;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.PlainPluginFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.rules.TemporaryFolder;
import ste.craft.ServerStub;
import ste.craft.WorldStub;
import ste.craft.scheduler.SchedulerStub;
import ste.xtest.logging.ListLogHandler;
import ste.xtest.net.StubStreamHandler;
import ste.xtest.net.StubURLConnection;
import ste.xtest.reflect.PrivateAccess;

/**
 *
 */
public abstract class MCWeatherBase {

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

    // ------------------------------------------------------- protected methods

    protected MCWeather newMCWeatherInstance() throws Exception {
        MCWeather plugin =  (MCWeather)PlainPluginFactory.newPluginInstance(DATA.getRoot(), server, "MCWeather", "1.0", MCWeather.class.getName()
        );

        Logger l = plugin.getLogger();
        l.addHandler(new ListLogHandler());

        return plugin;
    }

    protected void givenConfiguration(String configFile) throws IOException {
        FileUtils.copyFile(new File("src/test/config", configFile), DATA.newFile("config.yml"), true);
    }

    protected ListLogHandler findListLogHandler(JavaPlugin p) throws Exception {
        for (Handler h: p.getLogger().getHandlers()) {
            if (h instanceof ListLogHandler) {
                return (ListLogHandler)h;
            }
        }

        throw new Exception("no ListLogHandler found in ");
    }

    protected void fireFetching(final MCWeather plugin) {
        plugin.onEnable();

        WorldWeather ww = new WorldWeather(
            plugin,
            plugin.getServer().getWorld("world"),
            Double.parseDouble(String.valueOf(plugin.getConfig().getMapList("worlds").get(0).get("latitude"))),
            Double.parseDouble(String.valueOf(plugin.getConfig().getMapList("worlds").get(0).get("longitude")))
        );

        ww.run();

        SchedulerStub scheduler = (SchedulerStub)plugin.getServer().getScheduler();
        scheduler.fire();
    }
}
