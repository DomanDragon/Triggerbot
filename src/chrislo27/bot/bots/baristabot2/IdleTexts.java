package chrislo27.bot.bots.baristabot2;

import java.util.ArrayList;
import java.util.Collections;

public class IdleTexts {

	public static final ArrayList<String> texts = new ArrayList<>();
	private static int cycle = 0;

	static {
		refresh();
	}

	public static void refresh() {
		cycle = 0;
		texts.clear();

		texts.add("RHINO");
		texts.add("ウラオモテ");
		texts.add("RHA");

		Collections.shuffle(texts);
	}

	public static String cycleNext() {
		if (texts.size() < 2) return "REQUIRES MORE THAN TWO";

		cycle++;
		if (cycle >= texts.size()) {
			cycle = 0;
			Collections.shuffle(texts);
		}

		return texts.get(cycle);
	}

}
