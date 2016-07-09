package chrislo27.bot.bots.baristabot2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Stream;

public class IdleTexts {

	public static final ArrayList<String> texts = new ArrayList<>();
	private static int cycle = 0;

	static {
		refresh();
	}

	public static void refresh() {
		cycle = 0;
		texts.clear();

		try (Stream<String> stream = Files.lines(Paths.get("resources/idlePlaying.txt"))) {
			stream.forEach((String s) -> {
				if (!s.isEmpty()) texts.add(s);
			});
		} catch (IOException e) {
			e.printStackTrace();
		}

		Collections.shuffle(texts);
	}

	public static String cycleNext() {
		cycle++;
		if (cycle >= texts.size()) {
			cycle = 0;
			Collections.shuffle(texts);
		}

		return texts.get(cycle);
	}

}
