/*
 * xTest
 * Copyright (C) 2024 Stefano Fornari
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License version 3 as published by
 * the Free Software Foundation with the addition of the following permission
 * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
 * WORK IN WHICH THE COPYRIGHT IS OWNED BY Stefano Fornari, Stefano Fornari
 * DISCLAIMS THE WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 */
package ste.xtest.jdbc;

/**
 *
 */
import org.junit.Test;
import org.junit.Before;
import static org.assertj.core.api.Assertions.*;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.util.Properties;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import ste.xtest.jdbc.Utils.EmptyConnectionHandler;

public class BugFreeXDriver {
    private String handlerId;
    private String jdbcUrl;
    private final DriverPropertyInfo[] NO_META_PROPS = new DriverPropertyInfo[0];
    private final ConnectionHandler defaultHandler = new EmptyConnectionHandler();
    private final ste.xtest.jdbc.XDriver driver = new ste.xtest.jdbc.XDriver();

    @Before
    public void setup() {
        handlerId = "test-" + System.identityHashCode(this);
        jdbcUrl = String.format("jdbc:xtest:test?handler=%s", handlerId);

        // Workaround for classloader issues
        java.util.Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            drivers.nextElement();
        }
    }

    @Test
    public void shouldBeAssignableAsJdbcDriver() {
        assertThat(new ste.xtest.jdbc.XDriver())
            .isInstanceOf(Driver.class);
    }

    @Test
    public void shouldBeAutoRegisteredUsingSPIMechanism() {
        assertThat(isRegistered(ste.xtest.jdbc.XDriver.class))
            .isTrue();
    }

    @Test
    public void shouldReturnXTestDriverForJdbcUrl() throws Exception {
        assertThat(DriverManager.getDriver(jdbcUrl))
            .isInstanceOf(ste.xtest.jdbc.XDriver.class);
    }

    @Test
    public void shouldAcceptValidJdbcUrl() throws Exception {
        assertThat(driver.acceptsURL(jdbcUrl))
            .isTrue();
    }

    @Test
    public void shouldReturnNullConnectionForUnsupportedUrl() throws Exception {
        assertThat(directConnect("https://www.google.com"))
            .isNull();
    }

    @Test
    public void shouldNotBeJdbcCompliant() {
        assertThat(driver.jdbcCompliant())
            .isFalse();
    }

    @Test
    public void shouldSupportNoMetaProperties() throws Exception {
        assertThat(driver.getPropertyInfo(jdbcUrl, null))
            .isEqualTo(NO_META_PROPS);
    }

    @Test
    public void shouldAcceptConnectionProperties() throws Exception {
        Properties props = new Properties();
        props.put("_test", "_val");

        ste.xtest.jdbc.XDriver.register(handlerId, defaultHandler);

        // Test with Properties
        assertThat(new ste.xtest.jdbc.XDriver().connect(jdbcUrl, props).getProperties())
            .isEqualTo(props);
        assertThat(ste.xtest.jdbc.XDriver.connection(defaultHandler, props).getProperties())
            .isEqualTo(props);
        assertThat(ste.xtest.jdbc.XDriver.connection(CompositeHandler.empty(), props).getProperties())
            .isEqualTo(props);

        // Test with Driver.Property
        ste.xtest.jdbc.XDriver.Property prop = new ste.xtest.jdbc.XDriver.Property("_test", "_val");

        assertThat(new ste.xtest.jdbc.XDriver().connect(jdbcUrl, prop).getProperties())
            .isEqualTo(props);
        assertThat(ste.xtest.jdbc.XDriver.connection(defaultHandler, prop).getProperties())
            .isEqualTo(props);
        assertThat(ste.xtest.jdbc.XDriver.connection(CompositeHandler.empty(), prop).getProperties())
            .isEqualTo(props);
    }

    @Test
    public void shouldNotOpenConnectionWithoutHandler() {
        // Test all variations of invalid handler scenarios
        assertThatThrownBy(() -> directConnect("jdbc:xtest:test"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid handler ID: null");

        assertThatThrownBy(() -> ste.xtest.jdbc.XDriver.connection((ConnectionHandler) null))
            .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> ste.xtest.jdbc.XDriver.connection((StatementHandler) null))
            .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> ste.xtest.jdbc.XDriver.connection((ConnectionHandler) null, (Properties) null))
            .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> ste.xtest.jdbc.XDriver.connection((StatementHandler) null, (Properties) null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldNotOpenConnectionWithInvalidHandler() {
        assertThatThrownBy(() -> directConnect("jdbc:xtest:test?handler=test"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("No matching handler: test");
    }

    @Test
    public void shouldSuccessfullyReturnConnectionForValidInformation() throws Exception {
        ste.xtest.jdbc.XDriver.register(handlerId, defaultHandler);

        assertThat(directConnect(jdbcUrl, null, defaultHandler))
            .isNotNull();
    }

    @Test
    public void shouldRefuseNullHandler() {
        assertThatThrownBy(() -> ste.xtest.jdbc.XDriver.register("id", (ConnectionHandler) null))
            .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> ste.xtest.jdbc.XDriver.register("id", (StatementHandler) null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldManageHandlerRegistrySuccessfully() {
        CompositeHandler handler = new CompositeHandler();
        ste.xtest.jdbc.XDriver.register("id", handler);

        assertThat(ste.xtest.jdbc.XDriver.unregister("id").getStatementHandler())
            .isEqualTo(handler);
    }

    @Test
    public void shouldHandleMultiThreadedAccess() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<Void>> futures = new ArrayList<>();

        for (int i = 0; i < 1000; i++) {
            futures.add(executor.submit(() -> {
                String handlerId = UUID.randomUUID().toString();
                ste.xtest.jdbc.XDriver.register(handlerId, new CompositeHandler());
                assertThat(ste.xtest.jdbc.XDriver.handlers.get(handlerId))
                    .isNotNull();
                return null;
            }));
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        for (Future<Void> future : futures) {
            future.get(); // Will throw if any test failed
        }
    }

    private boolean isRegistered(Class<?> driverClass) {
        ServiceLoader<Driver> driverSpi = ServiceLoader.load(Driver.class);
        for (Driver d : driverSpi) {
            if (driverClass.equals(d.getClass())) {
                return true;
            }
        }
        return false;
    }

    private java.sql.Connection directConnect(String url) throws Exception {
        return directConnect(url, null, null);
    }

    private java.sql.Connection directConnect(
            String url,
            Map<String, String> props,
            ConnectionHandler handler) throws Exception {

        Properties properties = new Properties();

        if (handler != null) {
            properties.put("connection.handler", handler);
        }

        if (props != null) {
            props.forEach(properties::put);
        }

        return driver.connect(url, properties);
    }
}