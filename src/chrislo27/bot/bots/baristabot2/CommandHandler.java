package chrislo27.bot.bots.baristabot2;

import chrislo27.bot.Main;
import chrislo27.bot.MusicDatabase;
import chrislo27.bot.MusicDatabase.Song;
import chrislo27.bot.bots.baristabot2.transit.TransitSystems;
import chrislo27.bot.bots.baristabot2.transit.TransitSystems.LineEdge;
import chrislo27.bot.bots.baristabot2.trivia.Questions;
import chrislo27.bot.bots.baristabot2.trivia.TriviaGame;
import chrislo27.bot.util.EightBall;
import chrislo27.bot.util.Utils;
import chrislo27.bot.util.WanaKanaJava;
import org.jgrapht.alg.DijkstraShortestPath;
import sx.blah.discord.Discord4J;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.handle.obj.IMessage.Attachment;
import sx.blah.discord.util.*;
import sx.blah.discord.util.MessageBuilder.Styles;
import sx.blah.discord.util.audio.AudioPlayer.Track;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

public class CommandHandler {

	public final BaristaBot2 bot;

	public CommandHandler(BaristaBot2 bot) {
		this.bot = bot;
	}

	public void addNormalHelpToBuilder(MessageBuilder builder) {
		builder.appendContent("**__Normal commands:__**\n");
		builder.appendContent("*Use three percents %%% to embed a command to be ran*\n");
		builder.appendContent(
				"%help/? [trusted/mod/moderator/admin/radio/music/trivia] - Shows this message or the desired help " +
						"page\n");
		builder.appendContent("%info - What does this bot do?\n");
		builder.appendContent("%woof - woof\n");
		builder.appendContent("%hi/hello - Hello!\n");
		builder.appendContent(
				"%reaction/react/img [image] - Posts a reaction picture or displays the list, if no image provided\n");
		builder.appendContent("%8ball/8-ball - Ask the magic 8-ball\n");
		builder.appendContent("%uptime - View how long the bot has been up\n");
		builder.appendContent("%stats - View miscellaneous statistics\n");
		builder.appendContent("%incidents - View past incidents\n");
		builder.appendContent("%rps <rock/paper/scissors/r/p/s> - Play rock paper scissors\n");
		builder.appendContent("@" + bot.client.getOurUser().getName()
				+ " <text> - Talk to the barista (Uses Cleverbot)\n");
		builder.appendContent("%timeline - View the server's timeline\n");
		//		builder.appendContent(
		//				"%japaneseify <romaji> - Converts Romaji to Katakana, use for English loanwords and such");
		//		builder.appendContent(
		//				"%toKana <romaji> - Converts Romaji to Japanese Kana (use only if you know what you're doing!)
		// - "
		//						+ "Use lowercase for Hiragana (ひらがな), and capitals for Katakana (カタカナ) - "
		//						+ "Powered by `https://github.com/MasterKale/WanaKanaJava`\n");
		builder.appendContent("%shippingforecast - Gets the shipping forecast\n");
		builder.appendContent("%timetravel [ms time] - Time travel\n");
		builder.appendContent("%girafferator [troger/giraffe/judge] [size multiplier] [positionX offset in pixels] " +
				"[positionY " +
				"offset" +
				" " +
				"in pixels]\n");
	}

	public void addTrustedHelpToBuilder(MessageBuilder builder) {
		builder.appendContent("**__Trusted commands:__**\n");
		builder.appendContent("%sfx <name> [any more] - Plays your SFX in the music queue\n");
		builder.appendContent("%sfxlist/sfxdatabase/sfxdb - Displays SFX list\n");
	}

	public void addModHelpToBuilder(MessageBuilder builder) {
		builder.appendContent("**__Moderator commands:__**\n");
		builder.appendContent("%getid <name> - Gets the ID of users containing the name");
		builder.appendContent(
				"%getusername <id> - Gets the name and discriminator from their ID\n");
		builder.appendContent("%listpermissions - Lists people's permissions, along with tiers\n");
		builder.appendContent(
				"%insertsong <index> <song> - Inserts the song at the place number, bypasses queue limit\n");
		builder.appendContent("%removesong <index> - Removes the song at the place number\n");
		builder.appendContent(
				"%reconnectaudio - Attempts to reconnect the audio system and channel\n");
		builder.appendContent("%clearqueue - Clears the queue\n");
	}

	public void addAdminHelpToBuilder(MessageBuilder builder) {
		builder.appendContent("**__Administrator commands:__**\n");
		builder.appendContent("%exit/quit - Logs off and quits the bot\n");
		builder.appendContent("%setpermissions <id> <level> - Sets the user's permission level\n");
		builder.appendContent("%refreshdb/refreshdatabase - Refreshes song database\n");
		builder.appendContent("%togglequeue - Toggles queuing\n");
		builder.appendContent(
				"%tempban/arrest <id> <seconds> [+] - Temporarily bans someone for the duration/additive\n");
		builder.appendContent("%username <name> - Sets the bot's username\n");
		builder.appendContent("%bitrate [value] - Gets or sets the bitrate of the radio channel\n");
		builder.appendContent(
				"%debug - Toggles debug mode, which stops non-admins from doing music actions\n");
		builder.appendContent(
				"%setstatus/setplaying [status] - Sets the status, or removes it (cannot override debug status)\n");
		builder.appendContent("%idle - Toggles idle status light\n");
		builder.appendContent("%senddistress - Sends a test distress signal\n");
		builder.appendContent("%say <channelID> <message> - Say a thing\n");
		builder.appendContent("%senddm <id> <message> - Send a DM\n");
		builder.appendContent(
				"%movetovoicechannel [id] - Move to your first-connected or specified voice channel\n");
		builder.appendContent("%popcorn - Purchase popcorn\n");
		builder.appendContent("%perms [id] - Gets the ID of either you, or the ID provided\n");
	}

	public void addMusicHelpToBuilder(MessageBuilder builder) {
		// music
		builder.appendContent("**__Music commands__** *(please do only in <#"
				+ BaristaBot2.IDEAL_CHANNEL + ">):*\n");
		builder.appendContent("%play/queue <song name> - Queues the song of your choice\n");
		builder.appendContent(
				"%random [#] [criteria]- Queues a random song or as many as you want up to the limit ("
						+ BaristaBot2.RANDOM_LIMIT
						+ "), optional criteria will only match song **names** containing it\n");
		builder.appendContent("%shuffle - Shuffles the queue, ignoring the first song\n");
		builder.appendContent("%showqueue/nowplaying/np - Shows queue\n");
		builder.appendContent("%database/showdatabase/db <page> - Shows the song database\n");
		builder.appendContent("%search <key terms> - Searches for a song\n");
		builder.appendContent(
				"%skip - Vote to skip the current song (or retract your vote if you did already)\n");
	}

