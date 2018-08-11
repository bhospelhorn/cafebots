package waffleoRai_cafebotCommands.Commands;

import waffleoRai_cafebotCommands.CommandAdapter;
import waffleoRai_cafebotCommands.MessageID;
import waffleoRai_cafebotCore.AbstractBot;
import waffleoRai_schedulebot.EventType;

/*
 * UPDATES
 * 
 * Creation | June 19, 2018
 * Version 1.0.0 Documentation | July 1, 2018
 * 
 * 1.0.0 -> 1.1.0 | July 20, 2018
 * 	Added cmdID param
 * 1.1.0 -> 1.1.1 | August 11, 2018
 * 	MessageID update
 * 
 */

/**
 * A command for sending a message informing the user that the arguments for an event
 * creation command could not be parsed correctly.
 * <br>This event cannot be induced via command line. It is internal only.
 * @author Blythe Hospelhorn
 * @version 1.1.1
 * @since August 11, 2018
 */
public class CMD_InsufficientArgs extends CommandAdapter{
	
	private long chid;
	private EventType type;
	private String username;
	private long gid;
	
	/**
	 * Construct an InsufficientArgs command.
	 * @param channelID Discord long UID of channel command was issued on and reply should be sent to.
	 * @param et EventType of event creation attempt.
	 */
	public CMD_InsufficientArgs(long channelID, EventType et, String uname, long guildID, long cmdID)
	{
		chid = channelID;
		type = et;
		username = uname;
		MessageID cmdmsg = new MessageID(cmdID, channelID);
		super.setCommandMessageID(cmdmsg);
	}

	/**
	 * Select the function to execute based on the set event type.
	 * @param bot Bot to execute function on.
	 */
	private void pickFunct(AbstractBot bot)
	{
		switch(type)
		{
		case BIRTHDAY:
			bot.insufficientArgsMessage_birthday(chid, username);
			break;
		case BIWEEKLY:
			bot.insufficientArgsMessage_general(chid, username, EventType.BIWEEKLY);
			break;
		case DEADLINE:
			bot.insufficientArgsMessage_general(chid, username, EventType.DEADLINE);
			break;
		case MONTHLYA:
			bot.insufficientArgsMessage_general(chid, username, EventType.MONTHLYA);
			break;
		case MONTHLYB:
			bot.insufficientArgsMessage_general(chid, username, EventType.MONTHLYB);
			break;
		case ONETIME:
			bot.insufficientArgsMessage_general(chid, username, EventType.ONETIME);
			break;
		case WEEKLY:
			bot.insufficientArgsMessage_general(chid, username, EventType.WEEKLY);
			break;
		default:
			break;
		}
	}
	
	@Override
	public long getChannelID()
	{
		return chid;
	}
	
	public long getUserID()
	{
		return -1;
	}
	
	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute(AbstractBot bot) 
	{
		pickFunct(bot);
		super.cleanAfterMyself(bot);
	}

	@Override
	public String toString()
	{
		return "insufargs";
	}

	public long getGuildID()
	{
		return gid;
	}
	
}
