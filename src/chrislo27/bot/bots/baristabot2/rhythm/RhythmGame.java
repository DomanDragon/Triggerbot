package chrislo27.bot.bots.baristabot2.rhythm;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;

public abstract class RhythmGame {

	public static final float DEVIENCY = 2.5f;
	
	public final File music;
	public final ArrayList<Timing> timings = new ArrayList<>();

	public RhythmGame(File music) {
		this.music = music;
	}

	public final void reset() {
		timings.sort(new Comparator<Timing>() {

			@Override
			public int compare(Timing arg0, Timing arg1) {
				if (arg0.timing < arg1.timing) return -1;
				if (arg0.timing > arg1.timing) return 1;

				return 0;
			}

		});

		for (Timing t : timings) {
			t.reset();
		}
	}

	public static class Timing {

		public final float timing;
		public float inputDelay = 0;
		public boolean wasInputted = false;

		public Timing(float t) {
			timing = t;
		}

		public final void reset() {
			inputDelay = 0;
			wasInputted = false;
		}

		public void input(float delay) {
			inputDelay = delay;
			wasInputted = true;
		}

	}

}
