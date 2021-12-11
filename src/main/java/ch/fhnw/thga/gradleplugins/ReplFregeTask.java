package ch.fhnw.thga.gradleplugins;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.TaskAction;

public abstract class ReplFregeTask extends DefaultTask {
    public static final String REPL_MAIN_CLASS = "frege.repl.FregeRepl";

    @InputFiles
    public abstract ConfigurableFileCollection getFregeClasspath();

    @TaskAction
    public void printStartFregeReplCommand() {
        System.out.println("Execute the following command to start the Frege Repl:");
        System.out.println(String.format("java -cp %s %s", getFregeClasspath().getAsPath(), REPL_MAIN_CLASS));
    }
}
