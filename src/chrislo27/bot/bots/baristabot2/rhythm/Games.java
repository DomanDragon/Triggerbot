package chrislo27.bot.bots.baristabot2.rhythm;

import java.util.HashMap;

import chrislo27.bot.bots.baristabot2.rhythm.games.SneakySpirits2;

public class Games {

	private static Games instance;

	private Games() {
	}

	public static Games instance() {
		if (instance == null) {
			instance = new Games();
			instance.loadResources();
		}
		return instance;
	}

	public final HashMap<String, RhythmGame> map = new HashMap<>();

	private void loadResources() {
		add("sneakyspirits2", new SneakySpirits2());
	}

	public void add(String key, RhythmGame game) {
		map.put(key, game);
	}

	public static RhythmGame getRhythmGame(String key) {
		return instance().map.get(key);
	}

}
