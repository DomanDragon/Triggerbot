package chrislo27.bot.bots;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.UnsupportedAudioFileException;

import chrislo27.bot.Main;
import chrislo27.bot.MusicDatabase;
import chrislo27.bot.util.EightBall;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.DiscordDisconnectedEvent;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.UserJoinEvent;
import sx.blah.discord.handle.impl.events.UserLeaveEvent;
import sx.blah.discord.handle.impl.obj.User;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.Presences;
import sx.blah.discord.handle.obj.Status;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MessageBuilder.Styles;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.RequestBuffer;
import sx.blah.discord.util.audio.AudioPlayer;
import sx.blah.discord.util.audio.AudioPlayer.Track;
import sx.blah.discord.util.audio.events.TrackFinishEvent;
import sx.blah.discord.util.audio.events.TrackStartEvent;

public class BaristaBot extends Bot {

	private static final int QUEUE_LIMIT = 25;
	private static final String[] RESTRICTED_CHANNELS = { "general" };
	private static final int NOBODY_IDLE_TIME = 300_000;
	private static final int RANDOM_LIMIT = QUEUE_LIMIT;

	private ArrayList<String> allowedRconUsers = new ArrayList<>();
	private IVoiceChannel radioChannel = null;
	private AudioPlayer audioPlayer;
	private Date startTime;
	private boolean nextQueueIsForce = false;
	private boolean canAddToQueue = true;
	private double secondsPlaying = 0;
	private long playingStartTime = System.currentTimeMillis();
	private long nobodyTime = System.currentTimeMillis();
	private boolean isNoOneListening = false;

	public BaristaBot() {
		allowedRconUsers.add("188789412426022914");
		startTime = new Date();
	}

	private MessageBuilder getNewBuilder(IChannel channel) {
		return new MessageBuilder(client).withChannel(channel);
	}

	private void sendMessage(MessageBuilder builder) {
		RequestBuffer.request(() -> {
			try {
				builder.build();
			} catch (DiscordException e) {
				Main.error("Failed to send console message!");
				e.printStackTrace();
			} catch (MissingPermissionsException e) {
				Main.warn("Missing permissions!");
				e.printStackTrace();
			}
		});
	}

	@Override
	public void tickUpdate(float delta) {
		super.tickUpdate(delta);
	}

	@EventSubscriber
	public void onUserConnect(UserJoinEvent event) {

	}

	@EventSubscriber
	public void onUserDisconnect(UserLeaveEvent event) {

	}

	@EventSubscriber
	public void onMeDisconnect(DiscordDisconnectedEvent event) {
		Main.info("This bot " + event.getClient().getOurUser().getName() + "#"
				+ event.getClient().getOurUser().getDiscriminator() + " was disconnected for "
				+ event.getReason());
	}

	@Override
	@EventSubscriber
	public void onReadyEvent(ReadyEvent event) {
		super.onReadyEvent(event);

		attemptConnectToRadioChannel();
		client.changeStatus(Status.empty());
	}

