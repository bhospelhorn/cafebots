package waffleoRai_cafebotCommands.Commands;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import waffleoRai_cafebotCommands.CommandAdapter;
import waffleoRai_cafebotCommands.MessageID;
import waffleoRai_cafebotCore.AbstractBot;

/*
 * UPDATES
 * 
 * Creation | June 24, 2018
 * Version 1.0.0 Documentation | July 1, 2018
 * 
 * 1.0.0 -> 1.1.0 | July 20, 2018
 * 	Added command message ID note
 * 1.1.0 -> 1.1.1 | August 11, 2018
 * 	MessageID update
 * 
 */


/**
 * A Command for changing a user's timezone to be used in that guild.
 * <br><br><b>Standard Command:</b>
 * <br>changetz [timezone]
 * @author Blythe Hospelhorn
 * @version 1.2.0
 * @since January 14, 2019
 */
public class CMD_SetTZ extends CommandAdapter {

	private MessageChannel ch;
	//private Member user;
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
		//user = u;
		super.requestingUser = u;
		tz = tzcode;
		MessageID cmdmsg = new MessageID(cmdID, ch.getIdLong());
		super.setCommandMessageID(cmdmsg);
	}
	
	@Override
	public long getChannelID()
	{
		return ch.getIdLong();
	}
	
	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute(AbstractBot bot) throws InterruptedException {
		bot.setUserTimezone(ch.getIdLong(), super.requestingUser, tz);
		super.cleanAfterMyself(bot);
	}

	@Override
	public String toString()
	{
		return "changetz";
	}
	
}
