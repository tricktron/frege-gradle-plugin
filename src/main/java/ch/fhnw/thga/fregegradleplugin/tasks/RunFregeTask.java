package ch.fhnw.thga.fregegradleplugin.tasks;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;

import ch.fhnw.thga.fregegradleplugin.SharedTaskLogic;

public abstract class RunFregeTask extends DefaultTask {
    public static final Logger LOGGER = Logging.getLogger(SetupFregeTask.class);
    private final JavaExec javaExec;

    @InputFile
    public abstract RegularFileProperty getFregeCompilerJar();

    @InputDirectory
    public abstract DirectoryProperty getFregeOutputDir();

    @Input
    @Option(option = "mainModule",
           description = "The full name of the Frege module with a main function, e.g. 'my.mod.Name'")
    public abstract Property<String> getMainModule();

    @Input
    public abstract Property<String> getFregeDependencies();

    @Inject
    public RunFregeTask(ObjectFactory objectFactory) {
        javaExec = objectFactory.newInstance(JavaExec.class);
    }

    @TaskAction
    public void runFrege() {
        javaExec.getMainClass().set(getMainModule());
        javaExec.setClasspath(
            SharedTaskLogic.setupClasspath(
                getProject(),
                getFregeDependencies(),
                getFregeCompilerJar(),
                getFregeOutputDir())
        .get()).exec();
    }
}
