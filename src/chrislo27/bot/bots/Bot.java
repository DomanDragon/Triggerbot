package chrislo27.bot.bots;

import java.util.ArrayList;
import java.util.List;

import chrislo27.bot.Main;
import chrislo27.bot.util.TickTask;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.DiscordDisconnectedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;

public abstract class Bot {

	public IDiscordClient client;
	private boolean isReady = false;

	private List<TickTask> tickTasks = new ArrayList<>();

	public Bot() {

	}

	public void scheduleTickTask(int ticksLater, TickTask tt) {
		tt.tickToExecute = Main.ticks + ticksLater;
		tickTasks.add(tt);
	}

	public void setClient(IDiscordClient c) {
		client = c;
	}

	public boolean isReady() {
		return isReady;
	}

	public void tickUpdate(float delta) {
		for (int i = tickTasks.size() - 1; i >= 0; i--) {
			TickTask tt = tickTasks.get(i);

			if (Main.ticks >= tt.tickToExecute) {
				tt.run();

				tickTasks.remove(i);
			}
		}
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
