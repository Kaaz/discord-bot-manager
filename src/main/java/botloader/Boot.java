package botloader;

import com.kaaz.configuration.ConfigurationBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class Boot {
	//amount of times to try rebooting the bot
	public final static int UNPLANNED_RESTART_LIMIT = 10;

	//reset the unplanned restart counter after this time:
	public final static long RESET_UNPLANNED_AFTER = TimeUnit.MINUTES.toMillis(30);
	public static boolean isWindows = System.getProperty("os.name").startsWith("Windows");


	private static int unplannedRestarts = 0;
	private static long lastUnplannedRestart = 0;

	public static void main(String... args) throws Exception {

		new ConfigurationBuilder(Config.class, new File("boot.cfg")).build(true);
		if (!Config.APP_ENABLED) {

			System.out.println("Boot not enabled, see boot.cfg for the app_enabled setting");
			System.exit(0);
		}
		File productionJarFile = new File(Config.BOT_JAR_LOCATION);
		if (!productionJarFile.exists()) {
			BotBuilder.download();
			BotBuilder.build();
			BotBuilder.copyBuildJarToProduction();
		}
		ExitCode exitCode;
		while (true) {
			ProcessBuilder builder = new ProcessBuilder();
			builder.redirectErrorStream(true);
			builder.command(Config.JAVA_LOCATION, "-jar", productionJarFile.getAbsolutePath(), "-XX:+UseConcMarkSweepGC");
			Process botProcess = builder.start();
			try (
					InputStreamReader inputStreamReader = new InputStreamReader(botProcess.getInputStream());
					BufferedReader reader = new BufferedReader(inputStreamReader)
			) {
				String line;
				System.out.println("starting process");
				if (Config.SHOW_BOT_OUTPUT) {
					while ((line = reader.readLine()) != null) {
						System.out.println(line);
					}
				}
				botProcess.waitFor();
				exitCode = ExitCode.fromCode(botProcess.exitValue());
			}
			switch (exitCode) {
				case SHITTY_CONFIG:
					System.out.println("Fix the bot's config first!");
				case STOP:
					System.out.println("I guess we'll stop then");
					System.exit(0);
					break;
				case REBOOT:
					break;
				case DISCONNECTED:
					registerUnplannedRestart();
					System.out.println("to the next iteration we go!");
					break;
				case UPDATE:
					BotBuilder.download();
					BotBuilder.build();
					BotBuilder.copyBuildJarToProduction();
					break;
				case GENERIC_ERROR:
					Mail.getInstance().send("There was an unspecified error, the bot stopped running! check the logs");
					System.out.println("Uhm, unknown error, check the bot's log, exiting for now");
					System.exit(0);
				default:
					registerUnplannedRestart();
					Mail.getInstance().send("There was an error with an unknown cause, the bot stopped running! check the logs exitcode=" + botProcess.exitValue());
					System.out.println("Not sure what to do :(, exiting!");
					System.out.println("Exit value: " + botProcess.exitValue());
					break;
			}
			botProcess.destroy();
			System.gc();
			if (unplannedRestartLimitHit()) {
				Mail.getInstance().send("Unplanned restart limit", "Unplanned restart limit hit!");
			}
		}
	}

	private static boolean unplannedRestartLimitHit() {
		return unplannedRestarts >= UNPLANNED_RESTART_LIMIT;
	}

	private static void registerUnplannedRestart() {
		if ((lastUnplannedRestart + RESET_UNPLANNED_AFTER) < System.currentTimeMillis()) {
			unplannedRestarts = 0;
		}
		unplannedRestarts++;
		lastUnplannedRestart = System.currentTimeMillis();

	}
}
