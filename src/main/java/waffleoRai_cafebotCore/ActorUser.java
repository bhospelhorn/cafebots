package waffleoRai_cafebotCore;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.TimeZone;

import net.dv8tion.jda.core.entities.User;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;

public class ActorUser {
	
	public static final int ACTOR_GENDER_UNKNOWN = 0;
	public static final int ACTOR_GENDER_FEMALE = 1;
	public static final int ACTOR_GENDER_MALE = 2;
	public static final int ACTOR_GENDER_MULTIPLE_MIXED = 3;
	public static final int ACTOR_GENDER_MULTIPLE_ALLFEM = 4;
	public static final int ACTOR_GENDER_MULTIPLE_ALLMEN = 5;
	
	private long UID;
	
	private TimeZone timezone;
	
	private String microphoneModel;
	private String interfaceModel;
	private String DAW;
	
	private int gender;
	
	public ActorUser(User u)
	{
		//User u = mem.getUser();
		//userlink = mem;
		UID = u.getIdLong();
		timezone = TimeZone.getDefault();
		
		microphoneModel = null;
		interfaceModel = null;
		DAW = null;
		
		gender = ACTOR_GENDER_UNKNOWN;
	}
	
	public ActorUser(BufferedReader userfile) throws UnsupportedFileTypeException
	{
		try
		{
			String line = userfile.readLine();
			UID = Long.parseUnsignedLong(line);
			timezone = TimeZone.getTimeZone(userfile.readLine());
			
			line = userfile.readLine();
			if (line.equals("null")) microphoneModel = null;
			else microphoneModel = line;
			
			line = userfile.readLine();
			if (line.equals("null")) interfaceModel = null;
			else interfaceModel = line;
			
			line = userfile.readLine();
			if (line.equals("null")) DAW = null;
			else DAW = line;
			
			gender = Integer.parseInt(userfile.readLine());
			
		}
		catch(Exception e)
		{
			throw new FileBuffer.UnsupportedFileTypeException();
		}
	}

	/* ----- Getters ----- */
	
	public long getUID()
	{
		return UID;
	}
	
	public TimeZone getTimeZone()
	{
		return timezone;
	}
	
	public String getMicrophoneName()
	{
		return this.microphoneModel;
	}
	
	public String getInterfaceName()
	{
		return this.interfaceModel;
	}
	
	public String getDAWName()
	{
		return this.DAW;
	}

	public int getGender()
	{
		return gender;
	}
	
	/* ----- Setters ----- */
	
	public void setTimeZone(String tzID)
	{
		timezone = TimeZone.getTimeZone(tzID);
	}
	
	public void setMicrophoneName(String name)
	{
		this.microphoneModel = name;
	}
	
	public void setInterfaceName(String name)
	{
		this.interfaceModel = name;
	}
	
	public void setDAWName(String name)
	{
		this.DAW = name;
	}
	
	public void setGender(int gender)
	{
		if (gender < 0 || gender > 2) gender = 0;
		this.gender = gender;
	}
	
	/* ----- Disk Access ----- */
	
	public void writeToFile(BufferedWriter bw) throws IOException
	{
		bw.write(Long.toUnsignedString(UID) + "\n");
		bw.write(timezone.getID() + "\n");
		bw.write(microphoneModel + "\n");
		bw.write(interfaceModel + "\n");
		bw.write(DAW + "\n");
		bw.write(Integer.toString(gender) + "\n");
	}
	
	/* ----- Misc. ----- */
	
	public static int getGroupGender(Collection<ActorUser> users)
	{
		if (users == null) return ACTOR_GENDER_UNKNOWN;
		if (users.size() == 1)
		{
			for (ActorUser u : users) return u.getGender();
		}
		int fcount = 0;
		int mcount = 0;
		for (ActorUser u : users)
		{
			int g = u.getGender();
			if (g == ACTOR_GENDER_UNKNOWN) return ACTOR_GENDER_MULTIPLE_MIXED;
			else if (g == ACTOR_GENDER_FEMALE) fcount++;
			else if (g == ACTOR_GENDER_MALE) mcount++;
		}
		
		if (mcount > 0 && fcount == 0) return ACTOR_GENDER_MULTIPLE_ALLMEN;
		if (fcount > 0 && mcount == 0) return ACTOR_GENDER_MULTIPLE_ALLFEM;
		return ACTOR_GENDER_MULTIPLE_MIXED;
	}
	
	public static int getGuildUserGroupGender(Collection<GuildUser> users)
	{
		if (users == null) return ACTOR_GENDER_UNKNOWN;
		if (users.size() == 1)
		{
			for (GuildUser u : users) return u.getUserProfile().getGender();
		}
		int fcount = 0;
		int mcount = 0;
		for (GuildUser u : users)
		{
			int g = u.getUserProfile().getGender();
			if (g == ACTOR_GENDER_UNKNOWN) return ACTOR_GENDER_MULTIPLE_MIXED;
			else if (g == ACTOR_GENDER_FEMALE) fcount++;
			else if (g == ACTOR_GENDER_MALE) mcount++;
		}
		
		if (mcount > 0 && fcount == 0) return ACTOR_GENDER_MULTIPLE_ALLMEN;
		if (fcount > 0 && mcount == 0) return ACTOR_GENDER_MULTIPLE_ALLFEM;
		return ACTOR_GENDER_MULTIPLE_MIXED;
	}
	
}
