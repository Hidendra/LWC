package org.getlwc;

import org.getlwc.command.ConsoleCommandSender;
import org.getlwc.configuration.Configuration;
import org.getlwc.configuration.YamlConfiguration;
import org.getlwc.entity.Player;
import org.getlwc.lang.Locale;
import org.getlwc.model.Protection;
import org.getlwc.sql.Database;
import org.getlwc.sql.MemoryDatabase;
import org.getlwc.world.MemoryWorld;
import org.getlwc.world.Schematic;
import org.getlwc.world.SchematicLoader;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * In order to best test the matcher a small world was created with a variety of blocks to match.
 * The current world looks like this: http://files.mcstats.org/c97684a7.png which is found in resources/world.schematic.
 * The blocks marked with a red X are to be protected implicitly and also any block that should
 * also be protected are implied (and would be matched) e.g. the other chest on a double chest or the block below a door.
 *
 * p.s. the pink blocks are trapped chests
 */
public class ProtectionMatcherTest {

    @Mock
    private Engine engine;

    private Database database;
    private MemoryWorld world;
    private ProtectionManager protectionManager;
    private Configuration configuration;

    private List<Location> implicitProtections;

    ConsoleCommandSender consoleCommandSender = new ConsoleCommandSender() {
        public void sendMessage(String message) {
            System.out.println(message);
        }

        public boolean hasPermission(String node) {
            return true;
        }

        public Locale getLocale() {
            return null;
        }

        public void setLocale(Locale locale) {
            throw new UnsupportedOperationException("");
        }
    };

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        implicitProtections = new ArrayList<Location>();

        configuration = new YamlConfiguration(getClass().getResourceAsStream("/config/config.yml"));
        database = new MemoryDatabase(engine);
        protectionManager = new SimpleProtectionManager(engine);
        world = new MemoryWorld();

        SaveQueue saveQueue = new SaveQueue();

        // engine mocks
        when(engine.getConfiguration()).thenReturn(configuration);
        when(engine.getConsoleSender()).thenReturn(consoleCommandSender);
        when(engine.getDatabase()).thenReturn(database);
        when(engine.getProtectionManager()).thenReturn(protectionManager);
        when(engine.getSaveQueue()).thenReturn(saveQueue);

        loadWorld();

        // implicit protections
        createProtection("Hidendra", 3, 1, 14);
        createProtection("Hidendra", 6, 1, 14);
        createProtection("Hidendra", 8, 1, 11);
        createProtection("Hidendra", 6, 1, 11);
        createProtection("Hidendra", 4, 1, 11);
        createProtection("Hidendra", 6, 1, 8);
        createProtection("Hidendra", 9, 1, 8);
        createProtection("Hidendra", 6, 1, 5);
        createProtection("Hidendra", 5, 1, 5);
        createProtection("Hidendra", 12, 1, 0);
        createProtection("Hidendra", 10, 2, 0);
        createProtection("Hidendra", 6, 1, 0);
        createProtection("Hidendra", 5, 2, 0);
        createProtection("Hidendra", 3, 2, 0);
        createProtection("Hidendra", 0, 1, 0);
    }

    @Test
    public void testImplicitProtections() {
        assertTrue(implicitProtections.size() > 0);

        for (Location location : implicitProtections) {
            assertNotNull(engine.getProtectionManager().findProtection(location));
        }
    }

    /**
     * These protections are implied via a connection with another block, e.g. a double chest or a door.
     */
    @Test
    public void testImpliedProtections() {
        assertProtectionExists(0, 0, 0);
        assertProtectionExists(3, 1, 0);
        assertProtectionExists(3, 0, 0);
        assertProtectionExists(5, 1, 0);
        assertProtectionExists(5, 0, 0);
        assertProtectionExists(6, 2, 0);
        assertProtectionExists(6, 0, 0);
        assertProtectionExists(12, 2, 0);
        assertProtectionExists(12, 1, 0);
        assertProtectionExists(12, 0, 0);
        assertProtectionExists(6, 1, 9);
        assertProtectionExists(9, 1, 11);
        assertProtectionExists(3, 1, 11);
        assertProtectionExists(6, 1, 13);
        assertProtectionExists(4, 1, 14);
    }

    /**
     * These blocks are not valid protections. Some of them are simply just protectable blocks that are not
     * implicit (and thus making sure they do not become an implied protection by accident)
     */
    @Test
    public void testInvalidProtections() {
        assertNoProtectionExists(2, 0, 0);
        assertNoProtectionExists(2, 1, 0);
        assertNoProtectionExists(2, 2, 0);
        assertNoProtectionExists(5, 3, 0);
        assertNoProtectionExists(0, 3, 0);
        assertNoProtectionExists(7, 2, 0);
        assertNoProtectionExists(7, 1, 0);
        assertNoProtectionExists(7, 0, 0);
        assertNoProtectionExists(3, 1, 8);
        assertNoProtectionExists(9, 1, 14);
        assertNoProtectionExists(15, 0, 15);
        assertNoProtectionExists(9, 2, 0);
        assertNoProtectionExists(9, 1, 0);
        assertNoProtectionExists(9, 0, 0);
        assertNoProtectionExists(7, 1, 5);
    }

    /**
     * Assert that a protection exists at the given location
     *
     * @param x
     * @param y
     * @param z
     */
    private void assertProtectionExists(int x, int y, int z) {
        assertNotNull(engine.getProtectionManager().findProtection(new Location(world, x, y, z)));
    }

    /**
     * Assert that a protection does not exist at the given location
     *
     * @param x
     * @param y
     * @param z
     */
    private void assertNoProtectionExists(int x, int y, int z) {
        assertNull(engine.getProtectionManager().findProtection(new Location(world, x, y, z)));
    }

    /**
     * Create a protection at the given coordinates
     *
     * @param owner
     * @param x
     * @param y
     * @param z
     */
    private void createProtection(String owner, int x, int y, int z) {
        Location location = new Location(world, x, y, z);

        implicitProtections.add(location);
        engine.getProtectionManager().createProtection(owner, location);
    }

    /**
     * Load the world from the schematic file
     */
    private void loadWorld() {
        InputStream stream = getClass().getResourceAsStream("/world.schematic");

        Schematic schematic;

        try {
            schematic = SchematicLoader.loadSchematicBlocks(stream);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        for (int x = 0; x < schematic.getWidth(); ++x) {
            for (int y = 0; y < schematic.getHeight(); ++y) {
                for (int z = 0; z < schematic.getLength(); ++z) {
                    int index = y * schematic.getWidth() * schematic.getLength() + z * schematic.getWidth() + x;

                    Block block = world.getBlockAt(x, y, z);
                    block.setType(schematic.getBlocks()[index] & 0xFF);
                    block.setData(schematic.getData()[index]);
                }
            }
        }
    }

}