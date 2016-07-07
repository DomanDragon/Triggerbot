package chrislo27.bot.bots;

import chrislo27.bot.Main;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.DiscordDisconnectedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.util.RateLimitException;

public abstract class Bot {

	public IDiscordClient client;
	private boolean isReady = false;

	public Bot() {

	}

	public void setClient(IDiscordClient c) {
		client = c;
	}

	public boolean isReady() {
		return isReady;
	}

	public void tickUpdate(float delta) {

	}

	public void onProgramExit() {

	}

	@EventSubscriber
	public void onReadyEvent(ReadyEvent event) {
		isReady = true;

		Main.info("This bot " + event.getClient().getOurUser().getName() + "#"
				+ event.getClient().getOurUser().getDiscriminator() + " has been logged in");
	}

	@EventSubscriber
	public void onDisconnectedEvent(DiscordDisconnectedEvent event) {
		Main.info("This bot " + event.getClient().getOurUser().getName() + "#"
				+ event.getClient().getOurUser().getDiscriminator() + " was disconnected for "
				+ event.getReason());
	}

}
