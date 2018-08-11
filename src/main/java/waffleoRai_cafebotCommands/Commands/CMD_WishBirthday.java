package waffleoRai_cafebotCommands.Commands;

import java.util.Random;

import waffleoRai_cafebotCommands.CommandAdapter;
import waffleoRai_cafebotCommands.MessageID;
import waffleoRai_cafebotCommands.ParseCore;
import waffleoRai_cafebotCore.AbstractBot;
import waffleoRai_schedulebot.Birthday;

/*
 * UPDATES
 * 
 * Creation | June 9, 2018
 * Version 1.0.0 Documentation | July 1, 2018
 * 
 * 1.0.0 -> 1.0.1 | August 11, 2018
 * 	MessageID update
 * 
 */


/**
 * A command for ordering a bot to send a birthday wishes message to the requested
 * guild's (server's) birthday wishes channel.
 * <br>This event cannot be induced via command line. It is internal only.
 * @author Blythe Hospelhorn
 * @version 1.0.1
 * @since August 11, 2018
 */
public class CMD_WishBirthday extends CommandAdapter{
	
	private Birthday bday;
	private long guild;
	private boolean cointoss;
	
	/**
	 * Construct a WishBirthday command.
	 * @param b Birthday to wish (includes user UID and date).
	 * @param guildID ID of guild to wish user a happy birthday in.
	 */
	public CMD_WishBirthday(Birthday b, long guildID)
	{
		bday = b;
		guild = guildID;
		Random r = new Random();
		cointoss = r.nextBoolean();
	}
	
	public long getUserID()
	{
		return bday.getRequestingUser();
	}

	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute(AbstractBot bot) {	
		bot.wishBirthday(bday.getRequestingUser(), guild, cointoss);
	}

	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute_timeout(AbstractBot bot) {
		bot.wishBirthday(bday.getRequestingUser(), guild, cointoss);
	}

	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute_rerequest(AbstractBot bot, MessageID msgid) {
		bot.wishBirthday(bday.getRequestingUser(), guild, cointoss);
	}
	
	@Override
	public String toString()
	{
		return ParseCore.CMD_WISHBIRTHDAY;
	}

	public long getGuildID()
	{
		return guild;
	}
}
