package waffleoRai_cafebotCommands.Commands;

import net.dv8tion.jda.core.entities.Member;
import waffleoRai_cafebotCommands.CommandAdapter;
import waffleoRai_cafebotCommands.MessageID;
import waffleoRai_cafebotCore.AbstractBot;

/*
 * UPDATES
 * 
 * Creation | June 9, 2018
 * Version 1.0.0 Documentation | July 1, 2018
 * 
 * 1.0.0 -> 1.1.0 | July 20, 2018
 * 	Added command message ID note
 * 
 * 1.1.0 -> 1.2.0 | August 11, 2018
 * 	Command ID now MessageID
 * 
 */


/**
 * A Command for adding a birthday event to the schedule.
 * <br><br><b>Standard Command:</b>
 * <br>birthday mm dd
 * @author Blythe Hospelhorn
 * @version 1.3.0
 * @since January 14, 2019
 */
public class CMD_AddBirthday extends CommandAdapter{

	private int month;
	private int day;
	//private Member user;
	private long replyChannel;

	/**
	 * Construct an AddBirthday command.
	 * @param m Birthday month
	 * @param d Birthday day of month
	 * @param u User requesting addition as a Discord API Member object
	 * @param chID UID of channel to send messages to
	 * @param cmdID UID of the message the command was sent in.
	 */
	public CMD_AddBirthday(int m, int d, Member u, long chID, long cmdID)
	{
		month = m;
		day = d;
		//user = u;
		super.requestingUser = u;
		replyChannel = chID;
		MessageID cmdmsg = new MessageID(cmdID, chID);
		super.setCommandMessageID(cmdmsg);
	}
	
	@Override
	public long getChannelID()
	{
		return replyChannel;
	}
	
	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute(AbstractBot bot) throws InterruptedException 
	{
		bot.setBirthday(super.requestingUser, month, day, replyChannel);
		cleanAfterMyself(bot);
	}

	@Override
	public String toString()
	{
		return "birthday";
	}
	
}
