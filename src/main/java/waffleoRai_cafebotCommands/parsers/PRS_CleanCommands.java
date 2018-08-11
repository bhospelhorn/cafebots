package waffleoRai_cafebotCommands.parsers;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import waffleoRai_cafebotCommands.Command;
import waffleoRai_cafebotCommands.Parser;
import waffleoRai_cafebotCommands.Commands.CMD_CleanCommands;

public class PRS_CleanCommands implements Parser{

	@Override
	public Command generateCommand(String[] args, MessageReceivedEvent event) {
		return new CMD_CleanCommands(event.getChannel(), event.getMember(), event.getMessageIdLong());
	}

}