	public void addTriviaHelpToBuilder(MessageBuilder builder) {
		builder.appendContent("**__Trivia game commands for moderators+:__**\n");
		builder.appendContent("%trivia newgame <number of questions> - "
				+ "Starts a trivia game in the current channel (there can only be one at a time!)"
				+ " with the desired number of questions\n");
		builder.appendContent("%trivia endgame - Stops the current trivia game, if any\n");
		builder.appendContent("%trivia questions - Outputs the number of questions\n");
		builder.appendContent(
				"\n__How to play:__ *(very similar to Kahoot, if you've played that)*\n");
		builder.appendContent(
				"The quizmaster (the bot) will set out a question with the multiple choice answers.\n");
		builder.appendContent("Simply respond with the answer's letter (A-Z) to answer.\n");
		builder.appendContent(
				"**The __faster__ you answer correctly, the __more points__ you'll get! (between 500 to 1000)**\n");
		builder.appendContent(
				"You only get one answer per question. You __cannot__ change your answer!\n");
		builder.appendContent(
				"After the time limit expires (the bot will give a three second warning),"
						+ " the answer and the winners of the round will be announced,"
						+ " along with the new leaderboard.\n");
	}

	public String doCommand(String command, IMessage message, IChannel channel, IUser user,
							String[] args) {
		String caseCommand = command.toLowerCase();
		String musicRestricted = null;
		long permLevel = PermPrefs.getPermissionsLevel(user.getID());

		if (permLevel < 0) {
			return "You are tempbanned for "
					+ (((Math.abs(permLevel) - System.currentTimeMillis()) / 1000) + 1)
					+ " more second(s).";
		}

		switch (caseCommand) {
			case "help":
			case "?":
				if (permLevel < PermissionTier.NORMAL) {
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.NORMAL);
				} else {
					MessageBuilder builder = bot.getNewBuilder(channel);
					String page = args.length < 1 ? "" : args[0].toLowerCase();

					switch (page) {
						case "mod":
						case "moderator":
							addModHelpToBuilder(builder);
							break;
						case "trusted":
							addTrustedHelpToBuilder(builder);
							break;
						case "admin":
							addAdminHelpToBuilder(builder);
							break;
						case "radio":
						case "music":
							builder.appendContent("Here are the commands for music actions.\n");
							addMusicHelpToBuilder(builder);
							break;
						case "trivia":
							builder.appendContent("Here are the rules and commands for trivia.\n");
							addTriviaHelpToBuilder(builder);
							break;
						default:
							addNormalHelpToBuilder(builder);
							break;
					}

					bot.sendMessage(builder);
				}
				return null;
			case "woof":
				if (permLevel < PermissionTier.NORMAL)
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.NORMAL);

