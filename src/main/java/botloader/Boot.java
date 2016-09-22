package botloader;

import com.wezinkhof.configuration.ConfigurationBuilder;

import java.io.File;

/**
 *
 */
public class Boot {

	public static void main(String... args) throws Exception {
		new ConfigurationBuilder(Config.class, new File("boot.cfg")).build();
		if (!Config.APP_ENABLED) {
			System.out.println("Boot not enabled, see boot.cfg for the app_enabled setting");
		}
		File productionJarFile = new File(Config.BOT_JAR_LOCATION);
		if (!productionJarFile.exists()) {
			BotBuilder.download();
			BotBuilder.build();
			BotBuilder.copyBuildJarToProduction();
		}

		while (true) {
			ProcessBuilder builder = new ProcessBuilder();
			builder.command("java", "-jar", productionJarFile.getAbsolutePath());

			Process botProcess = builder.start();
			botProcess.waitFor();
			ExitCode exitCode = ExitCode.fromCode(botProcess.exitValue());
			switch (exitCode) {
				case SHITTY_CONFIG:
					System.out.println("Fix the bot's config first!");
				case STOP:
					System.out.println("I guess we'll stop then");
					System.exit(0);
					break;
				case REBOOT:
				case DISCONNECTED:
					System.out.println("to the next iteration we go!");
					break;
				case UPDATE:
					BotBuilder.download();
					BotBuilder.build();
					BotBuilder.copyBuildJarToProduction();
					break;
				case GENERIC_ERROR:
					System.out.println("Uhm, unknown error, check the bot's log, exiting for now");
					System.exit(0);
				default:
					System.out.println("Not sure what to do :(, exiting!");
					System.exit(0);
					break;
			}
		}
	}
}
