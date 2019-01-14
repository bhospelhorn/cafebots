package waffleoRai_cafebotCommands.Commands;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
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
 * 1.1.0 -> 1.1.1 | August 11, 2018
 * 	MessageID update
 */


/**
 * A Command that simply displays a message.
 * <br><br><b>Standard Command:</b>
 * <br>saysomething
 * @author Blythe Hospelhorn
 * @version 1.2.0
 * @since January 14, 2019
 */
public class CMD_SaySomething extends CommandAdapter{

	public static final String STANDARD_CMD = "saysomething";
	
	private long targetChannel;
	//private Guild guild;
	
	/**
	 * Construct a SaySomething command.
	 * @param cmdChan Channel command was issued on and reply should be sent to.
	 * @param cmdID Long UID of the message the command was sent in.
	 */
	public CMD_SaySomething(MessageChannel cmdChan, Member m, long cmdID)
	{
		targetChannel = cmdChan.getIdLong();
		//cmdChan.guild = g;
		super.requestingUser = m;
		MessageID cmdmsg = new MessageID(cmdID, cmdChan.getIdLong());
		super.setCommandMessageID(cmdmsg);
	}
	
	@Override
	public long getChannelID()
	{
		return targetChannel;
	}
	
	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute(AbstractBot bot) throws InterruptedException {
		if (bot == null) return;
		bot.saySomething(targetChannel);
		super.cleanAfterMyself(bot);
	}
	
	@Override
	public String toString()
	{
		return "saysomething";
	}
	
}
