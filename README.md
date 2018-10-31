# cafebots

## Command Overview
Although many of these commands are listing when requesting !help from the bots directly, !help does not provide a comprehensive list.
Therefore, I shall include a comprehensive list here instead.

### Prefixes
For the message listener to recognize a sent message as a command and even bother to process it, the message must meet one of two requirements:
  - A. It must begin with "!" OR
  - B. It must contain mentions of one or more bot accounts connected to the core.
  
Note that if it contains both, the command may not parse correctly.

The "!" prefix allows the parser to choose which bot (and bot account) to send the command to. Using bot mentions allows the user to specify which bot they wish to carry out the command in theory... however, many bots do not recognize specific commands, and may defer to a bot that does anyway (the bot that would likely be chosen by the parser had the user used "!").

### Standard Commands

General

```
help 
    View current list of commands
eventhelp arghelp
    Get information on event creation arguments.
eventhelp sorhelp
    Get information on the sor command.
qchan birthday
    Check which channel is set as the birthday wishes channel.
qchan greeting
    Check which channel is set as the greetings/farewell channel.
checkg greeting (me)
    Check greeting status for this guild/server. If optional "me" arg is included, 
    check if greeting pings are on for user.
checkg farewell (me)
    Check farewell status for this guild/server. If optional "me" arg is included, 
    check if farewell pings are on for user.
```

User Profile Management

```
gettz
    Get your timezone
changetz [tzid]
    Change your timezone to the one specified by tzid.
seealltz
    Get a text file containing the list of all possible tzids.
sor [etype] [rlevel]
    Switch on/off specific reminders.
sor alloff
    Turn off all reminders.
sor allon
    Turn on all reminders.
sor defo
    Reset all reminders to default on/off settings.
sor default
    Reset all reminders to default on/off settings.
sor view [etype]
    Get a list of all reminder times for that event type.
callme "[Name]"
    Set the name for the bots to refer to you by in the local guild.
setgender [f/m/n]
    Set user gender (bots may refer to users by pronouns). Default is n (neutral).
```

Event Creation

```
onetime [yyyy] [mm] [dd] [hh:mm] "[eventname]" [rchannel] [tchannel] [target1] (target2+)
    Create a one-time event that will take place on mm dd, yyyy at hh:mm relative to requesting user's timezone.
deadline [yyyy] [mm] [dd] [hh:mm] "[eventname]" [rchannel] [tchannel] [target1] (target2+)
    Create a deadline on mm dd, yyyy at hh:mm relative to requesting user's timezone.
weekly [DOW] [hh:mm] "[eventname]" [rchannel] [tchannel] [target1] (target2+)
    Create a weekly recurring event that happens at hh:mm on DOW of every week, relative to requesting user's timezone.
biweekly [DOW] [hh:mm] "[eventname]" [rchannel] [tchannel] [target1] (target2+)
    Create a bi-weekly recurring event that happens at hh:mm on DOW of every week, relative to requesting user's timezone.
monthlyday [dd] [hh:mm] "[eventname]" [rchannel] [tchannel] [target1] (target2+)
    Create a monthly event that occurs on the dd-th of every month at hh:mm, relative to requesting user's timezone.
monthlydow [DOW] [ww] [hh:mm] "[eventname]" [rchannel] [tchannel] [target1] (target2+)
    Create a monthly event that occurs on the ww-th DOW every month (eg. 2nd Monday) at hh:mm, relative to requesting user's timezone.
birthday [mm] [dd]
    Add your birthday to the birthday list.
```

Event Management

```
myevents
    View a list of all events you are scheduled for (both those you requested, and those you are invited to and haven't declined)
ecancel [eventID] (instance) (silent)
    Cancel an event you created. Instance and silent arguments are optional, and should be bools if included.
checkrsvp [eventID]
    Check your rsvp status for an event.
eventinfo [eventID]
    Get details on a specific event.
```

Role Management

