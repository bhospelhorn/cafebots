package waffleoRai_cafebotCommands.parsers;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import waffleoRai_cafebotCommands.Command;
import waffleoRai_cafebotCommands.Parser;
import waffleoRai_cafebotCommands.Commands.CMD_GetTZ;

/**
 * Get user timezone command (gettz) parser
 * @author Blythe Hospelhorn
 * @version 1.0.1
 * @since July 20, 2018
 */
public class PRS_GetTZ implements Parser{

	@Override
	public Command generateCommand(String[] args, MessageReceivedEvent event) {
		return new CMD_GetTZ(event.getChannel(), event.getMember(), event.getMessageIdLong());
	}

}