	@EventSubscriber
	public void onMessageGet(MessageReceivedEvent event) {
		IMessage messageObj = event.getMessage();
		IChannel channel = messageObj.getChannel();
		String message = messageObj.getContent();

		if (event.getMessage().getMentions().contains(client.getOurUser())
				&& event.getMessage().getAuthor() != client.getOurUser()) {
			return;
		}

		if (!message.startsWith("%") || message.length() <= 1) return;
		if (messageObj.getAuthor().isBot()) return;

		IUser author = messageObj.getAuthor();
		String[] args = message.substring(1).split("\\s+");
		String command = args[0].toLowerCase();

		switch (command) {
		case "help":
			MessageBuilder help = getNewBuilder(channel);

			help.appendContent("Here's your commands:", Styles.ITALICS).appendContent("\n");
			help.appendContent("__Use the percent sign % before commands.__\n");
			if (RESTRICTED_CHANNELS.length > 0) {
				help.appendContent(
						"**Please note** that this bot is musically restricted in the following channels: "
								+ Arrays.toString(RESTRICTED_CHANNELS) + "\n");
			}
			help.appendContent("help - shows this message\n");
			help.appendContent("woof - does what it says\n");
			help.appendContent("play/queue <song name> - adds the song to the queue\n");
			help.appendContent("database/showdatabase <page> - shows a list of songs\n");
			help.appendContent(
					"random/playrandom/queuerandom [1-limit] - queues a random song from the database not already in the queue\n");
			help.appendContent("skip - skips to the next song in the track\n");
			help.appendContent("showqueue/nowplaying - shows the queue\n");
			help.appendContent("shuffle - shuffles the queue\n");
			help.appendContent("wot [] - aliens\n");
			help.appendContent("8ball/8-ball - ask the magic 8-ball\n");
			help.appendContent("stats - returns miscellaneous statistics\n");

			sendMessage(help);
			break;
		case "woof":
			sendMessage(getNewBuilder(channel).appendContent("wœuf"));
			break;
		case "rcon":
			String[] consoleArguments = Arrays.copyOfRange(args, 1, args.length);
			if (allowedRconUsers.contains(author.getID())) {
				Main.info("Rcon command: " + args[1]);

				Main.outputMessageBuilder = getNewBuilder(channel);
				Main.outputMessageBuilder.appendContent("Console: ");

				try {
					onConsoleInput(consoleArguments);
					sendMessage(Main.outputMessageBuilder);
				} catch (RateLimitException e) {
					Main.outputMessageBuilder = null;
					Main.warn(
							"Exceeded rate limit! Must wait at least " + e.getRetryDelay() + " ms");
					e.printStackTrace();
				}

				Main.outputMessageBuilder = null;
			} else {
				sendMessage(getNewBuilder(channel).appendContent(author.mention())
						.appendContent(" You're not a registered remote console user!"));
				Main.info("Rcon attempt by " + author.getName() + "(" + author.getID()
						+ ") with arguments " + Arrays.toString(consoleArguments));
			}
			break;
		case "play":
		case "queue":
			if (checkMusicRestricted(channel, author)) break;

			String entire = getContent(args, 1);
			File file = MusicDatabase.instance().files.get(entire.toLowerCase());

			if (entire.startsWith("!") && entire.endsWith("#")) file = null;

			if (file != null) {
				MessageBuilder builder = getNewBuilder(channel);
				queueAudio(channel, file, nextQueueIsForce, builder);
				nextQueueIsForce = false;
				sendMessage(builder);
				showQueue(channel);
			} else {
				sendMessage(getNewBuilder(channel).appendContent(
						"The music you want (" + entire + ") wasn't found in the database."));
				Main.info("Couldn't find " + entire + " in song database");
			}

			break;
		case "skip":
			if (checkMusicRestricted(channel, author)) break;

			skipTrack(channel);
			break;
		case "random":
		case "playrandom":
		case "queuerandom":
			if (checkMusicRestricted(channel, author)) break;

			int number = 1;
			if (args.length >= 2) {
				try {
					number = Integer.parseInt(args[1]);
				} catch (Exception e) {
					number = 1;
				}
			}

			number = Math.max(1,
					Math.min((audioPlayer == null ? RANDOM_LIMIT
							: Math.min(RANDOM_LIMIT, QUEUE_LIMIT - audioPlayer.playlistSize())),
							number));

			Main.info("Queuing " + number + " random song(s)");

			MessageBuilder builder = getNewBuilder(channel);

			for (int i = 0, n = number; i < n; i++) {
				int attempts = 0;
				outer: while (true) {
					String key = MusicDatabase.instance().allKeys
							.get((int) (Math.random() * MusicDatabase.instance().allKeys.size()));

					for (Track track : audioPlayer.getPlaylist()) {
						String trackName = ((File) track.getMetadata().get("file")).getName();
						String fileName = MusicDatabase.instance().files.get(key.toLowerCase())
								.getName();

						Main.debug("num " + i + ", key " + key + ", attempt " + attempts);

						if (trackName.equalsIgnoreCase(fileName)
								|| (key.startsWith("!") && key.endsWith("#"))) {
							attempts++;

							if (attempts >= 255) {
								sendMessage(getNewBuilder(channel).appendContent(
										"Failed to add random song: too many attempts (" + attempts
												+ ")"));

								break outer;
							} else {
								// re-roll
								continue outer;
							}

						}
					}

					queueAudio(channel, MusicDatabase.instance().files.get(key.toLowerCase()),
							nextQueueIsForce, builder);
					builder.appendContent("\n");
					break;
				}
			}

			nextQueueIsForce = false;

			sendMessage(builder);
			showQueue(channel);

			break;
		case "nowplaying":
		case "showqueue":
			if (checkMusicRestricted(channel, author)) break;

			showQueue(channel);
			break;
		case "shuffle":
			if (checkMusicRestricted(channel, author)) break;

			shuffle(channel);
			break;
		case "database":
		case "showdatabase":
			if (checkMusicRestricted(channel, author)) break;

			if (args.length == 1) {
				sendMessage(getNewBuilder(channel)
						.appendContent("You need to specify a page number! (1 to "
								+ MusicDatabase.getAllMusicPages() + ")"));
			} else {
				try {
					int page = Integer.parseInt(args[1]);

					if (page <= 0 || page > MusicDatabase.getAllMusicPages()) {
						throw new RuntimeException("Out of range.");
					}

					showDatabase(channel, page - 1);
				} catch (Exception e) {
					sendMessage(getNewBuilder(channel)
							.appendContent("That wasn't a valid page number."));
				}
			}
			break;
		case "wot":
			// shows aliens staring from Second Contact (drawn version if second argument present)
			sendMessage(getNewBuilder(channel).appendContent(args.length == 1
					? "http://i.imgur.com/nyK1MXi.png" : "http://i.imgur.com/laXsEG9.png"));
			break;
		case "8-ball":
		case "8ball":
			sendMessage(getNewBuilder(channel).appendContent(":8ball: ")
					.appendContent(EightBall.getResponse(), Styles.INLINE_CODE));
			break;
		case "stats":
			MessageBuilder stats = getNewBuilder(channel);

			long diff = System.currentTimeMillis() - startTime.getTime();

			stats.appendContent("Uptime since: " + startTime.toString() + "\n");
			stats.appendContent(
					"Current uptime: " + String.format("%.4f", diff / 60_000f) + " minutes\n");
			stats.appendContent("Music streamed since uptime start: __"
					+ String.format("%.4f", secondsPlaying / 60f) + " minutes__, or about __"
					+ String.format("%.2f", (secondsPlaying * 64_000) / 8 / 1024 / 1024)
					+ " MB__ (64 kbps) - last updated "
					+ String.format("%.2f",
							(System.currentTimeMillis() - playingStartTime) / 60_000f)
					+ " minutes ago\n");

			if (isNoOneListening) {
				stats.appendContent("Currently waiting for active players");
				if (System.currentTimeMillis() - nobodyTime <= NOBODY_IDLE_TIME) {
					stats.appendContent(
							", and will clear the queue in "
									+ String.format("%.2",
											(NOBODY_IDLE_TIME
													- (System.currentTimeMillis() - nobodyTime))
													/ 60_000f)
									+ " minutes");
				}
				stats.appendContent("\n*Why do I do this?*\nI clear the music queue after "
						+ ((int) (NOBODY_IDLE_TIME / 60_000f))
						+ " minutes of people being idle/not present to save bandwidth.");
				stats.appendContent("\n");
			}
			stats.appendContent("Made in :flag_ca:\n");

			sendMessage(stats);
			break;
		case "sfx":
			if (args.length == 1) {
				break;
			}

			for (IRole role : event.getMessage().getAuthor()
					.getRolesForGuild(event.getMessage().getGuild())) {
				if (audioPlayer != null && (role.getName().equals("Top Floor Team")
						|| role.getName().equals("Moderators")
						|| event.getMessage().getAuthor().getID().equals("199220174555971585"))) {
					File f = MusicDatabase.instance().files
							.get("!" + getContent(args, 1).toLowerCase() + "#");

					if (f != null) {
						insertTrack(f, 0);
					}

					break;
				}
			}
			break;
		}

		System.gc();
	}

