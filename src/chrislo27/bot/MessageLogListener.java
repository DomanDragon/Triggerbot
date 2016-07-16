package chrislo27.bot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.List;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageDeleteEvent;
import sx.blah.discord.handle.impl.events.MessagePinEvent;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.MessageUnpinEvent;
import sx.blah.discord.handle.impl.events.MessageUpdateEvent;
import sx.blah.discord.handle.impl.events.PresenceUpdateEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.UserBanEvent;
import sx.blah.discord.handle.impl.events.UserPardonEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IMessage.Attachment;
import sx.blah.discord.handle.obj.IUser;

public class MessageLogListener {

	public final File logFile;
	private final PrintWriter writer;

	public MessageLogListener(File logFile) throws IOException {
		this.logFile = logFile;

		writer = new PrintWriter(new FileWriter(this.logFile, true), true);
	}

	public void dispose() {
		if (writer != null) writer.close();
	}

	private void printStart(String code, IUser user, IGuild guild) {
		writer.print(Main.getTimestamp() + " [" + code + "] ");
		writer.print("[User: " + (guild == null ? user.getName() : user.getDisplayName(guild)) + "#"
				+ user.getDiscriminator() + " (" + user.getID() + ")] ");
	}

	private String getIndent() {
		return "        ";
	}

	private void printMessageContent(IMessage message) {
		writer.println("[" + message.getContent() + "]");

		List<Attachment> attachments = message.getAttachments();

		if (attachments.size() > 0) {
			writer.println(getIndent() + "with attachments:");
		}

		for (Attachment a : attachments) {
			writer.println(getIndent() + "[" + a.getFilename() + ", " + a.getFilesize() / 1024
					+ " KB, " + a.getUrl() + "]");
		}
	}

	@EventSubscriber
	public void onReady(ReadyEvent event) {
		writer.println("> Start ready event info\n");

		writer.println("User presences by guild:");
		IDiscordClient client = event.getClient();

		for (IGuild guild : client.getGuilds()) {
			writer.println("\nGuild " + guild.getName() + " (" + guild.getID() + ") owned by "
					+ guild.getOwner().getName() + "#" + guild.getOwner().getDiscriminator() + " ("
					+ guild.getOwnerID() + ")\n");

			List<IUser> users = guild.getUsers();
			users.sort(new Comparator<IUser>() {

				@Override
				public int compare(IUser o1, IUser o2) {
					int c = o1.getPresence().compareTo(o2.getPresence());

					if (c == 0) {
						return o1.getDisplayName(guild).compareTo(o2.getDisplayName(guild));
					}

					return c;
				}
			});

			for (IUser u : users) {
				writer.println(u.getDisplayName(guild) + "#" + u.getDiscriminator() + " ("
						+ u.getID() + "): " + u.getPresence());
			}
		}

		writer.println("\n> End ready event info");
		writer.print("\n\n");
	}

	@EventSubscriber
	public void onMessageGet(MessageReceivedEvent event) {
		printStart("MSG_GET", event.getMessage().getAuthor(), event.getMessage().getGuild());

		printMessageContent(event.getMessage());
	}

	@EventSubscriber
	public void onMessageEdited(MessageUpdateEvent event) {
		printStart("MSG_EDIT", event.getNewMessage().getAuthor(), event.getNewMessage().getGuild());

		writer.println("Old message:");
		printMessageContent(event.getOldMessage());
		writer.println("\nchanged to\n");
		printMessageContent(event.getNewMessage());
	}

	@EventSubscriber
	public void onMessageDeleted(MessageDeleteEvent event) {
		printStart("MSG_DELETE", event.getMessage().getAuthor(), event.getMessage().getGuild());

		printMessageContent(event.getMessage());
	}

	@EventSubscriber
	public void onMessagePinned(MessagePinEvent event) {
		printStart("PIN", event.getMessage().getAuthor(), event.getMessage().getGuild());

		writer.println("Fired from channel #" + event.getChannel().getName() + " ("
				+ event.getChannel().getID() + ")");
		printMessageContent(event.getMessage());
	}

	@EventSubscriber
	public void onMessageUnpinned(MessageUnpinEvent event) {
		printStart("UNPIN", event.getMessage().getAuthor(), event.getMessage().getGuild());

		writer.println("Fired from channel #" + event.getChannel().getName() + " ("
				+ event.getChannel().getID() + ")");
		printMessageContent(event.getMessage());
	}

	@EventSubscriber
	public void onUserBanned(UserBanEvent event) {
		printStart("BAN", event.getUser(), event.getGuild());
	}

	@EventSubscriber
	public void onUserPardoned(UserPardonEvent event) {
		printStart("PARDON", event.getUser(), event.getGuild());
	}

	@EventSubscriber
	public void onPresenceChange(PresenceUpdateEvent event) {
		printStart("PRESENCE", event.getUser(), null);
		writer.println("Presence changed from " + event.getOldPresence().toString() + " to "
				+ event.getOldPresence().toString());
	}

}
