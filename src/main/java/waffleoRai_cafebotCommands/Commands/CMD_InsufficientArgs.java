package waffleoRai_cafebotCommands.Commands;

import net.dv8tion.jda.core.entities.Member;
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
 * @version 1.2.0
 * @since January 14, 2019
 */
public class CMD_InsufficientArgs extends CommandAdapter{
	
	private long chid;
	private EventType type;
	//private String username;
	//private long gid;
	
	/**
	 * Construct an InsufficientArgs command.
	 * @param channelID Discord long UID of channel command was issued on and reply should be sent to.
	 * @param et EventType of event creation attempt.
	 */
	public CMD_InsufficientArgs(long channelID, EventType et, Member m, long guildID, long cmdID)
	{
		chid = channelID;
		type = et;
		//username = uname;
		super.requestingUser = m;
		MessageID cmdmsg = new MessageID(cmdID, channelID);
		super.setCommandMessageID(cmdmsg);
	}

	/**
	 * Select the function to execute based on the set event type.
	 * @param bot Bot to execute function on.
	 * @throws InterruptedException 
	 */
	private void pickFunct(AbstractBot bot) throws InterruptedException
	{
		switch(type)
		{
		case BIRTHDAY:
			bot.insufficientArgsMessage_birthday(chid, requestingUser);
			break;
		case BIWEEKLY:
			bot.insufficientArgsMessage_general(chid, requestingUser, EventType.BIWEEKLY);
			break;
		case DEADLINE:
			bot.insufficientArgsMessage_general(chid, requestingUser, EventType.DEADLINE);
			break;
		case MONTHLYA:
			bot.insufficientArgsMessage_general(chid, requestingUser, EventType.MONTHLYA);
			break;
		case MONTHLYB:
			bot.insufficientArgsMessage_general(chid, requestingUser, EventType.MONTHLYB);
			break;
		case ONETIME:
			bot.insufficientArgsMessage_general(chid, requestingUser, EventType.ONETIME);
			break;
		case WEEKLY:
			bot.insufficientArgsMessage_general(chid, requestingUser, EventType.WEEKLY);
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
	
	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute(AbstractBot bot) throws InterruptedException 
	{
		pickFunct(bot);
		super.cleanAfterMyself(bot);
	}

	@Override
	public String toString()
	{
		return "insufargs";
	}

}
