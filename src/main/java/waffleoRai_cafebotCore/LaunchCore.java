package waffleoRai_cafebotCore;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.LoginException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_cafebotCommands.BotScheduler;
import waffleoRai_schedulebot.Schedule;

public class LaunchCore {
	
	public static final int OS_WINDOWS = 0;
	public static final int OS_LINUX = 1;
	public static final int OS_MAC = 2;
	
	public static final String initDir = ".waffleDiscord";
	public static final String initFile = "cafebots.ini"; 
	//All it has is the path to the directory to be used. 
	//An ini needs to be made for each user, but the same directory can be used between users.
	
	public static final String winDefo = ".waffleDiscord\\cafebots";
	public static final String nixDefo = "waffleorai/cafebots";
	
	public static final String botinitXML = "botinit.xml"; //Contains tokens, versions
	
	public static final String tokenXMLKey = "token";
	public static final String versionXMLKey = "version";
	public static final String constructorXMLKey = "constructor";
	
	public static final String defo_bot_XML_keyStem = "bot_";
	
	public static final String DIR_BOTDATA = "botdata";
	public static final String DIR_USERDATA = "userdata";
	public static final String DIR_COREDATA = "commondata";
	//public static final String DIR_BOTCOM = "botcom";
	
	public static final String DIR_COREDATA_DATA = "data";
	
	public static final String SETTINGS_FILE = "settings.ini";
	public static final String REMINDERTIMES_FILE = "remindtimes.xml";
	public static final String BOTSCHED_FILE = "bsched.file";
	public static final String BOTPERM_FILE = "bperm.file";
	public static final String TZLIST_FILE = "TZrawid.txt";
	
	public static final String LANINI_KEY_DIR = "DIR";
	public static final String LANINI_KEY_CMN = "COMMON";
	public static final String LANINI_KEY_MTH = "MONTHS";
	public static final String LANINI_KEY_BOTSTEM = "BOT";
	public static final String LANINI_KEY_BOTCONSTEM = "BOTCON";
	
	public static final String SETTINGS_KEY_LAN = "LANGUAGE";
	
	public static final String[] COMMON_DATA_FILES={"grch38_refSeq.gbgd", "GRCh38.gbdh", "TZrawid.txt"};
	
	public static final int SHIFTS_PER_DAY_DEFO = 6;
	
	public static int detectOS(boolean verbose)
	{
		String osname = System.getProperty("os.name");
		if (verbose) System.err.println("Operating System Detected: " + osname);
		osname = osname.toLowerCase();
		if (osname.indexOf("win") >= 0) return OS_WINDOWS;
		if (osname.indexOf("mac") >= 0) return OS_MAC;
		if (osname.indexOf("nix") >= 0 || osname.indexOf("nux") >= 0) return OS_LINUX;
		return -1;
	}
	
	public static String getUserHome(boolean verbose)
	{
		int os = detectOS(verbose);
		String username = System.getProperty("user.name");
		if (username == null || username.isEmpty()) return null;
		switch(os)
		{
		case OS_WINDOWS:
			if (verbose) System.err.println("Operating System Class: Windows");
			//I really hate putting stuff in My Documents. That's for -documents-, yo.
			//And I don't trust Windows to not make Documents the home directory.
			String dir = "C:\\Users\\" + username;
			if (verbose) System.err.println("Trying directory " + dir + " ...");
			if (FileBuffer.directoryExists(dir)) return dir;
			if (verbose) System.err.println("Default Windows user directory not found.");
			dir = System.getProperty("user.home");
			if (verbose) System.err.println("Trying directory " + dir + " ...");
			if (FileBuffer.directoryExists(dir)) return dir;
			else
			{
				if (verbose) System.err.println("ERROR: Home directory could not be found!");
				return null;
			}
		case OS_MAC:
			if (verbose) System.err.println("Operating System Class: Macintosh");
			String mdir = System.getProperty("user.home");
			if (verbose) System.err.println("Trying directory " + mdir + " ...");
			if (FileBuffer.directoryExists(mdir)) return mdir;
			else
			{
				if (verbose) System.err.println("ERROR: Home directory could not be found!");
				return null;
			}
		case OS_LINUX:
			if (verbose) System.err.println("Operating System Class: Unix");
			String udir = System.getProperty("user.home");
			if (verbose) System.err.println("Trying directory " + udir + " ...");
			if (FileBuffer.directoryExists(udir)) return udir;
			else
			{
				if (verbose) System.err.println("ERROR: Home directory could not be found!");
				return null;
			}
		}
		if (verbose) System.err.println("Operating system not recognized! Will attempt to retrieve home directory path anyway...");
		String dir = System.getProperty("user.home");
		if (verbose) System.err.println("Trying directory " + dir + " ...");
		if (FileBuffer.directoryExists(dir)) return dir;
		else
		{
			if (verbose) System.err.println("ERROR: Home directory could not be found!");
			return null;
		}
	}