```
listroles
    Get a list of your assigned roles and deadlines.
roleinfo [roleID]
    Get details on a specific role.
amiaudconf
    See whether you have been marked as having confirmed audio quality.
setmic "[Mic name]"
    Set a String name for your microphone. If you wish to include spaces in the name, please surround argument with quotes.
setiface "[Audio interface name]"
    Set a String name for your audio interface. If you wish to include spaces in the name, please surround argument with quotes.
setdaw "[DAW name]"
    Set a String name for your DAW (audio software). If you wish to include spaces in the name, please surround argument with quotes.
seemic [user]
    Get the name of a user's microphone as specified by the user.
seeiface [user]
    Get the name of a user's audio interface as specified by the user.
seedaw [user]
    Get the name of a user's DAW (audio software) as specified by the user.
```

Message Cleaning

```
cleanme
    Wipe all of your messages from this channel.
cleanmeday
    Wipe all of your messages from this channel that were posted in the past 24 hours.
```

### Reply Commands

Events

```
attend [eventID]
    Mark yourself as attending the specified event (instance).
cantgo [eventID]
    Mark yourself as not attending the specified event (instance).
```

Roles

```
accept [roleID]
    Formally accept a role offered to you.
decline [roleID]
    Formally decline a role offered to you.
```

### Admin Commands

Greetings/ Farewells/ Birthdays

```
chchan birthday [channel]
    Set the channel birthday messages are sent to. Defaults to #general.
chchan greeting [channel]
    Set the channel greeting messages are sent to. Defaults to #general.
setGreetings [on/off]
    Turn on/off greetings when new members join.
pingGreetings [on/off]
    Turn on/off whether to ping you when new members join. Pings will be sent to channel command was last issued in.
setFarewells [on/off]
    Turn on/off farewell messages when members leave.
pingFarewells [on/off]
    Turn on/off whether to ping you when members leave. Pings will be sent to channel command was last issued in.
```

Role Management

```
makerole [rolename] [user] [deadline (yyyy mm dd)] [channel] [notes]
    Setup a role announcement (automatically generates deadline event if audconf is true).
seeroles [user]
    See all roles for a user.
revokerole [roleID]
    Delete a role record.
completerole [roleID]
    Mark a role as completed.
pushdeadline [roleID] [new deadline (yyyy mm dd)]
    Change a role deadline.
audconf [user] [bool]
    Set whether a user has confirmed audio.
```

Permissions

```
checkperm
    Check which roles have admin permissions. The server owner always has admin permissions.
addperm [discordrole]
    Add admin permissions for a Discord role.
remperm [discordrole]
    Remove admin permissions from a Discord role.
setname [user] "[Name]"
    Set name for bots to refer to user by in local guild.
```

Message Cleaning

```
cleanday
    Clean all messages from this channel that were posted in the last 24 hours (additional Discord permissions apply).
cmdclean
    Clean all recognized bot command messages on this server/guild that were sent since the last bot bootup.
autocmdclean [on/off]
    Turn on or off auto command clean - if on, cmdclean will be automatically run every 24 hours.
```

### Arguments - Events

Times

```
yyyy - 4 digit year (>= 2015; Discord doesn't record time before the year 2015)
mm - 2 digit month (Must be 01-12)
dd - 2 digit day (Lowest it can be is 01, highest is the last day of that month)
hh - Hour on 24 hour clock (00-23)
mm - Minute (00-59)
DOW - Day of the week. Not case sensitive. Has to be one of the following: [mon, tue, wed, thu, fri, sat, sun]
ww - Week number in month (01-05)
```

Event Name

```
If the event name contains any whitespace, it MUST be surrounded by double quotes!
```

Channels

```
rchannel - Name of channel to issue event reminders to event creator in.
tchannel - Name of channel to issue event reminders to event participants in.

Channels can be specified by channel names or long UIDs. Don't include the # in channel names. The parser doesn't recognize it at the moment.
```

Users

