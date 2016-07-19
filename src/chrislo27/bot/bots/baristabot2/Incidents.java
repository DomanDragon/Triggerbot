package chrislo27.bot.bots.baristabot2;

import java.util.ArrayList;

public class Incidents {

	public static final ArrayList<String> incidents = new ArrayList<>();

	static {
		refresh();
	}

	public static void refresh() {
		incidents.clear();

		incidents.add("RHINO Yelling Prohibition");
		incidents.add("RHINO *dodges* a bad idea");
		incidents.add("RHINO Disaster");
		incidents.add("Bouncy Road");
		incidents.add("Love Lab Controls");
	}

}
