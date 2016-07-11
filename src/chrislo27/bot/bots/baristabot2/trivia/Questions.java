package chrislo27.bot.bots.baristabot2.trivia;

import java.util.ArrayList;
import java.util.List;

import chrislo27.bot.Main;
import chrislo27.bot.bots.baristabot2.trivia.Question.CorrectAnswer;
import chrislo27.bot.util.Utils;

public class Questions {

	private static Questions instance;

	private Questions() {
	}

	public static Questions instance() {
		if (instance == null) {
			instance = new Questions();
			instance.shouldReload = true;
		}

		if (instance.shouldReload) {
			instance.loadResources();
			instance.shouldReload = false;
		}

		return instance;
	}

	public final ArrayList<Question> questions = new ArrayList<>();
	private boolean shouldReload = true;

	private void loadResources() {
		questions.clear();
		Question.resetIdReg();

		// questions.add(new Question("", null));
		questions.add(new Question(
				"Are the songs in Big Rock Finish randomized? *(excluding start and end)*", null,
				"Yes for DS, No for Megamix", "No for DS, Yes for Megamix", "No for DS and Megamix",
				new CorrectAnswer("Yes for DS and Megamix")));
		questions.add(new Question("What control method is used for First/Second Contact?", null,
				"A only", "A and D-Pad right", "A and D-Pad left", "A and D-Pad up",
				"A and D-Pad down", new CorrectAnswer("A and D-Pad")));
		questions.add(new Question("How many are in the Clappy Trio?", null, "2",
				new CorrectAnswer("3"), "4", new CorrectAnswer("3.5")));
		//questions.add(new Question("", null));

		shouldReload = false;
	}

	/**
	 * 
	 * @param exclude List of IDs to exclude, should have no duplicates
	 * @return null if couldn't find any, or the questoin
	 */
	public static Question getRandomQuestion(List<Integer> exclude) {
		Questions instance = instance();

		// ran out of questions
		if (instance.questions.size() <= exclude.size()) return null;

		int i;
		while (exclude.contains(i = Utils.random.nextInt(instance.questions.size())))
			;

		return instance.questions.get(i);
	}

	public void forceReload() {
		shouldReload = true;
	}

}
