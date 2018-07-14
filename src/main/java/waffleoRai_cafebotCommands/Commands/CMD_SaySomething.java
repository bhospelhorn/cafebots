package waffleoRai_cafebotCommands.Commands;

import net.dv8tion.jda.core.entities.MessageChannel;
import waffleoRai_cafebotCommands.CommandAdapter;
import waffleoRai_cafebotCore.AbstractBot;

/*
 * UPDATES
 * 
 * Creation | June 9, 2018
 * Version 1.0.0 Documentation | July 1, 2018
 * 
 */


/**
 * A Command that simply displays a message.
 * <br><br><b>Standard Command:</b>
 * <br>saysomething
 * @author Blythe Hospelhorn
 * @version 1.0.0
 * @since July 1, 2018
 */
public class CMD_SaySomething extends CommandAdapter{

	public static final String STANDARD_CMD = "saysomething";
	
	private long targetChannel;
	
	/**
	 * Construct a SaySomething command.
	 * @param cmdChan Channel command was issued on and reply should be sent to.
	 */
	public CMD_SaySomething(MessageChannel cmdChan)
	{
		targetChannel = cmdChan.getIdLong();
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
	}
	
	@Override
	public String toString()
	{
		return "saysomething";
	}

}
