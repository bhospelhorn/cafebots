package waffleoRai_cafebotCommands.parsers;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import waffleoRai_cafebotCommands.Command;
import waffleoRai_cafebotCommands.Parser;
import waffleoRai_cafebotCommands.Commands.CMD_BadCommandMessage;
import waffleoRai_cafebotCommands.Commands.CMD_CancelEvent;

public class PRS_CancelEvent implements Parser{

	@Override
	public Command generateCommand(String[] args, MessageReceivedEvent event) {
		if (args.length < 2) return new CMD_BadCommandMessage(event.getChannel());
		long eid = -1;
		try
		{
			Long.parseUnsignedLong(args[1]);
		}
		catch (NumberFormatException e)
		{
			return new CMD_BadCommandMessage(event.getChannel());
		}
		return new CMD_CancelEvent(event.getChannel(), event.getMember(), eid);
	}

}
