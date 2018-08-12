package waffleoRai_cafebotCore;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.dv8tion.jda.core.entities.Guild;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_cafebotCommands.ParseCore;

public class GuildMap {
	
	//Simple map of guild UIDs to guild setting objects
	
	/* ----- Constants ----- */
	
	public static final String GUILD_DIRNAME = "guilds";
	public static final String GUILD_INIT_FILENAME = "guilds.ini"; //Also a tsv: guildUID\tdirname
	
	/* ----- Instance Variables ----- */
	
	private String installDirectory;
	
	private Map<Long, GuildSettings> guilds;
	private Map<Long, String> pathmap;
	
	/* ----- Construction/Parsing ----- */
	
	public GuildMap(String installdir)
	{
		guilds = new HashMap<Long, GuildSettings>();
		pathmap = new HashMap<Long, String>();
		installDirectory = installdir;
	}
	
	public GuildMap(String installdir, ParseCore parser) throws IOException, UnsupportedFileTypeException
	{
		this(installdir);
		String gpath = installdir + File.separator + GUILD_DIRNAME;
		String inipath = gpath + File.separator + GUILD_INIT_FILENAME;
		if (!FileBuffer.fileExists(inipath))
		{
			System.out.println("GuildMap.<init> || Guild info init file does not exist! Nothing to read...");
			return;
		}
		
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
			GuildSettings gs = new GuildSettings(dp, parser);
			guilds.put(l, gs);
		}
		
	}
	
	/* ----- Getters ----- */
	
	public synchronized GuildSettings getGuildSettings(long guildUID)
	{
		return guilds.get(guildUID);
	}
	
	public synchronized String getGuildDirectoryName(long guildUID)
	{
		return pathmap.get(guildUID);
	}
	
	/* ----- Setters ----- */
	
	public synchronized void newGuild(Guild g, ParseCore parser, String userdatadir)
	{
		long uid = g.getIdLong();
		String path = userdatadir + File.separator + GUILD_DIRNAME + File.separator + Long.toHexString(uid);
		GuildSettings gs = new GuildSettings(g, parser, path);
		pathmap.put(uid, Long.toHexString(uid));
		guilds.put(uid, gs);
	}
	
	public synchronized GuildSettings removeGuild(long guildUID)
	{
		pathmap.remove(guildUID);
		return guilds.remove(guildUID);
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
	
	public synchronized void writeInitFile(String installdir) throws IOException
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
	}
	

}
