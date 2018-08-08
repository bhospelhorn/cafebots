package waffleoRai_cafebotCommands.Commands;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
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
 */


/**
 * A Command that simply displays a message.
 * <br><br><b>Standard Command:</b>
 * <br>saysomething
 * @author Blythe Hospelhorn
 * @version 1.1.0
 * @since July 20, 2018
 */
public class CMD_SaySomething extends CommandAdapter{

	public static final String STANDARD_CMD = "saysomething";
	
	private long targetChannel;
	private Guild guild;
	
	/**
	 * Construct a SaySomething command.
	 * @param cmdChan Channel command was issued on and reply should be sent to.
	 * @param cmdID Long UID of the message the command was sent in.
	 */
	public CMD_SaySomething(MessageChannel cmdChan, Guild g, long cmdID)
	{
		targetChannel = cmdChan.getIdLong();
		guild = g;
		super.setCommandMessageID(cmdID);
	}
	
	@Override
	public long getChannelID()
	{
		return targetChannel;
	}
	
	public long getUserID()
	{
		return -1;
	}
	
	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute(AbstractBot bot) {
		if (bot == null) return;
		bot.saySomething(targetChannel);
		super.cleanAfterMyself(bot);
	}
	
	@Override
	public String toString()
	{
		return "saysomething";
	}
	
	public long getGuildID()
	{
		return guild.getIdLong();
	}

}
