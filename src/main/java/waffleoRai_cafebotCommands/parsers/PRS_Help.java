package waffleoRai_cafebotCommands.parsers;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import waffleoRai_cafebotCommands.Command;
import waffleoRai_cafebotCommands.Parser;
import waffleoRai_cafebotCommands.Commands.CMD_Help;

public class PRS_Help implements Parser{
	
	public Command generateCommand(String[] args, MessageReceivedEvent event)
	{
		return new CMD_Help(event.getChannel(), event.getMember());
	}

}
