package waffleoRai_cafebotRoles;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;

public class ActorRole implements Comparable<ActorRole> {
	
	/* ----- Constants ----- */
	
	public static final int ROLETYPE_MAJOR = 0;
	public static final int ROLETYPE_SUPPORT = 1;
	public static final int ROLETYPE_EXT_ADULT = 2;
	public static final int ROLETYPE_EXT_ADULT_CROWD = 3;
	public static final int ROLETYPE_EXT_ANNOUNCE = 4;
	public static final int ROLETYPE_EXT_ANIMAL = 5;
	public static final int ROLETYPE_EXT_ANIMAL_CROWD = 6;
	public static final int ROLETYPE_EXT_CHILD = 7;
	public static final int ROLETYPE_EXT_CHILD_CROWD = 8;
	public static final int ROLETYPE_EXT_MONSTER = 9;
	public static final int ROLETYPE_EXT_MONSTER_CROWD = 10;
	public static final int ROLETYPE_EXT_SCHOOLGIRL = 11;
	public static final int ROLETYPE_EXT_SCHOOLGIRL_CROWD = 12;
	public static final int ROLETYPE_EXT_SCHOOLBOY = 13;
	public static final int ROLETYPE_EXT_SCHOOLBOY_CROWD = 14;
	public static final int ROLETYPE_EXT_STUDENT = 15;
	public static final int ROLETYPE_EXT_STUDENT_CROWD = 16;
	
	/* ----- Instance Variables ----- */
	
	private long roleUID;
	
	private int typecode;
	
	private long dirUser;
	private long actUser;
	private long deadline;
	
	private String name;
	private String notes;
	
	/* ----- Construction ----- */
	
	public ActorRole(long director, int type)
	{
		GregorianCalendar stamp = new GregorianCalendar();
		roleUID = stamp.getTimeInMillis();
		typecode = type;
		dirUser = director;
		actUser = 0;
		deadline = 0;
		name = "";
		notes = "";
	}
	
	public ActorRole(String tsvRecord) throws UnsupportedFileTypeException
	{
		String[] fields = tsvRecord.split("\t");
		if (fields.length != 7) throw new FileBuffer.UnsupportedFileTypeException();
		try
		{
			roleUID = Long.parseUnsignedLong(fields[0]);
			typecode = Integer.parseInt(fields[1]);
			dirUser = Long.parseUnsignedLong(fields[2]);
			actUser = Long.parseUnsignedLong(fields[3]);
			deadline = Long.parseUnsignedLong(fields[4]);
			name = fields[5];
			notes = fields[6];
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new FileBuffer.UnsupportedFileTypeException();
		}
	}
	
	/* ----- Getters ----- */
	
	public long getRoleUID()
	{
		return roleUID;
	}

	public int getRoleType()
	{
		return typecode;
	}
	
	public long getDirectorUser()
	{
		return dirUser;
	}
	
	public long getActorUser()
	{
		return actUser;
	}
	
	public long getDeadline_absMilliseconds()
	{
		return deadline;
	}
	
	public GregorianCalendar getDeadline_timestamp(TimeZone tz)
	{
		GregorianCalendar stamp = new GregorianCalendar(tz);
		stamp.setTimeInMillis(deadline);
		return stamp;
	}
	
	public String getRoleName()
	{
		return name;
	}
	
	public String getNotes()
	{
		return notes;
	}
	
	/* ----- Setters ----- */
	
	public void setRoleType(int code)
	{
		typecode = code;
	}
	
	public void setDirectorUser(long uid)
	{
		dirUser = uid;
	}
	
	public void setActorUser(long uid)
	{
		actUser = uid;
	}
	
	public void setDeadline(long millis)
	{
		deadline = millis;
	}
	
	public void setDeadline(GregorianCalendar stamp)
	{
		deadline = stamp.getTimeInMillis();
	}
	
	public void setDeadline(int year, int month, int day, int hour, int minute, TimeZone tz)
	{
		GregorianCalendar d = new GregorianCalendar(tz);
		d.set(Calendar.YEAR, year);
		d.set(Calendar.MONTH, month);
		d.set(Calendar.DAY_OF_MONTH, day);
		d.set(Calendar.HOUR_OF_DAY, hour);
		d.set(Calendar.MINUTE, minute);
		setDeadline(d);
	}

	public void setRoleName(String n)
	{
		if (n == null) return;
		name = n;
	}
	
	public void setNotes(String n)
	{
		if (n == null) return;
		notes = n;
	}
	
	/* ----- Compare ----- */
	
	public int hashCode()
	{
		return (int)roleUID;
	}
	
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null) return false;
		if (!(o instanceof ActorRole)) return false;
		ActorRole a = (ActorRole)o;
		return (a.roleUID == this.roleUID);
	}
	
	@Override
	public int compareTo(ActorRole o) {
		//Defaults to sorting by roleUID. Then by type. Then name.
		if (o == null) return 1;
		if (this.roleUID != o.roleUID) return (int)(this.roleUID - o.roleUID);
		if (this.typecode != o.typecode) return this.typecode - o.typecode;
		return this.name.compareTo(o.name);
	}
	
	/* ----- Serialization ----- */
	
	public String to_tsv_record()
	{
		String s = Long.toUnsignedString(roleUID) + "\t" 
				+ typecode + "\t" + Long.toUnsignedString(dirUser) + "\t" 
				+ Long.toUnsignedString(actUser) + "\t"
				+ Long.toUnsignedString(deadline) + "\t" + name + "\t" + notes;
		return s;
	}

}
