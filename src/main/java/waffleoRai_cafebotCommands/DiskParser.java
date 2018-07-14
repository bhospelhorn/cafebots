/*
 * The DiskParser setup was originally intended to be used for special bot-to-bot commands
 * passed by reading and writing text files on disk.
 * 
 * Then I realized that there wasn't any particular reason I couldn't just run all of the
 * bots out of a single Java instance and just keep everything in memory!
 */

package waffleoRai_cafebotCommands;

@Deprecated
public interface DiskParser {
	public Command generateCommand(String[] args);
	
}
