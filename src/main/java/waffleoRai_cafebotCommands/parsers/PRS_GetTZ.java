package waffleoRai_cafebotCommands.parsers;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import waffleoRai_cafebotCommands.Command;
import waffleoRai_cafebotCommands.Parser;
import waffleoRai_cafebotCommands.Commands.CMD_GetTZ;

public class PRS_GetTZ implements Parser{

	@Override
	public Command generateCommand(String[] args, MessageReceivedEvent event) {
		return new CMD_GetTZ(event.getChannel(), event.getMember());
	}

}
