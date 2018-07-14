package waffleoRai_cafebotCommands.Commands;

import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import waffleoRai_cafebotCommands.CommandAdapter;
import waffleoRai_cafebotCore.AbstractBot;

/*
 * UPDATES
 * 
 * Creation | June 21, 2018
 * Version 1.0.0 Documentation | July 1, 2018
 * 
 */


/**
 * A Command for requesting a list of valid TimeZone strings for timezone setting.
 * <br><br><b>Standard Command:</b>
 * <br>seealltz
 * @author Blythe Hospelhorn
 * @version 1.0.0
 * @since July 1, 2018
 */
public class CMD_SeeTZ extends CommandAdapter{

	private MessageChannel ch;
	private User user;
	
	/**
	 * Construct a SeeTZ (See All Timezones) command.
	 * @param channel Channel command was issued on and reply should be sent to.
	 * @param u User who issued the command, as a JDA User object.
	 */
	public CMD_SeeTZ(MessageChannel channel, User u)
	{
		ch = channel;
		user = u;
	}
	
	@Override
	public long getChannelID()
	{
		return ch.getIdLong();
	}
	
	public long getUserID()
	{
		return user.getIdLong();
	}
	
	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute(AbstractBot bot) {
		bot.postTimezoneList(ch.getIdLong(), user.getName());
		
	}
	
	@Override
	public String toString()
	{
		return "seealltz";
	}

}
