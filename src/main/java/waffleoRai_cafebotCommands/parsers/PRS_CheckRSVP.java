package waffleoRai_cafebotCommands.parsers;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import waffleoRai_cafebotCommands.Command;
import waffleoRai_cafebotCommands.Parser;
import waffleoRai_cafebotCommands.Commands.CMD_BadCommandMessage;
import waffleoRai_cafebotCommands.Commands.CMD_CheckRSVP;

public class PRS_CheckRSVP implements Parser{
	
	@Override
	public Command generateCommand(String[] args, MessageReceivedEvent event) {
		if (args.length < 2) return new CMD_BadCommandMessage(event.getChannel(), event.getMember(), event.getMessageIdLong());
		//Attempt to long-parse the second argument
		try
		{
			long eid = Long.parseUnsignedLong(args[1]);
			return new CMD_CheckRSVP(event.getChannel(), event.getMember(), eid, event.getMessageIdLong());
		}
		catch (Exception e)
		{
			return new CMD_BadCommandMessage(event.getChannel(), event.getMember(), event.getMessageIdLong());
		}
	}

}
