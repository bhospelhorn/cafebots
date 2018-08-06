package waffleoRai_cafebotCommands.Commands;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import waffleoRai_cafebotCommands.CommandAdapter;
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
 * 
 * 1.1.0 -> 1.2.0 | August 5, 2018
 * 	Added view functionality
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
 * @version 1.2.0
 * @since August 5, 2018
 */
public class CMD_SOR extends CommandAdapter {
	
	private MessageChannel channel;
	private Member user;
	private boolean dir;
	private boolean all;
	private boolean defo;
	private EventType type;
	private int level;
	
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
		user = u;
		dir = direction;
		all = true;
		type = null;
		defo = setdefo;
		level = -1;
		super.setCommandMessageID(cmdID);
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
		user = u;
		dir = false;
		all = false;
		defo = false;
		type = t;
		level = l;
		super.setCommandMessageID(cmdID);
	}
	
	/**
	 * Construct a SOR command for viewing reminder times for a particular event type.
	 * @param ch Channel to send message to.
	 * @param t Type of event to view reminder times for.
	 * @param cmdID ID of command message.
	 */
	public CMD_SOR(MessageChannel ch, EventType t, long cmdID)
	{
		channel = ch;
		type = t;
		super.setCommandMessageID(cmdID);
	}
	
	@Override
	public long getChannelID()
	{
		return channel.getIdLong();
	}
	
	public long getUserID()
	{
		return user.getUser().getIdLong();
	}

	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute(AbstractBot bot) 
	{
		if (user == null)
		{
			bot.printReminderTimes(channel.getIdLong(), type);
			return;
		}
		if (all)
		{
			if(dir) bot.sorAllOn(channel.getIdLong(), user);
			else bot.sorAllOff(channel.getIdLong(), user);
		}
		else if (defo)
		{
			bot.sorDefo(channel.getIdLong(), user);
		}
		else bot.sor(channel.getIdLong(), user, type, level);
		
	}
	
	@Override
	public String toString()
	{
		return "sor";
	}

}
