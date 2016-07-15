package chrislo27.bot.bots.baristabot2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

public class PermPrefs {

	private static PermPrefs instance;

	private PermPrefs() {
	}

	public static PermPrefs instance() {
		if (instance == null) {
			instance = new PermPrefs();
			instance.loadResources();
		}
		return instance;
	}

	private Properties properties;
	private final HashMap<String, Long> defaultPermissions = new HashMap<>();
	private final File file = new File("savedata/permissions.properties");
	private final File folder = new File("savedata/");

	private void loadResources() {
		properties = new Properties();
		load();

		defaultPermissions.put("188789412426022914", PermissionTier.ADMIN);
	}

	public static Properties getProperties() {
		return instance().properties;
	}

	public void save() {
		FileOutputStream fos = null;

		try {
			folder.mkdirs();
			file.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		try {
			fos = new FileOutputStream(file);

			properties.store(fos, null);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static long getPermissionsLevel(String id) {
		if (instance().properties.containsKey(id) == false) {
			setPermissionsLevel(id,
					instance().defaultPermissions.getOrDefault(id, PermissionTier.NORMAL));
			instance().save();
		}

		long perm = Long.parseLong((String) instance().properties.get(id));

		if (perm < 0) {
			if (Math.abs(perm) <= System.currentTimeMillis()) {
				perm = PermissionTier.NORMAL;
				setPermissionsLevel(id, perm);
			}
		}

		return perm;
	}

	public static void setPermissionsLevel(String id, long level) {
		instance().properties.put(id, level + "");
	}

	public void load() {
		FileInputStream fis = null;

		try {
			folder.mkdirs();
			file.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		try {
			fis = new FileInputStream(file);

			properties.load(fis);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
