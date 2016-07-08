package chrislo27.bot;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import chrislo27.bot.util.Utils;

public class MusicDatabase {

	private static MusicDatabase instance;

	private MusicDatabase() {
	}

	public static MusicDatabase instance() {
		if (instance == null) {
			instance = new MusicDatabase();
			instance.shouldReupdate = true;
		}

		if (instance.shouldReupdate) {
			instance.loadResources();
		}

		return instance;
	}

	private boolean shouldReupdate = false;
	private ArrayList<String> allMusic = new ArrayList<>();
	public HashMap<String, File> files = new HashMap<>();
	public ArrayList<String> allKeys = new ArrayList<>();
	private ArrayList<String> keySets = new ArrayList<>();
	public String sfxList = "";
	private ArrayList<Entry<String, File>> tempCriteria = new ArrayList<>();

	private void loadResources() {
		allMusic.clear();
		keySets.clear();
		files.clear();
		allKeys.clear();
		sfxList = "";
		tempCriteria.clear();

		// add(new File("music/special/.mp3"), "!#");
		add(new File("music/special/corruptHai.wav"), "!hai hai hai ha ho#", "!a-hai#");
		add(new File("music/special/donkdonk.wav"), "!donkdonk#");
		add(new File("music/special/donkdwonk.wav"), "!donkdwonk#");
		add(new File("music/special/deetdeetduh.wav"), "!deetdeetduh#");
		add(new File("music/special/deetdeetdoot.wav"), "!deetdeetdoot#");
		add(new File("music/special/together.wav"), "!together#");
		add(new File("music/special/medictapspy.wav"), "!medictapspy#");
		add(new File("music/special/dat_engineer.wav"), "!dat_engineer#");
		add(new File("music/special/powhaha.wav"), "!powhaha#");
		add(new File("music/special/steam.wav"), "!steam#");
		add(new File("music/special/stop.wav"), "!stop#");
		add(new File("music/special/nobatter.wav"), "!nobatter#");
		add(new File("music/special/spysnort.wav"), "!spysnort#");
		add(new File("music/special/noot.wav"), "!noot#");
		add(new File("music/special/doverboys.wav"), "!doverboys#");
		add(new File("music/special/johncena.mp3"), "!johncena#");
		add(new File("music/special/nggyu.mp3"), "!nggyu#");
		add(new File("music/special/warcriminal.wav"), "!warcriminal#");
		add(new File("music/special/malomart.mp3"), "!malomart#");
		add(new File("music/special/disco_loop.mp3"), "!discoloop#");
		add(new File("music/special/mwak.mp3"), "!mwak#");
		add(new File("music/special/scratcho.wav"), "!scratcho#");
		add(new File("music/special/dondondon.wav"), "!dondondon#");
		add(new File("music/special/awahaun.wav"), "!awahaun#");
		add(new File("music/special/oldmacdonald.mp3"), "!oldmacdonald#");
		add(new File("music/special/yes.mp3"), "!yes#");
		add(new File("music/special/no.mp3"), "!no#");
		add(new File("music/special/governmentofcanada.mp3"), "!governmentofcanada#", "!govt#",
				"!govtofcanada#");
		add(new File("music/special/babombombom.mp3"), "!babombombom#");
		add(new File("music/special/markiaccel.mp3"), "!markiaccel#");
		add(new File("music/special/canadagoose.mp3"), "!canadagoose#", "!goose#");
		add(new File("music/special/canadagoose2.mp3"), "!canadagoose2#", "!goose2#");
		add(new File("music/special/sanjidesuka.mp3"), "!sanjidesuka#", "!3pm?#");
		add(new File("music/special/oyatsudesuka.mp3"), "!oyatsudesuka#", "!snacktime?#");
		add(new File("music/special/oishiikamone.mp3"), "!oishiikamone#", "!tasty?#");
		add(new File("music/special/hontodesuka.mp3"), "!hontodesuka#", "!allright?#");
		add(new File("music/special/chinesejingles.mp3"), "!chinesejingles#");
		add(new File("music/special/blastoff.wav"), "!blastoff#");

		// add(new File("music/practice/Practice.mp3"), "Practice");
		for (File f : new File("music/practice/").listFiles()) {
			if (f.getName().endsWith(".mp3")) {
				add(f, Utils.stripExtension(f.getName()));
			}
		}

		// add(new File("music/hidden/.mp3"), "?");
		for (File f : new File("music/hidden/").listFiles()) {
			if (f.getName().endsWith(".mp3")) {
				add(f, "?" + Utils.stripExtension(f.getName()));
			}
		}

		// add(new File("music/.mp3"), "");
		add(new File("music/Karate Man Returns.mp3"), "Karate Man Returns", "hey baby",
				"GBA Karate Man", "Karate Man GBA", "this beat is nonstop",
				"this beat is non-stop");
		add(new File("music/Clappy Trio 2.mp3"), "Clappy Trio 2", "Clappy Trio Returns");
		add(new File("music/Tap Trial.mp3"), "Tap Trial");
		add(new File("music/Space Dance.mp3"), "Space Dance");
		add(new File("music/Honey Sweet Angel.mp3"), "Honey Sweet Angel",
				"Honey Sweet Angel of Love", "GBA Remix 3", "Remix 3 GBA");
		add(new File("music/Wish - Can't Wait For You.mp3"), "Can't Wait For You", "GBA Remix 5",
				"Remix 5 GBA");
		add(new File("music/Night Walk GBA.mp3"), "Night Walk GBA", "GBA Night Walk");
		add(new File("music/Bon-Odori.mp3"), "Bon-Odori", "Bon Odori",
				"dondon panpan dondon panpan dondo pan pan");
		add(new File("music/Bon Dance.mp3"), "Bon Dance", "Bon-Dance",
				"panpan dondo panpan dondo pan panpanpan");
		add(new File("music/Cosmic Dance.mp3"), "Cosmic Dance", "Cosmo Dance");
		add(new File("music/Rhythm Tweezers 2.mp3"), "Rhythm Tweezers 2", "Vegetapull",
				"Vegeta-pull", "Vegga-pull");
		add(new File("music/Snappy Trio.mp3"), "Snappy Trio");
		add(new File("music/Turbo Tap Trial.mp3"), "Turbo Tap Trial", "Tap Trial 2");
		add(new File("music/Wizard's Waltz.mp3"), "Wizard's Waltz", "Magician");
		add(new File("music/Marching Orders.mp3"), "Marching Orders", "Marcher", "Marchers");
		add(new File("music/Spaceball.mp3"), "Spaceball", "Air Batter");
		add(new File("music/Sneaky Spirits 2.mp3"), "Sneaky Spirits 2");
		add(new File("music/Samurai Slice GBA.mp3"), "Samurai Slice GBA", "GBA Samurai Slice");
		add(new File("music/Rat Race.mp3"), "Rat Race", "Cheese Heist");
		add(new File("music/DS Title.mp3"), "DS Title");
		add(new File("music/DS Game Select 1.mp3"), "DS Game Select 1", "Game Select 1 DS");
		add(new File("music/Built to Scale DS.mp3"), "Built to Scale DS", "DS Built to Scale");
		add(new File("music/DS OK Rank.mp3"), "DS OK Rank");
		add(new File("music/DS Practice.mp3"), "DS Practice", "Practice DS");
		add(new File("music/Glee Club 2.mp3"), "Glee Club 2");
		add(new File("music/DS Superb Rank.mp3"), "DS Superb Rank");
		add(new File("music/Fillbots 2.mp3"), "Fillbots 2");
		add(new File("music/DS Try Again Rank.mp3"), "DS Try Again Rank");
		add(new File("music/Sick Beats.mp3"), "Sick Beats", "Dr. Bacteria", "Doctor Bacteria",
				"Dr Bacteria");
		add(new File("music/Fan Club JP.mp3"), "Fan Club JP");
		add(new File("music/DS Remix 1.mp3"), "DS Remix 1", "Remix 1 DS");
		add(new File("music/DS Extra Things.mp3"), "DS Extra Things");
		add(new File("music/Rhythm Rally 2.mp3"), "Rhythm Rally 2");
		add(new File("music/Shoot-'em-up 2.mp3"), "Shoot-'em-up 2", "Shoot-em-up 2",
				"Shoot em up 2");
		add(new File("music/Blue Birds.mp3"), "Blue Birds");
		add(new File("music/Moai Doo-Wop.mp3"), "Moai Doo-Wop");
		add(new File("music/DS Remix 2.mp3"), "DS Remix 2", "Remix 2 DS");
		add(new File("music/DS Endless Games.mp3"), "DS Endless Games");
		add(new File("music/Love Lizards.mp3"), "Love Lizards");
		add(new File("music/Showtime.mp3"), "Showtime");
		add(new File("music/Crop Stomp.mp3"), "Crop Stomp");
		add(new File("music/Freeze Frame.mp3"), "Freeze Frame");
		add(new File("music/Dazzles JP.mp3"), "Dazzles JP", "The Dazzles JP");
		add(new File("music/DS Remix 3.mp3"), "DS Remix 3", "Remix 3 DS");
		add(new File("music/DS Rhythm Toys.mp3"), "DS Rhythm Toys");
		add(new File("music/Moai Doo-Wop 2.mp3"), "Moai Doo-Wop 2", "hell");
		add(new File("music/Quiz Show.mp3"), "Quiz Show", "Quiz",
				"How many are in the Clappy Trio?");
		add(new File("music/Rap Men.mp3"), "Rap Men");
		add(new File("music/Rap Women.mp3"), "Rap Women");
		add(new File("music/Munchy Monk.mp3"), "Munchy Monk");
		add(new File("music/Munchy Monk 2.mp3"), "Munchy Monk 2");
		add(new File("music/DJ School.mp3"), "DJ School");
		add(new File("music/Bunny Hop.mp3"), "Bunny Hop", "Rabbit Jump");
		add(new File("music/Tunnel.mp3"), "Tunnel");
		add(new File("music/Frog Hop JP.mp3"), "Frog Hop JP");
		add(new File("music/Jumpin' Jazz.mp3"), "Jumpin' Jazz", "Jumpin Jazz", "Frog Hop 2");
		add(new File("music/Built to Scale 2 DS.mp3"), "Built to Scale 2 DS",
				"DS Built to Scale 2");
		add(new File("music/Toss Boys.mp3"), "Toss Boys");
		add(new File("music/Fan Club 2 JP.mp3"), "Fan Club 2 JP");
		add(new File("music/GBA Try Again Rank.mp3"), "GBA Try Again Rank");
		add(new File("music/GBA OK Rank.mp3"), "GBA OK Rank");
		add(new File("music/GBA Superb Rank.mp3"), "GBA Superb Rank");
		add(new File("music/GBA Perfect Rank.mp3"), "GBA Perfect Rank");
		add(new File("music/GBA Game Select 1.mp3"), "GBA Game Select 1", "Game Select 1 GBA");
		add(new File("music/GBA Game Select 2.mp3"), "GBA Game Select 2", "Game Select 2 GBA");
		add(new File("music/Blue Birds 2.mp3"), "Blue Birds 2");
		add(new File("music/Glass Tappers.mp3"), "Glass Tappers");
		add(new File("music/Lockstep.mp3"), "Lockstep", "hai");
		add(new File("music/Lockstep 2.mp3"), "Lockstep 2");
		add(new File("music/Airboarder JP.mp3"), "Airboarder JP");
		add(new File("music/Airboarder EN.mp3"), "Airboarder EN", "That's Paradise");
		add(new File("music/Ninja Bodyguard.mp3"), "Ninja Bodyguard", "Ninja");
		add(new File("music/DS Game Select 2.mp3"), "DS Game Select 2", "Game Select 2 DS");
		add(new File("music/Polyrhythm.mp3"), "Polyrhythm", "Poly Rhythm", "Built to Scale GBA",
				"GBA Built to Scale");
		add(new File("music/Tram and Pauline.mp3"), "Tram and Pauline");
		add(new File("music/Samurai Slice Endless DS.mp3"), "Samurai Slice Endless DS");
		add(new File("music/Splashdown.mp3"), "Splashdown");
		add(new File("music/Splashdown 2.mp3"), "Splashdown 2");
		add(new File("music/Dazzles 2 JP.mp3"), "Dazzles 2 JP", "The Dazzles 2 JP");
		add(new File("music/Fireworks.mp3"), "Fireworks");
		add(new File("music/Power Calligraphy.mp3"), "Power Calligraphy",
				"angry man drawing Japanese letters and faces");
		add(new File("music/Dog Ninja.mp3"), "Dog Ninja");
		add(new File("music/Karate Man Kicks! JP.mp3"), "Karate Man Kicks JP",
				"Karate Man Kicks! JP");
		add(new File("music/Karate Man Kicks! 2 JP.mp3"), "Karate Man Kicks 2 JP",
				"Karate Man Kicks! 2 JP");
		add(new File("music/Drummer Duel.mp3"), "Drummer Duel");
		add(new File("music/Love Lab.mp3"), "Love Lab");
		add(new File("music/DS Remix 4.mp3"), "DS Remix 4", "Remix 4 DS");
		add(new File("music/DS Perfect Rank.mp3"), "DS Perfect Rank");
		add(new File("music/DS Remix 10.mp3"), "DS Remix 10", "Remix 10 DS");
		add(new File("music/GBA Remix 8.mp3"), "GBA Remix 8", "Remix 8 GBA");
		add(new File("music/GBA Remix 6.mp3"), "GBA Remix 6", "Remix 6 GBA");
		add(new File("music/GBA Rhythm Toys.mp3"), "GBA Rhythm Toys");
		add(new File("music/Glee Club 2.mp3"), "Glee Club 2");
		add(new File("music/GBA Title.mp3"), "GBA Title");
		add(new File("music/Cosmic Rally.mp3"), "Cosmic Rally", "Cosmo Rally", "Rhythm Rally 3",
				"Cosmic Rhythm Rally");
		add(new File("music/Fillbots 3.mp3"), "Fillbots 3");
		add(new File("music/Rockers.mp3"), "Rockers");
		add(new File("music/Rockers 2.mp3"), "Rockers 2");
		add(new File("music/Shoot-'em-up 3.mp3"), "Shoot-'em-up 3", "Shoot-em-up 3",
				"Shoot em up 3");
		add(new File("music/Big Rock Finish A.mp3"), "Big Rock Finish A");
		add(new File("music/Big Rock Finish B.mp3"), "Big Rock Finish B");
		add(new File("music/Big Rock Finish C.mp3"), "Big Rock Finish C");
		add(new File("music/Big Rock Finish D.mp3"), "Big Rock Finish D");
		add(new File("music/Big Rock Finish E.mp3"), "Big Rock Finish E");
		add(new File("music/Big Rock Finish F.mp3"), "Big Rock Finish F");
		add(new File("music/Big Rock Finish G.mp3"), "Big Rock Finish G");
		add(new File("music/Big Rock Finish H.mp3"), "Big Rock Finish H");
		add(new File("music/DS Remix 5.mp3"), "DS Remix 5", "Remix 5 DS");
		add(new File("music/DS Remix 6.mp3"), "DS Remix 6", "Remix 6 DS");
		add(new File("music/DS Remix 7.mp3"), "DS Remix 7", "Remix 7 DS");
		add(new File("music/DS Remix 8.mp3"), "DS Remix 8", "Remix 8 DS");
		add(new File("music/DS Remix 9.mp3"), "DS Remix 9", "Remix 9 DS");
		add(new File("music/Confession Machine.mp3"), "Confession Machine");
		add(new File("music/Space Soccer 2.mp3"), "Space Soccer 2");
		add(new File("music/Space Soccer.mp3"), "Space Soccer");
		add(new File("music/GBA Remix 7.mp3"), "GBA Remix 7", "Remix 7 GBA");
		add(new File("music/DS Cafe.mp3"), "DS Cafe");
		add(new File("music/DS Cast.mp3"), "DS Cast");
		add(new File("music/DS Guitar Lesson.mp3"), "DS Guitar Lesson");
		add(new File("music/Rhythmove Dungeon.mp3"), "Rhythmove Dungeon");
		add(new File("music/Battle of the Bands.mp3"), "Battle of the Bands");
		add(new File("music/GBA Menu.mp3"), "GBA Menu");
		add(new File("music/Rhythm Reference Room.mp3"), "Rhythm Reference Room");
		add(new File("music/GBA Opening.mp3"), "GBA Opening");
		add(new File("music/GBA Staff Credits.mp3"), "GBA Staff Credits");
		add(new File("music/GBA Cafe.mp3"), "GBA Cafe");
		add(new File("music/GBA Practice 1.mp3"), "GBA Practice 1", "Practice 1 GBA",
				"GBA Practice", "Practice GBA");
		add(new File("music/GBA Practice 2.mp3"), "GBA Practice 2", "Practice 2 GBA");
		add(new File("music/GBA Practice 3.mp3"), "GBA Practice 3", "Practice 3 GBA");
		add(new File("music/GBA Rhythm Toys.mp3"), "GBA Rhythm Toys");
		add(new File("music/Horse Machine 1.mp3"), "Horse Machine 1");
		add(new File("music/Horse Machine 2.mp3"), "Horse Machine 2");
		add(new File("music/Horse Machine 3.mp3"), "Horse Machine 3");
		add(new File("music/Horse Machine 4.mp3"), "Horse Machine 4");
		add(new File("music/GBA Endless Games.mp3"), "GBA Endless Games");
		add(new File("music/Drum Lesson.mp3"), "Drum Lesson");
		add(new File("music/Rhythm Feeling Check.mp3"), "Rhythm Feeling Check");
		add(new File("music/Rap Machine.mp3"), "Rap Machine");
		add(new File("music/Tap Troupe.mp3"), "Tap Troupe", "bom bom bom");
		add(new File("music/Packing Pests.mp3"), "Packing Pests");
		add(new File("music/Packing Pests 2.mp3"), "Packing Pests 2");
		add(new File("music/Fever Title.mp3"), "Fever Title");
		add(new File("music/Fever Practice.mp3"), "Fever Practice", "Practice Fever");
		add(new File("music/Flipper-Flop 2.mp3"), "Flipper-Flop 2", "Flipper Flop 2");
		add(new File("music/Ringside.mp3"), "Ringside");
		add(new File("music/Air Rally 2.mp3"), "Air Rally 2");
		add(new File("music/Donk-Donk.mp3"), "Donk-Donk", "Donk Donk", "that thing we do");
		add(new File("music/Shrimp Shuffle.mp3"), "Shrimp Shuffle");
		add(new File("music/Exhibition Match.mp3"), "Exhibition Match");
		add(new File("music/Fork Lifter.mp3"), "Fork Lifter");
		add(new File("music/Tambourine.mp3"), "Tambourine");
		add(new File("music/Board Meeting.mp3"), "Board Meeting");
		add(new File("music/Monkey Watch.mp3"), "Monkey Watch");
		add(new File("music/Working Dough.mp3"), "Working Dough");
		add(new File("music/Built to Scale Fever.mp3"), "Built to Scale Fever",
				"Fever Built to Scale");
		add(new File("music/Figure Fighter 2.mp3"), "Figure Fighter 2");
		add(new File("music/Figure Fighter 3.mp3"), "Figure Fighter 3");
		add(new File("music/Built to Scale 2 Fever.mp3"), "Built to Scale 2 Fever",
				"Fever Built to Scale 2");
		add(new File("music/Fever Game Select 1.mp3"), "Fever Game Select 1",
				"Game Select 1 Fever");
		add(new File("music/Fever Cast.mp3"), "Fever Cast");
		add(new File("music/Greetings.mp3"), "Greetings");
		add(new File("music/Flock Step.mp3"), "Flock Step", "Flockstep");
		add(new File("music/Fever Remix 2.mp3"), "Fever Remix 2", "Remix 2 Fever");
		add(new File("music/Fever Remix 3 JP.mp3"), "Fever Remix 3 JP", "Remix 3 Fever JP");
		add(new File("music/Fever Remix 3 EN.mp3"), "Fever Remix 3 EN", "Tonight",
				"Remix 3 Fever EN", "Remix 3 Fever", "Fever Remix 3");
		add(new File("music/Micro-Row 2.mp3"), "Micro-Row 2");
		add(new File("music/Samurai Slice Fever.mp3"), "Samurai Slice Fever");
		add(new File("music/Fever Remix 4.mp3"), "Fever Remix 4", "Remix 4 Fever");
		add(new File("music/Catch of the Day.mp3"), "Catch of the Day");
		add(new File("music/Fever Remix 5.mp3"), "Fever Remix 5", "Remix 5 Fever");
		add(new File("music/Launch Party.mp3"), "Launch Party");
		add(new File("music/Bossa Nova.mp3"), "Bossa Nova");
		add(new File("music/Love Rap.mp3"), "Love Rap");
		add(new File("music/Love Rap 2.mp3"), "Love Rap 2");
		add(new File("music/Fever Remix 6.mp3"), "Fever Remix 6", "Remix 6 Fever");
		add(new File("music/Cheer Readers.mp3"), "Cheer Readers");
		add(new File("music/Karate Man Combos! JP.mp3"), "Karate Man Combos! JP",
				"Karate Man Combos JP");
		add(new File("music/Karate Man Combos! EN.mp3"), "Karate Man Combos! EN",
				"Karate Man Combos EN");
		add(new File("music/Fever Remix 7.mp3"), "Fever Remix 7", "Remix 7 Fever");
		add(new File("music/Fever Congratulations.mp3"), "Fever Congratulations");
		add(new File("music/Night Walk Fever JP.mp3"), "Night Walk Fever JP");
		add(new File("music/Night Walk Fever EN.mp3"), "Night Walk Fever EN",
				"Dreams of our Generation");
		add(new File("music/Fever Game Select 2.mp3"), "Fever Game Select 2");
		add(new File("music/Samurai Slice 2 Fever.mp3"), "Samurai Slice 2 Fever");
		add(new File("music/Working Dough 2.mp3"), "Working Dough 2");
		add(new File("music/Double Date 2.mp3"), "Double Date 2");
		add(new File("music/Fever Remix 8 EN.mp3"), "Fever Remix 8 EN", "My One and Only",
				"Remix 8 Fever EN", "what can I do");
		add(new File("music/Fever Remix 8 JP.mp3"), "Fever Remix 8 JP", "Remix 8 Fever JP");
		add(new File("music/Hole in One.mp3"), "Hole in One");
		add(new File("music/Cheer Readers 2.mp3"), "Cheer Readers 2");
		add(new File("music/Hole in One 2.mp3"), "Hole in One 2");
		add(new File("music/Screwbot Factory 2.mp3"), "Screwbot Factory 2");
		add(new File("music/Fever Remix 9 EN.mp3"), "Fever Remix 9 EN", "Beautiful One Day",
				"Remix 9 Fever EN");
		add(new File("music/Fever Remix 9 JP.mp3"), "Fever Remix 9 JP", "Remix 9 Fever JP");
		add(new File("music/Micro-Row 3.mp3"), "Micro-Row 3");
		add(new File("music/Karate Man Combos! 2 JP.mp3"), "Karate Man Combos 2 JP",
				"Karate Man Combos! 2 JP");
		add(new File("music/Karate Man Combos! 2 EN.mp3"), "Karate Man Combos 2 EN",
				"Karate Man Combos! 2 JP");
		add(new File("music/Fever Remix 10.mp3"), "Fever Remix 10", "Remix 10 Fever");
		add(new File("music/Screwbot Factory.mp3"), "Screwbot Factory");
		add(new File("music/Double Date.mp3"), "Double Date");
		add(new File("music/See-Saw.mp3"), "See-Saw");
		add(new File("music/Fever Remix 1.mp3"), "Fever Remix 1", "Remix 1 Fever");
		add(new File("music/Air Rally.mp3"), "Air Rally", "Air Rally Story");
		add(new File("music/Animal Acrobat.mp3"), "Animal Acrobat");
		add(new File("music/Badges Menu.mp3"), "Badges Menu", "Badge Menu");
		add(new File("music/Barbershop Remix.mp3"), "Barbershop Remix");
		add(new File("music/Big Rock Finish.mp3"), "Big Rock Finish");
		add(new File("music/Blue Bear.mp3"), "Blue Bear", "Sad Bear", ":o|");
		add(new File("music/Bouncy Road.mp3"), "Bouncy Road", "Hopping Road");
		add(new File("music/Catchy Tune.mp3"), "Catchy Tune", "Bouncing Road");
		add(new File("music/Catchy Tune 2.mp3"), "Catchy Tune 2", "Bouncing Road 2");
		add(new File("music/Charging Chicken.mp3"), "Charging Chicken");
		add(new File("music/Citrus Remix.mp3"), "Citrus Remix", "Orange Remix");
		add(new File("music/Clap Trap JP.mp3"), "Clap Trap JP");
		add(new File("music/Clappy Trio.mp3"), "Clappy Trio", "Clappy Trio Story");
		add(new File("music/Coin Toss.mp3"), "Coin Toss");
		add(new File("music/Donut Remix.mp3"), "Donut Remix");
		add(new File("music/Earth World.mp3"), "Earth World");
		add(new File("music/Figure Fighter.mp3"), "Figure Fighter", "Figure Fighter Story");
		add(new File("music/Fillbots.mp3"), "Fillbots", "Fillbots Story");
		add(new File("music/Final Goodbye.mp3"), "Final Goodbye");
		add(new File("music/Final Remix JP.mp3"), "Final Remix JP");
		add(new File("music/Final Towers.mp3"), "Final Towers");
		add(new File("music/First Contact.mp3"), "First Contact", "PORK RICE BOWLS");
		add(new File("music/Flipper-Flop.mp3"), "Flipper-Flop", "Flipper-Flop Story");
		add(new File("music/Fruit Basket.mp3"), "Fruit Basket");
		add(new File("music/Fruit Basket 2.mp3"), "Fruit Basket 2");
		add(new File("music/Glee Club.mp3"), "Glee Club", "Glee Club Story");
		add(new File("music/Heaven World.mp3"), "Heaven World");
		add(new File("music/Honeybee Remix.mp3"), "Honeybee Remix", "Bee Remix", "I'm a lady now");
		add(new File("music/Jungle Gymnast.mp3"), "Jungle Gymnast");
		add(new File("music/Karate Man Senior.mp3"), "Karate Man Senior", "Karate Man's Father");
		add(new File("music/Karate Man.mp3"), "Karate Man", "Karate Man Story");
		add(new File("music/Kitties!.mp3"), "Kitties", "Cat Clap", "Kitties!");
		add(new File("music/Left-Hand Remix.mp3"), "Left-Hand Remix", "Left Remix",
				"Left Hand Remix");
		add(new File("music/LumBEARjack.mp3"), "LumBEARjack");
		add(new File("music/LumBEARjack 2.mp3"), "LumBEARjack 2");
		add(new File("music/Lush Remix JP.mp3"), "Lush Remix JP", "Forest Remix JP",
				"tokimeki no story");
		add(new File("music/Machine Remix JP.mp3"), "Machine Remix JP");
		add(new File("music/Mascots Menu.mp3"), "Mascots Menu");
		add(new File("music/Megamix Cafe.mp3"), "Megamix Cafe");
		add(new File("music/Megamix Game Stats.mp3"), "Megamix Game Stats");
		add(new File("music/Megamix OK Rank.mp3"), "Megamix OK Rank");
		add(new File("music/Megamix Perfect Rank.mp3"), "Megamix Perfect Rank");
		add(new File("music/Memories Menu.mp3"), "Memories Menu");
		add(new File("music/Micro-Row.mp3"), "Micro-Row", "Micro-Row Story");
		add(new File("music/Museum.mp3"), "Museum");
		add(new File("music/Pajama Party.mp3"), "Pajama Party", "Pajamas");
		add(new File("music/Post-Earth World.mp3"), "Post-Earth World");
		add(new File("music/Pre-Final Remix Towers.mp3"), "Pre-Final Remix Towers");
		add(new File("music/Rhythm Rally.mp3"), "Rhythm Rally", "Rhythm Rally Story");
		add(new File("music/Rhythm Tweezers.mp3"), "Rhythm Tweezers", "Rhythm Tweezers Story",
				"Vegeta-Pull Story", "Vegetapull Story");
		add(new File("music/Right-Hand Remix JP.mp3"), "Right-Hand Remix JP", "Right Remix JP",
				"Right Hand Remix JP");
		add(new File("music/Second Contact.mp3"), "Second Contact", "First Contact 2",
				"PORK RICE BOWLS 2");
		add(new File("music/Shoot-'em-up.mp3"), "Shoot-'em-up", "Shoot-'em-up Story", "Shoot-em-up",
				"Shoot-em-up Story", "Shoot em up", "Shoot em up Story");
		add(new File("music/Sneaky Spirits.mp3"), "Sneaky Spirits", "Sneaky Spirits Story");
		add(new File("music/Songbird Remix.mp3"), "Songbird Remix", "Bird Remix");
		add(new File("music/Super Samurai Slice.mp3"), "Super Samurai Slice");
		add(new File("music/Super Samurai Slice 2.mp3"), "Super Samurai Slice 2");
		add(new File("music/Tangotronic 3000.mp3"), "Tangotronic 3000", "Tangotron",
				"Waltz-a-tron");
		add(new File("music/Tongue Lashing.mp3"), "Tongue Lashing", "Chameleon");
		add(new File("music/Tower Game Select.mp3"), "Tower Game Select");
		add(new File("music/Sumo Brothers.mp3"), "Sumo Brothers", "Sumo Bros.", "Sumo Bros",
				"Sumology");
		add(new File("music/Megamix Title.mp3"), "Megamix Title");
		add(new File("music/Megamix Credits 2 EN.mp3"), "Megamix Credits 2 EN");
		add(new File("music/Clap Trap EN.mp3"), "Clap Trap EN");
		add(new File("music/Dazzles EN.mp3"), "Dazzles EN", "The Dazzles EN");
		add(new File("music/Fan Club EN.mp3"), "Fan Club EN", "Fan Club");
		add(new File("music/Fan Club 2 EN.mp3"), "Fan Club 2 EN", "Fan Club");
		add(new File("music/Final Remix EN.mp3"), "Final Remix EN", "Final Remix");
		add(new File("music/Frog Hop EN.mp3"), "Frog Hop EN", "Frog Hop",
				"Young Love Rock and Roll");
		add(new File("music/Karate Man Kicks! EN.mp3"), "Karate Man Kicks! EN", "Karate Man Kicks EN");
		add(new File("music/Lush Remix EN.mp3"), "Lush Remix EN", "Lush Remix");
		add(new File("music/Machine Remix EN.mp3"), "Machine Remix EN", "Machine Remix");
		add(new File("music/Right-Hand Remix EN.mp3"), "Right-Hand Remix EN", "Right Remix EN",
				"Right Hand Remix EN");

		// this is for the database
		Collections.sort(keySets);
		String current = "";
		for (int i = 0, limit = 0; i < keySets.size(); i++) {
			// ignore key sets that start with ! and end with #
			if (keySets.get(i).startsWith("!") && keySets.get(i).endsWith("#")) {
				continue;
			}

			// hidden songs
			if (keySets.get(i).startsWith("?")) continue;

			current += keySets.get(i) + "\n";

			limit++;
			if (limit >= 25) {
				limit = 0;
				allMusic.add(current);
				current = "";
			}
		}

		if (!current.isEmpty()) allMusic.add(current);

		shouldReupdate = false;
		System.gc();
	}