```
target[n] - Usernames or UIDs of users to include as event participants. You may include as many users as you wish, including none.

To make the event a universal event, you can substitute "everybody", "all", or "group" for the target1 argument. This will cause reminders for this event to ping @everyone. Note that if you include any additional participants after this argument, they will be ignored.
```

ecancel Optional Arguments

```
(instance) - Whether to cancel only the next instance of a recurring event (true) or to cancel the entire event series (false).
(silent) - If silent is true, the event participants will NOT be notified when the event is cancelled. Otherwise, they will.
```

### Arguments - SOR

Event Types

```
onetime
deadline
weekly
biweekly
monthlyday
monthlydow
```

Event Levels

```
There are 3-5 reminder levels for each event type. The index system is 1-based. Therefore, the reminder closest to the event time is reminder level 1, whereas the reminder furthest from the event time will be reminder level 3, reminder level 4, or reminder level 5.
```

### Arguments - Other

- All single arguments that contain whitespace should be surrounded by double quotes.
- All IDs (event IDs, role IDs, Discord object UIDs) must be unsigned decimal long values.
- For user arguments, one of three things can be specified...
    + The User's Discord UID (as an unsigned decimal long)
    + The User's username
    + The User's local nickname
- For channel arguments, one of two things can be specified
    + The channel's Discord UID (as an unsigned decimal long)
    + The channel's name WITHOUT the # pound prefix.
- The notes argument for role creation is just a string, and should be surrounded by double quotes if it includes whitespace.
- The optional (instance) and (silent) arguments for ecancel are bools.
- For those unfamiliar with the term "bool" is short for "boolean" which is a simple true/false. Acceptable bool values are...
    + TRUE: "true", "t", "yes", "y", "1"
    + FALSE: "false", "f", "no", "n", "0"
  Boolean arguments are not case sensitive.

## Known Issues

### The Parser Bottleneck

This program is thoroughly multi-threaded... except for the parser.
At the moment, the listener tosses every message it catches at the parser (putting them in a synchronized queue for the parser thread to
pull when ready), and the parser has only one thread to sift through and process every single message thrown at it.

This is not a problem at the scale I planned to use the framework at - that is a single small Discord server. I mean, I'm hosting my copy on a two-core computer... 

However, if the bots ever encounter a situation where they are receiving a large volume of commands simultaneously, this might become quite problematic down the line.

In theory, multithreading the parser should be trivial. The problem is that I anticipate serious race conditions with the prompt-response framework, and I just haven't really felt like going in and restructuring it so that it can be compatible with a multi-threaded parser...

### Guild-Specific User Profiles

User profiles (that is, information the bots need to know about a specific user) are stored separately per guild/server. What this means is that as far as the bots are concerned, the same user on two different Discord servers is two different users.

This is a fine setup for a bot that's only to be used by a single guild. It's absolutely terrible for a bot that's to be used on multiple guilds.

The most obvious problem is that it makes a serious pain for users - they essentially have to reset their profiles every time they want to use the same bots on a different guild/server.

The more worrisome problem is that it's a complete memory gobbler. I mean, instead of having one user profile per user, it has one (redundant) user profile per user per server. Plus, it eats more disk space when it saves the user data. 

It's a pretty bad model.

The reason it's there is because there are a lot of guild specific user fields (isAdmin, greeting pings, etc.) that I simply tied to the user object. To fix this issue, I would need to split the universal user fields (SOR settings, timezone, etc.) from these guild specific fields, which would require major restructuring of both how the program accesses this info and how it stores/retrieves this info from files on disk.

That's something I actually intend to fix... but I'm working on an insane number of projects and it isn't a high priority...

### Everything is Slow. Everything is Copypaste.

I really didn't intend to spend much time developing this bot framework, but as with all things, I just kept getting neat ideas and decided to add them in.

As a result, there are certainly quite a few parts that are just... not optimized. It's a bit of a patchwork, really.
I might fix that one day. Maybe.

## Future Changes

## Translation
