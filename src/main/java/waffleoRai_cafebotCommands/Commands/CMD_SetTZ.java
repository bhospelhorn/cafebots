package waffleoRai_cafebotCommands.Commands;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import waffleoRai_cafebotCommands.CommandAdapter;
import waffleoRai_cafebotCore.AbstractBot;

/*
 * UPDATES
 * 
 * Creation | June 24, 2018
 * Version 1.0.0 Documentation | July 1, 2018
 * 
 * 1.0.0 -> 1.1.0 | July 20, 2018
 * 	Added command message ID note
 * 
 */


/**
 * A Command for changing a user's timezone to be used in that guild.
 * <br><br><b>Standard Command:</b>
 * <br>changetz [timezone]
 * @author Blythe Hospelhorn
 * @version 1.1.0
 * @since July 20, 2018
 */
public class CMD_SetTZ extends CommandAdapter {

	private MessageChannel ch;
	private Member user;
	private String tz;
	
	/**
	 * Construct a SetTZ (Set TimeZone) command.
	 * @param c Channel command was issued on and reply should be sent to.
	 * @param u User who issued the command, as a JDA Member object.
	 * @param tzcode String recognized by <i>java.util.timezone</i> as a timezone ID.
	 * @param cmdID Long UID of the message the command was sent in.
	 */
	public CMD_SetTZ(MessageChannel c, Member u, String tzcode, long cmdID)
	{
		ch = c;
		user = u;
		tz = tzcode;
		super.setCommandMessageID(cmdID);
	}
	
	@Override
	public long getChannelID()
	{
		return ch.getIdLong();
	}
	
	public long getUserID()
	{
		return user.getUser().getIdLong();
	}
	
	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute(AbstractBot bot) {
		bot.setUserTimezone(ch.getIdLong(), user, tz);
		super.cleanAfterMyself(bot);
	}

	@Override
	public String toString()
	{
		return "changetz";
	}
	
	public long getGuildID()
	{
		return user.getGuild().getIdLong();
	}
}
