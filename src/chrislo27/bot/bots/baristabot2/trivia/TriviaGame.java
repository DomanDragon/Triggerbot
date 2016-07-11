package chrislo27.bot.bots.baristabot2.trivia;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import chrislo27.bot.Main;
import chrislo27.bot.bots.baristabot2.BaristaBot2;
import chrislo27.bot.bots.baristabot2.trivia.Question.Answer;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.MessageBuilder;

public class TriviaGame {

	public static final int QUESTION_TIME_LIMIT = 10;
	public static final int LEADERBOARD_DISPLAY_TIME = 5;
	public static final int ANSWER_DISPLAY_DELAY = 5;
	public static final int MAX_QUESTION_SCORE = 1000;

	public final BaristaBot2 bot;
	public final int numberOfQuestions;
	public final IChannel channel;

	private int ticksElapsed = 0;

	private Question current = null;
	/**
	 * Used to judge score more finely than the tick rate.
	 */
	private long timeQuestionStarted = 0;

	private int postPossibleAnswersAt = -1;
	private int endQuestionAt = -1;
	private int startNextQuestionAt = 0;

	/**
	 * Player scores
	 */
	private HashMap<String, Score> playerScores = new HashMap<>();

	private ArrayList<Integer> alreadyDoneIds = new ArrayList<>();
	private int questionsCompleted = 0;

	public TriviaGame(BaristaBot2 bot, int numOfQuestions, IChannel channel) {
		if (numOfQuestions < 1 || numOfQuestions > Questions.instance().questions.size())
			throw new IllegalArgumentException("Number of questions must be in range of 1 to "
					+ Questions.instance().questions.size() + ". (Got " + numOfQuestions + ")");

		if (channel.isPrivate())
			throw new IllegalArgumentException("Cannot host game in a private channel.");

		this.bot = bot;
		this.numberOfQuestions = numOfQuestions;
		this.channel = channel;
	}

	public void tickUpdate() {
		if (questionsCompleted == numberOfQuestions) {
			finishGame();
		} else if (ticksElapsed == postPossibleAnswersAt) {
			postPossibleAnswers();
		} else if (ticksElapsed == endQuestionAt) {
			endQuestionAndPostResults();
		} else if (ticksElapsed == startNextQuestionAt) {
			startNewQuestion();
		} else if (ticksElapsed == endQuestionAt - (Main.TICK_RATE * 3)) {
			bot.sendMessage(bot.getNewBuilder(channel).appendContent("**Three seconds left!**"));
		}

		ticksElapsed++;
	}

	public void startNewQuestion() {
		current = Questions.getRandomQuestion(alreadyDoneIds);
		alreadyDoneIds.add(current.id);
		current.shuffleAnswers();

		postPossibleAnswersAt = ticksElapsed + (ANSWER_DISPLAY_DELAY * Main.TICK_RATE);
		endQuestionAt = postPossibleAnswersAt + (QUESTION_TIME_LIMIT * Main.TICK_RATE);
		timeQuestionStarted = System.currentTimeMillis() + (ANSWER_DISPLAY_DELAY * 1000);

		MessageBuilder builder = bot.getNewBuilder(channel);

		builder.appendContent("Question."
				+ (current.author != null ? " (by " + current.author + ")" : "") + "\n");
		builder.appendContent("**" + current.question + "**\n");
		builder.appendContent(
				"*The answers will appear in " + ANSWER_DISPLAY_DELAY + " seconds...*");

		bot.sendMessage(builder);
	}

	public void postPossibleAnswers() {
		MessageBuilder builder = bot.getNewBuilder(channel);

		builder.appendContent("*" + QUESTION_TIME_LIMIT + " seconds to answer:*\n");

		for (int i = 0; i < current.answers.size(); i++) {
			Answer a = current.answers.get(i);

			builder.appendContent("`" + ((char) ('A' + i)) + ".` " + a.answer + "\n");
		}

		bot.sendMessage(builder);
	}

