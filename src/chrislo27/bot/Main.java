package chrislo27.bot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

import chrislo27.bot.bots.Bot;
import chrislo27.bot.util.BotServerPermissions;
import sx.blah.discord.Discord4J;
import sx.blah.discord.Discord4J.Discord4JLogger;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MessageBuilder.Styles;
import sx.blah.discord.util.RateLimitException;

public class Main {

	private static final SimpleDateFormat dateformat = new SimpleDateFormat(
			"MMMM dd yyyy HH:mm:ss");
	public static MessageBuilder outputMessageBuilder = null;
	private static boolean shouldKillThreads = false;
	public static final int TICK_RATE = 5;
	public static int ticks = 0;
	public static float lastTickDelta = 0;
	public static boolean shouldExit = false;
	private static PrintWriter consoleOutput = null;

	public static String getTimestamp() {
		return "[" + dateformat.format(Calendar.getInstance().getTime()) + "]";
	}

	public static void info(String s) {
		String out = getTimestamp() + " [INFO] " + s;
		System.out.println(out);

		if (outputMessageBuilder != null) {
			outputMessageBuilder.appendContent(out + "\n", Styles.CODE);
		}

		if (consoleOutput != null) {
			consoleOutput.println(out);
		}
	}

	public static void warn(String s) {
		String out = getTimestamp() + " [WARN] " + s;
		System.out.println(out);

		if (outputMessageBuilder != null) {
			outputMessageBuilder.appendContent(out + "\n", Styles.CODE);
		}

		if (consoleOutput != null) {
			consoleOutput.println(out);
		}
	}

	public static void error(String s) {
		String out = getTimestamp() + " [ERROR] " + s;
		System.out.println(out);

		if (outputMessageBuilder != null) {
			outputMessageBuilder.appendContent(out + "\n", Styles.CODE);
		}

		if (consoleOutput != null) {
			consoleOutput.println(out);
		}
	}

	public static void debug(String s) {
		String out = getTimestamp() + " [DEBUG] " + s;
		System.out.println(out);

		if (outputMessageBuilder != null) {
			outputMessageBuilder.appendContent(out + "\n", Styles.CODE);
		}

		if (consoleOutput != null) {
			consoleOutput.println(out);
		}
	}

	public static void main(String[] args) throws InstantiationException, IllegalAccessException,
			DiscordException, InterruptedException {
		if (args.length < 1) {
			error("No bot name!");

			return;
		} else if (args.length < 2) {
			error("No token!");

			return;
		}

		Class<? extends Bot> clzz = Bots.get(args[0]);

		if (clzz == null) {
			error("Bot not found! " + args[0]);

			return;
		}

		new File("consoleLogs/").mkdir();
		try {
			String date = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss")
					.format(new Date(System.currentTimeMillis()));
			String fileName = "consoleLogs/" + date + ".txt";
			new File(fileName).createNewFile();
			consoleOutput = new PrintWriter(new FileWriter(fileName, true), true);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		info("Starting with " + clzz.getSimpleName() + "...");

		((Discord4J.Discord4JLogger) Discord4J.LOGGER).setLevel(Discord4JLogger.Level.INFO);

		Bot bot = clzz.newInstance();
		IDiscordClient discordClient = getClient(args[1], true);
		bot.setClient(discordClient);

		EventDispatcher dispatcher = discordClient.getDispatcher();
		dispatcher.registerListener(bot);

		int permission = 0;
		permission |= BotServerPermissions.CREATE_INSTANT_INVITE;
		permission |= BotServerPermissions.READ_MESSAGES;
		permission |= BotServerPermissions.SEND_MESSAGES;
		permission |= BotServerPermissions.EMBED_LINKS;
		permission |= BotServerPermissions.READ_MESSAGE_HISTORY;
		permission |= BotServerPermissions.MENTION_EVERYONE;
		permission |= BotServerPermissions.CONNECT;
		permission |= BotServerPermissions.SPEAK;
		permission |= BotServerPermissions.CHANGE_NICKNAME;

		info("Bot invite link: " + "https://discordapp.com/oauth2/authorize?client_id="
				+ discordClient.getApplicationClientID() + "&scope=bot&permissions=" + permission);

		Thread tickUpdate = new Thread("Tick Update") {

			@Override
			public void run() {
				long lastTime = System.nanoTime();

				Main.info("Starting tick update thread");

				while (!shouldKillThreads) {
					try {
						float delta = (System.nanoTime() - lastTime) / 1_000_000_000f;
						lastTickDelta = delta;

						try {
							bot.tickUpdate(delta);
						} catch (Exception e) {
							Main.error("Error during tick update " + ticks);
							e.printStackTrace();
						}

						ticks++;
						lastTime = System.nanoTime();
						long sleepTime = (long) (1000f / TICK_RATE);
						sleep(sleepTime);
					} catch (Exception e) {
						Main.error("Error during tick sleep " + ticks);
						e.printStackTrace();
					}
				}

				Main.info("Ended tick update thread");
			}
		};
		tickUpdate.setDaemon(true);

		while (!discordClient.isReady()) {

		}

		tickUpdate.start();

		Thread scanThread = new Thread("Scanning Input") {

			@Override
			public void run() {
				Scanner scanner = new Scanner(System.in);
				while (scanner.hasNextLine()) {
					String input = scanner.hasNextLine() ? scanner.nextLine() : "exit";

					if (input.equalsIgnoreCase("exit")) {
						break;
					}
				}

				scanner.close();
				shouldExit = true;
			}
		};
		scanThread.setDaemon(true);
		scanThread.start();

		while (!shouldExit) {
			Thread.sleep(500L);
		}

		info("Exiting program...");

		// give time to send messages
		Thread.sleep(1000);

		shouldKillThreads = true;
		dispatcher.unregisterListener(bot);

		if (discordClient.isReady()) {
			info("Logging out...");

			boolean loggedOut = false;

			while (!loggedOut) {
				try {
					discordClient.logout();

					loggedOut = true;
				} catch (RateLimitException e) {
					e.printStackTrace();
					warn("Exceeded rate limit, must wait " + e.getRetryDelay() + " ms");

					Thread.sleep(e.getRetryDelay() + 500);
					info("Retrying logout...");
				}
			}

			info("Logged out successfully.");
		}

		bot.onProgramExit();

		info("Goodbye!");

		consoleOutput.close();
		consoleOutput = null;
		System.exit(0);
	}

	public static IDiscordClient getClient(String token, boolean login) throws DiscordException { //Returns an instance of the discord client
		ClientBuilder clientBuilder = new ClientBuilder(); //Creates the ClientBuilder instance
		clientBuilder.withToken(token);
		if (login) {
			return clientBuilder.login(); //Creates the client instance and logs the client in
		} else {
			return clientBuilder.build(); //Creates the client instance but it doesn't log the client in yet, you would have to call client.login() yourself
		}
	}

}
