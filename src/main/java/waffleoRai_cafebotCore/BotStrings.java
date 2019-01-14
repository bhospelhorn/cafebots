package waffleoRai_cafebotCore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import waffleoRai_schedulebot.EventType;

public class BotStrings {
	
	public static final String STRINGNOTFOUND_ENG = "[STRING NOT FOUND]";
	
	//Common Strings
	public static final String KEY_MAINGROUP_COMMON = "commonstrings";
	
	//Groups
	public static final String KEY_MAINGROUP_BOTSTRINGS = "botstrings";
	public static final String KEY_GROUP_FILEINFO = ".fileinfo";
	public static final String KEY_GROUP_GENERAL = ".generalbotstrings";
	public static final String KEY_GROUP_USERQUERY = ".userquery";
	public static final String KEY_GROUP_PERMMANAGE = ".permissionsmanage";
	public static final String KEY_GROUP_GREETINGS = ".greetings";
	public static final String KEY_GROUP_EVENTMANAGE = ".eventmanage";
	public static final String KEY_GROUP_USERMANAGE = ".usermanage";
	public static final String KEY_GROUP_ROLEMANAGE = ".roles";
	public static final String KEY_GROUP_ROLE_MAJOR = ".role_major";
	public static final String KEY_GROUP_ROLE_SUPPORT = ".role_support";
	public static final String KEY_GROUP_ROLE_EXTRA = ".role_ext";
	public static final String KEY_GROUP_BIRTHDAY = ".event_birthday";
	public static final String KEY_GROUP_ONETIME = ".event_onetime";
	public static final String KEY_GROUP_DEADLINE = ".event_deadline";
	public static final String KEY_GROUP_WEEKLY = ".event_weekly";
	public static final String KEY_GROUP_BIWEEKLY = ".event_biweekly";
	public static final String KEY_GROUP_MONTHLYA = ".event_monthlya";
	public static final String KEY_GROUP_MONTHLYB = ".event_monthlyb";
	public static final String KEY_GROUP_CLEANMSG = ".cleanmessages";
	public static final String KEY_GROUP_GAMESTATUS = ".gameplayingstatus";
	
	//Group: File Info
	public static final String KEY_GSALTFILE_NEUTRAL_REQ = ".genderspecxml_nr";
	public static final String KEY_GSALTFILE_FEMALE_REQ = ".genderspecxml_fr";
	public static final String KEY_GSALTFILE_MALE_REQ = ".genderspecxml_mr";
	public static final String KEY_GSALTFILE_NEUTRAL_TARG = ".genderspecxml_nu";
	public static final String KEY_GSALTFILE_FEMALE_TARG = ".genderspecxml_fu";
	public static final String KEY_GSALTFILE_MALE_TARG = ".genderspecxml_mu";
	public static final String KEY_GSALTFILE_NEUTRALGROUP_TARG = ".genderspecxml_multin";
	public static final String KEY_GSALTFILE_FEMALEGROUP_TARG = ".genderspecxml_multif";
	public static final String KEY_GSALTFILE_MALEGROUP_TARG = ".genderspecxml_multim";
	
	//Group: General
	public static final String KEY_SOR_ON = ".soron";
	public static final String KEY_SOR_OFF = ".soroff";
	public static final String KEY_SOR_ALLON = ".soronall";
	public static final String KEY_SOR_ALLOFF = ".soroffall";
	public static final String KEY_SOR_DEFO = ".sordefo";
	public static final String KEY_RESPONSE_GENERALNO = ".userno_general";
	public static final String KEY_BADRESPONSE_TIMEOUT = ".responsetimeout";
	public static final String KEY_BADRESPONSE_REPROMPT = ".responseinvalid";
	public static final String KEY_PARSERBLOCKED = ".parserblocked";
	public static final String KEY_GREET = ".servergreeting";
	public static final String KEY_PINGGREET = ".newmemberping";
	public static final String KEY_FAREWELL = ".serverfarewell";
	public static final String KEY_PINGDEPARTURE = ".memberleaveping";
	public static final String KEY_NOADMINPERM = ".insufficentPermissions";
	public static final String KEY_BADCMD = ".cannotunderstand";
	public static final String KEY_OTHERBOT = ".theygotit";
	public static final String KEY_WRONGBOT = ".wrongbot";
	public static final String KEY_EVENTHELPSTEM = ".eventhelpmessage";
	public static final String KEY_SORHELPSTEM = ".sorhelpmessage";
	public static final String KEY_HELPSTEM_STANDARD = ".helpmessage";
	public static final String KEY_HELPSTEM_ADMIN = "_admin";
	public static final String KEY_INTERNALERROR = ".internalerror";
	
