package waffleoRai_cafebotCommands.parsers;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import waffleoRai_cafebotCommands.Command;
import waffleoRai_cafebotCommands.Parser;
import waffleoRai_cafebotCommands.Commands.CMD_BadCommandMessage;
import waffleoRai_cafebotCommands.Commands.CMD_SetAutocleanCommands;

public class PRS_SetAutocleanCommands implements Parser{

	public static final String OPT_ON = "on";
	public static final String OPT_OFF = "off";
	
	@Override
	public Command generateCommand(String[] args, MessageReceivedEvent event) {
		if (args.length < 2) return new CMD_BadCommandMessage(event.getChannel(), event.getMember(), event.getMessageIdLong());
		if (args[1] == null || args[1].isEmpty()) return new CMD_BadCommandMessage(event.getChannel(), event.getMember(), event.getMessageIdLong());
		boolean on = false;
		if (args[1].equalsIgnoreCase(OPT_ON)) on = true;
		else if (args[1].equalsIgnoreCase(OPT_OFF)) on = false;
		else return new CMD_BadCommandMessage(event.getChannel(), event.getMember(), event.getMessageIdLong());
		return new CMD_SetAutocleanCommands(event.getChannel(), event.getMember(), on, event.getMessageIdLong());
	}

}
