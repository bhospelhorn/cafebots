package waffleoRai_cafebotCommands.parsers;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import waffleoRai_cafebotCommands.Command;
import waffleoRai_cafebotCommands.Parser;
import waffleoRai_cafebotCommands.Commands.CMD_BadCommandMessage;
import waffleoRai_cafebotCommands.Commands.CMD_qChan;

/**
 * Query channel command (qchan) parser
 * @author Blythe Hospelhorn
 * @version 1.1.0
 * @since January 14, 2019
 */
public class PRS_qChan implements Parser{
	
	public static final String BIRTHDAY = "birthday";
	public static final String GREETING = "greeting";
	
	@Override
	public Command generateCommand(String[] args, MessageReceivedEvent event) {
		if (args.length < 2) return new CMD_BadCommandMessage(event.getChannel(), event.getMember(), event.getMessageIdLong());
		if (args[1].equals(BIRTHDAY)) return new CMD_qChan(event.getChannel(), event.getMember(), true, event.getMessageIdLong());
		else if (args[1].equals(GREETING)) return new CMD_qChan(event.getChannel(), event.getMember(), false, event.getMessageIdLong());
		
		return new CMD_BadCommandMessage(event.getChannel(), event.getMember(), event.getMessageIdLong());
	}

}