				bot.sendMessage(bot.getNewBuilder(channel).appendContent(
						"wœuf (Response time: " + message.getClient().getResponseTime() + " ms)"));
				return null;
			case "hi":
			case "hello":
				if (permLevel < PermissionTier.NORMAL)
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.NORMAL);
				bot.sendMessage(
						bot.getNewBuilder(channel).appendContent("Hello " + user.mention() + "!"));
				return null;
			case "reaction":
			case "react":
			case "img":
				if (permLevel < PermissionTier.NORMAL)
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.NORMAL);
				if (args.length < 1) {
					MessageBuilder builder = bot.getNewBuilder(channel).appendContent(
							user.mention() + " __Allowed reaction/img arguments:__\n\n`");

					builder.appendContent("wot, ");
					builder.appendContent("wotdraw, ");
					builder.appendContent("wotserver, ");
					builder.appendContent("wotservernew, ");
					builder.appendContent("salt, ");
					builder.appendContent("donk, ");
					builder.appendContent("zodiackiller, ");
					builder.appendContent("sickbeats, ");
					builder.appendContent("blame, ");
					builder.appendContent("uncle, ");
					builder.appendContent("pgj, ");
					builder.appendContent("273rdcontact, ");
					builder.appendContent("ohyes|justright|*justright*, ");
					builder.appendContent("ilovetap, ");
					builder.appendContent("splendidanswers, ");
					builder.appendContent("ilovetap.jpg, ");
					builder.appendContent("bluebear, ");
					builder.appendContent("x_x, ");
					builder.appendContent("275thcontact, ");
					builder.appendContent("gramps, ");
					builder.appendContent("kameksansbad");

					// TODO tmp +1
					if (permLevel >= PermissionTier.ADMIN + 1) {
						builder.appendContent("`\n\n__Mod+ only:__\n\n`");
						builder.appendContent("we didn't ask you, ");
						builder.appendContent("personality");
					}

					bot.sendMessage(builder.appendContent("`"));
					return null;
				} else {
					MessageBuilder builder = bot.getNewBuilder(channel);
					boolean send = true;

					switch (Utils.getContent(args, 0).toLowerCase()) {
						case "wot":
							builder.appendContent("http://i.imgur.com/rBafTiE.png");
							break;
						case "wotdraw":
							builder.appendContent("http://i.imgur.com/9M2XmwE.png");
							break;
						case "wotserver":
							builder.appendContent("http://i.imgur.com/ed56Odd.png");
							break;
						case "wotservernew":
							builder.appendContent("http://i.imgur.com/XfyO9ZI.png");
							break;
						case "salt":
							builder.appendContent("http://i.imgur.com/43ibnvL.jpg");
							break;
						case "donk":
							builder.appendContent("http://i.imgur.com/Gz1aOi1.png");
							break;
						case "zodiackiller":
							builder.appendContent("http://i.imgur.com/Dis6XfB.jpg");
							break;
						case "sickbeats":
							builder.appendContent("http://i.imgur.com/Ie3KDQ2.jpg");
							break;
						case "blame":
							builder.appendContent("http://i.imgur.com/HFHj456.jpg");
							break;
						case "uncle":
							builder.appendContent("http://i.imgur.com/RBYEFE1.png");
							break;
						case "pgj":
							builder.appendContent("http://i.imgur.com/tUBkEbO.png");
							break;
						case "273rdcontact":
							builder.appendContent("http://i.imgur.com/qKaXnoQ.png");
							break;
						case "ohyes":
						case "*justright*":
						case "justright":
							builder.appendContent("http://i.imgur.com/2hFqWyM.png");
							break;
						case "ilovetap":
							builder.appendContent("http://i.imgur.com/8JzsqRR.png");
							break;
						case "splendidanswers":
							builder.appendContent("http://i.imgur.com/UoAFy1c.png");
							break;
						case "ilovetap.jpg":
							builder.appendContent("http://i.imgur.com/ZLPRZJz.jpg");
							break;
						case "bluebear":
							builder.appendContent("http://i.imgur.com/pHXyBvL.gif");
							break;
						case "x_x":
							builder.appendContent("http://i.imgur.com/jPCB6tx.png");
							break;
						case "275thcontact":
							builder.appendContent("http://i.imgur.com/nCvliS9.png");
							break;
						case "gramps":
							builder.appendContent("http://i.imgur.com/hGxqSDD.png");
							break;
						case "kameksansbad":
							builder.appendContent("http://i.imgur.com/AFAkqAC.jpg");
							break;
						case "we didn't ask you":
							if (permLevel < PermissionTier.MODERATOR) {
								send = false;
								break;
							}
							builder.appendContent("http://i.imgur.com/z0PFkCo.png");
							break;
						case "personality":
							if (permLevel < PermissionTier.MODERATOR) {
								send = false;
								break;
							}
							builder.appendContent("http://i.imgur.com/9kq4Wes.png");
							break;
						default:
							send = false;
							break;
					}

					if (send) {
						bot.sendMessage(builder);
						return null;
					}
				}

				return "Couldn't find the image you wanted (" + Utils.getContent(args, 0) + ")!";
			case "8-ball":
			case "8ball":
				if (permLevel < PermissionTier.NORMAL)
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.NORMAL);
				bot.sendMessage(bot.getNewBuilder(channel).appendContent(":8ball:  ")
						.appendContent(EightBall.getResponse(), Styles.INLINE_CODE));
				return null;
			case "uptime":
				if (permLevel < PermissionTier.NORMAL) {
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.NORMAL);
				} else {
					MessageBuilder stats = bot.getNewBuilder(channel);

					long startTimeDiff = (System.currentTimeMillis() - bot.startTime.getTime()) / 1000;

					if (bot.audioPlayer != null && bot.audioPlayer.getPlaylist().size() > 0) {
						bot.secondsPlaying += Math
								.abs(System.currentTimeMillis() - bot.playingStartTime) / 1000.0D;
						bot.playingStartTime = System.currentTimeMillis();
					}

					stats.appendContent(user.mention() + " __Uptime:__\n");
					stats.appendContent("Started on: " + bot.startTime.toString() + "\n");
					stats.appendContent("Current uptime: __"
							+ TimeUnit.DAYS.convert(startTimeDiff, TimeUnit.SECONDS) + " days__, __"
							+ TimeUnit.HOURS.convert(startTimeDiff, TimeUnit.SECONDS) % 24
							+ " hours__, __"
							+ TimeUnit.MINUTES.convert(startTimeDiff, TimeUnit.SECONDS) % 60
							+ " minutes__\n");

					stats.appendContent(
							"Music streamed since uptime start: __"
									+ TimeUnit.DAYS.convert((long) bot.secondsPlaying, TimeUnit.SECONDS)
									+ " days__, __"
									+ TimeUnit.HOURS.convert((long) bot.secondsPlaying,
									TimeUnit.SECONDS) % 24
									+ " hours__, __"
									+ TimeUnit.MINUTES.convert((long) bot.secondsPlaying,
									TimeUnit.SECONDS) % 60
									+ " minutes__, or about __"
									+ String.format("%.2f",
									(bot.secondsPlaying * bot.radioChannel.getBitrate()) / 8 / 1024
											/ 1024)
									+ " MB__ (" + bot.radioChannel.getBitrate() / 1000f + " kbps)\n");

					bot.sendMessage(stats);
				}
				return null;
			case "stats":
				if (permLevel < PermissionTier.NORMAL) {
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.NORMAL);
				} else {
					MessageBuilder stats = bot.getNewBuilder(channel);

					stats.appendContent("__Stats:__\n");
					stats.appendContent("Current Discord4J version: " + Discord4J.VERSION + "\n");

					// TODO add more
					{
						int peopleListening = 0;
						for (IUser u : bot.radioChannel.getUsersHere()) {
							if (u.isDeafLocally() || u.isBot() || u.isDeaf(bot.radioChannel.getGuild()))
								continue;
							peopleListening++;
						}

						stats.appendContent("People actively (not deafened) listening in the radio: "
								+ peopleListening + (peopleListening == 0 ? " :(" : "") + "\n");
					}
					{

					}

					bot.sendMessage(stats);
				}
				return null;
			case "incidents":
				if (permLevel < PermissionTier.NORMAL) {
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.NORMAL);
				} else {
					MessageBuilder stats = bot.getNewBuilder(channel);

					stats.appendContent(user.mention() + "\n__Incidents:__ (newest last)\n");
					for (String s : Incidents.incidents) {
						stats.appendContent(s + "\n");
					}

					bot.sendMessage(stats);
				}
				return null;
			case "rps":
			case "rockpaperscissors":
				if (permLevel < PermissionTier.NORMAL) {
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.NORMAL);
				} else if (args.length < 1) {
					return "You need to play your hand!";
				} else {
					int pchoice = 0;
					int mychoice = (int) (Math.random() * 3);

					switch (args[0].toLowerCase()) {
						case "rock":
						case "r":
							pchoice = 0;
							break;
						case "paper":
						case "p":
							pchoice = 1;
							break;
						case "scissors":
						case "s":
							pchoice = 2;
							break;
						default:
							return "You can't play that! *what a cheater*";
					}

					MessageBuilder builder = bot.getNewBuilder(channel);

					builder.appendContent(user.mention() + " I chose __"
							+ (mychoice == 0 ? "rock" : (mychoice == 1 ? "paper" : "scissors")));
					builder.appendContent("__, you chose __"
							+ (pchoice == 0 ? "rock" : (pchoice == 1 ? "paper" : "scissors")) + "__. ");

					if (mychoice == pchoice) {
						builder.appendContent("It's a tie!");
					} else {
						if (mychoice == 0) {
							if (pchoice == 1) {
								builder.appendContent("You win!");
							} else {
								builder.appendContent("I win!");
							}
						} else if (mychoice == 1) {
							if (pchoice == 2) {
								builder.appendContent("You win!");
							} else {
								builder.appendContent("I win!");
							}
						} else {
							if (pchoice == 0) {
								builder.appendContent("You win!");
							} else {
								builder.appendContent("I win!");
							}
						}
					}

					bot.sendMessage(builder);
				}

				return null;
			case "timeline":
				if (permLevel < PermissionTier.NORMAL) {
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.NORMAL);
				}
				bot.sendMessage(bot.getNewBuilder(channel).appendContent("__Server Timeline:__\n")
						.appendContent("General Discussion\n" + "Repainted\n" + "Dead\n"
								+ "Kinda alive\n" + "Dead\n" + "General Discussion w/ Slight Shitpost\n"
								+ "Custom Remix Editors\n" + "Shitpost\n" + "RHINO Start\n"
								+ "RHINO End\n"

								+ "More Shitpost\n" + "Controlled Shitpost (now)"));
				return null;
			case "tokana":
				if (permLevel < PermissionTier.NORMAL) {
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.NORMAL);
				} else if (args.length < 1) {
					return "Requires romaji argument! Use lowercase for Hiragana, and uppercase for Katakana.";
				} else {
					String content = Utils.getContent(args, 0);

					bot.sendMessage(bot.getNewBuilder(channel).appendContent(
							user.mention() + " " + WanaKanaJava.globalInstance.toKana(content)));
				}
				return null;
			case "toromaji":
				if (permLevel < PermissionTier.NORMAL) {
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.NORMAL);
				} else if (args.length < 1) {
					return "Requires Japanese argument!";
				} else {
					String content = Utils.getContent(args, 0);

					bot.sendMessage(bot.getNewBuilder(channel).appendContent(
							user.mention() + " " + WanaKanaJava.globalInstance.toRomaji(content)));
				}
				return null;
			case "japaneseify":
				if (permLevel < PermissionTier.NORMAL) {
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.NORMAL);
				} else if (args.length < 1) {
					return "Requires romaji argument!";
				} else {
					String content = Utils.getContent(args, 0);

					bot.sendMessage(bot.getNewBuilder(channel).appendContent(
							user.mention() + " " + WanaKanaJava.globalInstance.toKatakana(content)));
				}
				return null;
			case "shippingforecast":
				if (permLevel < PermissionTier.NORMAL) {
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.NORMAL);
				} else {
					MessageBuilder builder = bot.getNewBuilder(channel);

					builder.appendContent("__Shipping Forecast Predictions (7-day):__\n");

					List<IUser> guildUsers = channel.getGuild().getUsers();
					ArrayList<IUser> onlineUsers = new ArrayList<>();

					for (IUser u : guildUsers) {
						if (u.getPresence() == Presences.ONLINE) {
							onlineUsers.add(u);
						}
					}

					builder.appendContent("Day 1: KamekSans x Whistler_420\n");
					for (int i = 1; i < 7; i++) {
						long time = System.currentTimeMillis() / (1000 * 60 * 60 * 24) + i;

						int one = (int) (time % onlineUsers.size());
						int two = (int) ((time + 24353) % onlineUsers.size());

						builder.appendContent("Day " + (i + 1) + ": " + onlineUsers.get(one).getName()
								+ " x " + onlineUsers.get(two).getName() + "\n");
					}

					bot.sendMessage(builder);
				}

				return null;
			case "girafferator":
				if (permLevel < PermissionTier.NORMAL) {
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.NORMAL);
				} else if (message.getAttachments().size() == 0) {
					return "No attachment found!";
				} else {
					String toUse = "giraffe";
					if (args.length > 0) {
						if (args[0].equalsIgnoreCase("troger")) {
							toUse = "troger";
						} else if (args[0].equalsIgnoreCase("judge")) {
							toUse = "judge";
						} else if (args[0].equalsIgnoreCase("giraffe")) {
							toUse = "giraffe";
						}
					}

					Attachment attachment = message.getAttachments().get(0);

					if (!(attachment.getFilename().toLowerCase().endsWith(".jpg")
							|| attachment.getFilename().toLowerCase().endsWith(".png") || attachment.getFilename()
							.toLowerCase().endsWith(".jpeg"))) {
						return "File must be png or jpg!";
					}

					if (attachment.getFilesize() > (2 * 1024 * 1024)) {
						return "File must be at most 2 MB!";
					}

					try {
						final HttpURLConnection connection = (HttpURLConnection) new URL(
								attachment.getUrl()).openConnection();
						connection.setRequestProperty("User-Agent",
								"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.31 (KHTML, like " +
										"Gecko)" +
										" Chrome/26.0.1410.65 Safari/537.31");
						BufferedImage sentIn = ImageIO.read(connection.getInputStream());
						BufferedImage paste = ImageIO
								.read(new File("resources/giraffe/" + toUse + ".png"));

						BufferedImage combined = new BufferedImage(sentIn.getWidth(),
								sentIn.getHeight(), BufferedImage.TYPE_INT_ARGB);

						Graphics g = combined.getGraphics();

						float x = 0;
						float y = 0;
						float size = Math.min(combined.getWidth(), combined.getHeight()) / 3;

						if (args.length >= 2) {
							try {
								size *= Float.parseFloat(args[1]);

								if (args.length >= 3)
									x = Float.parseFloat(args[2]);
								if (args.length >= 4)
									y = Float.parseFloat(args[3]);
							} catch (Exception e) {

							}
						}

						float width = size;
						float height = (paste.getHeight() * 1f / paste.getWidth()) * width;

						g.drawImage(sentIn, 0, 0, null);
						g.drawImage(paste, (int) (x), (int) ((combined.getHeight() / 2 - height / 2) + y), (int)
										(width),
								(int) (height), null);

						File output = new File("resources/giraffe/tmp.png");
						ImageIO.write(combined, "png", output);

						RequestBuffer.request(() -> {
							try {
								channel.sendFile(output, user.mention());
							} catch (IOException | MissingPermissionsException | DiscordException e) {
								e.printStackTrace();
							}

							output.delete();
						});
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				return null;
			case "transit":
				if (permLevel < PermissionTier.NORMAL) {
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.NORMAL);
				} else if (args.length < 1) {
					return "Requires start -> end arguments!";
				} else {
					int arrowIndex = -1;
					for (int i = 0; i < args.length; i++) {
						if (args[i].equals("->")) {
							arrowIndex = i;
							break;
						}
					}

					if (arrowIndex == -1)
						return "No arrow found!";

					String startStation = Utils.getContent(args, 0, arrowIndex);
					String endStation = Utils.getContent(args, arrowIndex + 1);

					TransitSystems systems = TransitSystems.instance();

					if (!systems.system.containsVertex(startStation))
						return "Start station not found! `" + startStation + "`";
					if (!systems.system.containsVertex(endStation))
						return "End station not found! `" + endStation + "`";

					DijkstraShortestPath<String, LineEdge> path = systems.getPath(startStation,
							endStation);

					if (path.getPathLength() == Double.POSITIVE_INFINITY)
						return "No path found!";

					List<LineEdge> edges = path.getPathEdgeList();
					MessageBuilder builder = bot.getNewBuilder(channel);

					builder.appendContent(user.mention() + " Route from __" + startStation + "__ to __"
							+ endStation + "__:\n");
					builder.appendContent("Start at station __" + startStation + "__ on line **"
							+ edges.get(0).getLine() + "**\n");

					LineEdge<String> lastEdge = edges.get(0);
					String currentLine = edges.get(0).getLine();
					for (int i = 0; i < edges.size(); i++) {
						LineEdge<String> edge = edges.get(i);

						if (!currentLine.equalsIgnoreCase(edge.getLine())) {
							String sharedStation = null;
							if (lastEdge.getV1().equals(edge.getV1())) {
								sharedStation = edge.getV1();
							} else if (lastEdge.getV2().equals(edge.getV1())) {
								sharedStation = edge.getV1();
							} else if (lastEdge.getV2().equals(edge.getV2())) {
								sharedStation = edge.getV2();
							} else if (lastEdge.getV1().equals(edge.getV2())) {
								sharedStation = edge.getV2();
							}

							builder.appendContent("Change lines to **" + edge.getLine() + "** at __"
									+ sharedStation + "__\n");
						}

						currentLine = edge.getLine();
						lastEdge = edge;
					}

					builder.appendContent("Finish journey at __" + endStation + "__\n");

					bot.sendMessage(builder);
				}
				return null;
			case "timetravel":
				if (permLevel < PermissionTier.NORMAL) {
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.NORMAL);
				} else {
					long randTime = (long) (System.currentTimeMillis()
							+ (Utils.lerp(-1, 1, Utils.random.nextDouble()) * 1000 * 60 * 60 * 24 * 365
							* 100));

					if (args.length >= 1) {
						try {
							long l = Long.parseLong(args[0]);

							randTime = l;
						} catch (NumberFormatException e) {

						}
					}

					bot.sendMessage(bot.getNewBuilder(channel)
							.appendContent(user.mention() + " Travelling to `"
									+ new SimpleDateFormat("EEEE, MMMM d, yyyy HH:mm:ss")
									.format(new Date(randTime))
									+ "`"));

					return null;
				}
			case "info":
				if (permLevel < PermissionTier.NORMAL) {
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.NORMAL);
				} else {
					MessageBuilder builder = bot.getNewBuilder(channel);

					builder.appendContent(user.mention() + " __What does this bot do?__\n");
					builder.appendContent("It is a utility bot for the Rhythm Heaven Discord server.\n");
					builder.appendContent("It can play RH soundtracks in the Radio voice channel (`%help radio`) " +
							"among" +
							" other things.\n");
					builder.appendContent("It is made and maintained by chrislo27.");

					bot.sendMessage(builder);

					return null;
				}
		}

		// music
		switch (caseCommand) {
			case "\u25B6":
			case "play":
			case "queue":
				if (permLevel < PermissionTier.NORMAL)
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.NORMAL);
				if ((musicRestricted = bot.checkMusicRestricted(channel, user)) != null) {
					return musicRestricted;
				} else if (args.length < 1) {
					return "Requires a song!";
				} else {
					String entire = Utils.getContent(args, 0);
					//entire = "Bouncy Road";
					Song song = MusicDatabase.instance().files.get(entire.toLowerCase());

					if (entire.startsWith("!") && entire.endsWith("#"))
						song = null;

					if (song != null) {
						if (bot.emptyQueueIfAllGone(channel, bot.getNewBuilder(channel))) {

						} else {
							MessageBuilder builder = bot.getNewBuilder(channel);
							bot.queueAudio(channel, song, builder);
							bot.sendMessage(builder);
							bot.showQueue(channel);
						}
					} else {
						Main.info("Couldn't find " + entire + " in song database");

						String suggestions = entire.length() < 3 ? null
								: MusicDatabase.instance().getSuggestions(entire);
						MessageBuilder builder = bot.getNewBuilder(channel);
						builder.appendContent(
								"The music you want (" + entire + ") wasn't found in the database.\n");

						if (suggestions != null && !suggestions.isEmpty()) {
							builder.appendContent("*Did you mean:*\n");
							builder.appendContent(suggestions);
						}

						bot.sendMessage(builder);
					}
				}

				return null;
			case "skip":
				if (permLevel < PermissionTier.NORMAL)
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.NORMAL);
				if ((musicRestricted = bot.checkMusicRestricted(channel, user)) != null) {
					return musicRestricted;
				} else {
					return bot.voteToSkipTrackAndAct(user, channel);
				}
			case "random":
				if (permLevel < PermissionTier.NORMAL)
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.NORMAL);
				if ((musicRestricted = bot.checkMusicRestricted(channel, user)) != null) {
					return musicRestricted;
				} else {
					if (bot.emptyQueueIfAllGone(channel, null))
						return null;

					String criteria = args.length < 2 ? null : Utils.getContent(args, 1).toLowerCase();

					int number = 1;
					int requested = 1;
					if (args.length >= 1) {
						try {
							number = Integer.parseInt(args[0]);
							requested = number;
						} catch (Exception e) {
							number = 1;
						}
					}

					number = Utils.clamp(
							bot.audioPlayer == null ? BaristaBot2.RANDOM_LIMIT
									: Math.min(BaristaBot2.RANDOM_LIMIT,
									BaristaBot2.QUEUE_LIMIT - bot.audioPlayer.getPlaylistSize()),
							1, number);

					//criteria = "Bouncy Road";
					List<Entry<String, Song>> bound = MusicDatabase.instance().getSearch(criteria);
					Collections.shuffle(bound);

					number = Math.min(bound.size(), number);

					if (bound.size() == 0) {
						return "No music found with the criteria: " + criteria;
					}

					Main.info("Queuing " + number + " random song(s)"
							+ (criteria == null ? "" : " with criteria: " + criteria));

					MessageBuilder builder = bot.getNewBuilder(channel);

					// gets criteria list, shuffles, it, starts iterating through it until number is reached, or
					// current index is out of range
					int totalQueued = 0;
					outer:
					for (int i = 0, n = number, boundIndex = 0; i < n
							&& boundIndex < bound.size(); boundIndex++) {
						Entry<String, Song> e = bound.get(boundIndex);
						File file = e.getValue().file;
						String fileName = Utils.stripExtension(file.getName());

						if (e.getKey().startsWith("!") && e.getKey().endsWith("#"))
							continue;
						if (e.getKey().startsWith("?"))
							continue;

						for (Track track : bot.audioPlayer.getPlaylist()) {
							String trackName = ((File) track.getMetadata().get("file")).getName();

							if (trackName.equalsIgnoreCase(file.getName())) {
								continue outer;
							}
						}

						bot.queueAudio(channel, e.getValue(), builder);
						builder.appendContent("\n");
						i++;
						totalQueued++;
					}

					if (totalQueued < requested) {
						builder.appendContent("**Could only add " + totalQueued
								+ " songs instead of requested " + requested + ".**");
					} else {
						builder.appendContent("Added " + totalQueued + " randomly chosen songs");
						if (criteria != null)
							builder.appendContent(" with criteria \"" + criteria + "\"");
						builder.appendContent(".");
					}

					bot.sendMessage(builder);
					bot.showQueue(channel);
				}
				return null;
			case "shuffle":
				if (permLevel < PermissionTier.NORMAL)
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.NORMAL);
				if ((musicRestricted = bot.checkMusicRestricted(channel, user)) != null) {
					return musicRestricted;
				} else {
					bot.shuffle(channel);

					return null;
				}
			case "showqueue":
			case "nowplaying":
			case "np":
				if (permLevel < PermissionTier.NORMAL)
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.NORMAL);
				if ((musicRestricted = bot.checkMusicRestricted(channel, user)) != null) {
					return musicRestricted;
				} else {
					bot.showQueue(channel);
				}

				return null;
			case "database":
			case "db":
			case "showdatabase":
				if (permLevel < PermissionTier.NORMAL)
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.NORMAL);
				if ((musicRestricted = bot.checkMusicRestricted(channel, user)) != null) {
					return musicRestricted;
				} else {
					if (args.length < 1) {
						bot.sendMessage(bot.getNewBuilder(channel)
								.appendContent("You need to specify a page number! (1 to "
										+ MusicDatabase.getAllMusicPages() + ")"));
					} else {
						try {
							int page = Integer.parseInt(args[0]);

							if (page <= 0 || page > MusicDatabase.getAllMusicPages()) {
								throw new RuntimeException("Out of range.");
							}

							bot.showDatabase(channel, page - 1, user);
						} catch (Exception e) {
							bot.sendMessage(bot.getNewBuilder(channel)
									.appendContent("That wasn't a valid page number."));
						}
					}
				}
				return null;
			case "search":
				if (permLevel < PermissionTier.NORMAL)
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.NORMAL);
				if (args.length < 1) {
					return "Requires at least one argument!";
				} else if ((musicRestricted = bot.checkMusicRestricted(channel, user)) != null) {
					return musicRestricted;
				} else {
					try {
						String content = Utils.getContent(args, 0).trim();

						if (content.length() < 3) {
							return "Requires at least 3 letters.";
						}

						String suggestions = MusicDatabase.instance().getSuggestions(content);

						MessageBuilder builder = bot.getNewBuilder(channel);

						builder.appendContent("Searching for __" + content + "__:\n");
						builder.appendContent((suggestions == null || suggestions.isEmpty())
								? "*Found nothing.*" : suggestions);

						bot.sendMessage(builder);
					} catch (Exception e) {
						e.printStackTrace();
						return "An exception occurred: `" + e.toString() + "`";
					}
				}
				return null;

		}

		// -----------------------------------------------------------------------------
		// trusted commands
		switch (caseCommand) {
			case "sfx":
				if (permLevel < PermissionTier.TRUSTED)
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.TRUSTED);
				if (args.length < 1) {
					return "Requires at least one argument!";
				} else if (args.length > BaristaBot2.QUEUE_LIMIT) {
					return "Cannot add more than random limit! (" + BaristaBot2.RANDOM_LIMIT + ")";
				} else if ((musicRestricted = bot.checkMusicRestricted(channel, user)) != null) {
					return musicRestricted;
				} else {
					int lastSfxSlot = 0;

					List<Track> playlist = bot.audioPlayer.getPlaylist();
					for (int i = 0; i < playlist.size(); i++) {
						File f = (File) playlist.get(i).getMetadata().get("file");

						if (f.getParentFile().getName().endsWith("special")) {
							lastSfxSlot = i;
						}
					}

					for (int i = 0; i < args.length; i++) {
						String text = args[i];
						Song song = MusicDatabase.instance().files.get("!" + text.toLowerCase() + "#");

						if (song == null) {
							return "Couldn't find SFX " + text;
						} else {
							if (!bot.insertTrack(song, Math.max(0,
									Math.min(bot.audioPlayer.getPlaylist().size(), lastSfxSlot + i)))) {
								return "Failed to add SFX " + text;
							}
						}
					}
				}
				return null;
			case "sfxdatabase":
			case "sfxdb":
			case "sfxlist":
				if (permLevel < PermissionTier.TRUSTED) {
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.TRUSTED);
				} else if ((musicRestricted = bot.checkMusicRestricted(channel, user)) != null) {
					return musicRestricted;
				} else {
					bot.sendMessage(bot.getNewBuilder(channel).appendContent("*SFX Database*\n")
							.appendContent(MusicDatabase.instance().sfxList));

					return null;
				}
		}

		// -----------------------------------------------------------------------------
		// mod commands
		switch (caseCommand) {
			case "getid":
				if (permLevel < PermissionTier.MODERATOR)
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.MODERATOR);
				if (args.length < 1) {
					return "Requires at least one argument!";
				} else {
					String part = args[0].toLowerCase();
					MessageBuilder getIdBuilder = bot.getNewBuilder(channel);

					getIdBuilder
							.appendContent("This server's ID is " + channel.getGuild().getID() + "\n");

					boolean found = false;
					for (IUser u : channel.getUsersHere()) {
						if (u.getName().toLowerCase().contains(part)
								|| (u.getNicknameForGuild(channel.getGuild()).isPresent()
								&& u.getNicknameForGuild(channel.getGuild()).get().toLowerCase()
								.contains(part))) {
							getIdBuilder.appendContent("The ID for " + u.getName() + "#"
									+ u.getDiscriminator() + " is " + u.getID() + "\n");

							found = true;
						}
					}

					if (found) {
						bot.sendMessage(getIdBuilder);

						return null;
					} else {
						return "Couldn't find the user " + args[0]
								+ (args.length >= 2 ? "#" + args[1] : "");
					}
				}
			case "getusername":
				if (permLevel < PermissionTier.MODERATOR)
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.MODERATOR);
				if (args.length < 1) {
					return "Requires at least one argument! <id>";
				} else {
					String part = args[0].toLowerCase();
					MessageBuilder builder = bot.getNewBuilder(channel);

					for (IUser u : channel.getUsersHere()) {
						if (u.getID().equals(args[0])) {
							builder.appendContent("The name of the user with ID " + args[0] + " is "
									+ u.getName() + "#" + u.getDiscriminator());

							bot.sendMessage(builder);

							return null;
						}
					}

					return "Couldn't find the user " + args[0];
				}
			case "listpermissions":
				if (permLevel < PermissionTier.MODERATOR) {
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.MODERATOR);
				} else {
					MessageBuilder builder = bot.getNewBuilder(channel);

					builder.appendContent(user.mention() + " __Permission tiers:__ \n");

					Field[] fields = PermissionTier.class.getFields();
					for (Field f : fields) {
						if (f.getType() == long.class) {
							try {
								builder.appendContent(f.getName() + " - " + f.getLong(null) + "\n");
							} catch (IllegalArgumentException | IllegalAccessException e) {
								e.printStackTrace();
							}
						}
					}

					builder.appendContent("\n__Listing non-NORMAL permissions:__\n");

					Properties pr = PermPrefs.getProperties();
					for (Entry<Object, Object> entry : pr.entrySet()) {
						String key = (String) entry.getKey();
						String value = (String) entry.getValue();
						IUser u = bot.client.getUserByID(key);

						// used to update overdue bans
						long l = PermPrefs.getPermissionsLevel(key);

						if (l == 1)
							continue;
						builder.appendContent((u == null ? "`" + key + "` (user not found!)"
								: u.getName() + "#" + u.getDiscriminator())
								+ ": "
								+ (l < 0 ? ("tempbanned for "
								+ ((Math.abs(l) - System.currentTimeMillis()) / 1000)
								+ " more seconds") : "`" + l + "`")
								+ "\n");
					}

					bot.sendMessage(builder);
				}
				return null;
			case "removesong":
				if (permLevel < PermissionTier.MODERATOR) {
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.MODERATOR);
				}

				if (args.length < 1) {
					return "Requires at least one argument!";
				} else if (bot.audioPlayer.getPlaylistSize() == 0) {
					return "There's nothing in the queue!";
				} else {
					try {
						int index = Integer.parseInt(args[0]);

						if (index < 1 || index > bot.audioPlayer.getPlaylistSize()) {
							return "The index " + index + " is out of range!";
						}

						Track removed = bot.audioPlayer.getPlaylist().remove(index - 1);
						String info = "Removed number " + index + ", track " + MusicDatabase
								.getDisguisedName(((Song) removed.getMetadata().get("song")));

						Main.info(info);
						bot.sendMessage(bot.getNewBuilder(channel).appendContent(info));
						bot.showQueue(channel);

					} catch (NumberFormatException nfe) {
						nfe.printStackTrace();
						return "Couldn't parse number: " + args[0];
					} catch (Exception e) {
						e.printStackTrace();
						return "An exception occurred:\n```" + e.toString() + "```";
					}
				}
				return null;
			case "reconnectaudio":
				if (permLevel < PermissionTier.MODERATOR) {
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.MODERATOR);
				}

				bot.sendMessage(
						bot.getNewBuilder(channel).appendContent("Attempting to reconnect audio..."));
				bot.attemptConnectToRadioChannel(bot.getDefaultRadioChannel());
				if (bot.audioPlayer == null) {
					bot.sendMessage(bot.getNewBuilder(channel).appendContent("Failed to reconnect!",
							Styles.BOLD));
				} else {
					bot.sendMessage(
							bot.getNewBuilder(channel).appendContent("Reconnected successfully!"));
				}

				return null;
			case "insertsong":
				if (permLevel < PermissionTier.MODERATOR) {
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.MODERATOR);
				} else {
					try {
						String text = Utils.getContent(args, 1);
						Song song = MusicDatabase.instance().files.get(text.toLowerCase());
						int index = Integer.parseInt(args[0]);

						if (index < 1 || index > bot.audioPlayer.getPlaylistSize()) {
							return "The index " + index + " is out of range!";
						}

						if (song == null) {
							return "Couldn't find song in database: " + text;
						} else {
							boolean b = bot.insertTrack(song, index - 1);

							if (b) {
								bot.sendMessage(bot.getNewBuilder(channel)
										.appendContent("Inserted " + text + " to position " + index));
								bot.showQueue(channel);
							}

							return b ? null : "Failed to add song " + text;
						}
					} catch (NumberFormatException e) {
						e.printStackTrace();
						return "Couldn't parse index: " + args[0];
					}
				}
			case "clearqueue":
				if (permLevel < PermissionTier.MODERATOR) {
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.MODERATOR);
				} else {
					if (bot.audioPlayer == null) {
						return "AudioPlayer is null!";
					}

					bot.audioPlayer.clear();
					bot.sendMessage(bot.getNewBuilder(channel).appendContent("Cleared queue."));
					bot.setStatus(null);

					return null;
				}
			case "trivia":
				if (permLevel < PermissionTier.MODERATOR) {
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.MODERATOR);
				} else {
					if (args.length < 1)
						return "Requires at least one argument!";

					String cmd = args[0].toLowerCase();

					if (cmd.equals("newgame")) {
						if (bot.getCurrentTrivia() != null)
							return "Game already in progress!";
						if (args.length < 2)
							return "Requires the number of questions you're going to use!";

						int number = Utils.clamp(Integer.parseInt(args[1]), 1,
								Questions.instance().questions.size());

						TriviaGame game = new TriviaGame(bot, number, channel);

						Main.info("Started new trivia with " + number + " questions");
						bot.sendMessage(bot.getNewBuilder(channel).appendContent(
								"Started new trivia game with " + number + " questions."));

						bot.setCurrentTrivia(game);

					} else if (cmd.equals("endgame")) {
						if (bot.getCurrentTrivia() == null)
							return "No current game!";

						bot.getCurrentTrivia().finishGame();
						bot.setCurrentTrivia(null);

						Main.info("Ended current trivia game");
						bot.sendMessage(
								bot.getNewBuilder(channel).appendContent("Ended current trivia game."));
					} else if (cmd.equals("questions")) {
						bot.sendMessage(bot.getNewBuilder(channel)
								.appendContent("Number of trivia questions in database: "
										+ Questions.instance().questions.size()));
					} else {
						return "Sub-command not found!";
					}
				}
				return null;
		}

		// -----------------------------------------------------------------------------

		// admin commands
		switch (caseCommand) {
			case "exit":
			case "quit":
				if (permLevel < PermissionTier.ADMIN)
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.ADMIN);
				bot.sendMessage(bot.getNewBuilder(channel).appendContent("Shutting down..."));

				Main.shouldExit = true;
				return null;
			case "username":
				if (permLevel < PermissionTier.ADMIN) {
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.ADMIN);
				} else if (args.length < 1) {
					return "Requires name argument!";
				}

				RequestBuffer.request(() -> {
					try {
						bot.client.changeUsername(Utils.getContent(args, 0).trim());
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
				return null;
			case "setpermissions":
				if (permLevel < PermissionTier.ADMIN)
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.ADMIN);
				if (args.length < 2) {
					return "Requires at least two arguments! <id> <level>";
				} else {
					if (args[0].equals("188789412426022914")) {
						return "You cannot set the permissions of the owner of the bot!";
					}

					try {
						PermPrefs.setPermissionsLevel(args[0], Integer.parseInt(args[1]));
						PermPrefs.instance().save();
					} catch (Exception e) {
						e.printStackTrace();
						return "An exception occurred.\n```" + e.toString() + "```";
					}

					IUser u = bot.client.getUserByID(args[0]);

					bot.sendMessage(
							bot.getNewBuilder(channel)
									.appendContent("Set permissions of user ID \"" + args[0] + "\""
											+ (u != null ? " (" + u.getName() + ")" : "") + " to: "
											+ args[1]));
					return null;
				}
			case "refreshdatabase":
			case "refreshdb":
				if (permLevel < PermissionTier.ADMIN)
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.ADMIN);

				MusicDatabase.instance().forceReupdate();
				MusicDatabase.instance();
				Incidents.refresh();
				Questions.instance().forceReload();
				Questions.instance();
				TransitSystems.instance().refresh();

				bot.sendMessage(bot.getNewBuilder(channel).appendContent("Refreshed databases."));
				return null;
			case "togglequeue":
				if (permLevel < PermissionTier.ADMIN)
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.ADMIN);

				bot.canAddToQueue = !bot.canAddToQueue;
				bot.sendMessage(
						bot.getNewBuilder(channel)
								.appendContent(bot.canAddToQueue
										? ":heavy_check_mark: __Enabled__ adding to queue."
										: ":x: __Disabled__ adding to queue."));
				return null;
			case "arrest":
			case "tempban":
				if (permLevel < PermissionTier.ADMIN)
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.ADMIN);

				if (args.length < 2) {
					return "Requires at least two arguments!";
				} else {
					int duration = Integer.parseInt(args[1]);

					if (duration < 0) {
						return "Duration cannot be less than 0!";
					}

					if (System.currentTimeMillis() <= 0) {
						return "Must be currently after epoch!";
					}

					boolean additive = args.length >= 3 && args[2].equals("+");
					long currentPermissions = PermPrefs.getPermissionsLevel(args[0]);
					long banEndTime = (!additive || currentPermissions >= 0 ? System.currentTimeMillis() : Math.abs
							(currentPermissions)
					) + duration * 1000;

					PermPrefs.setPermissionsLevel(args[0], -banEndTime);

					IUser u = bot.client.getUserByID(args[0]);

					bot.sendMessage(bot.getNewBuilder(channel)
							.appendContent((caseCommand.equals("arrest") ? "Arrested" : "Tempbanned")
									+ " " + args[0] + (u != null ? " " + u.getName() : "") + " for " + (additive ?
									"another " : "")
									+ duration + " seconds (perm level now " + PermPrefs.getPermissionsLevel(args[0])
									+ ")"));
				}
				return null;
			case "bitrate":
				if (permLevel < PermissionTier.ADMIN)
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.ADMIN);
				if (args.length < 1) {
					bot.sendMessage(bot.getNewBuilder(channel).appendContent(
							"The bitrate of the radio channel is " + bot.radioChannel.getBitrate()));
				} else {
					int bitrate = Integer.parseInt(args[0]);

					try {
						bot.radioChannel.changeBitrate(bitrate);

						bot.sendMessage(bot.getNewBuilder(channel)
								.appendContent("Changed radio channel bitrate to " + bitrate));
					} catch (RateLimitException | MissingPermissionsException | DiscordException e) {
						e.printStackTrace();
						bot.sendMessage(bot.getNewBuilder(channel)
								.appendContent("Failed to set bitrate: " + e.toString()));
					}
				}

				return null;
			case "debug":
				if (permLevel < PermissionTier.ADMIN) {
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.ADMIN);
				} else {
					bot.setDebugging(!bot.isDebugging());
					bot.sendMessage(bot.getNewBuilder(channel).appendContent(
							":wrench: Debug mode is now __" + bot.isDebugging() + "__."));

				}
				return null;
			case "setplaying":
			case "setstatus":
				if (permLevel < PermissionTier.ADMIN) {
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.ADMIN);
				} else {
					bot.setStatus(args.length < 1 ? null : Utils.getContent(args, 0));
				}
				return null;
			case "senddistress":
				if (permLevel < PermissionTier.ADMIN) {
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.ADMIN);
				} else {
					bot.sendDistressSignal();
				}
				return null;
			case "say":
				if (permLevel < PermissionTier.ADMIN) {
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.ADMIN);
				} else if (args.length < 2) {
					return "Requires at least two arguments!";
				} else {
					IChannel c = bot.client.getChannelByID(args[0]);

					if (c == null)
						return "Couldn't find that channel (" + args[0] + ").";

					bot.sendMessage(bot.getNewBuilder(c).appendContent(Utils.getContent(args, 1)));
				}
				return null;
			case "idle":
				if (permLevel < PermissionTier.ADMIN) {
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.ADMIN);
				} else {
					bot.client.changePresence(bot.client.getOurUser().getPresence() != Presences.IDLE);

					return null;
				}
			case "senddm":
				if (permLevel < PermissionTier.ADMIN) {
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.ADMIN);
				} else if (args.length < 1) {
					return "Requires at least two arguments! <id> <message>";
				} else {
					IUser u = bot.client.getUserByID(args[0]);

					if (u == null)
						return "Couldn't find that user! `" + args[0] + "`";

					try {
						bot.sendMessage(bot.getNewBuilder(bot.client.getOrCreatePMChannel(u))
								.withContent("*Sent by bot admin:* " + args[1]));
						bot.sendMessage(bot.getNewBuilder(channel).appendContent(
								"Sent a DM to " + u.getName() + " (" + u.getID() + "): " + args[1]));
						Main.info("Sent DM to " + u.getName());
					} catch (RateLimitException | DiscordException e) {
						e.printStackTrace();
					}
				}
				return null;
			case "movetovoicechannel":
				if (permLevel < PermissionTier.ADMIN) {
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.ADMIN);
				} else {
					String id = null;

					if (args.length >= 1) {
						id = args[0];
					}

					IVoiceChannel v = bot.client.getVoiceChannelByID(id);

					if (v == null) {
						if (user.getConnectedVoiceChannels().size() == 0)
							return "You aren't connected to any voice channels!";

						v = user.getConnectedVoiceChannels().get(0);
					}

					bot.sendMessage(bot.getNewBuilder(channel).withContent(
							"Attempting to connect to " + v.getName() + " (" + v.getID() + ")"));

					if (bot.radioChannel != null) {
						bot.radioChannel.leave();
					}

					bot.attemptConnectToRadioChannel(v);

					if (bot.radioChannel.equals(v)) {
						bot.sendMessage(bot.getNewBuilder(channel).withContent("Success!"));
					} else {
						bot.sendMessage(
								bot.getNewBuilder(channel).withContent("Failed, check console!F"));
					}

					return null;
				}
			case "popcorn":
				if (permLevel < PermissionTier.ADMIN) {
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.ADMIN);
				} else {
					bot.sendMessage(bot.getNewBuilder(channel)
							.withContent("__Selling popcorn and alcohol!__\n"
									+ ":popcorn: :popcorn: :popcorn: :popcorn: :popcorn: :popcorn: :popcorn:\n"
									+ ":beer: :beer: :beer: :beer: :beer: :beer: :beer:\n"
									+ "*Get additive (stacking) discounts for the following:*\n"
									+ "**10% DISCOUNT!** - if chrislo27 or ahemtoday is involved\n"
									+ "**10% MORE!** - if megaminerzero is involved\n"
									+ "**50% MORE!** - if bluemurderguitarbunny is selling popcorn at the same time"));
					return null;
				}
			case "perms":
				if (permLevel < PermissionTier.ADMIN)
					return CommandResponse.insufficientPermission(permLevel, PermissionTier.ADMIN);

				if (args.length < 1) {
					bot.sendMessage(bot.getNewBuilder(channel)
							.appendContent(user.mention() + " Your permission tier is "
									+ PermPrefs.getPermissionsLevel(user.getID())));
				} else {
					bot.sendMessage(bot.getNewBuilder(channel).appendContent("The permission level of "
							+ args[0] + " is " + PermPrefs.getPermissionsLevel(args[0])));
				}

				return null;
		}

		return CommandResponse.doesNotExist();
	}

	/**
	 * Returns a failure message, or null if there isn't any.
	 *
	 * @param command
	 * @param args
	 * @param message
	 * @param channel
	 * @param author
	 * @return
	 */
	public String onCommand(String command, String[] args, IMessage message, IChannel channel,
							IUser author) {
		Main.info(author.getName() + "#" + author.getDiscriminator() + " attempting command "
				+ command + " in channel " + channel.getName() + " with arguments "
				+ Arrays.toString(args));

		String result;

		try {
			result = doCommand(command, message, channel, author, args);
		} catch (Exception e) {
			e.printStackTrace();
			return "An **uncaught** exception occurred: `" + e.toString() + "`";
		}

		return result;
	}

	public static class CommandResponse {

		public static String doesNotExist() {
			// best practice indicates that it should fail silently
			return null;
			//return "The command does not exist.";
		}

		public static String insufficientPermission(long perm, long required) {
			// best practice indicates that it should fail silently
			return null;
			//						return "You have insufficient permissions. (Required " + required + ", you have "
			// + perm
			//								+ ")";
		}

	}

}
