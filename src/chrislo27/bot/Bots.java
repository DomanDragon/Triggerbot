package chrislo27.bot;

import java.util.HashMap;

import chrislo27.bot.bots.Bot;
import chrislo27.bot.bots.baristabot2.BaristaBot2;

public class Bots {

	private static Bots instance;

	private Bots() {
	}

	public static Bots instance() {
		if (instance == null) {
			instance = new Bots();
			instance.loadResources();
		}
		return instance;
	}

	public final HashMap<String, Class<? extends Bot>> classes = new HashMap<>();

	private void loadResources() {
		classes.put("BaristaBot", BaristaBot2.class);
	}

	public static Class<? extends Bot> get(String key) {
		return instance().classes.get(key);
	}

}
