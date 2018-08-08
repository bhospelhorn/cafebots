package waffleoRai_cafebotCommands.parsers;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import waffleoRai_cafebotCommands.Command;
import waffleoRai_cafebotCommands.Parser;
import waffleoRai_cafebotCommands.Commands.CMD_BadCommandMessage;
import waffleoRai_cafebotCommands.Commands.CMD_CancelEvent;

/**
 * Cancel event (ecancel) command parser.
 * @author Blythe Hospelhorn
 * @version 1.0.1
 * @since July 20, 2018
 */
public class PRS_CancelEvent implements Parser{

	private static final String[] trueArgs = {"true", "t", "yes", "y", "1"};
	private static final String[] falseArgs = {"false", "f", "no", "n", "0"};
	
	@Override
	public Command generateCommand(String[] args, MessageReceivedEvent event) {
		if (args.length < 2) return new CMD_BadCommandMessage(event.getChannel(), event.getGuild(), event.getMessageIdLong());
		long eid = -1;
		try
		{
			Long.parseUnsignedLong(args[1]);
		}
		catch (NumberFormatException e)
		{
			return new CMD_BadCommandMessage(event.getChannel(), event.getGuild(), event.getMessageIdLong());
		}
		boolean cancelAll = true;
		boolean silent = false;
		if (args.length > 2)
		{
			for (String s : trueArgs)
			{
				if (args[2].equalsIgnoreCase(s)) cancelAll = false;
				if (args.length > 3)
				{
					if (args[3].equalsIgnoreCase(s)) silent = true;
				}
			}
			for (String s : falseArgs)
			{
				if (args[2].equalsIgnoreCase(s)) cancelAll = true;
				if (args.length > 3)
				{
					if (args[3].equalsIgnoreCase(s)) silent = false;
				}
			}
		}
		return new CMD_CancelEvent(event.getChannel(), event.getMember(), eid, event.getMessageIdLong(), silent, cancelAll);
	}

}
