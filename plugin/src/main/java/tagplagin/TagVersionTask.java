package tagplagin;

import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;


public class TagVersionTask extends DefaultTask {

    public TagVersionTask() {
        setGroup("gitTagsHandle");
    }

    @TaskAction
    public void tagVersion() {
        System.out.println("tagVersion plugins starts");
        try {
            BufferedReader gitStatus = Util.executeShellCommand("git status");
            boolean unCommited = !gitStatus.lines().anyMatch(st -> st.contains("nothing to commit"));
            if (unCommited) {
                String number = Util.executeShellCommand("git describe --long --tags").readLine();
                number += ".uncommitted";
                System.out.println("There are uncommited files ");
                Logger logger = this.getLogger();
                logger.info(number);
            } else {
                String branch = Util.executeShellCommand("git symbolic-ref --short HEAD").readLine();
                String lastCommit = Util.executeShellCommand("git log --decorate=full").readLine();
                boolean hasntTag = !lastCommit.contains("tag: refs/tags/");
                if (!hasntTag && branch.equals("master") && (lastCommit.indexOf("refs/heads/", 25) != -1)) {
                    hasntTag = true;
                }
                if (hasntTag) {
                    BufferedReader versions = Util.executeShellCommand("git tag");
                    int[] lastVersion = Util.getLastVersion(versions.lines());
                    String version = "v";
                    switch (branch) {
                        case "dev", "qa" -> {
                            version = version + lastVersion[0] + "." + (++lastVersion[1]);
                        }
                        case "master" -> {
                            version = version + (++lastVersion[0]) + ".0";
                        }
                        case "stage" -> {
                            version = version + lastVersion[0] + "." + (++lastVersion[1]) + "-rc";
                        }
                        default -> {
                            version = version + lastVersion[0] + "." + (++lastVersion[1]) + "-SNAPSHOT";
                        }
                    }
                    Util.executeShellCommand("git tag " + version);
                    Util.executeShellCommand("git push origin " + version);
                }
            }
        } catch (IOException | ExecutionException e) {
            System.err.println("Error of git command execution");
        } catch (InterruptedException | TimeoutException e) {
            System.err.println("Error of git command execution");
        }
    }
}
