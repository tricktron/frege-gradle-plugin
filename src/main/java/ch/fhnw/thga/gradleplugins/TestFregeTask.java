package ch.fhnw.thga.gradleplugins;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;
import org.gradle.api.tasks.Internal;
import org.gradle.api.provider.Provider;

public abstract class TestFregeTask extends DefaultTask {
    private static final String TEST_MAIN_CLASS = "frege.tools.Quick";
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
    @Option(option = "args",
           description = "optional args passed to frege")
    public abstract Property<String> getFregeArgs();

   @Internal
   final Provider<String> getAllArgs()
   {
       return getFregeArgs()
        .map(args -> String.format("%s %s", args, getMainModule().get()));
   } 

    @Input
    public abstract Property<String> getFregeDependencies();

    @Inject
    public TestFregeTask(ObjectFactory objectFactory) {
        javaExec = objectFactory.newInstance(JavaExec.class);
    }

    @TaskAction
    public void runFrege() {
        javaExec.getMainClass().set(TEST_MAIN_CLASS);
        javaExec
            .setClasspath(
                SharedTaskLogic.setupClasspath(
                    getProject(),
                    getFregeDependencies(),
                    getFregeCompilerJar(),
                    getFregeOutputDir())
                .get()
            )
            .setArgsString(getAllArgs().get())
            .exec();
    }
}