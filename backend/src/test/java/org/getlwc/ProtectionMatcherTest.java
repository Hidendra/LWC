package org.getlwc;

import org.getlwc.command.CommandHandler;
import org.getlwc.command.ConsoleCommandSender;
import org.getlwc.command.SimpleCommandHandler;
import org.getlwc.configuration.Configuration;
import org.getlwc.configuration.YamlConfiguration;
import org.getlwc.db.Database;
import org.getlwc.db.memory.MemoryDatabase;
import org.getlwc.lang.Locale;
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
import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        world = new MemoryWorld();
        CommandHandler commandHandler = new SimpleCommandHandler(engine);

        // engine mocks
        when(engine.getServerLayer()).thenReturn(mock(ServerLayer.class));
        when(engine.getConfiguration()).thenReturn(configuration);
        when(engine.getConsoleSender()).thenReturn(consoleCommandSender);
        when(engine.getDatabase()).thenReturn(database);
        when(engine.getCommandHandler()).thenReturn(commandHandler);

        protectionManager = new SimpleProtectionManager(engine);
        when(engine.getProtectionManager()).thenReturn(protectionManager);

        try {
            loadWorld();
        } catch (IOException e) {
            e.printStackTrace();
            fail("Exception occurred: " + e.getMessage());
        }

        // implicit protections
        createProtection(UUID.fromString("86553713-3d20-4923-9fd6-587aa7ed7c16"), 3, 1, 14);
        createProtection(UUID.fromString("86553713-3d20-4923-9fd6-587aa7ed7c16"), 6, 1, 14);
        createProtection(UUID.fromString("86553713-3d20-4923-9fd6-587aa7ed7c16"), 8, 1, 11);
        createProtection(UUID.fromString("86553713-3d20-4923-9fd6-587aa7ed7c16"), 6, 1, 11);
        createProtection(UUID.fromString("86553713-3d20-4923-9fd6-587aa7ed7c16"), 4, 1, 11);
        createProtection(UUID.fromString("86553713-3d20-4923-9fd6-587aa7ed7c16"), 6, 1, 8);
        createProtection(UUID.fromString("86553713-3d20-4923-9fd6-587aa7ed7c16"), 9, 1, 8);
        createProtection(UUID.fromString("86553713-3d20-4923-9fd6-587aa7ed7c16"), 6, 1, 5);
        createProtection(UUID.fromString("86553713-3d20-4923-9fd6-587aa7ed7c16"), 5, 1, 5);
        createProtection(UUID.fromString("86553713-3d20-4923-9fd6-587aa7ed7c16"), 12, 1, 0);
        createProtection(UUID.fromString("86553713-3d20-4923-9fd6-587aa7ed7c16"), 10, 2, 0);
        createProtection(UUID.fromString("86553713-3d20-4923-9fd6-587aa7ed7c16"), 6, 1, 0);
        createProtection(UUID.fromString("86553713-3d20-4923-9fd6-587aa7ed7c16"), 5, 2, 0);
        createProtection(UUID.fromString("86553713-3d20-4923-9fd6-587aa7ed7c16"), 3, 2, 0);
        createProtection(UUID.fromString("86553713-3d20-4923-9fd6-587aa7ed7c16"), 0, 1, 0);
        createProtection(UUID.fromString("86553713-3d20-4923-9fd6-587aa7ed7c16"), 14, 2, 6); // sign
        createProtection(UUID.fromString("86553713-3d20-4923-9fd6-587aa7ed7c16"), 14, 2, 8); // sign
        createProtection(UUID.fromString("86553713-3d20-4923-9fd6-587aa7ed7c16"), 13, 1, 10); // sign
        createProtection(UUID.fromString("86553713-3d20-4923-9fd6-587aa7ed7c16"), 13, 1, 12); // sign
        createProtection(UUID.fromString("86553713-3d20-4923-9fd6-587aa7ed7c16"), 11, 1, 12); // trap door
        createProtection(UUID.fromString("86553713-3d20-4923-9fd6-587aa7ed7c16"), 12, 2, 8); // wooden button
        createProtection(UUID.fromString("86553713-3d20-4923-9fd6-587aa7ed7c16"), 12, 2, 6); // stone button
        createProtection(UUID.fromString("86553713-3d20-4923-9fd6-587aa7ed7c16"), 13, 2, 3); // lever on wall
        createProtection(UUID.fromString("86553713-3d20-4923-9fd6-587aa7ed7c16"), 14, 2, 1); // lever on ground
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
        assertProtectionExists(14, 1, 6); // bottom of sign
        assertProtectionExists(14, 1, 8); // bottom of sign
        assertProtectionExists(14, 1, 10); // behind sign
        assertProtectionExists(14, 1, 12); // behind sign
        assertProtectionExists(11, 1, 13); // trap door
        assertProtectionExists(14, 1, 1); // block wall lever is attached to
        assertProtectionExists(14, 2, 3); // block ground lever is attached to
        assertProtectionExists(13, 2, 6); // block stone button is attached to
        assertProtectionExists(13, 2, 8); // block wooden button is attached to
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
        assertNoProtectionExists(15, 2, 6);
        assertNoProtectionExists(14, 2, 5);
        assertNoProtectionExists(14, 2, 7);
        assertNoProtectionExists(15, 2, 8);
        assertNoProtectionExists(14, 2, 9);
        assertNoProtectionExists(14, 1, 9);
        assertNoProtectionExists(14, 2, 10);
        assertNoProtectionExists(15, 2, 10);
        assertNoProtectionExists(14, 1, 11);
        assertNoProtectionExists(14, 2, 12);
        assertNoProtectionExists(14, 1, 13);
        assertNoProtectionExists(12, 1, 12);
        assertNoProtectionExists(11, 1, 11);
        assertNoProtectionExists(10, 1, 12);
        assertNoProtectionExists(12, 1, 6);
        assertNoProtectionExists(12, 1, 8);
        assertNoProtectionExists(11, 2, 12);
        assertNoProtectionExists(15, 1, 1);
        assertNoProtectionExists(15, 2, 1);
        assertNoProtectionExists(13, 2, 1);
        assertNoProtectionExists(14, 2, 2);
        assertNoProtectionExists(14, 2, 0);
        assertNoProtectionExists(14, 3, 1);
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
    private void createProtection(UUID owner, int x, int y, int z) {
        Location location = new Location(world, x, y, z);

        implicitProtections.add(location);
        engine.getProtectionManager().createProtection(owner, location);
    }

    /**
     * Load the world from the schematic file
     */
    private void loadWorld() throws IOException {
        try (InputStream stream = getClass().getResourceAsStream("/world.schematic")) {
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

}
