package chrislo27.bot.bots.baristabot2;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.UnsupportedAudioFileException;

import com.google.code.chatterbotapi.ChatterBot;
import com.google.code.chatterbotapi.ChatterBotFactory;
import com.google.code.chatterbotapi.ChatterBotSession;
import com.google.code.chatterbotapi.ChatterBotType;

import chrislo27.bot.Main;
import chrislo27.bot.MusicDatabase;
import chrislo27.bot.bots.baristabot2.rhythm.Games;
import chrislo27.bot.bots.baristabot2.rhythm.RhythmGame;
import chrislo27.bot.util.EightBall;
import chrislo27.bot.util.Utils;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Status;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MessageBuilder.Styles;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.RequestBuffer;
import sx.blah.discord.util.audio.AudioPlayer.Track;

public class CommandHandler {

	public final BaristaBot2 bot;

	public CommandHandler(BaristaBot2 bot) {
		this.bot = bot;
	}

	public void addHelpToBuilder(MessageBuilder builder, long permLevel) {
		// builder.appendContent("");

		if (permLevel < PermissionTier.NORMAL) return;
		// normal
		builder.appendContent("\n**__Normal commands:__**\n");
		builder.appendContent(
				"help/? [music/game] - Shows this message or the desired help page\n");
		builder.appendContent("woof - woof\n");
		builder.appendContent("hi/hello - Hello!\n");
		builder.appendContent(
				"reaction/react <wot/wotdraw/salt/donk/zodiackiller/blame> - Posts a reaction picture\n");
		builder.appendContent("8ball/8-ball - Ask the magic 8-ball\n");
		builder.appendContent("perms [uuid] - Gets the UUID of either you, or the UUID provided\n");
		builder.appendContent("uptime - View how long the bot has been up\n");
		builder.appendContent("stats - View miscellaneous statistics\n");
		builder.appendContent("incidents - View past incidents\n");
		builder.appendContent("rps <rock/paper/scissors/r/p/s> - Play rock paper scissors\n");
		builder.appendContent("@" + bot.client.getOurUser().getName()
				+ " <text> - Talk to the barista (Uses Cleverbot)\n");

		if (permLevel < PermissionTier.TRUSTED) return;
		// trusted
		builder.appendContent("\n**__Trusted commands:__**\n");
		builder.appendContent("sfx <name> [any more] - Plays your SFX in the music queue\n");
		builder.appendContent("sfxlist/sfxdatabase/sfxdb - Displays SFX list\n");

		if (permLevel < PermissionTier.MODERATOR) return;
		// moderator
		builder.appendContent("\n**__Moderator commands:__**\n");
		builder.appendContent("getuuid <username/part of username> [discriminator]"
				+ " - Gets the user's UUID (universally unique ID) from their username and *optional* discriminator (#XXXX),"
				+ " also returns server UUID\n");
		builder.appendContent(
				"getusername <uuid> - Gets the name and discriminator from their UUID\n");
		builder.appendContent("permissiontiers - Displays all permission tiers\n");
		builder.appendContent(
				"insertsong <index> <song> - Inserts the song at the place number, bypasses queue limit\n");
		builder.appendContent("removesong <index> - Removes the song at the place number\n");
		builder.appendContent(
				"reconnectaudio - Attempts to reconnect the audio system and channel\n");
		builder.appendContent("clearqueue - Clears the queue\n");

		if (permLevel < PermissionTier.ADMIN) return;
		// admin
		builder.appendContent("\n**__Administrator commands:__**\n");
		builder.appendContent("exit/quit - Logs off and quits the bot\n");
		builder.appendContent("setpermissions <uuid> <level> - Sets the user's permission level\n");
		builder.appendContent("refreshdb/refreshdatabase - Refreshes song database\n");
		builder.appendContent("togglequeue - Toggles queuing\n");
		builder.appendContent(
				"tempban/arrest <uuid> <seconds> - Temporarily bans someone for the duration\n");
		builder.appendContent("username <name> - Sets the bot's username\n");
		builder.appendContent("bitrate [value] - Gets or sets the bitrate of the radio channel\n");
		builder.appendContent(
				"debug - Toggles debug mode, which stops non-admins from doing music actions\n");
		builder.appendContent(
				"setstatus [status] - Sets the status, or removes it (cannot override debug status)\n");
		builder.appendContent("senddistress - Sends a test distress signal\n");
	}