	public static void writeInitXML(BotSet bots, String path) throws IOException
	{
		if (bots == null) throw new NullPointerException();
		Files.createDirectories(Paths.get(path));
		System.err.println("DEBUG || LaunchCore.writeInitXML || Path = " + path);
		//Oops. Need to actually make path...
		String xmlpath = path + File.separator + botinitXML;
		FileWriter fw = new FileWriter(xmlpath);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		bw.write("<cafebotsinit>\n");
		
		List<Bot> botlist = bots.getBots();
		for (Bot b : botlist)
		{
			if (b != null)
			{
				bw.write("\t<" + b.getXMLKey() + ">\n");
				bw.write("\t\t<" + tokenXMLKey + ">");
				bw.write(b.getToken());
				bw.write("</" + tokenXMLKey + ">\n");
				bw.write("\t\t<" + versionXMLKey + ">");
				bw.write(b.getVersion());
				bw.write("</" + versionXMLKey + ">\n");
				bw.write("\t\t<" + constructorXMLKey + ">");
				bw.write(Integer.toString(b.getConstructorType()));
				bw.write("</" + constructorXMLKey + ">\n");
				bw.write("\t</" + b.getXMLKey() + ">\n");	
			}
		}
		bw.write("</cafebotsinit>");
		
		bw.close();
		fw.close();
	}
	
	public static String getDataDirectory(String homedir) throws IOException
	{
		String ini = homedir + File.separator + initDir + File.separator + initFile;
		
		FileReader fr = new FileReader(ini);
		BufferedReader br = new BufferedReader(fr);
		
		String path = br.readLine();
		
		br.close();
		fr.close();
		
		return path;
	}
	
	public static Bot getInitialBotInfo(String datadir, String botxml) throws FileNotFoundException, XMLStreamException
	{
		//https://stackoverflow.com/questions/5059224/which-is-the-best-library-for-xml-parsing-in-java
		//System.err.println("LaunchCore.getInitialBotInfo || Called!");
		//System.err.println("LaunchCore.getInitialBotInfo || datadir = " + datadir);
		//System.err.println("LaunchCore.getInitialBotInfo || botxml = " + botxml);
		String initxml = datadir + File.separator + botinitXML;
		//System.err.println("LaunchCore.getInitialBotInfo || initxml = " + initxml);
		FileInputStream fis = new FileInputStream(initxml);
		XMLInputFactory xif = XMLInputFactory.newInstance();
		XMLStreamReader reader = xif.createXMLStreamReader(fis);
		//System.err.println("LaunchCore.getInitialBotInfo || XML stream opened!");
		String t = null;
		String v = null;
		String c = null;
		boolean mybot = false;
		String lastelement = "";
		while(reader.hasNext())
		{
			if(mybot)
			{
				if(reader.getEventType() == XMLStreamReader.START_ELEMENT)
				{
					lastelement = reader.getLocalName();
				}
				else if(reader.getEventType() == XMLStreamReader.END_ELEMENT)
				{
					if(reader.getLocalName().equals(botxml))break;
					else lastelement = "";
				}
				else if(reader.getEventType() == XMLStreamReader.CHARACTERS)
				{
					if(lastelement.equals(tokenXMLKey)) t = reader.getText();
					else if(lastelement.equals(versionXMLKey)) v = reader.getText();
					else if(lastelement.equals(constructorXMLKey)) c = reader.getText();
				}
				else return null;
			}
			if(reader.getEventType() == XMLStreamReader.START_ELEMENT)
			{
				if(reader.getLocalName().equals(botxml)) mybot = true;
			}
			reader.next();
		}
		reader.close();
		//System.err.println("LaunchCore.getInitialBotInfo || t = " + t);
		//System.err.println("LaunchCore.getInitialBotInfo || v = " + v);
		//System.err.println("LaunchCore.getInitialBotInfo || c = " + c);
		if (t == null) return null;
		if (v == null) return null;
		if (c == null) return null;
		int ci = 0;
		try {ci = Integer.parseInt(c);}
		catch(NumberFormatException e){return null;}
		
		InitBot b = new InitBot(botxml);
		b.setToken(t);
		b.setVersionString(v);
		b.setConstructorType(ci);
		return b;
	}
	
