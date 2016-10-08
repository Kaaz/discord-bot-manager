package botloader;

import com.google.common.base.Joiner;
import com.google.common.io.Files;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class BotBuilder {
	/**
	 * Simple function to log the process arguments
	 *
	 * @param arguments process parameters
	 * @return the processbuilder
	 */
	private static ProcessBuilder makebuilder(String... arguments) {
		System.out.println("PROCESS: " + Joiner.on(" ").join(arguments));
		ProcessBuilder builder = new ProcessBuilder().command(arguments);
		builder.redirectErrorStream(true);
		return builder;
	}

	/**
	 * Updates the repository with the latest changes
	 *
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static void download() throws InterruptedException, IOException {
		File directory = new File(Config.PROJECT_BUILD_LOCATION);
		File[] dirContent = directory.listFiles();
		ProcessBuilder pb;
		if (!directory.exists() || (dirContent == null || dirContent.length == 0)) {
			if (Boot.isWindows) {
				pb = makebuilder("cmd", "/c", "git", "clone", Config.GIT_REPOSITORY, Config.PROJECT_BUILD_LOCATION);
			} else {
				pb = makebuilder("git", "clone", Config.GIT_REPOSITORY, Config.PROJECT_BUILD_LOCATION);
			}
		} else {
			if (Boot.isWindows) {
				pb = makebuilder("cmd", "/c", "git", "-C", Config.PROJECT_BUILD_LOCATION, "pull", "origin", Config.GIT_BRANCH);
			} else {
				pb = makebuilder("git", "-C", Config.PROJECT_BUILD_LOCATION, "pull", "origin", Config.GIT_BRANCH);
			}
		}
		Process gitProcess = pb.start();
		BufferedReader reader = new BufferedReader(new InputStreamReader(gitProcess.getInputStream()));
		String line;
		if (!gitProcess.waitFor(2, TimeUnit.MINUTES)) {
			System.out.println("Update process took too long");
			throw new RuntimeException("Update process took too long");
		}
		while ((line = reader.readLine()) != null) {
			System.out.println(line);
		}
		reader.close();
		gitProcess.destroy();
	}

	/**
	 * Builds the bot project with mvn
	 *
	 * @return success or exception :D
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static boolean build() throws IOException, InterruptedException {
		File directory = new File(Config.PROJECT_BUILD_LOCATION);
		ProcessBuilder pb;
		File pomFile = new File(directory.getAbsoluteFile() + "/pom.xml");
		if (Boot.isWindows) {
			pb = makebuilder("cmd", "/c", "mvn", "-f", pomFile.getAbsolutePath(), "clean", "process-resources", "compile", "assembly:single");
		} else {
			pb = makebuilder("mvn", "-f", pomFile.getAbsolutePath(), "clean", "process-resources", "compile", "assembly:single");
		}
		Process mvnProcess = pb.start();
		String line;
		BufferedReader reader = new BufferedReader(new InputStreamReader(mvnProcess.getInputStream()));
		while ((line = reader.readLine()) != null) {
			System.out.println(line);
		}
		reader.close();
		if (!mvnProcess.waitFor(2, TimeUnit.MINUTES)) {
			System.out.println("Update process took too long");
			throw new RuntimeException("Update process took too long");
		}
		mvnProcess.destroy();
		File mvnTarget = new File(directory.getAbsoluteFile() + "/target");
		File[] files = mvnTarget.listFiles((dir, name) -> name.endsWith(".jar"));
		if (files.length == 0) {
			throw new RuntimeException("Maven build failed");
		}
		return true;
	}

	/**
	 * Copies the most recently made bot.jar
	 *
	 * @return success
	 * @throws IOException
	 */
	public static boolean copyBuildJarToProduction() throws IOException {
		File currentProduction = new File(Config.BOT_JAR_LOCATION);
		File currentBackup = new File(Config.BOT_JAR_LOCATION_BACKUP);
		if (currentProduction.exists()) {
			if (currentBackup.exists()) {
				currentBackup.delete();
			}
			Files.move(currentProduction, currentBackup);
		}
		File directory = new File(Config.PROJECT_BUILD_LOCATION + "/target");
		File[] files = directory.listFiles((dir, name) -> name.endsWith(".jar"));
		if (files.length > 0) {
			Files.move(files[0], currentProduction);
			return true;
		}
		return false;
	}
}