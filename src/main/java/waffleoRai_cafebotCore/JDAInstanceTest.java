package waffleoRai_cafebotCore;

import java.util.List;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;

public class JDAInstanceTest {

	public static void main(String[] args) throws InterruptedException {
		JDABuilder builder = new JDABuilder(AccountType.BOT);
		String token = "nope<3";
		builder.setToken(token);
		//addListeners(builder);
		JDA jda = null;
		try {
			jda = builder.buildBlocking();
		} catch (LoginException e) {
			e.printStackTrace();
		}
		//Looks like we need to wait until it's done logging in...
		
		
		List<Guild> glist = jda.getGuilds();
		Guild someguild = null;
		for (Guild g : glist)
		{
			System.out.println("Guild found: " + g.getName());
			someguild = g;
			break;
		}
		MessageChannel somechannel = someguild.getDefaultChannel();
		
		System.out.println("Initial login: Guild - " + someguild.getName() + " (" + someguild.getId() + ")");
		System.out.println("Initial login: Defo channel - " + somechannel.getName() + " (" + somechannel.getId() + ")");
		
		//Logout
		jda.shutdown();
		System.out.println("Logout: Guild - " + someguild.getName() + " (" + someguild.getId() + ")");
		System.out.println("Logout: Defo channel - " + somechannel.getName() + " (" + somechannel.getId() + ")");
		
		//Log back in
		try {
			jda = builder.buildBlocking();
		} catch (LoginException e) {
			e.printStackTrace();
		}
		System.out.println("Second login: Guild - " + someguild.getName() + " (" + someguild.getId() + ")");
		System.out.println("Second login: Defo channel - " + somechannel.getName() + " (" + somechannel.getId() + ")");
		
		System.exit(0);
	}

}
