package waffleoRai_cafebotCommands.parsers;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import waffleoRai_cafebotCommands.Command;
import waffleoRai_cafebotCommands.Parser;
import waffleoRai_cafebotCommands.Commands.CMD_SaySomething;

/**
 * Say something command (saysomething) parser
 * @author Blythe Hospelhorn
 * @version 1.1.0
 * @since January 14, 2019
 */
public class PRS_SaySomething implements Parser{
	
	public Command generateCommand(String[] args, MessageReceivedEvent event)
	{
		return new CMD_SaySomething(event.getChannel(), event.getMember(), event.getMessageIdLong());
	}

}
