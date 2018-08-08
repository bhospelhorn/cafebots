package waffleoRai_cafebotCommands.Commands;

import net.dv8tion.jda.core.entities.Member;
import waffleoRai_cafebotCommands.CommandAdapter;
import waffleoRai_cafebotCommands.ParseCore;
import waffleoRai_cafebotCore.AbstractBot;

/*
 * UPDATES
 * 
 * Creation | July 14, 2018
 * Version 1.0.0 Documentation | July 14, 2018
 * 
 */

/**
 * A bot command for sending a farewell message to the server when a user leaves,
 * and pinging requesting admins to alert them to the departure.
 * <br>This event cannot be induced via command line. It is internal only.
 * @author Blythe Hospelhorn
 * @version 1.0.0
 * @since July 1, 2018
 */
public class CMD_MemberFarewell extends CommandAdapter{

	private Member user;
	
	/**
	 * Construct a MemberFarewell command.
	 * @param m Member that just left.
	 */
	public CMD_MemberFarewell(Member m)
	{
		user = m;
	}
	
	@Override
	public long getUserID()
	{
		return user.getUser().getIdLong();
	}
	
	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute(AbstractBot bot) {
		bot.farewellUser(user);
		bot.pingUserDeparture(user);
	}

	public String toString()
	{
		return ParseCore.CMD_FAREWELLMEMBER;
	}

	public long getGuildID()
	{
		return user.getGuild().getIdLong();
	}
	
}
