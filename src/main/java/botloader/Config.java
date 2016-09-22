package botloader;


import com.wezinkhof.configuration.ConfigurationOption;

public class Config {
	@ConfigurationOption
	public static boolean APP_ENABLED = false;

	@ConfigurationOption
	public static String BOT_JAR_LOCATION = "bot.jar";
	@ConfigurationOption
	public static String BOT_JAR_LOCATION_BACKUP = "bot_backup.jar";

	@ConfigurationOption
	public static String PROJECT_BUILD_LOCATION = "H:/projects/discordbot";

	@ConfigurationOption
	public static String GIT_REPOSITORY = "https://github.com/MaikWezinkhof/DiscordBot";
	@ConfigurationOption
	public static String GIT_BRANCH = "master";

	@ConfigurationOption
	public static boolean SHOW_BOT_OUTPUT = true;
}
