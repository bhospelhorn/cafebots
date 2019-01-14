package waffleoRai_cafebotCommands.parsers;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import waffleoRai_cafebotCommands.Command;
import waffleoRai_cafebotCommands.Parser;
import waffleoRai_cafebotCommands.Commands.CMD_BadCommandMessage;
import waffleoRai_cafebotCommands.Commands.CMD_SetTZ;

/**
 * Set user timezone (changetz) parser
 * @author Blythe Hospelhorn
 * @version 1.1.0
 * @since January 14, 2019
 */
public class PRS_SetTZ implements Parser{

	@Override
	public Command generateCommand(String[] args, MessageReceivedEvent event) {
		if (args.length < 2) return new CMD_BadCommandMessage(event.getChannel(), event.getMember(), event.getMessageIdLong());
		return new CMD_SetTZ(event.getChannel(), event.getMember(), args[1], event.getMessageIdLong());
	}

}