	public void addGameHelpToBuilder(MessageBuilder builder) {
		builder.appendContent("__Locked to trusted permissions__\n");
		builder.appendContent("r start/play <game> - Starts a rhythm game for you\n");
		builder.appendContent("r - Your input for the rhythm game - advised to copy/paste this\n");
		builder.appendContent("r games - List of games\n");
		builder.appendContent("r end - Stops the rhythm game\n");
	}

	public void addMusicHelpToBuilder(MessageBuilder builder) {
		// music
		builder.appendContent("\n**__Music commands__** *(please do only in <#"
				+ BaristaBot2.IDEAL_CHANNEL + ">):*\n");
		builder.appendContent("play/queue <song name> - Queues the song of your choice\n");
		builder.appendContent(
				"random [#] [criteria]- Queues a random song or as many as you want up to the limit ("
						+ BaristaBot2.RANDOM_LIMIT
						+ "), optional criteria will only match song **names** containing it\n");
		builder.appendContent("shuffle - Shuffles the queue, ignoring the first song\n");
		builder.appendContent("showqueue/nowplaying/np - Shows queue\n");
		builder.appendContent("database/showdatabase/db <page> - Shows the song database\n");
		builder.appendContent("search <key terms> - Searches for a song\n");
		builder.appendContent(
				"skip - Vote to skip the current song (or retract your vote if you did already)\n");
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
				case "game":
					builder.appendContent("Here are the commands for games.\n");
					builder.appendContent(
							"__Please use a percent sign % before issuing commands.__\n");
					addGameHelpToBuilder(builder);
					break;
				case "music":
					builder.appendContent("Here are the commands for music actions.\n");
					builder.appendContent(
							"__Please use a percent sign % before issuing commands.__\n");
					addMusicHelpToBuilder(builder);
					break;
				default:
					builder.appendContent("Here's your commands for your permission level ("
							+ permLevel + ").\n");
					builder.appendContent(
							"__Please use a percent sign % before issuing commands.__\n");
					addHelpToBuilder(builder, permLevel);
					break;
				}

