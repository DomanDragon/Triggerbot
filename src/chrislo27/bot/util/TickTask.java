package chrislo27.bot.util;

public abstract class TickTask {

	public int tickToExecute = 0;

	public TickTask() {

	}

	public abstract void run();

}
