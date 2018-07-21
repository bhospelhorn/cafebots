package waffleoRai_cafebotCommands.parsers;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import waffleoRai_cafebotCommands.Command;
import waffleoRai_cafebotCommands.Parser;
import waffleoRai_cafebotCommands.Commands.CMD_CleanMessages;

/**
 * Message clean command (cleanme) parser
 * @author Blythe Hospelhorn
 * @version 1.0.1
 * @since July 20, 2018
 */
public class PRS_CleanMessages_1 implements Parser{

	@Override
	public Command generateCommand(String[] args, MessageReceivedEvent event) {
		
		return new CMD_CleanMessages(event.getChannel(), event.getMember(), true, false, event.getMessageIdLong());
	}

}
