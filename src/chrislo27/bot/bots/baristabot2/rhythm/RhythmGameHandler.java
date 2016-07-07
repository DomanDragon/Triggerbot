package chrislo27.bot.bots.baristabot2.rhythm;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.sound.sampled.UnsupportedAudioFileException;

import chrislo27.bot.Main;
import chrislo27.bot.bots.baristabot2.BaristaBot2;
import chrislo27.bot.bots.baristabot2.rhythm.RhythmGame.Timing;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.audio.AudioPlayer.Track;
import sx.blah.discord.util.audio.events.TrackFinishEvent;

public class RhythmGameHandler {

	public final BaristaBot2 bot;

	public RhythmGame currentGame = null;
	public IUser gameAuthor = null;
	private IChannel lastChannel;

	public RhythmGameHandler(BaristaBot2 bot) {
		this.bot = bot;
	}

	public float input(long time) {
		if (currentGame == null) return 0;

		long currentTime = bot.audioPlayer.getCurrentTrack().getCurrentTrackTime();
		int closest = -1;

		for (int i = 0; i < currentGame.timings.size(); i++) {
			Timing t = currentGame.timings.get(i);

			if (t.wasInputted) continue;

			long timeDifference = (long) (currentTime - (t.timing * 1000));

			if (Math.abs(timeDifference / 1000f) > RhythmGame.DEVIENCY) continue;

			if (closest == -1 || (Math
					.abs(currentTime - (currentGame.timings.get(closest).timing * 1000))) > Math
							.abs(timeDifference)) {
				closest = i;
			}
		}

		if (closest == -1) return 0;

		Timing t = currentGame.timings.get(closest);
		long timeDifference = (long) (currentTime - (t.timing * 1000));
		float delay = (timeDifference / 1000f) / RhythmGame.DEVIENCY;

		t.input(delay);

		return delay;
	}

	@EventSubscriber
	public void onAudioFinish(TrackFinishEvent event) {
		if (currentGame == null) return;

		if (((File) event.getOldTrack().getMetadata().get("file")) == currentGame.music) {
			bot.sendMessage(bot.getNewBuilder(lastChannel).appendContent(getResults()));
		}

		cancelGame();
	}

	public String getResults() {
		if (currentGame == null) return "currentGame null";

		float accuracy = 0;
		int misses = 0;
		for (Timing t : currentGame.timings) {
			accuracy += !t.wasInputted ? 0 : (1f - t.inputDelay);
			if (!t.wasInputted) misses++;
		}

		accuracy /= currentGame.timings.size();

		float newAccuracy = Math.abs(accuracy);

		return gameAuthor.mention() + "\nOverall accuracy: **" + newAccuracy * 100
				+ "%**\nMissed inputs: **" + misses + " / " + currentGame.timings.size()
				+ "**\nOn average you were leaning towards being **"
				+ (misses >= currentGame.timings.size() ? "a failure"
						: (accuracy < 0 ? "early" : "late"))
				+ "**";
	}

	public boolean startGame(IUser starter, RhythmGame game, IChannel channel)
			throws IOException, UnsupportedAudioFileException {
		if (currentGame != null) return false;

		lastChannel = channel;

		game.reset();
		currentGame = game;
		gameAuthor = starter;

		Track t = bot.audioPlayer.queue(game.music);
		bot.audioPlayer.getPlaylist().remove(t);
		bot.audioPlayer.getPlaylist().add(0, t);

		return true;
	}

	public void cancelGame() {
		if (currentGame == null) return;

		List<Track> tracks = bot.audioPlayer.getPlaylist();
		for (int i = 0; i < tracks.size(); i++) {
			if (((File) tracks.get(i).getMetadata().get("file")) == currentGame.music) {
				tracks.remove(i);
				break;
			}
		}

		currentGame.reset();
		currentGame = null;
		gameAuthor = null;
	}

}
