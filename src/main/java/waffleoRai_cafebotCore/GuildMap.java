package waffleoRai_cafebotCore;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_cafebotCommands.ParseCore;

public class GuildMap {
	
	//Simple map of guild UIDs to guild setting objects
	
	/* ----- Constants ----- */
	
	public static final String USERS_DIRNAME = "users";
	public static final String GUILD_DIRNAME = "guilds";
	public static final String GUILD_INIT_FILENAME = "guilds.ini"; //Also a tsv: guildUID\tdirname
	
	/* ----- Instance Variables ----- */
	
	private String installDirectory;
	
	private ConcurrentHashMap<Long, GuildSettings> guilds;
	private ConcurrentHashMap<Long, String> pathmap;
	
	private ConcurrentHashMap<Long, ActorUser> actorMap;
	
	/* ----- Construction/Parsing ----- */
	
	public GuildMap(String installdir)
	{
		guilds = new ConcurrentHashMap<Long, GuildSettings>();
		pathmap = new ConcurrentHashMap<Long, String>();
		actorMap = new ConcurrentHashMap<Long, ActorUser>();
		installDirectory = installdir;
		GuildSettings.setUserDataDirectory(installdir);
	}
	
	public GuildMap(String installdir, ParseCore parser) throws IOException, UnsupportedFileTypeException
	{
		this(installdir);
		String upath = installdir + File.separator + USERS_DIRNAME;
		String gpath = installdir + File.separator + GUILD_DIRNAME;
		String inipath = gpath + File.separator + GUILD_INIT_FILENAME;
		if (!FileBuffer.fileExists(inipath))
		{
			System.out.println("GuildMap.<init> || Guild info init file does not exist! Nothing to read...");
			return;
		}
		
		//Read in user data
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(upath)))
		{
			for (Path f : stream)
			{
				String strPath = f.toString();
				if (FileBuffer.fileExists(strPath))
				{
					//If it's a file...
					FileReader fr = new FileReader(f.toString());
					BufferedReader br = new BufferedReader(fr);
					try
					{
						ActorUser u = new ActorUser(br);
						actorMap.put(u.getUID(), u);
					}
					catch (Exception e)
					{
						e.printStackTrace();
						br.close();
						fr.close();
					}
					br.close();
					fr.close();
				}
			}
			stream.close();
		}
		
		//Read init file
		FileReader fr = new FileReader(inipath);
		BufferedReader br = new BufferedReader(fr);
		
		String line = null;
		while((line = br.readLine()) != null)
		{
			String[] fields = line.split("\t");
			if (fields.length != 2)
			{
				br.close();
				fr.close();
				throw new FileBuffer.UnsupportedFileTypeException();
			}
			try
			{
				long uid = Long.parseUnsignedLong(fields[0]);
				String dirname = fields[1];
				pathmap.put(uid, dirname);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				br.close();
				fr.close();
				throw new FileBuffer.UnsupportedFileTypeException();
			}
		}
		
		br.close();
		fr.close();
		
		Set<Long> gset = pathmap.keySet();
		for (Long l : gset)
		{
			String dp = gpath + File.separator + pathmap.get(l);
			GuildSettings gs = new GuildSettings(dp, parser, actorMap);
			guilds.put(l, gs);
		}
		
	}
	
	/* ----- Getters ----- */
	
	public GuildSettings getGuildSettings(long guildUID)
	{
		return guilds.get(guildUID);
	}
	
	public String getGuildDirectoryName(long guildUID)
	{
		return pathmap.get(guildUID);
	}
	
	public ActorUser getUserProfile(long userID)
	{
		return actorMap.get(userID);
	}
	
	public GuildUser getGuildUser(long guildID, long userID)
	{
		GuildSettings gs = guilds.get(guildID);
		if (gs == null) return null;
		return gs.getUser(userID);
	}
	
	/* ----- Setters ----- */
	
	public void newGuild(Guild g, ParseCore parser)
	{
		long uid = g.getIdLong();
		//String path = userdatadir + File.separator + GUILD_DIRNAME + File.separator + Long.toHexString(uid);
		GuildSettings gs = new GuildSettings(g, parser, actorMap);
		pathmap.put(uid, Long.toHexString(uid));
		guilds.put(uid, gs);
	}
	
	public GuildSettings removeGuild(long guildUID)
	{
		pathmap.remove(guildUID);
		return guilds.remove(guildUID);
	}
	
	public boolean newMember(Member m, ParseCore parser)
	{
		Guild guild = m.getGuild();
		GuildSettings gs = guilds.get(guild.getIdLong());
		if (gs == null){
			newGuild(guild, parser);
			return false; //Adds all members upon creating a new guild.
		}
		long uid = m.getUser().getIdLong();
		ActorUser au = actorMap.get(uid);
		ActorUser rau = gs.newMember(m, au);
		if (au == null) actorMap.put(uid, rau);
		return true;
	}
	
	public boolean newMember(Member m)
	{
		Guild guild = m.getGuild();
		GuildSettings gs = guilds.get(guild.getIdLong());
		if (gs == null){
			return false;
		}
		long uid = m.getUser().getIdLong();
		ActorUser au = actorMap.get(uid);
		ActorUser rau = gs.newMember(m, au);
		if (au == null) actorMap.put(uid, rau);
		return true;
	}
	
	public boolean newUser(User u)
	{
		if (actorMap.get(u.getIdLong()) != null) return false;
		ActorUser au = new ActorUser(u);
		actorMap.put(u.getIdLong(), au);
		return true;
	}
	
	/* ----- Threading ----- */
	
	public void startAllBackgroundThreads()
	{
		Set<Long> gidset = guilds.keySet();
		for (Long gid : gidset)
		{
			GuildSettings gs = guilds.get(gid);
			gs.startBackupThread();
			gs.startScheduleThreads();
		}
	}
	
	public synchronized void terminateAllBackgroundThreads()
	{
		Set<Long> gidset = guilds.keySet();
		for (Long gid : gidset)
		{
			GuildSettings gs = guilds.get(gid);
			gs.terminateScheduleThreads();
			gs.terminateBackupThread();
		}
	}
	
	/* ----- Serialization ----- */
	
	public synchronized void forceBackups() throws IOException
	{
		/*
		Set<Long> allgids = guilds.keySet();
		for (Long l : allgids)
		{
			guilds.get(l).forceBackup();
		}*/
		saveAllToDisk(installDirectory);
	}
	
	public void writeInitFile(String installdir) throws IOException
	{
		String gpath = installdir + File.separator + GUILD_DIRNAME;
		String inipath = gpath + File.separator + GUILD_INIT_FILENAME;
		
		FileWriter fw = new FileWriter(inipath);
		BufferedWriter bw = new BufferedWriter(fw);
		
		Set<Long> gset = pathmap.keySet();
		boolean first = true;
		for (Long l : gset)
		{
			String p = pathmap.get(l);
			if (p == null || p.isEmpty())
			{
				p = Long.toHexString(l);
				pathmap.put(l, p);
			}
			if (!first)bw.write("\n");
			bw.write(Long.toUnsignedString(l) + "\t" + p);
			first = false;
		}
		
		bw.close();
		fw.close();
		
	}
	
	public void saveAllToDisk(String installdir) throws IOException
	{
		writeInitFile(installdir);
		String gpath = installdir + File.separator + GUILD_DIRNAME;
		Set<Long> gset = guilds.keySet();
		for (Long l : gset)
		{
			GuildSettings g = guilds.get(l);
			if (g == null) continue;
			String dir = pathmap.get(l);
			if (dir == null) dir = Long.toHexString(l);
			g.saveToDisk(gpath + File.separator + dir); 
		}
		
		String upath = installdir + File.separator + USERS_DIRNAME;
		Collection<ActorUser> allUsers = actorMap.values();
		for(ActorUser u : allUsers)
		{
			String path = upath + File.separator + Long.toUnsignedString(u.getUID()) + ".user";
			FileWriter fw = new FileWriter(path);
			BufferedWriter bw = new BufferedWriter(fw);
			
			try
			{
				u.writeToFile(bw);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				bw.close();
				fw.close();
			}
			bw.close();
			fw.close();
		}
	}
	

}
