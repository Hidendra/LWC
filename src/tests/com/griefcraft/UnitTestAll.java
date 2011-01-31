package com.griefcraft;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ThreadServerApplication;

import org.bukkit.craftbukkit.CraftServer;
import org.junit.Test;

import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCPlugin;
import com.griefcraft.model.ProtectionTypes;
import com.griefcraft.model.RightTypes;
import com.griefcraft.sql.Database;

/**
 * Run tests using JUnit. Runs a minecraft server to virtualize a real server. MC server that is run is ran on a random port and is only up until tests are completed
 */
public class UnitTestAll {

	/**
	 * Static vars so they remain the same throughout each test and connections remain active
	 */

	private static MinecraftServer minecraftServer;
	private static CraftServer craftServer;

	private static LWCPlugin plugin;
	private static LWC lwc;

	public static boolean running = false;

	public UnitTestAll() throws Exception {
		if (running) {
			return;
		}

		/* Load initial options */
		OptionSet optionSet = loadOptions();

		/* Load the Minecraft Server and start it */
		minecraftServer = new MinecraftServer(optionSet);
		new ThreadServerApplication("Server thread", minecraftServer).start();

		/* Wait for the CraftServer instance to be set */
		do {
			try {
				Thread.sleep(50);
			} catch (Exception e) {

			}
		} while ((craftServer = minecraftServer.server) == null);

		System.out.println("minecraftServer.server isn't null");

		/* Load LWC */
		plugin = (LWCPlugin) craftServer.getPluginManager().loadPlugin(new File("dist/LWC.jar"));
		craftServer.getPluginManager().enablePlugin(plugin);

		lwc = plugin.getLWC();
		running = true;
	}

	@Test
	public void testConnection() {
		assertTrue(Database.isConnected());
		assertTrue(Database.isConnected());
	}

	@Test
	public void testQueries() {
		lwc.getPhysicalDatabase().unregisterProtectionEntities();
		lwc.getPhysicalDatabase().unregisterProtectionLimits();
		lwc.getPhysicalDatabase().unregisterProtectionRights();

		lwc.getMemoryDatabase().registerUnlock("Action", 1);

		lwc.getPhysicalDatabase().registerProtectedEntity(ProtectionTypes.PRIVATE, "Hidendra", "", 0, 0, 0);
		lwc.getPhysicalDatabase().registerProtectedEntity(ProtectionTypes.PRIVATE, "Bob", "", 10, 0, 0);
		lwc.getPhysicalDatabase().registerProtectedEntity(ProtectionTypes.PRIVATE, "Bob", "", 15, 0, 0);
		lwc.getPhysicalDatabase().registerProtectionLimit(RightTypes.GROUP, 1, "g:default");
		lwc.getPhysicalDatabase().registerProtectionLimit(RightTypes.PLAYER, 2, "Hidendra");

		assertTrue(lwc.getMemoryDatabase().getUnlockID("Action") == 1);
		assertNotNull(lwc.getPhysicalDatabase().loadProtectedEntity(0, 0, 0));
		assertTrue(lwc.getPhysicalDatabase().loadProtectedEntities(0, 0, 0, 20).size() == 3);
	}

	@Test
	public void testSinglePhysicalQuery() {
		lwc.getPhysicalDatabase().loadProtectedEntity(0);
	}

	@Test
	public void testSingleMemoryQuery() {
		lwc.getMemoryDatabase().getUnlockID("Action");
	}

	@Test
	public void testPermissions() {

	}

	private OptionSet loadOptions() {
		OptionParser parser = new OptionParser() {
			{
				acceptsAll(asList("?", "help"), "Show the help");

				acceptsAll(asList("c", "config"), "Properties file to use").withRequiredArg().ofType(File.class).defaultsTo(new File("server.properties")).describedAs("Properties file");

				acceptsAll(asList("P", "plugins"), "Plugin directory to use").withRequiredArg().ofType(File.class).defaultsTo(new File("plugins")).describedAs("Plugin directory");

				acceptsAll(asList("h", "host", "server-ip"), "Host to listen on").withRequiredArg().ofType(String.class).describedAs("Hostname or IP");

				acceptsAll(asList("p", "port", "server-port"), "Port to listen on").withRequiredArg().ofType(Integer.class).describedAs("Port");

				acceptsAll(asList("o", "online-mode"), "Whether to use online authentication").withRequiredArg().ofType(Boolean.class).describedAs("Authentication");

				acceptsAll(asList("s", "size", "max-players"), "Maximum amount of players").withRequiredArg().ofType(Integer.class).describedAs("Server size");
			}
		};

		return parser.parse("-p25570");
	}

	private static List<String> asList(String... params) {
		return Arrays.asList(params);
	}

}
