package ch.fhnw.thga.gradleplugins;

import static ch.fhnw.thga.gradleplugins.SharedTaskLogic.extractClassNameFromFregeModuleName;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;

public abstract class ReplFregeTask extends DefaultTask 
{
    private static final Logger LOGGER             = Logging.getLogger(ReplFregeTask.class);
    public static final String REPL_MAIN_CLASS     = "frege.repl.FregeRepl";
    private static final String REPL_START_MESSAGE = 
    "Execute the following command to start the Frege Repl and load the Frege module:";

    @InputFile
    public abstract RegularFileProperty getFregeCompilerJar();

    @Input
    public abstract Property<String> getFregeDependencies();

    @InputDirectory
    public abstract DirectoryProperty getFregeOutputDir();

    @InputDirectory
    public abstract DirectoryProperty getFregeMainSourceDir();

    @Input
    @Option
    (
        option      = "replModule",
        description = "The full name of the module which you want to load into the repl, e.g. 'my.mod.Name'"
    )
    public abstract Property<String> getReplModule();

    @Internal
    public final Provider<String> getReplClassName()
    {
        return getReplModule()
            .map(replSource -> extractClassNameFromFregeModuleName(replSource));
    }

    private final Provider<FileTree> filterFilesBySuffix
        (DirectoryProperty root, String suffix)
    {
        return root
        .zip
        (
            getReplClassName(), 
            (rootDir, replClassname) ->
                rootDir
                .getAsFileTree()
                .matching
                (pattern -> pattern.include(String.format("**/%s.%s", replClassname, suffix)))
        ); 
    }

    @Internal
    public final Provider<FileTree> getReplClassFiles()
    {
        return filterFilesBySuffix(getFregeOutputDir(), "class");
    }

    @Internal
    public final Provider<FileTree> getReplJavaFiles()
    {
        return filterFilesBySuffix(getFregeOutputDir(), "java");
    }

    @Internal
    public final Provider<FileTree> getReplFregeFile()
    {
        return filterFilesBySuffix(getFregeMainSourceDir(), "fr");
    }

    @TaskAction
    public void printStartFregeReplCommand()
    {
        getProject().delete(getReplJavaFiles());
        getProject().delete(getReplClassFiles());
        LOGGER.lifecycle(REPL_START_MESSAGE);
        LOGGER.quiet
        (
            String.format
            (
                "(echo :l %s && cat) | java -cp %s %s",
                getReplFregeFile().get().getAsPath(),
                SharedTaskLogic.setupClasspath
                (
                    getProject(),
                    getFregeDependencies(),
                    getFregeCompilerJar(),
                    getFregeOutputDir()
                )
                .get().getAsPath(),
                REPL_MAIN_CLASS
            )
        );
    }
q
