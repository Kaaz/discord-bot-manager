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
        File projectFile;
        if (Boot.isMaven) {
            projectFile = new File(directory.getAbsoluteFile() + "/pom.xml");
        } else {
            projectFile = new File(directory.getAbsoluteFile() + "/build.gradle");
        }
        if (Boot.isWindows) {
            if (Boot.isMaven) {
                pb = makebuilder("cmd", "/c", "mvn", "-f", projectFile.getAbsolutePath(), "clean", "process-resources", "compile", "assembly:single");
            } else {
                pb = makebuilder("cmd", "/c", "gradle", "-p", projectFile.getParentFile().getAbsolutePath(), "fatJar");
            }
        } else {
            if (Boot.isMaven) {
                pb = makebuilder("mvn", "-f", projectFile.getAbsolutePath(), "clean", "process-resources", "compile", "assembly:single");
            } else {
                pb = makebuilder("gradle", "-p", projectFile.getParentFile().getAbsolutePath(), "fatJar");
            }
        }
        Process buildProcess = pb.start();
        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(buildProcess.getInputStream()));
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        reader.close();
        if (!buildProcess.waitFor(2, TimeUnit.MINUTES)) {
            System.out.println("Update process took too long");
            throw new RuntimeException("Update process took too long");
        }
        buildProcess.destroy();
        File targetFolder;
        if(Boot.isMaven) {
            targetFolder = new File(directory.getAbsoluteFile() + "/target");
        }
        else{
            targetFolder =new File(directory.getAbsoluteFile() + "/build/libs");
        }
        File[] files = targetFolder.listFiles((dir, name) -> name.endsWith(".jar"));
        if (files.length == 0) {
            throw new RuntimeException("Building project failed!");
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
        File directory;
        if(Boot.isMaven) {
            directory = new File(Config.PROJECT_BUILD_LOCATION + "/target");
        }
        else{
            directory =new File(Config.PROJECT_BUILD_LOCATION + "/build/libs");
        }
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".jar"));
        if (files.length > 0) {
            Files.move(files[0], currentProduction);
            return true;
        }
        return false;
    }
}