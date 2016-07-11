package chrislo27.bot.bots.baristabot2.trivia;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Question {

	private static int idRegistration = 0;

	public final int id;
	public final String question;
	public final String author;
	public final List<Answer> answers;

	public Question(String question, String author, Object... answers) {
		if (answers.length < 2 || answers.length > 26) throw new IllegalArgumentException(
				"Must have between 2 to 26 answers! (Got " + answers.length + ")");

		int correctAnswers = 0;
		for (Object o : answers) {
			if (o instanceof Answer && ((Answer) o).isRight) correctAnswers++;
		}

		if (correctAnswers < 1)
			throw new IllegalArgumentException("Must have at least one correct answer!");

		this.question = question;
		this.author = author;

		this.answers = new ArrayList<Answer>();
		for (Object o : answers) {
			if (o instanceof String) {
				this.answers.add(new Answer((String) o, false));
			} else if (o instanceof Answer) {
				this.answers.add((Answer) o);
			}
		}

		id = idRegistration++;
	}

	public final Question shuffleAnswers() {
		Collections.shuffle(answers);

		return this;
	}

	protected static void resetIdReg() {
		idRegistration = 0;
	}

	public static class Answer {

		public final String answer;
		public final boolean isRight;

		public Answer(String answer, boolean right) {
			this.answer = answer;
			this.isRight = right;
		}
	}

	public static class CorrectAnswer extends Answer {

		public CorrectAnswer(String answer) {
			super(answer, true);
		}

	}

}