	public static void cleanAllData(String datadir) throws IOException, DirectoryIteratorException
	{
		Path dirpath = Paths.get(datadir);
		clearDirectory(dirpath);
	}
	
	private static void clearDirectory(Path dirpath) throws IOException, DirectoryIteratorException
	{
		DirectoryStream<Path> stream = Files.newDirectoryStream(dirpath);
		for (Path f : stream)
		{
			if (FileBuffer.fileExists(f.toString()))
			{
				Files.delete(f);
			}
			else if (FileBuffer.directoryExists(f.toString()))
			{
				clearDirectory(f);
			}
			//System.out.println(f.getFileName());
		}
		stream.close();
	}
	
	public static String getDefaultInstallDir()
	{
		int os = detectOS(false);
		String dir = getUserHome(false);
		if (os == OS_WINDOWS) dir += "\\" + winDefo;
		else dir += "/" + nixDefo;
		
		return dir;
	}
	
	public static void setDataPath(String datadir) throws IOException
	{
		String inidir = LaunchCore.getUserHome(false) + File.separator + LaunchCore.initDir;
		String inifile = inidir + File.separator + LaunchCore.initFile;
		if(!FileBuffer.directoryExists(inidir)) Files.createDirectories(Paths.get(inifile));
		Files.deleteIfExists(Paths.get(inifile));
		
		FileWriter fw = new FileWriter(inifile);
		BufferedWriter bw = new BufferedWriter(fw);
		
		bw.write(datadir);
		
		bw.close();
		fw.close();
		
	}
	
	public static Map<String, String> mapXMLFile(String myxml) throws XMLStreamException, FileNotFoundException
	{
		Map<String, String> map = new HashMap<String, String>();
		FileInputStream fis = new FileInputStream(myxml);
		XMLInputFactory xif = XMLInputFactory.newInstance();
		XMLStreamReader reader = xif.createXMLStreamReader(fis);
		
		LinkedList<String> keys = new LinkedList<String>();
		while(reader.hasNext())
		{
			if(reader.getEventType() == XMLStreamReader.START_ELEMENT)
			{
				//Add to current key
				keys.addLast(reader.getLocalName());
			}
			else if(reader.getEventType() == XMLStreamReader.END_ELEMENT)
			{
				keys.removeLast();
			}
			else if(reader.getEventType() == XMLStreamReader.CHARACTERS)
			{
				String v = reader.getText();
				String k = "";
				boolean first = true;
				for (String s : keys)
				{
					if (!first) k += "." + s;
					else k += s;
					first = false;
				}
				map.put(k, v);
			}
			reader.next();
		}
		
		
		reader.close();
		
		return map;
	}
	
	public static Map<String, String> mapINIFile(String myini) throws IOException, UnsupportedFileTypeException
	{
		Map<String, String> map = new HashMap<String, String>();
		
		FileReader fr = new FileReader(myini);
		BufferedReader br = new BufferedReader(fr);
		String line = null;
		while ((line = br.readLine()) != null)
		{
			String[] fields = line.split("=");
			if (fields.length != 2){
				br.close();
				fr.close();
				throw new FileBuffer.UnsupportedFileTypeException();
			}
			map.put(fields[0], fields[1]);
		}

		br.close();
		fr.close();
		return map;
	}
	
