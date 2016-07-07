package chrislo27.bot.util;

import java.util.Random;

public class EightBall {

	private static final String[] responses = { "It is certain", "It is decidedly so",
			"Without a doubt", "Yes, definitely", "You may rely on it", "As I see it, yes",
			"Most likely", "Outlook good", "Yes", "Signs point to yes", "Reply hazy, try again",
			"Ask again later", "Better not tell you now", "Cannot predict now",
			"Concentrate and ask again", "Don't count on it", "My reply is no", "My sources say no",
			"Outlook not so good", "Very doubtful" };
	private static final Random random = new Random();

	public static String getResponse() {
		return responses[random.nextInt(responses.length)];
	}

}