	//Group: Perm Manage
	public static final String KEY_PERMS_QUERY = ".query";
	public static final String KEY_PERMS_CONFIRMADD = ".confirmadd";
	public static final String KEY_PERMS_CONFIRMREM = ".confirmrem";
	public static final String KEY_PERMS_CONFIRM = ".confirm";
	public static final String KEY_PERMS_CONFIRMNEG = ".confirmneg";
	public static final String KEY_PERMS_REJECT = ".reject";
	
	//Group: Greetings
	public static final String KEY_GREET_CHCHAN_SUCCESS = ".confirm_chset_success";
	public static final String KEY_GREET_CHCHAN_FAILURE = ".confirm_chset_failure";
	public static final String KEY_GREET_CHECKCHAN = ".checkch";
	public static final String KEY_GREET_CHECKCHAN_EMPTY = ".checkchempty";
	public static final String KEY_GREET_CHECKG_ON = ".checkgsettingon";
	public static final String KEY_GREET_CHECKG_OFF = ".checkgsettingoff";
	public static final String KEY_GREET_CHECKGP_ON = ".checkpsettingon";
	public static final String KEY_GREET_CHECKGP_OFF = ".checkpsettingoff";
	public static final String KEY_GREET_CHECKF_ON = ".checkfsettingon";
	public static final String KEY_GREET_CHECKF_OFF = ".checkfsettingoff";
	public static final String KEY_GREET_CHECKFP_ON = ".checkfpsettingon";
	public static final String KEY_GREET_CHECKFP_OFF = ".checkfpsettingoff";
	public static final String KEY_GREET_SWITCH_ON = ".setgreeton";
	public static final String KEY_GREET_SWITCH_OFF = ".setgreetoff";
	public static final String KEY_GREET_SWITCH_FAIL = ".setgreetfail";
	public static final String KEY_GREET_SWITCHGP_ON = ".setgreetpingon";
	public static final String KEY_GREET_SWITCHGP_OFF = ".setgreetpingoff";
	public static final String KEY_GREET_SWITCHGP_FAIL = ".setgreetpingfail";
	public static final String KEY_GREET_FSWITCH_ON = ".setfarewellon";
	public static final String KEY_GREET_FSWITCH_OFF = ".setfarewelloff";
	public static final String KEY_GREET_FSWITCH_FAIL = ".setfarewellfail";
	public static final String KEY_GREET_SWITCHFP_ON = ".setfarewellpingon";
	public static final String KEY_GREET_SWITCHFP_OFF = ".setfarewellpingoff";
	public static final String KEY_GREET_SWITCHFP_FAIL = ".setfarewellpingfail";
	
	//Group: Event Manage
	public static final String KEY_VIEWEVENTS_ALLUSER = ".viewuserevents";
	public static final String KEY_VIEWEVENTS_REQUSER = ".viewrequestedevents";
	public static final String KEY_CANCELEVENTS_PROMPT = ".canceleventconfirm_prompt";
	public static final String KEY_CANCELEVENTS_SUCCESS = ".canceleventconfirm_success";
	public static final String KEY_CANCELEVENTS_CANCEL = ".canceleventconfirm_cancel";
	public static final String KEY_CANCELEVENTS_FAILURE = ".canceleventconfirm_failure";
	public static final String KEY_EVENTINFO_FAILURE = ".vieweventinfo_fail";
	public static final String KEY_EVENTINFO = ".vieweventinfo";
	