	public static BotBrain loadCore(boolean verbose) throws IOException, UnsupportedFileTypeException, XMLStreamException, LoginException
	{
		String homedir = getUserHome(verbose);
		String datadir = getDataDirectory(homedir);
		
		System.out.println("LaunchCore.loadCore || Installation directory detected: " + datadir);
		
		
		//Detect initial settings (Language)
		System.out.println("LaunchCore.loadCore || Now loading initial settings...");
		String inifile = datadir + File.separator + SETTINGS_FILE;
		Map<String, String> inimap = mapINIFile(inifile);
		String lstr = inimap.get(SETTINGS_KEY_LAN);
		if (lstr == null || lstr.isEmpty())
		{
			System.err.println("LaunchCore.loadCore || Program core load failed: Unable to detect language.");
			return null;
		}
		Language lan = Language.getLanguage(lstr);
		if (lan == null)
		{
			System.err.println("LaunchCore.loadCore || Program core load failed: Unable to detect language.");
			return null;
		}
		System.out.println("LaunchCore.loadCore || Language detected: " + lan.getCode());
		
		BotBrain core = new BotBrain(datadir, lan);
		System.out.println("LaunchCore.loadCore || BotBrain instantiated ");
		
		//Load common data
		String cdatdir = datadir + File.separator + DIR_COREDATA;
		System.out.println("LaunchCore.loadCore || Loading common data...");
			//Language strings
		System.out.println("LaunchCore.loadCore || Loading language settings...");
		String lanini = cdatdir + File.separator + lan.getCode() + ".ini";
		inimap = mapINIFile(lanini);
		String landir = cdatdir + File.separator + inimap.get(LANINI_KEY_DIR);
		String commonstrfile = landir + File.separator + inimap.get(LANINI_KEY_CMN);
		String monthnamesfile = landir + File.separator + inimap.get(LANINI_KEY_MTH);
		String[] botstringxml = new String[10];
		
		for (int i = 1; i < 10; i++)
		{
			String key = LANINI_KEY_BOTSTEM + i;
			//System.err.println("LaunchCore.loadCore || lanini key: " + key);
			botstringxml[i] = inimap.get(key);
			if (botstringxml[i] == null) break;
			if (botstringxml[i].equals("null")) botstringxml[i] = null;
		}
		System.out.println("LaunchCore.loadCore || Loading common strings xml...");
		Map<String, String> commonstrings = mapXMLFile(commonstrfile);
		System.out.println("LaunchCore.loadCore || Loading scattered common strings...");
		Schedule.loadMonthNames(monthnamesfile);
			//Reminder times
		System.out.println("LaunchCore.loadCore || Loading reminder times...");
		String rtpath = datadir + File.separator + REMINDERTIMES_FILE;
		Map<String, String> rawrt = mapXMLFile(rtpath);
		
		//Load bots & botdata
		String bdatdir = datadir + File.separator + DIR_BOTDATA;
		//String botinitfile = datadir + File.separator + botinitXML;
		String bsfile = bdatdir + File.separator + BOTSCHED_FILE;
		String bpfile = bdatdir + File.separator + BOTPERM_FILE;
			//Bot schedule
		System.out.println("LaunchCore.loadCore || Loading bot shift schedule...");
		BotScheduler shiftmanager = new BotScheduler(bsfile, bpfile);
			//Bots & their strings
		System.out.println("LaunchCore.loadCore || Loading bots...");
		AbstractBot[] bots = new AbstractBot[10];
		//Bot binit1 = null;
		for (int i = 1; i < 10; i++)
		{
			System.out.println("LaunchCore.loadCore || Loading BOT" + i);
			Bot binit = getInitialBotInfo(datadir, "bot_" + i);
			if (binit != null) bots[i] = BotConstructor.makeBot(binit, core, i);
			else System.out.println("LaunchCore.loadCore || No data for BOT" + i);
			System.out.println("LaunchCore.loadCore || BOT" + i + " constructed...");
			//Get strings
			System.out.println("LaunchCore.loadCore || BOT" + i + ": Loading strings...");
			if (bots[i] != null)
			{
				StringImporter si = new StringImporter(bdatdir + File.separator + "BOT" + i + File.separator + botstringxml[i]);
				bots[i].setStringMap(si.getStringMap());	
			}
			System.out.println("LaunchCore.loadCore || BOT" + i + ": Strings loaded.");
			//if (i == 1) binit1 = binit;
		}
		
		//Load user data
		//String udatdir = datadir + File.separator + DIR_USERDATA;
		
		if (bots[1] == null)
		{
			System.err.println("LaunchCore.loadCore || Master bot is required!!");
			return null;
		}

		
		System.out.println("LaunchCore.loadCore || Adding data to core...");
		core.setCommonStringMap(commonstrings);
		core.setShiftManager(shiftmanager);
		//core.setUserDataMap(myguilds);
		for (int i = 1; i < 10; i++) core.setBot(bots[i], i);
		core.generateReminderTimeMap(rawrt);
		core.regenerateParserCore();
		core.loadUserData();
		System.out.println("LaunchCore.loadCore || Core loaded!");
		return core;
		
	}
	
	
}
