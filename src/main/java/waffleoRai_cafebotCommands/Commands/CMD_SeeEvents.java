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
 */


/**
 * A Command for requesting list of events user created or is involved in for that guild.
 * <br><br><b>Standard Command:</b>
 * <br>myevents
 * @author Blythe Hospelhorn
 * @version 1.2.0
 * @since January 14, 2019
 */
public class CMD_SeeEvents extends CommandAdapter{

	private MessageChannel channel;
	//private Member user;
	
	/**
	 * Construct a SeeEvents command.
	 * @param ch Channel command was issued on and reply should be sent to.
	 * @param u User who issued the command, as a JDA Member object.
	 * @param cmdID Long UID of the message the command was sent in.
	 */
	public CMD_SeeEvents(MessageChannel ch, Member u, long cmdID)
	{
		channel = ch;
		//user = u;
		super.requestingUser = u;
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
	public void execute(AbstractBot bot) throws InterruptedException {
		bot.displayAllUserEvents(channel.getIdLong(), super.requestingUser);
		super.cleanAfterMyself(bot);
	}
	
	@Override
	public String toString()
	{
		return "myevents";
	}
	
}
