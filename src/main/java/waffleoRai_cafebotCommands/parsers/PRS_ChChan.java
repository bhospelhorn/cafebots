package waffleoRai_cafebotCommands.parsers;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import waffleoRai_cafebotCommands.Command;
import waffleoRai_cafebotCommands.Parser;
import waffleoRai_cafebotCommands.Commands.CMD_BadCommandMessage;
import waffleoRai_cafebotCommands.Commands.CMD_ChChan;

/**
 * Change birthday/greeting channel (chchan) command parser.
 * @author Blythe Hospelhorn
 * @version 1.0.1
 * @since July 20, 2018
 */
public class PRS_ChChan implements Parser{

	/**
	 * chchan subcommand - used to change the birthday wishes channel
	 */
	public static final String BIRTHDAY = "birthday";
	
	/**
	 * chchan subcommand - used to change the greeting channel
	 */
	public static final String GREETING = "greeting";
	
	@Override
	public Command generateCommand(String[] args, MessageReceivedEvent event) {
		if (args.length < 3) return new CMD_BadCommandMessage(event.getChannel(), event.getMessageIdLong());
		if (args[1].equals(BIRTHDAY)) return new CMD_ChChan(event.getChannel(), event.getMember(), args[2], true, event.getMessageIdLong());
		else if (args[1].equals(GREETING)) return new CMD_ChChan(event.getChannel(), event.getMember(), args[2], false, event.getMessageIdLong());
		
		return new CMD_BadCommandMessage(event.getChannel(), event.getMessageIdLong());
	}

}
