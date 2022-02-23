package ch.fhnw.thga.gradleplugins;

import static ch.fhnw.thga.gradleplugins.SharedTaskLogic.extractClassNameFromFregeModuleName;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;

public abstract class ReplFregeTask extends DefaultTask {
    public static final String REPL_MAIN_CLASS = "frege.repl.FregeRepl";

    @InputFile
    public abstract RegularFileProperty getFregeCompilerJar();

    @Input
    public abstract Property<String> getFregeDependencies();

    @InputDirectory
    public abstract DirectoryProperty getFregeOutputDir();

    @Input
    @Option(option     = "replModule",
           description = "The full name of the module which you want to load into the repl, e.g. 'my.mod.Name'")
    public abstract Property<String> getReplModule();

    @Internal
    public final Provider<String> getReplClassName()
    {
        return getReplModule()
            .map(replSource -> extractClassNameFromFregeModuleName(replSource));
    }

    @Internal
    public final Provider<FileTree> getClasspathWithoutReplClassFile()
    {
        return getFregeOutputDir()
            .map(outDir -> outDir.getAsFileTree())
            .map(tree   -> tree.matching(pattern -> pattern.exclude("**/*.java")))
            .map(tree   -> tree.matching(pattern -> pattern.exclude(
                String.format(
                    "**/%s.class",
                    getReplClassName().get()
                )
            )));
    }

    @TaskAction
    public void printStartFregeReplCommand() {
        System.out.println("Execute the following command to start the Frege Repl:");
        System.out.println(String.format(
            "java -cp %s %s",
            SharedTaskLogic.setupClasspath(
                getProject(),
                getFregeDependencies(),
                getFregeCompilerJar(),
                getClasspathWithoutReplClassFile())
            .get().getAsPath(),
            REPL_MAIN_CLASS));
    }
}
