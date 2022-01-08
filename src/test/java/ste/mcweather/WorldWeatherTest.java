package ste.mcweather;

import static org.assertj.core.api.AssertionsForClassTypes.fail;
import static org.assertj.core.api.BDDAssertions.then;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.java.PlainPluginFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ste.craft.ServerStub;
import ste.craft.WorldStub;
import ste.xtest.reflect.PrivateAccess;

/**
 *
 */
public class WorldWeatherTest {

    @Rule
    public TemporaryFolder DATA= new TemporaryFolder();

    @Test
    public void constructor() throws Exception {

        Server server = new ServerStub(new WorldStub("world"));
        Bukkit.setServer(server);

        MCWeather plugin =  (MCWeather)PlainPluginFactory.newPluginInstance(DATA.getRoot(), server, "MCWeather", "1.0", MCWeather.class.getName());
        plugin.onEnable();

        //
        // OK
        //
        WorldWeather ww = new WorldWeather(plugin, server.getWorld("world"), 1, 2);

        then(PrivateAccess.getInstanceValue(ww, "plugin")).isSameAs(plugin);
        then(PrivateAccess.getInstanceValue(ww, "world")).isSameAs(server.getWorld("world"));
        then(String.valueOf(PrivateAccess.getInstanceValue(ww, "url"))).isEqualTo(
            "https://api.openweathermap.org/data/2.5/weather?lat=1.000000&lon=2.000000&appid="
        );

        //
        // KO
        //
        try {
            new WorldWeather(null, server.getWorld("world"), 0, 0);
            fail("missing plugin argument check");
        } catch (IllegalArgumentException x) {
            then(x).hasMessage("plugin can not be null");
        }
        try {
            new WorldWeather(plugin, null, 0, 0);
            fail("missing world argument check");
        } catch (IllegalArgumentException x) {
            then(x).hasMessage("world can not be null");
        }
    }
}