	@EventSubscriber
	public void onAudioStart(TrackStartEvent event) {
		File f = ((File) event.getTrack().getMetadata().get("file"));

		if (f != null) {
			client.changeStatus(Status.game("♫ " + stripExtension(f.getName()) + " ♫"));
			Main.info("Starting playing " + stripExtension(f.getName()));

			playingStartTime = System.currentTimeMillis();
		}

	}

	@EventSubscriber
	public void onAudioFinish(TrackFinishEvent event) {
		Main.info("Finished playing "
				+ stripExtension(((File) event.getOldTrack().getMetadata().get("file")).getName()));

		if (!event.getNewTrack().isPresent()) {
			client.changeStatus(Status.empty());
			Main.info("Finished queue");
		}

		secondsPlaying += Math.abs(System.currentTimeMillis() - playingStartTime) / 1000.0D;
	}

	public boolean canPlayMusic(IChannel channel) {
		if (radioChannel == null || radioChannel.isConnected() == false) {
			Main.info("Tried to play music, not connected to voice channel");
			sendMessage(getNewBuilder(channel).appendContent(
					"I'm not connected to the \"Radio\" channel, so I can't play music/do audio-related things."));

			return false;
		}

		if (audioPlayer == null) {
			Main.info("Audio player is null");
			sendMessage(getNewBuilder(channel).appendContent(
					"The AudioPlayer instance is null, ask a dev to reconnect it (reconnectaudio)"));

			return false;
		}

		return true;
	}