	//Group: Game playing status
	public static final String KEY_STATUSSTEM_OFF = ".off";
	public static final String KEY_STATUSSTEM_ON = ".on";
	
	//Group: User Query
	public static final String KEY_SAYSOMETHING = ".saysomething";
	
	//Group: User manage
	public static final String KEY_SEEALLTZ = ".seealltz";
	public static final String KEY_GETTZ = ".gettz";
	public static final String KEY_SETTZ_SUCCESS = ".changetz_success";
	public static final String KEY_SETTZ_FAIL = ".changetz_fail";
	
	//Group: Birthday
	public static final String KEY_BIRTHDAY_WISH_STEM = ".birthdaywish";
	public static final String KEY_BIRTHDAY_CHSET_FAIL = ".confirm_chset_failure";
	public static final String KEY_BIRTHDAY_CHSET_SUCCESS = ".confirm_chset_success";
	public static final String KEY_BIRTHDAY_CHECKCHANNEL = ".checkch";
	public static final String KEY_BIRTHDAY_CHECKCHANNEL_EMPTY = ".checkchempty";
	public static final String KEY_BIRTHDAY_CONFIRM_FAILURE = ".confirm_success";
	public static final String KEY_BIRTHDAY_CONFIRM_SUCCESS = ".confirm_failure";
	
	//Group: (Event)
	public static final String KEY_CONFIRMCREATE_STEM = ".confirmcreate";
	public static final String KEY_BADARGS = ".moreargs";
	public static final String KEY_BADCHANNEL = ".bad_channel";
	public static final String KEY_EVENTREMIND_STEM = ".remind";
	public static final String KEY_CONFIRMATTEND_STEM = ".confirmattend";
	public static final String KEY_NOTIFYATTEND_STEM = ".notifyattend";
	public static final String KEY_ATTENDLIST_STEM = ".attendlist";
	public static final String KEY_NOTIFYATTEND_0 = "rt";
	public static final String KEY_NOTIFYATTEND_1 = "tr";
	public static final String KEY_NOTIFYATTEND_2 = "tt";
	public static final String KEY_NOTIFYCANCEL = ".notifycancel";
	public static final String KEY_NOTIFYCANCELINSTANCE = ".notifycancelinstance";
	public static final String KEY_CHECKRSVP_STEM = ".checkrsvp";
	
	//Group: Clean
	public static final String KEY_USERALL_PROMPT = ".cmme_confirm";
	public static final String KEY_USERALL_SUCCESS = ".cmme_success";
	public static final String KEY_USERALL_FAIL = ".cmme_fail";
	public static final String KEY_USERDAY_PROMPT = ".cmmeday_confirm";
	public static final String KEY_USERDAY_SUCCESS = ".cmmeday_success";
	public static final String KEY_USERDAY_FAIL = ".cmmeday_fail";
	public static final String KEY_ALLDAY_PROMPT = ".cmday_confirm";
	public static final String KEY_ALLDAY_SUCCESS = ".cmday_success";
	public static final String KEY_ALLDAY_FAIL = ".cmday_fail";
	public static final String KEY_CMDCLEAN_SUCCESS = ".cmd_clean_success";
	public static final String KEY_CMDCLEAN_FAIL = ".cmd_clean_fail";
	public static final String KEY_CMDCLEAN_AUTO_ON_SUCCESS = ".set_auto_cmd_clean_on_success";
	public static final String KEY_CMDCLEAN_AUTO_ON_FAIL = ".set_auto_cmd_clean_on_fail";
	public static final String KEY_CMDCLEAN_AUTO_OFF_SUCCESS = ".set_auto_cmd_clean_off_success";
	public static final String KEY_CMDCLEAN_AUTO_OFF_FAIL = ".set_auto_cmd_clean_off_fail";
	
