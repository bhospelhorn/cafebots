package waffleoRai_cafebotCommands.Commands;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import waffleoRai_cafebotCommands.CommandAdapter;
import waffleoRai_cafebotCommands.ParseCore;
import waffleoRai_cafebotCore.AbstractBot;

/*
 * UPDATES
 * 
 * Creation | June 17, 2018
 * Version 1.0.0 Documentation | July 1, 2018
 * 
 * 1.0.0 -> 1.1.0 | August 6, 2018
 * 	Now adds member profile as well
 * 
 */

/**
 * A bot command for sending a greeting message to a user that has just joined a guild(server),
 * and pinging requesting admins to alert them to the new arrival.
 * <br>This event cannot be induced via command line. It is internal only.
 * @author Blythe Hospelhorn
 * @version 1.1.0
 * @since August 6, 2018
 */
public class CMD_NewMemberNotify extends CommandAdapter{

	private Guild guild;
	private Member user;
	
	/**
	 * Construct a NewMemberNotify command.
	 * @param g Guild the new user was detected arriving in.
	 * @param m User that just arrived, as a JDA Member object.
	 */
	public CMD_NewMemberNotify(Guild g, Member m)
	{
		guild = g;
		user = m;
	}
	
	public long getUserID()
	{
		return user.getUser().getIdLong();
	}
	
	@Override
	/**
	 * @throws NullPointerException If bot is null.
	 */
	public void execute(AbstractBot bot) {
		bot.greetNewUser(guild, user);
		bot.pingUserArrival(guild, user);
		bot.newMember(user);
	}

	@Override
	public String toString()
	{
		return ParseCore.CMD_GREETNEWMEMBER;
	}

	public long getGuildID()
	{
		return guild.getIdLong();
	}
}
