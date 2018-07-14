package waffleoRai_cafebotCommands.Commands;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
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
 * A Command for requesting general program help information.
 * <br><br><b>Standard Command:</b>
 * <br>help
 * @author Blythe Hospelhorn
 * @version 1.0.0
 * @since July 1, 2018
 */
public class CMD_Help extends CommandAdapter{

	private MessageChannel channel;
	private Member member;
	
	/**
	 * Construct a Help command.
	 * @param ch Channel command was issued on and reply should be sent to.
	 * @param m User who issued the command, as a JDA Member object.
	 */
	public CMD_Help(MessageChannel ch, Member m)
	{
		channel = ch;
		member = m;
	}
	
	@Override
	public long getChannelID()
	{
		return channel.getIdLong();
	}
	
	public long getUserID()
	{
		return member.getUser().getIdLong();
	}
	
	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute(AbstractBot bot) {
		bot.displayHelp(getChannelID(), member);
		
	}
	
	@Override
	public String toString()
	{
		return "help";
	}

}
