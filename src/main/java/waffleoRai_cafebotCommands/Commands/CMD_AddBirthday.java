package waffleoRai_cafebotCommands.Commands;

import net.dv8tion.jda.core.entities.Member;
import waffleoRai_cafebotCommands.CommandAdapter;
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
 */


/**
 * A Command for adding a birthday event to the schedule.
 * <br><br><b>Standard Command:</b>
 * <br>birthday mm dd
 * @author Blythe Hospelhorn
 * @version 1.1.0
 * @since July 20, 2018
 */
public class CMD_AddBirthday extends CommandAdapter{

	private int month;
	private int day;
	private Member user;
	private long replyChannel;

	/**
	 * Construct an AddBirthday command.
	 * @param m Birthday month
	 * @param d Birthday day of month
	 * @param u User requesting addition as a Discord API Member object
	 * @param chID UID of channel to send messages to
	 * @param cmdID Long UID of the message the command was sent in.
	 */
	public CMD_AddBirthday(int m, int d, Member u, long chID, long cmdID)
	{
		month = m;
		day = d;
		user = u;
		replyChannel = chID;
		super.setCommandMessageID(cmdID);
	}
	
	@Override
	public long getChannelID()
	{
		return replyChannel;
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
		bot.setBirthday(user, month, day, replyChannel);
		cleanAfterMyself(bot);
	}

	@Override
	public String toString()
	{
		return "birthday";
	}
	
	public long getGuildID()
	{
		return user.getGuild().getIdLong();
	}

}
