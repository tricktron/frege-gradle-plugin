package ch.fhnw.thga.gradleplugins;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;

public abstract class ReplFregeTask extends DefaultTask {
    public static final String REPL_MAIN_CLASS = "frege.repl.FregeRepl";

    @InputFile
    public abstract RegularFileProperty getFregeCompilerJar();

    @Input
    public abstract Property<String> getFregeDependencies();

    @InputDirectory
    public abstract DirectoryProperty getFregeOutputDir();

    @TaskAction
    public void printStartFregeReplCommand() {
        System.out.println("Execute the following command to start the Frege Repl:");
        System.out.println(String.format(
            "java -cp %s %s",
            SharedTaskLogic.setupClasspath(
                getProject(),
                getFregeDependencies(),
                getFregeCompilerJar(),
                getFregeOutputDir())
            .get().getAsPath(),
            REPL_MAIN_CLASS));
    }
}
