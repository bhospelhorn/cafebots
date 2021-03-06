package waffleoRai_cafebotCommands.Commands;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Guild;
import waffleoRai_cafebotCommands.CommandAdapter;
import waffleoRai_cafebotCommands.MessageID;
import waffleoRai_cafebotCore.AbstractBot;
import waffleoRai_schedulebot.EventType;

/*
 * UPDATES
 * 
 * Creation | June 24, 2018
 * Version 1.0.0 Documentation | July 1, 2018
 * 
 * 1.0.0 -> 1.1.0 | July 20, 2018
 * 	Added command message ID note
 * 1.1.0 -> 1.2.0 | August 5, 2018
 * 	Added view functionality
 * 1.2.0 -> 1.2.1 | August 11, 2018
 * 	MessageID update
 */

/**
 * A Command for turning on/off certain event reminders
 * <br><br><b>Standard Commands:</b>
 * <br>sor [etype] [rlevel]
 * <br>sor alloff
 * <br>sor allon
 * <br>sor defo
 * <br>sor view [etype]
 * @author Blythe Hospelhorn
 * @version 1.3.0
 * @since January 14, 2019
 */
public class CMD_SOR extends CommandAdapter {
	
	private MessageChannel channel;
	//private Member user;
	private boolean dir;
	private boolean all;
	private boolean defo;
	private EventType type;
	private int level;
	
	//private Guild guild;
	
	/**
	 * Construct a SOR (Switch on/off Reminders) command that switches all reminders on
	 * or off for requesting user.
	 * @param ch Channel command was issued on and reply should be sent to.
	 * @param u User who issued the command, as a JDA Member object.
	 * @param direction True if turning all reminders on, False if turning all reminders off.
	 * @param cmdID Long UID of the message the command was sent in.
	 */
	public CMD_SOR(MessageChannel ch, Member u, boolean direction, boolean setdefo, long cmdID)
	{
		//All	
		channel = ch;
		super.requestingUser = u;
		dir = direction;
		all = true;
		type = null;
		defo = setdefo;
		level = -1;
		MessageID cmdmsg = new MessageID(cmdID, ch.getIdLong());
		super.setCommandMessageID(cmdmsg);
	}
	
	/**
	 * Construct a SOR (Switch on/off Reminders) command that switches only a specific reminder
	 * on or off for requesting user. It will set it to the opposite state it is currently in.
	 * @param ch Channel command was issued on and reply should be sent to.
	 * @param u User who issued the command, as a JDA Member object.
	 * @param t Type of event to switch reminder on or off.
	 * @param l Reminder level to switch on or off.
	 * @param cmdID Long UID of the message the command was sent in.
	 */
	public CMD_SOR(MessageChannel ch, Member u, EventType t, int l, long cmdID)
	{
		//One
		channel = ch;
		//user = u;
		super.requestingUser = u;
		dir = false;
		all = false;
		defo = false;
		type = t;
		level = l;
		MessageID cmdmsg = new MessageID(cmdID, ch.getIdLong());
		super.setCommandMessageID(cmdmsg);
	}
	
	/**
	 * Construct a SOR command for viewing reminder times for a particular event type.
	 * @param ch Channel to send message to.
	 * @param g Guild message was sent in.
	 * @param t Type of event to view reminder times for.
	 * @param cmdID ID of command message.
	 */
	public CMD_SOR(MessageChannel ch, Guild g, EventType t, long cmdID)
	{
		channel = ch;
		type = t;
		//guild = g;
		MessageID cmdmsg = new MessageID(cmdID, ch.getIdLong());
		super.setCommandMessageID(cmdmsg);
	}
	
	@Override
	public long getChannelID()
	{
		return channel.getIdLong();
	}
	
	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute(AbstractBot bot) throws InterruptedException 
	{
		if (super.requestingUser == null)
		{
			bot.printReminderTimes(channel.getIdLong(), type);
			super.cleanAfterMyself(bot);
			return;
		}
		if (all)
		{
			if(dir) bot.sorAllOn(channel.getIdLong(), super.requestingUser);
			else bot.sorAllOff(channel.getIdLong(), super.requestingUser);
		}
		else if (defo)
		{
			bot.sorDefo(channel.getIdLong(), super.requestingUser);
		}
		else bot.sor(channel.getIdLong(), super.requestingUser, type, level);
		super.cleanAfterMyself(bot);
	}
	
	@Override
	public String toString()
	{
		return "sor";
	}

}