	public static final int STANDARD_HELP_PARTS = 4;
	public static final int ADMIN_HELP_PARTS = 2;
	
	
	private static Map<EventType, String> eventtype_groups;
	
	private static void mapEventGroupNames()
	{
		eventtype_groups = new HashMap<EventType, String>();
		eventtype_groups.put(EventType.BIRTHDAY, KEY_GROUP_BIRTHDAY);
		eventtype_groups.put(EventType.ONETIME, KEY_GROUP_ONETIME);
		eventtype_groups.put(EventType.DEADLINE, KEY_GROUP_DEADLINE);
		eventtype_groups.put(EventType.WEEKLY, KEY_GROUP_WEEKLY);
		eventtype_groups.put(EventType.BIWEEKLY, KEY_GROUP_BIWEEKLY);
		eventtype_groups.put(EventType.MONTHLYA, KEY_GROUP_MONTHLYA);
		eventtype_groups.put(EventType.MONTHLYB, KEY_GROUP_MONTHLYB);
	}
	
	public static String getStringKey_Event(EventType t, StringKey str, StringKey op1, int op2)
	{
		if (eventtype_groups == null) mapEventGroupNames();
		String base = KEY_MAINGROUP_BOTSTRINGS + eventtype_groups.get(t);
		switch(str)
		{
		case EVENT_BADARGS:
			return base + KEY_BADARGS;
		case EVENT_BADCHAN:
			return base + KEY_BADCHANNEL;
		case EVENT_CONFIRMATTEND:
			base += KEY_CONFIRMATTEND_STEM;
			if (op1 == StringKey.OP_YES) return base + "_yes";
			else if (op1 == StringKey.OP_NO) return base + "_no";
			else if (op1 == StringKey.OP_FAIL){
				base += "_fail";
				if (op2 == 1) return base + "r";
				if (op2 == 2) return base + "n";
				return base;
			}
			break;
		case EVENT_CONFIRMCREATE:
			base += KEY_CONFIRMCREATE_STEM;
			if (op1 == null) return base;
			if (op1 == StringKey.OP_YES) return base + "_yes";
			else if (op1 == StringKey.OP_NO) return base + "_no";
			else if (op1 == StringKey.OP_FAIL) return base + "_fail";
			break;
		case EVENT_NOTIFYATTEND:
			base += KEY_NOTIFYATTEND_STEM;
			switch (op2)
			{
			case 0: base += "_" + KEY_NOTIFYATTEND_0 + "_"; break;
			case 1: base += "_" + KEY_NOTIFYATTEND_1 + "_"; break;
			case 2: base += "_" + KEY_NOTIFYATTEND_2 + "_"; break;
			default: return null;
			}
			if (op1 == StringKey.OP_YES) return base + "yes";
			else if (op1 == StringKey.OP_NO) return base + "no";
			break;
		case EVENT_REMIND:
			String rbase = base + KEY_EVENTREMIND_STEM + Integer.toString(op2);
			switch (op1)
			{
			case OP_REQUSER: return rbase+"r";
			case OP_TARGUSER: return rbase+"t";
			case OP_GROUPUSER: return rbase+"g";
			default: return null;
			}
		case EVENT_ATTENDLIST:
			return base + KEY_ATTENDLIST_STEM + Integer.toString(op2);
		case EVENT_NOTIFYCANCEL:
			return base + KEY_NOTIFYCANCEL;
		case EVENT_NOTIFYCANCELINSTANCE:
			return base + KEY_NOTIFYCANCELINSTANCE;
		case EVENT_CHECKRSVP:
			String nbase = base + KEY_CHECKRSVP_STEM;
			switch (op1)
			{
			case OP_YES:
				return nbase + "_yes";
			case OP_NO:
				return nbase + "_no";
			case OP_UNKNOWN:
				return nbase + "_unk";
			case OP_FAIL:
				if (op2 != 0) return nbase + "_failn";
				else return nbase + "_fail";
			default: return null;
			}
		default:
			return null;
		}
		return null;
	}

