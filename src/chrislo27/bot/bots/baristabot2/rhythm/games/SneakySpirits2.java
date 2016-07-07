package chrislo27.bot.bots.baristabot2.rhythm.games;

import java.io.File;

import chrislo27.bot.bots.baristabot2.rhythm.RhythmGame;

public class SneakySpirits2 extends RhythmGame {

	public SneakySpirits2() {
		super(new File("music/rhythmgame/sneakyspirits2.mp3"));

		this.timings.add(new Timing(7.75f));
		this.timings.add(new Timing(13));
		this.timings.add(new Timing(17.6f));
		this.timings.add(new Timing(22.5f));
		this.timings.add(new Timing(28));
		this.timings.add(new Timing(35));
		this.timings.add(new Timing(40.5f));
		this.timings.add(new Timing(44.625f));
		this.timings.add(new Timing(51.2f));
		this.timings.add(new Timing(59.2f));
		this.timings.add(new Timing(60 + 7.1f));
		this.timings.add(new Timing(60 + 13.25f));
	}

}