	public void forceReupdate() {
		shouldReupdate = true;
	}

	public void add(File file, String... aliases) {
		String mus = "";

		boolean isSfx = false;
		boolean dontPut = false;

		for (int i = 0; i < aliases.length; i++) {
			String s = aliases[i];
			files.put(s.toLowerCase(), file);

			allKeys.add(s);

			if (s.startsWith("!") && s.endsWith("#")) {
				isSfx = true;
				s = s.substring(1, s.length() - 1);
			}

			if (s.startsWith("?")) dontPut = true;

			mus += s;
			if (i != aliases.length - 1) {
				mus += " | ";
			}
		}

		if (dontPut) return;

		if (!isSfx) {
			keySets.add(mus);
		} else {
			sfxList += mus + "\n";
		}
	}

	public String underlineWords(String line, String section) {
		int lastIndex = line.toLowerCase().indexOf(section.toLowerCase());

		return line.substring(0, lastIndex) + "__"
				+ line.substring(lastIndex, lastIndex + section.length()) + "__"
				+ line.substring(lastIndex + section.length());
	}

	public List<Entry<String, File>> getSearch(String search) {
		tempCriteria.clear();

		if (search != null) search = search.toLowerCase();

		for (Entry<String, File> e : files.entrySet()) {
			String key = Utils.stripExtension(e.getValue().getName());

			if (search != null && key.toLowerCase().contains(search) == false) continue;
			tempCriteria.add(e);
		}

		return tempCriteria;
	}