	public static String getInternalErrorKey()
	{
		return KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_GENERAL + KEY_INTERNALERROR;
	}
	
	public static List<String> parseStringList(String in)
	{
		if (in == null) return null;
		int commacount = 0;
		int slen = in.length();
		for(int i = 0; i < slen; i++)
		{
			if (in.charAt(i) == ',') commacount++;
		}
		List<String> list = new ArrayList<String>(commacount+1);
		
		int pos = 0;
		boolean inword = false;
		boolean escape = false;
		StringBuilder sb = null;
		while(pos < slen)
		{
			char c = in.charAt(pos);
			if (c == '\\')
			{
				escape = true;
				pos++;
				continue;
			}
			if(!inword)
			{
				//Check to see if c is a double quote
				if (!escape && c == '\"')
				{
					inword = true;
					sb = new StringBuilder();
				}
				//Otherwise, the character is discarded
			}
			else
			{
				//Check to see if c is a double quote
				if (!escape && c == '\"')
				{
					inword = false;
					list.add(sb.toString());
					sb = null;
				}
				else
				{
					//Add character to string
					sb.append(c);
				}
			}
			escape = false;
			pos++;
		}
		
		return list;
	}
	
	public static String getGenderStringFileNameKey_Req(int gender)
	{
		String base = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_FILEINFO;
		switch(gender)
		{
		case ActorUser.ACTOR_GENDER_FEMALE:
			return base + KEY_GSALTFILE_FEMALE_REQ;
		case ActorUser.ACTOR_GENDER_MALE:
			return base + KEY_GSALTFILE_MALE_REQ;
		default:
			return base + KEY_GSALTFILE_NEUTRAL_REQ;
		}
	}
	
	public static String getGenderStringFileNameKey_Targ(int gender)
	{
		String base = KEY_MAINGROUP_BOTSTRINGS + KEY_GROUP_FILEINFO;
		switch(gender)
		{
		case ActorUser.ACTOR_GENDER_FEMALE:
			return base + KEY_GSALTFILE_FEMALE_TARG;
		case ActorUser.ACTOR_GENDER_MALE:
			return base + KEY_GSALTFILE_MALE_TARG;
		case ActorUser.ACTOR_GENDER_MULTIPLE_MIXED:
			return base + KEY_GSALTFILE_NEUTRALGROUP_TARG;
		case ActorUser.ACTOR_GENDER_MULTIPLE_ALLFEM:
			return base + KEY_GSALTFILE_FEMALEGROUP_TARG;
		case ActorUser.ACTOR_GENDER_MULTIPLE_ALLMEN:
			return base + KEY_GSALTFILE_MALEGROUP_TARG;
		default:
			return base + KEY_GSALTFILE_NEUTRAL_TARG;
		}
	}

	public static String getGenderPronounCommonKeyStem(int gender)
	{
		String base = KEY_MAINGROUP_COMMON + "pronoun";
		switch(gender)
		{
		case ActorUser.ACTOR_GENDER_FEMALE:
			return base + ".feminine";
		case ActorUser.ACTOR_GENDER_MALE:
			return base + ".masculine";
		case ActorUser.ACTOR_GENDER_MULTIPLE_MIXED:
			return base + ".multi_n";
		case ActorUser.ACTOR_GENDER_MULTIPLE_ALLFEM:
			return base + ".multi_f";
		case ActorUser.ACTOR_GENDER_MULTIPLE_ALLMEN:
			return base + ".multi_m";
		default:
			return base + ".neutral";
		}
	}
	
	public static String capitalizeFirstLetter(String in)
	{
		if (in == null || in.isEmpty()) return "";
		if (in.length() == 1) return in.toUpperCase();
		String first = in.substring(0, 1).toUpperCase();
		String rest = in.substring(1);
		return first + rest;
	}
	
}
