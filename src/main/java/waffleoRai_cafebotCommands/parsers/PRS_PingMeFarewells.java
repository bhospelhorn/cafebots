package waffleoRai_cafebotCommands.parsers;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import waffleoRai_cafebotCommands.Command;
import waffleoRai_cafebotCommands.Parser;
import waffleoRai_cafebotCommands.Commands.CMD_BadCommandMessage;
import waffleoRai_cafebotCommands.Commands.CMD_PingMeFarewells;

public class PRS_PingMeFarewells implements Parser {

	public static final String OPTION_ON = "on";
	public static final String OPTION_OFF = "off";
	
	@Override
	public Command generateCommand(String[] args, MessageReceivedEvent event) {
		if (args.length < 2) return new CMD_BadCommandMessage(event.getChannel(), event.getMember(), event.getMessageIdLong());
		boolean on = false;
		if (args[1].equals(OPTION_ON)) on = true;
		else if (args[1].equals(OPTION_OFF)) on = false;
		else return new CMD_BadCommandMessage(event.getChannel(), event.getMember(), event.getMessageIdLong());
		return new CMD_PingMeFarewells(on, event.getMember(), event.getChannel(), event.getMessageIdLong());
	}
	
}