	/**
	 * Searches for the entire phrase, if that fails, splits it into words and searches by word.
	 * @param whatEntered
	 * @return
	 */
	public String getSuggestions(String whatEntered) {
		String enteredLower = whatEntered.toLowerCase();
		String[] words = whatEntered.toLowerCase().split("\\s+");
		StringBuilder builder = new StringBuilder();
		boolean foundWholeAlready = false;
		int lines = 0;
		final int lineLimit = 25;

		// check whole occurences first
		for (String keySet : keySets) {
			String keySetLower = keySet.toLowerCase();
			String line = "" + keySet;
			boolean found = false;

			if (keySetLower.contains(enteredLower)) {
				line = underlineWords(line, enteredLower);

				found = true;
				foundWholeAlready = true;
			}

			if (found) {
				lines++;
				builder.append(line).append('\n');
			}
			if (lines >= lineLimit) {
				builder.append("*Narrow your search down. This is only the first " + lineLimit
						+ " lines.*");
				break;
			}
		}

		// check words
		for (String keySet : keySets) {
			String keySetLower = keySet.toLowerCase();
			String line = "" + keySet;
			boolean found = false;
			if (foundWholeAlready) break;

			for (String word : words) {
				if (keySetLower.contains(word.toLowerCase())) {
					line = underlineWords(line, word);

					found = true;
				}
			}

			if (found) {
				lines++;
				builder.append(line).append('\n');
			}
			if (lines >= lineLimit) {
				builder.append("*Narrow your search down. This is only the first " + lineLimit
						+ " lines.*");
				break;
			}
		}

		return builder.toString();
	}

	public static String getAllMusicList(int page) {
		return instance().allMusic.get(page);
	}

	public static int getAllMusicPages() {
		return instance().allMusic.size();
	}

}
