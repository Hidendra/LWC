package com.griefcraft;

import java.util.Random;

import com.griefcraft.logging.Logger;
import com.griefcraft.sql.MemDB;
import com.griefcraft.sql.PhysDB;
import com.griefcraft.util.Config;

public class UnitTestInserts {

	public static void main(String[] args) throws Exception {
		new UnitTestInserts();
	}

	private Logger logger = Logger.getLogger("SQLTest");
	private PhysDB phys;

	private MemDB mem;

	public UnitTestInserts() throws Exception {
		Config.init();
		Config.getInstance().setProperty("db-path", "E:\\Java\\LWC\\lwc.db");

		phys = new PhysDB();
		mem = new MemDB();

		phys.connect();
		phys.load();

		mem.connect();
		mem.load();

		try {
			// createChests(5000);
			speedTest();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private void createChests(int count) throws Exception {
		Random rand = new Random();

		phys.getConnection().setAutoCommit(false);

		logger.log("Creating " + count + " queries");

		while (count > 0) {
			phys.registerProtectedEntity(rand.nextInt(1), "Hidendra", "", rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));

			count--;
		}

		logger.log("Commiting");

		phys.getConnection().commit();
		phys.getConnection().setAutoCommit(true);

		logger.log("Done. Count : " + phys.getProtectionCount());
	}

	@SuppressWarnings("unused")
	private void speedTest() throws Exception {
		Random rand = new Random();
		long start = System.currentTimeMillis();
		int queryCount = 0;

		int lastUpdate = 0;

		while (true) {
			long delta = System.currentTimeMillis() - start;
			int seconds = (int) (delta / 1000);
			int perSecond = seconds > 0 ? (int) (queryCount / seconds) : 0;

			if (lastUpdate != seconds && delta % 1000 == 0) {
				logger.log(String.format("[%ds] [Q:%d] QUERIES/SEC:%d", seconds, queryCount, perSecond));
				lastUpdate = seconds;
			}

			phys.loadProtectedEntity(rand.nextInt(50000));
			// mem.getModeData("Hidendra", "m" + rand.nextInt(50000));
			queryCount++;

			// Thread.sleep(100);
		}

	}

}
