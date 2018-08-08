package waffleoRai_cafebotCommands.parsers;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import waffleoRai_cafebotCommands.Command;
import waffleoRai_cafebotCommands.Parser;
import waffleoRai_cafebotCommands.Commands.CMD_AttendEvent;
import waffleoRai_cafebotCommands.Commands.CMD_BadCommandMessage;

public class PRS_AttendEvent implements Parser{

	@Override
	public Command generateCommand(String[] args, MessageReceivedEvent event) {
		if (args.length < 2) return new CMD_BadCommandMessage(event.getChannel(), event.getGuild(), event.getMessageIdLong());
		//Attempt to long-parse the second argument
		try
		{
			long eid = Long.parseUnsignedLong(args[1]);
			return new CMD_AttendEvent(event.getChannel(), event.getMember(), eid, event.getMessageIdLong());
		}
		catch (Exception e)
		{
			return new CMD_BadCommandMessage(event.getChannel(), event.getGuild(), event.getMessageIdLong());
		}
	}
	
}
