package chrislo27.bot.bots.baristabot2;

import chrislo27.bot.Main;
import chrislo27.bot.MusicDatabase;
import chrislo27.bot.MusicDatabase.Song;
import chrislo27.bot.bots.Bot;
import chrislo27.bot.bots.baristabot2.trivia.TriviaGame;
import chrislo27.bot.util.TickTask;
import chrislo27.bot.util.Utils;
import com.google.code.chatterbotapi.ChatterBot;
import com.google.code.chatterbotapi.ChatterBotFactory;
import com.google.code.chatterbotapi.ChatterBotSession;
import com.google.code.chatterbotapi.ChatterBotType;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.*;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.*;
import sx.blah.discord.util.MessageBuilder.Styles;
import sx.blah.discord.util.audio.AudioPlayer;
import sx.blah.discord.util.audio.AudioPlayer.Track;
import sx.blah.discord.util.audio.events.TrackFinishEvent;
import sx.blah.discord.util.audio.events.TrackStartEvent;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class BaristaBot2 extends Bot {

	public static final int QUEUE_LIMIT = 25;
	public static final int RANDOM_LIMIT = 10;
	public static final float VOTE_SKIP_RATIO = 0.5f;
	public static final String SONG_METADATA_VOTE_SKIP = "voteSkip_";
	public static final String[] RESTRICTED_CHANNELS = {"general", "botgeneralandmemes", "rhino"};
	public static final String IDEAL_CHANNEL = "201511701345075200";
	public IVoiceChannel radioChannel = null;
	public AudioPlayer audioPlayer;
	protected final Date startTime;
	protected double secondsPlaying = 0;
	protected long playingStartTime = System.currentTimeMillis();
	protected boolean canAddToQueue = true;

	public final CommandHandler cmdHandler;

	private ChatterBot cleverbot;
	private ChatterBotSession cleverbotSession;

	private boolean debugMode = false;
	private int distressingTicks = 0;
	private int ticksWithoutMusic = 0;

	private TriviaGame currentTrivia = null;

	public BaristaBot2() {
		PermPrefs.instance();

		startTime = new Date();
		cmdHandler = new CommandHandler(this);

		try {
			cleverbot = new ChatterBotFactory().create(ChatterBotType.CLEVERBOT);
			cleverbotSession = cleverbot.createSession();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setDebugging(boolean debug) {
		debugMode = debug;

		if (client != null) {
			client.changePresence(debugMode);
			setStatus(null);
		}
	}

	public boolean isDebugging() {
		return debugMode;
	}

	public TriviaGame getCurrentTrivia() {
		return currentTrivia;
	}

	public void setCurrentTrivia(TriviaGame trivia) {
		if (currentTrivia != null) {
			client.getDispatcher().unregisterListener(currentTrivia);
		}

		currentTrivia = trivia;

		if (currentTrivia != null) {
			client.getDispatcher().registerListener(currentTrivia);
		}
	}

	public void sendDistressSignal() {
		distressingTicks = Main.TICK_RATE * 15;
		client.changePresence(true);

		try {
			sendMessage(getNewBuilder(
					client.getOrCreatePMChannel(client.getUserByID("188789412426022914")))
					.appendContent(":warning: **Sending distress signal; please help!**\n")
					.appendContent("Currently "
							+ new Date(System.currentTimeMillis()).toString() + "\n"));
		} catch (RateLimitException | DiscordException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setClient(IDiscordClient c) {
		super.setClient(c);
	}

	public MessageBuilder getNewBuilder(IChannel channel) {
		// adds zero-width space to avoid triggering other bots - complies with best practice
		return new MessageBuilder(client).withChannel(channel).appendContent("\u200B");
	}

	public void sendMessage(MessageBuilder builder) {
		RequestBuffer.request(() -> {
			try {
				builder.send();
			} catch (RateLimitException e) {
				Main.error("Rate limited! Retry after " + e.getRetryDelay() + " ms, bucket: "
						+ e.getBucket());
				// RequestBuffer uses the exception to know when to try again
				throw e;
			} catch (DiscordException e) {
				Main.error("Failed to send message!");
				e.printStackTrace();

				if (e.getErrorMessage().contains("502")) {
					Main.info(
							"Exception was a 502, throwing RateLimitException to trick request buffer: "
									+ e.getErrorMessage());
					throw new RateLimitException("Fake exception thrown because of HTTP 502", 3500,
							"HTTP 502");
				}
			} catch (MissingPermissionsException e) {
				Main.warn("Missing permissions to send messages!");
				e.printStackTrace();
			}
		});
	}

	@EventSubscriber
	public void onReconnect(DiscordReconnectedEvent event) {
		attemptConnectToRadioChannel(radioChannel);
	}

	@EventSubscriber
	public void onMessageGet(MessageReceivedEvent event) {
		IMessage messageObj = event.getMessage();
		IChannel channel = messageObj.getChannel();
		String message = messageObj.getContent();
		IUser author = messageObj.getAuthor();

		if ((message.toLowerCase().contains("kill") && message.toLowerCase().contains("myself"))
				|| (message.toLowerCase().contains("commit")
				&& message.toLowerCase().contains("suicide"))) {
			MessageBuilder builder = getNewBuilder(channel);

			if (!channel.isPrivate())
				builder.appendContent(author.mention() + "\n");
			builder.appendContent("__United States__: 1-800-273-8255\n");
			builder.appendContent("__Canada__: 221, or 1-800-668-6868 for youth\n");
			builder.appendContent("__EU__: 116 123\n");
			builder.appendContent("__UK__: 116 123, or 0800-11-11 for youth\n");
			builder.appendContent(
					"__For other countries/numbers__: https://www.reddit.com/r/SuicideWatch/wiki/hotlines\n");
			builder.appendContent("\n**Please be safe.**");

			sendMessage(builder);

			return;
		}

		for (IUser mention : messageObj.getMentions()) {
			if (messageObj.mentionsEveryone())
				break;
			if (mention.getID().equals(client.getOurUser().getID())) {
				long permLevel = PermPrefs.getPermissionsLevel(author.getID());

				if (permLevel < PermissionTier.NORMAL) {
					return;
				} else {
					// replace mention - two types
					// <@!ID> and <@ID>
					String content = message.replace(client.getOurUser().mention(true), "")
							.replace("<@" + client.getOurUser().getID() + ">", "");

					if (message.isEmpty()) {
						Main.info("Cleverbot question was empty");

						return;
					}

					String response = null;
					try {
						response = cleverbotSession.think(content);
					} catch (Exception e) {
						e.printStackTrace();
					}

					Main.info("Cleverbot responding: " + response);

					sendMessage(
							getNewBuilder(channel).appendContent(":speech_balloon: " + response));
				}

				return;
			}
		}

		if (message.contains("%%%") && !message.startsWith("%")) {
			int i = message.indexOf("%%%");
			message = message.substring(i + 2);
		}
		if (!message.startsWith("%") || message.length() <= 1)
			return;
		if (author.isBot() && author != client.getOurUser())
			return;

		String[] args = message.substring(1).split("\\s+");
		String command = args[0].toLowerCase();
		String[] commandArgs = Arrays.copyOfRange(args, 1, args.length);

		String response = cmdHandler.onCommand(command, commandArgs, messageObj, channel, author);

		if (response != null) {
			sendMessage(getNewBuilder(channel)
					.appendContent(author.mention() + " **The command failed:**  ")
					.appendContent(response));
		}

		System.gc();
	}

	@Override
	public void tickUpdate(float delta) {
		super.tickUpdate(delta);

		if (distressingTicks > 0) {
			distressingTicks--;

			if (distressingTicks == 0) {
				client.changePresence(debugMode);
			} else {
				final float speed = 2;
				boolean yellow = ((distressingTicks % (Main.TICK_RATE * speed))
						/ (Main.TICK_RATE * speed)) < 0.666666f;

				if (client.getOurUser()
						.getPresence() != (yellow ? Presences.IDLE : Presences.ONLINE)) {
					client.changePresence(yellow);
				}
			}
		}

		if (audioPlayer != null && audioPlayer.getCurrentTrack() != null) {
			ticksWithoutMusic = 0;
		} else {
			ticksWithoutMusic++;
		}

		if (currentTrivia != null) {
			currentTrivia.tickUpdate();
		}

		if (Main.ticks % Main.TICK_RATE * 2 == 0) {
			Status st = client.getOurUser().getStatus();

			if (audioPlayer != null && audioPlayer.playlistSize() == 0 && !st.isEmpty()) {
				client.changeStatus(Status.empty());
			}
		}
	}

	public boolean emptyQueueIfAllGone(IChannel channel, MessageBuilder builder) {
		for (IUser user : radioChannel.getConnectedUsers()) {
			if (user == client.getOurUser())
				continue;
			if (user.isBot())
				continue;
			if (user.isDeafLocally() || user.isDeaf(radioChannel.getGuild()))
				continue;
			if (PermPrefs.getPermissionsLevel(user.getID()) <= PermissionTier.DUNGEON)
				continue;

			return false;
		}

		if (audioPlayer.playlistSize() == 0)
			return false;

		audioPlayer.skipTo(audioPlayer.playlistSize());
		setStatus(null);

		Main.info("Cleared queue due to no one/all deaf in the radio channel");
		if (channel != null && builder != null)
			builder.appendContent("Cleared queue because everyone is all gone, or all deafened.");
		return true;
	}

	@EventSubscriber
	public void onAudioStart(TrackStartEvent event) {
		File f = ((File) event.getTrack().getMetadata().get("file"));

		if (f != null) {
			setStatus("♫ " + MusicDatabase
					.getDisguisedName((Song) event.getTrack().getMetadata().get("song")) + " ♫");
			Main.info("Starting to play " + Utils.stripExtension(f.getName()));

			playingStartTime = System.currentTimeMillis();
		}
	}

	@EventSubscriber
	public void onAudioFinish(TrackFinishEvent event) {
		if (event.getOldTrack() == null) {
			Main.info("Audio finished, old track is null");
		} else {
			Main.info("Finished playing " + Utils.stripExtension(
					((File) event.getOldTrack().getMetadata().get("file")).getName()));
		}

		if (!event.getNewTrack().isPresent() || audioPlayer.getPlaylist().size() == 0) {
			setStatus(null);
			Main.info("Finished queue");
		}

		secondsPlaying += Math.abs(System.currentTimeMillis() - playingStartTime) / 1000.0D;

		emptyQueueIfAllGone(null, null);
	}

	public void warnUserIfNotMuted(IUser user) {
		if (!user.isMutedLocally() && !user.isMuted(radioChannel.getGuild())
				&& radioChannel.getUsersHere().contains(user)
				&& !user.equals(client.getOurUser())) {
			try {
				sendMessage(getNewBuilder(client.getOrCreatePMChannel(user)).appendContent(
						"Please mute yourself if you're in the radio channel. Thank you."));
				Main.info("Warned " + user.getName() + "#" + user.getDiscriminator() + " ("
						+ user.getID() + ") about muting themselves");
			} catch (RateLimitException | DiscordException e) {
				e.printStackTrace();
			}
		}
	}

	@EventSubscriber
	public void onUserConnectVoice(UserVoiceChannelJoinEvent event) {
		IUser user = event.getUser();

		if (!user.isMutedLocally() && !user.isMuted(radioChannel.getGuild())
				&& event.getChannel().getID().equals(radioChannel.getID())
				&& !user.equals(client.getOurUser())) {
			TickTask tt = new TickTask() {

				@Override
				public void run() {
					warnUserIfNotMuted(event.getUser());
				}
			};

			this.scheduleTickTask(30 * Main.TICK_RATE, tt);
			Main.info("Scheduled a TickTask to warn " + user.getName() + "#"
					+ user.getDiscriminator() + " (" + user.getID() + ") to muting themselves");
		}
	}

	@EventSubscriber
	public void onUserDisconnectVoice(UserVoiceChannelLeaveEvent event) {
		emptyQueueIfAllGone(null, null);
	}

	@EventSubscriber
	public void onUserMoveVoice(UserVoiceChannelMoveEvent event) {
		emptyQueueIfAllGone(null, null);
		if (event.getNewChannel().getID().equals(radioChannel.getID())) {
			warnUserIfNotMuted(event.getUser());
		}
	}

	@EventSubscriber
	public void onUserConnect(UserJoinEvent event) {

	}

	@EventSubscriber
	public void onUserDisconnect(UserLeaveEvent event) {

	}

	@EventSubscriber
	public void onMeConnect(ReadyEvent event) {
		attemptConnectToRadioChannel(getDefaultRadioChannel());
	}

	@Override
	public void onProgramExit() {
		super.onProgramExit();

		PermPrefs.instance().save();
	}

	public void attemptConnectToRadioChannel(IVoiceChannel radioChannel) {
		Main.info("Attempting to connect to audio channel "
				+ (radioChannel == null ? null : radioChannel.getName()) + "...");

		if (this.radioChannel != null && this.radioChannel.isConnected())
			this.radioChannel.leave();

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

		audioPlayer = null;
		if (this.radioChannel != null) {
			audioPlayer = AudioPlayer.getAudioPlayerForGuild(this.radioChannel.getGuild());

			Main.info("Successfully gotten AudioPlayer instance");
		}

		if (audioPlayer != null) {
			audioPlayer.getPlaylist().clear();
		}
		setStatus(null);
	}

	public IVoiceChannel getDefaultRadioChannel() {
		for (IVoiceChannel channel : client.getVoiceChannels()) {
			// whitelisted servers:
			// Rhythm Heaven, BaristaBotTest
			if (!channel.getGuild().getID().equals("155833718643228672")
					&& !channel.getGuild().getID().equals("199604421955420160"))
				continue;

			if (channel.getName().equalsIgnoreCase("radio"))
				return channel;
		}

		return null;
	}

	public String checkMusicRestricted(IChannel channel, IUser author) {
		if (audioPlayer == null) {
			return "The AudioPlayer is null, ask a mod to use the command `%reconnectaudio`";
		}

		if (debugMode && PermPrefs.getPermissionsLevel(author.getID()) < PermissionTier.ADMIN) {
			return ":wrench: The bot is in debug mode, so you can't do any music actions for right now.";
		}

		if (!channel.isPrivate()
				&& !channel.getGuild().getID().equals(radioChannel.getGuild().getID())) {
			return "The bot's radio channel isn't in this server!";
		}

		if (PermPrefs.getPermissionsLevel(author.getID()) >= PermissionTier.ADMIN)
			return null;

		for (String s : RESTRICTED_CHANNELS) {
			if (channel.getName().equalsIgnoreCase(s)) {
				return "All bot music actions should be done in <#" + IDEAL_CHANNEL
						+ ">, please. (Restricted in this current channel \"" + channel.getName()
						+ "\")";
			}
		}

		return null;
	}

	public boolean canPlayMusic(IChannel channel) {
		if (radioChannel == null || radioChannel.isConnected() == false) {
			Main.info("Tried to play music, not connected to voice channel");
			sendMessage(getNewBuilder(channel).appendContent(
					"I'm not connected to the \"Radio\" channel, so I can't play music/do audio-related things."));

			attemptConnectToRadioChannel(getDefaultRadioChannel());

			return false;
		}

		if (audioPlayer == null) {
			Main.info("Audio player is null");
			sendMessage(getNewBuilder(channel).appendContent(
					"The AudioPlayer instance is null, ask a dev to reconnect it (reconnectaudio)"));

			attemptConnectToRadioChannel(getDefaultRadioChannel());

			return false;
		}

		return true;
	}

	public void showQueue(IChannel channel) {
		if (!canPlayMusic(channel))
			return;

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
					+ MusicDatabase.getDisguisedName(((Song) track.getMetadata().get("song")))
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
		if (!canPlayMusic(channel))
			return;
		if (audioPlayer.playlistSize() == 0)
			return;

		Track first = audioPlayer.getCurrentTrack();
		Collections.shuffle(audioPlayer.getPlaylist());
		audioPlayer.getPlaylist().remove(first);
		audioPlayer.getPlaylist().add(0, first);

		sendMessage(getNewBuilder(channel).appendContent("The playlist has been shuffled."));
		Main.info("Shuffled queue");
		showQueue(channel);

	}

	public void queueAudio(IChannel channel, Song song, MessageBuilder builder) {
		if (!canPlayMusic(channel))
			return;
		if (song == null) {
			builder.appendContent("The file to be queued is null!");
			Main.warn("File for queuing was null");

			return;
		}

		if (audioPlayer.playlistSize() >= QUEUE_LIMIT) {
			builder.appendContent(
					"You can't add any more songs to the playlist (maximum " + QUEUE_LIMIT + ").");
			Main.info("Hit playlist limit when attempting to add more");

			return;
		}

		if (!canAddToQueue) {
			builder.appendContent(":no_entry: The bot has disabled queue-adding.");
			Main.info("Cannot add any more to queue");

			return;
		}

		try {
			Track newTrack = audioPlayer.queue(song.file);
			newTrack.getMetadata().put("song", song);
			boolean alreadyIn = false;

			for (Track track : audioPlayer.getPlaylist()) {
				if (track != newTrack && track.getMetadata().get("file")
						.equals(newTrack.getMetadata().get("file"))) {
					alreadyIn = true;
					break;
				}
			}

			String extensionless = Utils.stripExtension(song.file.getName());
			String disguisedName = MusicDatabase.getDisguisedName(song);

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
				builder.appendContent("__" + disguisedName + "__ has been queued, it is "
						+ audioPlayer.getPlaylist().size() + th + ".");
			} else {
				audioPlayer.getPlaylist().remove(audioPlayer.getPlaylist().size() - 1);
				Main.info("Didn't queue " + extensionless
						+ " because it was already in the playlist");
				builder.appendContent(disguisedName + " is already in the queue!");
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

		emptyQueueIfAllGone(channel, builder);
	}

	public void skipTrack(IChannel channel, boolean showSkippedMessage) {
		if (!canPlayMusic(channel))
			return;

		List<Track> playlist = audioPlayer.getPlaylist();
		MessageBuilder builder = getNewBuilder(channel);
		boolean showQueue = false;

		if (playlist.size() == 0) {
			builder.appendContent("There's nothing in the queue to skip!");
			sendMessage(builder);
		} else {
			if (showSkippedMessage) {
				builder.appendContent("Skipping to next song...");
				sendMessage(builder);
			}
			audioPlayer.skip();
			Main.info("Skipped to next song");
			showQueue = true;

			secondsPlaying += Math.abs(System.currentTimeMillis() - playingStartTime) / 1000.0D;
			playingStartTime = System.currentTimeMillis();
		}

		if (showQueue) {
			showQueue(channel);
		}
	}

	/**
	 * Votes/unvotes the user, then checks if skipping is available, then skips if it's possible.
	 *
	 * @param user
	 * @param channel
	 * @return
	 */
	public String voteToSkipTrackAndAct(IUser user, IChannel channel) {
		if (!canPlayMusic(channel))
			return null;
		if (audioPlayer.getCurrentTrack() == null)
			return "There isn't a song to skip.";
		if (channel.isPrivate()
				&& PermPrefs.getPermissionsLevel(user.getID()) < PermissionTier.ADMIN)
			return "Cannot vote to skip in a private channel.";

		Track current = audioPlayer.getCurrentTrack();
		Map<String, Object> metadata = current.getMetadata();
		final String metadataKey = SONG_METADATA_VOTE_SKIP + user.getID();

		boolean shouldStop = true;
		for (IUser u : radioChannel.getConnectedUsers()) {
			if (u.getID().equals(user.getID())) {
				shouldStop = false;
				break;
			}
		}

		if (shouldStop) {
			return "Cannot vote if you're not in the radio channel.";
		} else if ((!channel.isPrivate() && user.isDeaf(channel.getGuild()))
				|| user.isDeafLocally()) {
			return "Cannot vote if you're deafened.";
		}

		final boolean voted;
		if (!metadata.containsKey(metadataKey)) {
			// vote to skip
			metadata.put(metadataKey, Boolean.TRUE);
			Main.info(user.getName() + "#" + user.getDiscriminator()
					+ " votes to skip the current song");
			voted = true;
		} else {
			// unvote
			metadata.remove(metadataKey);
			Main.info(user.getName() + "#" + user.getDiscriminator() + " retracts their skip vote");
			voted = false;
		}

		int peopleListening = 0;
		int skipVoters = 0;

		for (IUser u : radioChannel.getConnectedUsers()) {
			// bots, deafened, idle, or dungeon/banned don't count
			if (u.isBot() || u.isDeaf(radioChannel.getGuild()) || u.isDeafLocally()
					|| PermPrefs.getPermissionsLevel(u.getID()) < PermissionTier.NORMAL
					|| u.getPresence() == Presences.IDLE)
				continue;

			peopleListening++;
			final String uMetaKey = SONG_METADATA_VOTE_SKIP + u.getID();
			if (metadata.containsKey(uMetaKey)) {
				if (metadata.get(uMetaKey) instanceof Boolean
						&& ((boolean) metadata.get(uMetaKey))) {
					skipVoters++;
				}
			}
		}

		float ratio = (skipVoters * 1f) / peopleListening;
		Main.info("Current skip vote ratio: " + ((int) (ratio * 100)) + "% " + skipVoters + " / "
				+ peopleListening);

		MessageBuilder builder = getNewBuilder(channel);

		builder.appendContent(user.mention() + " " + (voted ? "**voted to skip** the current song."
				: "**retracts their vote** to skip the current song.") + "\n");
		builder.appendContent("Current vote ratio: **" + skipVoters + " / " + peopleListening
				+ "** (__" + String.format("%.1f", (Math.round(ratio * 1000) / 10f)) + "__%)\n");

		if (ratio >= VOTE_SKIP_RATIO) {
			builder.appendContent("The ratio **is at or above** __"
					+ ((int) (VOTE_SKIP_RATIO * 100)) + "%__; skipping this song...");
		} else {
			builder.appendContent("The ratio must be at or above __"
					+ ((int) (VOTE_SKIP_RATIO * 100)) + "%__ to pass the vote.");
		}
		sendMessage(builder);

		if (ratio >= VOTE_SKIP_RATIO) {
			skipTrack(channel, false);
		}

		return null;
	}

	public void showDatabase(IChannel channel, int page, IUser user) {
		try {
			IPrivateChannel pm = client.getOrCreatePMChannel(user);

			MessageBuilder builder = getNewBuilder(pm);

			builder.appendContent("*Song Database* - Page " + (page + 1) + " / "
					+ MusicDatabase.getAllMusicPages()).appendContent("\n");
			builder.appendContent(MusicDatabase.getAllMusicList(page));

			sendMessage(builder);
		} catch (RateLimitException | DiscordException e) {
			e.printStackTrace();
		}

	}

	public boolean insertTrack(Song song, int index) {
		Track t = null;
		try {
			t = audioPlayer.queue(song.file);
			t.getMetadata().put("song", song);
			audioPlayer.getPlaylist().remove(t);
			audioPlayer.getPlaylist().add(index, t);

			return true;
		} catch (IOException | UnsupportedAudioFileException e) {
			e.printStackTrace();
		}

		return false;
	}

	public void setStatus(String status) {
		if (debugMode) {
			client.changeStatus(Status.game("the debugging game"));
			return;
		}

		client.changeStatus(status == null ? Status.empty() : Status.game(status));
	}

}
