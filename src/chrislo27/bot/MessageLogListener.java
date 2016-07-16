package chrislo27.bot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageDeleteEvent;
import sx.blah.discord.handle.impl.events.MessagePinEvent;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.MessageSendEvent;
import sx.blah.discord.handle.impl.events.MessageUnpinEvent;
import sx.blah.discord.handle.impl.events.MessageUpdateEvent;
import sx.blah.discord.handle.impl.events.PresenceUpdateEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.UserBanEvent;
import sx.blah.discord.handle.impl.events.UserJoinEvent;
import sx.blah.discord.handle.impl.events.UserLeaveEvent;
import sx.blah.discord.handle.impl.events.UserPardonEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IMessage.Attachment;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Presences;

public class MessageLogListener {

	public final File logFile;
	private final PrintWriter writer;

	public MessageLogListener(File logFile) throws IOException {
		this.logFile = logFile;

		writer = new PrintWriter(new FileWriter(this.logFile, true), true);
		writer.println(Main.getTimestamp() + " -------- START OF LOGGING --------\n\n");
	}

	public synchronized void dispose() throws IOException {
		if (writer != null) {
			writer.println("\n\n" + Main.getTimestamp() + " -------- END OF LOGGING --------");
			writer.close();
		}
	}

	private synchronized void printStart(String code, IMessage message) {
		IGuild guild = message.getChannel().isPrivate() ? null : message.getGuild();
		IUser user = message.getAuthor();

		printStart(code, user, guild);
	}

	private synchronized void printStart(String code, IUser user, IGuild guild) {
		writer.print(Main.getTimestamp() + " [" + code + "] ");
		writer.print("[" + (guild == null ? user.getName() : user.getDisplayName(guild)) + "#"
				+ user.getDiscriminator() + " (" + user.getID() + ")] ");
		if (guild != null) writer.print("[Guild " + guild.getName() + " (" + guild.getID() + ")] ");
	}

	private String getIndent() {
		return "        ";
	}

	private synchronized void printMessageContent(IMessage message, boolean showChannel) {
		if (showChannel) {
			writer.println("[#" + message.getChannel().getName() + " ("
					+ message.getChannel().getID() + ")]");
		}
		writer.println("[\n" + message.getContent() + "\n]");

		List<Attachment> attachments = message.getAttachments();

		if (attachments.size() > 0) {
			writer.println(getIndent() + "with attachments:");
		}

		for (Attachment a : attachments) {
			writer.println(getIndent() + "[" + a.getFilename() + ", " + a.getFilesize() / 1024
					+ " KB, " + a.getUrl() + "]");
		}
	}

	private synchronized void printMessageContent(IMessage message) {
		printMessageContent(message, true);
	}

	@EventSubscriber
	public synchronized void onReady(ReadyEvent event) {
		writer.println("> Start ready event info\n");

		writer.println("User presences by guild:");
		IDiscordClient client = event.getClient();

		writer.println();
		for (IGuild guild : client.getGuilds()) {
			writer.println("Guild " + guild.getName() + " (" + guild.getID() + ") owned by "
					+ guild.getOwner().getName() + "#" + guild.getOwner().getDiscriminator() + " ("
					+ guild.getOwnerID() + ")");

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
				if (u.getPresence() == Presences.OFFLINE) continue;

				writer.print(u.getDisplayName(guild) + "#" + u.getDiscriminator() + " (" + u.getID()
						+ "): " + u.getPresence() + ", ");
			}

			writer.println();
		}

		writer.println("\n> End ready event info");
		writer.print("\n\n");
	}

	@EventSubscriber
	public synchronized void onMessageGet(MessageReceivedEvent event) {
		printStart("MSG_GET", event.getMessage());

		printMessageContent(event.getMessage());
	}

	@EventSubscriber
	public synchronized void onMessageSend(MessageSendEvent event) {
		onMessageGet(new MessageReceivedEvent(event.getMessage()));
	}

	@EventSubscriber
	public synchronized void onMessageEdited(MessageUpdateEvent event) {
		printStart("MSG_EDIT", event.getNewMessage());

		writer.println("Old message:");
		printMessageContent(event.getOldMessage());
		writer.println("" + getIndent() + "changed to:");
		printMessageContent(event.getNewMessage(), false);
	}

	@EventSubscriber
	public synchronized void onMessageDeleted(MessageDeleteEvent event) {
		printStart("MSG_DELETE", event.getMessage());

		printMessageContent(event.getMessage());
	}

	@EventSubscriber
	public synchronized void onMessagePinned(MessagePinEvent event) {
		printStart("PIN", event.getMessage());

		printMessageContent(event.getMessage());
	}

	@EventSubscriber
	public synchronized void onMessageUnpinned(MessageUnpinEvent event) {
		printStart("UNPIN", event.getMessage());

		printMessageContent(event.getMessage());
	}

	@EventSubscriber
	public synchronized void onUserBanned(UserBanEvent event) {
		printStart("BAN", event.getUser(), event.getGuild());
	}

	@EventSubscriber
	public synchronized void onUserPardoned(UserPardonEvent event) {
		printStart("PARDON", event.getUser(), event.getGuild());
	}

	@EventSubscriber
	public synchronized void onPresenceChange(PresenceUpdateEvent event) {
		printStart("PRESENCE", event.getUser(), null);
		writer.println("Presence changed to " + event.getNewPresence().toString() + " (was "
				+ event.getOldPresence().toString() + ")");
	}

	@EventSubscriber
	public synchronized void onUserJoin(UserJoinEvent event) {
		printStart("USER_JOIN", event.getUser(), event.getGuild());
	}

	@EventSubscriber
	public synchronized void onUserLeave(UserLeaveEvent event) {
		printStart("USER_LEAVE", event.getUser(), event.getGuild());
	}

}
