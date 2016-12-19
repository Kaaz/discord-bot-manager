package botloader;


import com.wezinkhof.configuration.ConfigurationOption;

public class Config {
	@ConfigurationOption
	public static boolean APP_ENABLED = false;

	@ConfigurationOption
	public static String JAVA_LOCATION = "java";

	@ConfigurationOption
	public static String BOT_JAR_LOCATION = "bot.jar";
	@ConfigurationOption
	public static String BOT_JAR_LOCATION_BACKUP = "bot_backup.jar";

	@ConfigurationOption
	public static String PROJECT_BUILD_LOCATION = "H:/projects/discordbot";

	@ConfigurationOption
	public static String GIT_REPOSITORY = "https://github.com/Kaaz/DiscordBot";
	@ConfigurationOption
	public static String GIT_BRANCH = "master";

	@ConfigurationOption
	public static boolean SHOW_BOT_OUTPUT = true;

	@ConfigurationOption
	public static boolean EMAIL_ENABLED = true;

	@ConfigurationOption
	public static String EMAIL_PREFIX_SUBJECT = "[DISCORD] [BOT] - ";

	@ConfigurationOption
	public static String EMAIL_DEFAULT_SUBJECT = "Generic Error";

	//account info to send emails though gmail
	@ConfigurationOption
	public static String EMAIL_USERNAME = "gmail_username";

	@ConfigurationOption
	public static String EMAIL_PASSWORD = "gmail_password";

	//comma seperated for multiple
	@ConfigurationOption
	public static String EMAIL_RECIPIENTS = "recipients";

}
