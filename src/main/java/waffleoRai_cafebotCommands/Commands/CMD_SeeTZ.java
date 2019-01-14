package waffleoRai_cafebotCommands.Commands;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import waffleoRai_cafebotCommands.CommandAdapter;
import waffleoRai_cafebotCommands.MessageID;
import waffleoRai_cafebotCore.AbstractBot;

/*
 * UPDATES
 * 
 * Creation | June 21, 2018
 * Version 1.0.0 Documentation | July 1, 2018
 * 
 * 1.0.0 -> 1.1.0 | July 20, 2018
 * 	Added command message ID note
 * 1.1.0 -> 1.1.1 | August 7, 2018
 *  More command cleaning updating
 * 1.1.1 -> 1.1.2 | August 11, 2018
 *  MessageID update
 */


/**
 * A Command for requesting a list of valid TimeZone strings for timezone setting.
 * <br><br><b>Standard Command:</b>
 * <br>seealltz
 * @author Blythe Hospelhorn
 * @version 1.2.0
 * @since January 14, 2019
 */
public class CMD_SeeTZ extends CommandAdapter{

	private MessageChannel ch;
	//private Member user;
	
	/**
	 * Construct a SeeTZ (See All Timezones) command.
	 * @param channel Channel command was issued on and reply should be sent to.
	 * @param u User who issued the command, as a JDA User object.
	 * @param cmdID Long UID of the message the command was sent in.
	 */
	public CMD_SeeTZ(MessageChannel channel, Member u, long cmdID)
	{
		ch = channel;
		//user = u;
		super.requestingUser = u;
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
		bot.postTimezoneList(ch.getIdLong(), super.requestingUser);
		super.cleanAfterMyself(bot);
	}
	
	@Override
	public String toString()
	{
		return "seealltz";
	}
	
}