				bot.sendMessage(builder);
			}
			return null;
		case "woof":
			if (permLevel < PermissionTier.NORMAL)
				return CommandResponse.insufficientPermission(permLevel, PermissionTier.NORMAL);
			bot.sendMessage(bot.getNewBuilder(channel).appendContent("wœuf"));
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
			if (permLevel < PermissionTier.NORMAL)
				return CommandResponse.insufficientPermission(permLevel, PermissionTier.NORMAL);
			if (args.length < 1) {
				return "Requires at least one argument!";
			} else {
				MessageBuilder builder = bot.getNewBuilder(channel);
				boolean send = true;

				switch (args[0].toLowerCase()) {
				case "wot":
					builder.appendContent("http://i.imgur.com/rBafTiE.png");
					break;
				case "wotdraw":
					builder.appendContent("http://i.imgur.com/9M2XmwE.png");
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
					builder.appendContent("http://i.imgur.com/EAOQz0a.png");
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

			return "Couldn't find the image you wanted (" + args[0] + ")!";
		case "8-ball":
		case "8ball":
			if (permLevel < PermissionTier.NORMAL)
				return CommandResponse.insufficientPermission(permLevel, PermissionTier.NORMAL);
			bot.sendMessage(bot.getNewBuilder(channel).appendContent(":8ball:  ")
					.appendContent(EightBall.getResponse(), Styles.INLINE_CODE));
			return null;
		case "perms":
			if (permLevel < PermissionTier.NORMAL)
				return CommandResponse.insufficientPermission(permLevel, PermissionTier.NORMAL);

			if (args.length < 1) {
				bot.sendMessage(bot.getNewBuilder(channel)
						.appendContent(user.mention() + " Your permission tier is "
								+ PermPrefs.getPermissionsLevel(user.getID())));
			} else {
				bot.sendMessage(bot.getNewBuilder(channel).appendContent("The permission level of "
						+ args[0] + " is " + PermPrefs.getPermissionsLevel(args[0])));
			}

			return null;
		case "uptime":
			if (permLevel < PermissionTier.NORMAL) {
				return CommandResponse.insufficientPermission(permLevel, PermissionTier.NORMAL);
			} else {
				MessageBuilder stats = bot.getNewBuilder(channel);

				long diff = (System.currentTimeMillis() - bot.startTime.getTime()) / 1000;

				stats.appendContent(user.mention() + " __Uptime:__\n");
				stats.appendContent("Started on: " + bot.startTime.toString() + "\n");
				stats.appendContent("Current uptime: "
						+ TimeUnit.DAYS.convert(diff, TimeUnit.SECONDS) + " days, "
						+ TimeUnit.HOURS.convert(diff, TimeUnit.SECONDS) % 24 + " hours, "
						+ TimeUnit.MINUTES.convert(diff, TimeUnit.SECONDS) % 60 + " minutes");

				bot.sendMessage(stats);
			}
			return null;
		case "stats":
			if (permLevel < PermissionTier.NORMAL) {
				return CommandResponse.insufficientPermission(permLevel, PermissionTier.NORMAL);
			} else {
				MessageBuilder stats = bot.getNewBuilder(channel);

				if (bot.audioPlayer != null && bot.audioPlayer.getPlaylist().size() > 0) {
					bot.secondsPlaying += Math
							.abs(System.currentTimeMillis() - bot.playingStartTime) / 1000.0D;
					bot.playingStartTime = System.currentTimeMillis();
				}

				long diff = System.currentTimeMillis() - bot.startTime.getTime();

				stats.appendContent(user.mention() + " __Stats:__\n");
				stats.appendContent("Music streamed since uptime start: __"
						+ String.format("%.4f", bot.secondsPlaying / 60f)
						+ " minutes__, or about __"
						+ String.format("%.2f",
								(bot.secondsPlaying * bot.radioChannel.getBitrate()) / 8 / 1024
										/ 1024)
						+ " MB__ (" + bot.radioChannel.getBitrate() / 1000f + " kbps)\n");
				if (Incidents.incidents.size() > 0) stats.appendContent("Last incident: "
						+ Incidents.incidents.get(Incidents.incidents.size() - 1) + "\n");

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
		}

		// music
		switch (caseCommand) {
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
				File file = MusicDatabase.instance().files.get(entire.toLowerCase());

				if (entire.startsWith("!") && entire.endsWith("#")) file = null;

				if (file != null) {
					if (bot.emptyQueueIfAllGone(channel)) {

					} else {
						MessageBuilder builder = bot.getNewBuilder(channel);
						bot.queueAudio(channel, file, builder);
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
				if (bot.emptyQueueIfAllGone(channel)) return null;

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
										BaristaBot2.QUEUE_LIMIT - bot.audioPlayer.playlistSize()),
						1, number);

				//criteria = "Bouncy Road";
				List<Entry<String, File>> bound = MusicDatabase.instance().getSearch(criteria);
				Collections.shuffle(bound);

				number = Math.min(bound.size(), number);

				if (bound.size() == 0) {
					return "No music found with the criteria: " + criteria;
				}

				Main.info("Queuing " + number + " random song(s)"
						+ (criteria == null ? "" : " with criteria: " + criteria));

				MessageBuilder builder = bot.getNewBuilder(channel);

				// gets criteria list, shuffles, it, starts iterating through it until number is reached, or current index is out of range
				int totalQueued = 0;
				outer: for (int i = 0, n = number, boundIndex = 0; i < n
						&& boundIndex < bound.size(); boundIndex++) {
					Entry<String, File> e = bound.get(boundIndex);
					File file = e.getValue();
					String fileName = Utils.stripExtension(file.getName());

					if (e.getKey().startsWith("!") && e.getKey().endsWith("#")) continue;
					if (e.getKey().startsWith("?")) continue;

					for (Track track : bot.audioPlayer.getPlaylist()) {
						String trackName = ((File) track.getMetadata().get("file")).getName();

						if (trackName.equalsIgnoreCase(file.getName())) {
							continue outer;
						}
					}

					bot.queueAudio(channel, file, builder);
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
					File f = MusicDatabase.instance().files.get("!" + text.toLowerCase() + "#");

					if (f == null) {
						return "Couldn't find SFX " + text;
					} else {
						if (!bot.insertTrack(f, Math.max(
								bot.rgameHandler.currentGame == null ? 0 : 1,
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
			} else {
				bot.sendMessage(bot.getNewBuilder(channel).appendContent("*SFX Database*\n")
						.appendContent(MusicDatabase.instance().sfxList));

				return null;
			}
		}

		// -----------------------------------------------------------------------------
		// mod commands
		switch (caseCommand) {
		case "getuuid":
			if (permLevel < PermissionTier.MODERATOR)
				return CommandResponse.insufficientPermission(permLevel, PermissionTier.MODERATOR);
			if (args.length < 1) {
				return "Requires at least one argument! <username/part of username> [discriminator (4 digit code)]";
			} else {
				String part = args[0].toLowerCase();
				MessageBuilder getUuidBuilder = bot.getNewBuilder(channel);

				getUuidBuilder.appendContent(
						"This server's UUID is " + channel.getGuild().getID() + "\n");

				boolean found = false;
				for (IUser u : channel.getUsersHere()) {
					if (u.getName().toLowerCase().contains(part)) {
						if (args.length < 2 || u.getDiscriminator().equalsIgnoreCase(args[1])) {
							getUuidBuilder.appendContent("The UUID for " + u.getName() + "#"
									+ u.getDiscriminator() + " is " + u.getID() + "\n");

							found = true;
						}
					}
				}

				if (found) {
					bot.sendMessage(getUuidBuilder);

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
				return "Requires at least one argument! <uuid>";
			} else {
				String part = args[0].toLowerCase();
				MessageBuilder builder = bot.getNewBuilder(channel);

				for (IUser u : channel.getUsersHere()) {
					if (u.getID().equals(args[0])) {
						builder.appendContent("The name of the user with UUID " + args[0] + " is "
								+ u.getName() + "#" + u.getDiscriminator());

						bot.sendMessage(builder);

						return null;
					}
				}

				return "Couldn't find the user " + args[0];
			}
		case "permissiontiers":
			if (permLevel < PermissionTier.MODERATOR) {
				return CommandResponse.insufficientPermission(permLevel, PermissionTier.MODERATOR);
			} else {
				MessageBuilder builder = bot.getNewBuilder(channel);

				builder.appendContent("__Permission tiers:__ \n");

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

				bot.sendMessage(builder);
			}
			return null;
		case "removesong":
			if (permLevel < PermissionTier.MODERATOR) {
				return CommandResponse.insufficientPermission(permLevel, PermissionTier.MODERATOR);
			}

			if (args.length < 1) {
				return "Requires at least one argument!";
			} else if (bot.audioPlayer.playlistSize() == 0) {
				return "There's nothing in the queue!";
			} else {
				try {
					int index = Integer.parseInt(args[0]);

					if (index < 1 || index > bot.audioPlayer.playlistSize()) {
						return "The index " + index + " is out of range!";
					}

					Track removed = bot.audioPlayer.getPlaylist().remove(index - 1);
					String info = "Removed number " + index + ", track " + Utils
							.stripExtension(((File) removed.getMetadata().get("file")).getName());

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
			bot.attemptConnectToRadioChannel();
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
					File f = MusicDatabase.instance().files.get(text.toLowerCase());
					int index = Integer.parseInt(args[0]);

					if (index < 1 || index > bot.audioPlayer.playlistSize()) {
						return "The index " + index + " is out of range!";
					}

					if (f == null) {
						return "Couldn't find song in database: " + text;
					} else {
						boolean b = bot.insertTrack(f, index - 1);

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

				bot.audioPlayer.skipTo(bot.audioPlayer.playlistSize());
				bot.sendMessage(bot.getNewBuilder(channel).appendContent("Cleared queue."));
				bot.setStatus(null);
				bot.rgameHandler.cancelGame();

				return null;
			}
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
				return "Requires at least two arguments! <uuid> <level>";
			}

			if (args[0].equals("188789412426022914")) {
				return "You cannot set the permissions of the owner of the bot!";
			}

			try {
				PermPrefs.setPermissionsLevel(args[0], Integer.parseInt(args[1]));
			} catch (Exception e) {
				e.printStackTrace();
				return "An exception occurred.\n```" + e.toString() + "```";
			}

			PermPrefs.instance().save();
			bot.sendMessage(bot.getNewBuilder(channel).appendContent(
					"Set permissions of user UUID \"" + args[0] + "\" to: " + args[1]));
			return null;
		case "refreshdatabase":
		case "refreshdb":
			if (permLevel < PermissionTier.ADMIN)
				return CommandResponse.insufficientPermission(permLevel, PermissionTier.ADMIN);

			MusicDatabase.instance().forceReupdate();
			MusicDatabase.instance();
			Incidents.refresh();
			IdleTexts.refresh();

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
				return "Requires two arguments!";
			} else {
				int duration = Integer.parseInt(args[1]);

				if (duration < 0) {
					return "Duration cannot be less than 0!";
				}

				if (System.currentTimeMillis() < 0) {
					return "Cannot be before January 1, 1970!";
				}

				long banEndTime = System.currentTimeMillis() + duration * 1000;

				PermPrefs.setPermissionsLevel(args[0], -banEndTime);

				bot.sendMessage(bot.getNewBuilder(channel)
						.appendContent((caseCommand.equals("arrest") ? "Arrested" : "Tempbanned")
								+ " " + args[0] + " for " + duration + " seconds"));
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
		case "setstatus":
			if (permLevel < PermissionTier.ADMIN) {
				return CommandResponse.insufficientPermission(permLevel, PermissionTier.ADMIN);
			} else {
				bot.setStatus(args.length < 0 ? null : Utils.getContent(args, 0));
			}
			return null;
		case "senddistress":
			if (permLevel < PermissionTier.ADMIN) {
				return CommandResponse.insufficientPermission(permLevel, PermissionTier.ADMIN);
			} else {
				bot.sendDistressSignal();
			}
			return null;
		}

		if (caseCommand.equals("r")) {
			if (permLevel < PermissionTier.TRUSTED)
				return CommandResponse.insufficientPermission(permLevel, PermissionTier.TRUSTED);
			if (args.length == 0) {
				// input
				if (bot.rgameHandler.currentGame == null) return null;
				if (!bot.rgameHandler.gameAuthor.getID().equals(user.getID())) return null;

				// FIXME use localdatetime
				float acc = bot.rgameHandler.input(System.currentTimeMillis());

				bot.sendMessage(bot.getNewBuilder(channel)
						.appendContent("Input success at accuracy " + (1f - Math.abs(acc)) * 100
								+ "% (" + Math.abs(acc * RhythmGame.DEVIENCY) + " secs "
								+ (acc < 0 ? "early" : "late") + ")"));
			} else {
				switch (args[0].toLowerCase()) {
				case "play":
				case "start":
					if (args.length < 2) {
						return "Needs a game to play! Check `%r games` for a list of games.";
					} else {
						if (bot.rgameHandler.currentGame != null)
							return "A game is currently in progress.";

						RhythmGame game = Games.getRhythmGame(args[1].toLowerCase());

						if (game == null) {
							return "That game wasn't found.";
						}

						try {
							bot.rgameHandler.startGame(user, game, channel);
						} catch (IOException | UnsupportedAudioFileException e) {
							e.printStackTrace();
							return "An exception occurred. `" + e.toString() + "`";
						}

						MessageBuilder builder = bot.getNewBuilder(channel);
						builder.appendContent("Starting rhythm game: " + args[1].toLowerCase());
						bot.sendMessage(builder);
					}
					break;
				case "games": {
					MessageBuilder builder = bot.getNewBuilder(channel);

					builder.appendContent("*Here are the games available:*\n");

					for (String game : Games.instance().map.keySet()) {
						builder.appendContent(game + "\n");
					}

					bot.sendMessage(builder);
				}
					break;
				case "end":
					if (bot.rgameHandler.currentGame == null) return "There isn't a game playing!";
					if (user.getID().equals(bot.rgameHandler.gameAuthor.getID())
							|| PermPrefs.getPermissionsLevel(user.getID()) > PermPrefs
									.getPermissionsLevel(bot.rgameHandler.gameAuthor.getID())) {
						bot.rgameHandler.cancelGame();
						bot.sendMessage(bot.getNewBuilder(channel)
								.appendContent("Current rhythm game cancelled."));
					}

					break;
				}
			}

			return null;
		}

		return CommandResponse.doesNotExist();
	}

	/**
	 * Returns a failure message, or null if there isn't any.
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

		String result = null;

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
			return "The command does not exist.";
		}

		public static String insufficientPermission(long perm, long required) {
			return "You have insufficient permissions. (Required " + required + ", you have " + perm
					+ ")";
		}

	}

}