	public void showQueue(IChannel channel) {
		if (!canPlayMusic(channel)) return;

		boolean shouldBoldLimit = audioPlayer.playlistSize() >= (QUEUE_LIMIT - 5);
		String limitMsg = (shouldBoldLimit ? "**" : "") + audioPlayer.playlistSize()
				+ (shouldBoldLimit ? "**" : "") + " / " + QUEUE_LIMIT + " limit.";
		MessageBuilder builder = getNewBuilder(channel)
				.appendContent("Here's what's in the queue. ");
		builder.appendContent(limitMsg);
		if (shouldBoldLimit) {
			builder.appendContent(" ");
			if (audioPlayer.playlistSize() < QUEUE_LIMIT) {
				builder.appendContent("You're approaching the queue limit!", Styles.BOLD);
			} else if (audioPlayer.playlistSize() == QUEUE_LIMIT) {
				builder.appendContent("Queue full!", Styles.BOLD);
			} else {
				builder.appendContent("You're __over__ the queue limit!", Styles.BOLD);
			}
		}
		builder.appendContent("\n");

		for (int i = 0; i < audioPlayer.getPlaylist().size(); i++) {
			Track track = audioPlayer.getPlaylist().get(i);
			long elapsed = track.getCurrentTrackTime();
			long total = track.getTotalTrackTime();

			builder.appendContent((i == 0 ? "**" : "") + (i + 1) + ". "
					+ stripExtension(((File) track.getMetadata().get("file")).getName())
					+ (i == 0 ? "**" : ""));

			if (i == 0) {
				builder.appendContent(" - "
						+ String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(elapsed),
								TimeUnit.MILLISECONDS.toSeconds(elapsed) - TimeUnit.MINUTES
										.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsed)))
						+ " elapsed");
			}

			builder.appendContent("\n");
		}

		if (audioPlayer.getPlaylist().size() == 0) {
			builder.appendContent("Nothing's here...", Styles.ITALICS);
		}

		sendMessage(builder);
	}

	public void shuffle(IChannel channel) {
		if (!canPlayMusic(channel)) return;
		if (audioPlayer.playlistSize() == 0) return;

		Track first = audioPlayer.getCurrentTrack();
		Collections.shuffle(audioPlayer.getPlaylist());
		audioPlayer.getPlaylist().remove(first);
		audioPlayer.getPlaylist().add(0, first);

		sendMessage(getNewBuilder(channel).appendContent("The playlist has been shuffled."));
		Main.info("Shuffled queue");
		showQueue(channel);

	}

	public void queueAudio(IChannel channel, File file, boolean forceAdd, MessageBuilder builder) {
		if (!canPlayMusic(channel)) return;
		if (file == null) {
			builder.appendContent("The file to be queued is null!");
			Main.warn("File for queuing was null");

			return;
		}

		if (audioPlayer.playlistSize() >= QUEUE_LIMIT && !forceAdd) {
			builder.appendContent(
					"You can't add any more songs to the playlist (maximum " + QUEUE_LIMIT + ").");
			Main.info("Hit playlist limit when attempting to add more");

			return;
		}

		if (!canAddToQueue && !forceAdd) {
			builder.appendContent("The bot has disabled queue-adding.");
			Main.info("Cannot add any more to queue");

			return;
		}

		try {
			Track newTrack = audioPlayer.queue(file);
			boolean alreadyIn = false;

			for (Track track : audioPlayer.getPlaylist()) {
				if (track != newTrack && track.getMetadata().get("file")
						.equals(newTrack.getMetadata().get("file"))) {
					alreadyIn = true;
					break;
				}
			}

			String extensionless = stripExtension(file.getName());

			if (!alreadyIn) {
				String th = "th";
				if (audioPlayer.getPlaylist().size() == 1) {
					th = "st";
				} else if (audioPlayer.getPlaylist().size() == 2) {
					th = "nd";
				} else if (audioPlayer.getPlaylist().size() == 3) {
					th = "rd";
				}

				Main.info("Queued " + extensionless);
				builder.appendContent("__" + extensionless + "__ has been queued, it is "
						+ audioPlayer.getPlaylist().size() + th + ".");
			} else {
				audioPlayer.getPlaylist().remove(audioPlayer.getPlaylist().size() - 1);
				Main.info("Didn't queue " + extensionless
						+ " because it was already in the playlist");
				builder.appendContent(extensionless + " is already in the queue!");
			}
		} catch (IOException e) {
			Main.error("IOException while queuing audio");
			builder.appendContent("An exception occurred while trying to queue audio: ")
					.appendContent(e.getMessage(), Styles.CODE);
			e.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
			Main.error("Unsupported audio file");
			builder.appendContent("The audio file is unsupported. ").appendContent(e.getMessage());
			e.printStackTrace();
		}
	}

	public void skipTrack(IChannel channel) {
		if (!canPlayMusic(channel)) return;

		List<Track> playlist = audioPlayer.getPlaylist();
		MessageBuilder builder = getNewBuilder(channel);
		boolean showQueue = false;

		if (playlist.size() == 0) {
			builder.appendContent("There's nothing in the queue to skip!");
		} else {
			builder.appendContent("Skipping to next song...");
			audioPlayer.skip();
			Main.info("Skipped to next song");
			showQueue = true;

			secondsPlaying += Math.abs(System.currentTimeMillis() - playingStartTime) / 1000.0D;
			playingStartTime = System.currentTimeMillis();
		}

		sendMessage(builder);

		if (showQueue) {
			showQueue(channel);
		}
	}

	public void showDatabase(IChannel channel, int page) {
		MessageBuilder builder = getNewBuilder(channel);

		builder.appendContent(
				"*Song Database* - Page " + (page + 1) + " / " + MusicDatabase.getAllMusicPages())
				.appendContent("\n");
		builder.appendContent(MusicDatabase.getAllMusicList(page));

		sendMessage(builder);
	}

	public void attemptConnectToRadioChannel() {
		Main.info("Attempting to connect to radio audio channel...");

		IVoiceChannel radioChannel = getRadioChannel();

		if (radioChannel == null) {
			Main.warn("Radio channel not found!");
		} else {
			this.radioChannel = null;
			try {
				radioChannel.join();
				this.radioChannel = radioChannel;
				Main.info("Joined radio audio channel successfully.");
			} catch (MissingPermissionsException e) {
				Main.warn("Missing permission to join voice channel!");
				e.printStackTrace();
			}
		}

		if (audioPlayer == null) {
			if (this.radioChannel != null) {
				audioPlayer = AudioPlayer.getAudioPlayerForGuild(this.radioChannel.getGuild());
				Main.info("Successfully created new AudioPlayer instance");
			}
		}
	}

	public IVoiceChannel getRadioChannel() {
		for (IVoiceChannel channel : client.getVoiceChannels()) {
			if (!channel.getGuild().getName().equalsIgnoreCase("Rhythm Heaven")) continue;
			if (channel.getName().equalsIgnoreCase("radio")) return channel;
		}

		return null;
	}

	public String stripExtension(String fileName) {
		if (fileName.lastIndexOf('.') > 0) {
			return fileName.substring(0, fileName.lastIndexOf('.'));
		}

		return fileName;
	}

	public boolean checkMusicRestricted(IChannel channel, IUser author) {
		if (allowedRconUsers.contains(author.getID())) return false;

		if (author.isDeafLocally()) {
			sendMessage(getNewBuilder(channel)
					.appendContent("You can't do music actions when you're self-deafened!"));

			return true;
		}

		for (String s : RESTRICTED_CHANNELS) {
			if (channel.getName().equalsIgnoreCase(s)) {
				sendMessage(getNewBuilder(channel).appendContent(
						"All BaristaBot music actions should be done in #botgeneralandmemes, please. (Restricted in this current channel \""
								+ channel.getName() + "\")"));

				return true;
			}
		}

		return false;
	}

	public String getContent(String[] args, int start) {
		String content = "";
		for (int i = start; i < args.length; i++) {
			content += args[i];
			if (i != args.length - 1) {
				content += " ";
			}
		}

		return content;
	}

	public String getChannelIDByName(String name) {
		Collection<IChannel> all = client.getChannels(false);

		for (IChannel ch : all) {
			if (ch.getName().equals(name)) return ch.getID();
		}

		return null;
	}

	public boolean insertTrack(File file, int index) {
		Track t = null;
		try {
			t = audioPlayer.queue(file);
			audioPlayer.getPlaylist().remove(t);
			audioPlayer.getPlaylist().add(index, t);

			return true;
		} catch (IOException | UnsupportedAudioFileException e) {
			e.printStackTrace();
		}

		return false;
	}

	public void onConsoleInput(String[] args) throws RateLimitException {
		if (args.length == 0) return;

		String command = args[0].toLowerCase();

		switch (command) {
		case "getusers":
			for (IGuild guild : client.getGuilds()) {
				Main.info("Server " + guild.getName());
				for (IUser user : guild.getUsers()) {
					if (user.getPresence() == Presences.OFFLINE) continue;
					Main.info("User " + user.getName() + "#" + user.getDiscriminator() + " ("
							+ user.getID() + ")");
				}
			}

			break;
		case "getchannels":
			Collection<IChannel> all = client.getChannels(false);

			String channels = "";

			for (IChannel ch : all) {
				channels += ch.getName() + "(" + ch.getID() + "), ";
			}

			Main.info("Channels in this server: " + channels);

			break;
		case "saynoconsole":
		case "say":
			if (args.length == 2) {
				Main.info("Needs 3+ arguments! (say <channelIDOrName> <message>)");
				break;
			}

			MessageBuilder builder = new MessageBuilder(client);
			String attemptChannelId = getChannelIDByName(args[1]);
			IChannel generalChannel = client
					.getChannelByID(attemptChannelId == null ? args[1] : attemptChannelId);

			if (generalChannel == null) {
				Main.warn(args[1] + " channel not found!");
				break;
			}

		{
			String content = getContent(args, 2);

			builder.withChannel(generalChannel);
			if (!command.equals("saynoconsole")) {
				builder.appendContent("Console says:", Styles.BOLD).appendContent(" ");
			}
			builder.appendContent(content);

			Main.info("Sent message through console: " + content);
		}

			break;
		case "setplaying":
			String newStatus = getContent(args, 1);
			client.changeStatus(args.length == 1 ? Status.empty() : Status.game(newStatus));

			Main.info("Set status to " + (args.length == 1 ? "nothing" : "\"" + newStatus + "\""));

			break;
		case "reconnectaudio":
			attemptConnectToRadioChannel();

			break;
		case "refreshdb":
		case "refreshdatabase":
			MusicDatabase.instance().forceReupdate();
			Main.info("Database reloaded.");
			break;
		case "clearqueue":
			if (audioPlayer != null) {
				audioPlayer.getPlaylist().clear();
				Main.info("Queue cleared.");
			} else {
				Main.info("Couldn't clear queue. AudioPlayer is null.");
			}
			break;
		case "forcenextqueue":
			if (!nextQueueIsForce) {
				nextQueueIsForce = true;
				Main.info("The next queue add will bypass the limit (" + QUEUE_LIMIT
						+ ") and add ability");
			} else {
				nextQueueIsForce = false;
				Main.info("Disabled queue limit bypass");
			}
			break;
		case "togglequeue":
		case "togglequeuing":
			canAddToQueue = !canAddToQueue;

			Main.info("Ability to add to queue: " + canAddToQueue);
			break;
		case "remove":
			if (args.length == 1) {
				Main.info("Needs index argument! remove <index>");

				break;
			}

			try {
				int index = Integer.parseInt(args[1]);

				if (audioPlayer == null) {
					Main.warn("AudioPlayer is null");
					throw new RuntimeException("AudioPlayer is null");
				} else if (audioPlayer.playlistSize() == 0 || index < 0
						|| index >= audioPlayer.playlistSize()) {
					throw new RuntimeException("Out of range");
				}

				Track removed = audioPlayer.getPlaylist().remove(index);

				Main.info("Removed number " + index + ", track "
						+ stripExtension(((File) removed.getMetadata().get("file")).getName()));
			} catch (Exception e) {
				Main.info("Argument " + args[1] + " isn't a valid number"
						+ (audioPlayer.playlistSize() > 0
								? " (range is 0" + " to " + (audioPlayer.playlistSize() - 1) + ")"
								: ""));
			}

			break;
		case "getservers":
			Main.info("Servers connected:");
			for (IGuild guild : client.getGuilds()) {
				Main.info(guild.getName() + " (id " + guild.getID() + ") owned by "
						+ guild.getOwner().getName() + "#" + guild.getOwner().getDiscriminator()
						+ " (id " + guild.getOwnerID() + ")");
			}

			break;
		case "leaveserver":
			if (args.length < 2) {
				Main.info("Needs guild ID to remove from!");
				break;
			}

			IGuild guild = client.getGuildByID(args[1]);

			if (guild == null) {
				Main.info("Guild doesn't exist! " + args[1]);
			} else {
				if (canAddToQueue) {
					Main.info("Must disable queue-adding before leaving a guild!");
				} else {
					try {
						guild.leaveGuild();
					} catch (DiscordException e) {
						Main.error("Couldn't leave guild!");
						e.printStackTrace();
					}
					Main.info("Left server " + guild.getName() + ".");
				}
			}

			break;
		case "gc":
			System.gc();

			break;
		case "addstreamtime":
			if (args.length < 2) {
				Main.warn("Needs amount argument!");
				break;
			}

			try {
				double old = secondsPlaying;
				secondsPlaying = Math.max(0, secondsPlaying + Double.parseDouble(args[1]));

				Main.info("Added stream time - now " + secondsPlaying + " from " + old);
			} catch (Exception e) {
				Main.error("Invalid duration.");
			}

			break;
		case "prepareforstop":
			canAddToQueue = false;
			if (audioPlayer != null) {
				for (int i = audioPlayer.playlistSize() - 1; i >= 1; i--) {
					audioPlayer.getPlaylist().remove(i);
				}
			} else {
				Main.warn("AudioPlayer is null");
			}
			Main.info("Disabled queuing, emptied queue until last item");
			break;
		case "insert":
			if (args.length < 3) {
				Main.warn("Needs 3+ arguments! move <index> <song>");
			} else if (audioPlayer == null) {
				Main.warn("AudioPlayer is null");
			} else {
				try {
					int index = Integer.parseInt(args[1]);
					if (index < 0 || index >= audioPlayer.playlistSize())
						throw new RuntimeException("Out of range");

					String name = getContent(args, 2);

					File f = MusicDatabase.instance().files.get(name.toLowerCase());

					if (f == null) {
						Main.warn("File not found: " + name);
					} else {
						if (insertTrack(f, index)) {
							Main.info("Added " + name + " to index " + index);
						} else {
							Main.info("Failed to add " + name + " to index " + index);
						}
					}
				} catch (Exception e) {
					Main.warn(args[1] + " is not a valid index");
					e.printStackTrace();
				}
			}

			break;
		}

		System.gc();
	}

}