	public void endQuestionAndPostResults() {
		MessageBuilder builder = bot.getNewBuilder(channel);

		builder.appendContent("The answer was **");
		boolean first = true;
		for (int i = 0; i < current.answers.size(); i++) {
			if (current.answers.get(i).isRight == false) continue;

			if (!first) {
				builder.appendContent(", ");
			} else {
				first = false;
			}

			builder.appendContent(current.answers.get(i).answer);
		}

		builder.appendContent("**.\n\n");

		for (Score s : playerScores.values()) {
			s.points += ((int) (s.currentQuestionTime * MAX_QUESTION_SCORE));
		}

		showLeaderboard(builder);

		if (questionsCompleted < numberOfQuestions - 1) {
			builder.appendContent(
					"\nThe next question begins in " + LEADERBOARD_DISPLAY_TIME + " seconds.");
			startNextQuestionAt = ticksElapsed + (LEADERBOARD_DISPLAY_TIME * Main.TICK_RATE);
		} else {
			builder.appendContent("\n**That's the end of the game!**");
		}

		bot.sendMessage(builder);

		questionsCompleted++;
		current = null;
		for (Score s : playerScores.values()) {
			s.currentQuestionTime = 0;
		}
	}

	public void finishGame() {
		Main.info("Finished this trivia game");
		bot.setCurrentTrivia(null);
	}

	public void showLeaderboard(MessageBuilder builder) {
		List<Score> leaderboard = getLeaderboard();

		builder.appendContent("__Leaderboard:__\n");
		for (int i = 0; i < leaderboard.size(); i++) {
			Score s = leaderboard.get(i);

			builder.appendContent((i + 1) + ". " + s.playerName + " with " + s.points + " points");

			if (s.currentQuestionTime > 0) {
				builder.appendContent(
						" **+" + ((int) (MAX_QUESTION_SCORE * s.currentQuestionTime)) + "**");
			}

			builder.appendContent(" [" + s.correct + ":" + s.wrong + ", ");
			if (s.wrong == 0) {
				if (s.correct == 0) {
					builder.appendContent("Answered none");
				} else {
					builder.appendContent("Perfect");
				}
			} else {
				builder.appendContent(((int) ((s.correct * 1f) / s.wrong)) + "%");
			}

			builder.appendContent("]\n");
		}
	}

	public List<Score> getLeaderboard() {
		ArrayList<Score> score = new ArrayList<>();

		for (Score s : playerScores.values()) {
			score.add(s);
		}

		Collections.sort(score);
		return score;
	}

	/**
	 * Checks for answers only.
	 * @param event
	 */
	@EventSubscriber
	public void onMessageGet(MessageReceivedEvent event) {
		IMessage messageObj = event.getMessage();
		IChannel channel = messageObj.getChannel();
		String message = messageObj.getContent().toLowerCase();
		IUser user = messageObj.getAuthor();

		if (message.length() != 1) return;
		if (!this.channel.equals(channel)) return;
		if (current == null) return;

		char letter = message.charAt(0);

		if (!(letter >= 'a' && letter <= 'a' + (current.answers.size() - 1))) return;

		// add score if not in leaderboard yet
		if (!playerScores.containsKey(user.getID()))
			playerScores.put(user.getID(), new Score(user.getName()));

		Score score = playerScores.get(user.getID());

		int answerIndex = letter - 'a';

		// check correctness
		final boolean wasRight;
		if (current.answers.get(answerIndex).isRight) {
			wasRight = true;
		} else {
			wasRight = false;
		}

		if (wasRight) {
			score.correct++;
			// set question score
			score.currentQuestionTime = (System.currentTimeMillis() - timeQuestionStarted)
					/ (QUESTION_TIME_LIMIT * 1000f);
		} else {
			score.wrong++;
			score.currentQuestionTime = 0;
		}
	}

	public static class Score implements Comparable<Score> {

		public final String playerName;
		public int points = 0;
		public int correct = 0;
		public int wrong = 0;

		/**
		 * Used for the current question: 
		 * gets the player's percentage of where they answered correctly in the question's time frame - 0 is miss
		 */
		public float currentQuestionTime = 0;

		public Score(String name) {
			playerName = name;
		}

		@Override
		public int compareTo(Score o) {
			if (this.points < o.points) return 1;
			if (this.points > o.points) return -1;

			return 0;
		}

	}

}
