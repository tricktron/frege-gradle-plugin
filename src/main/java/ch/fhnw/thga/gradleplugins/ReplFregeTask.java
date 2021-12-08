package ch.fhnw.thga.gradleplugins;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

public abstract class ReplFregeTask extends DefaultTask {
    public static final Logger LOGGER = Logging.getLogger(SetupFregeTask.class);

    @InputFile
    public abstract RegularFileProperty getFregeCompilerJar();

    @Input
    public abstract Property<String> getFregeDependencies();

    @Internal
    public final Provider<FileCollection> getClasspath() {
        return getFregeDependencies().map(depsClasspath -> {
            return depsClasspath.isEmpty() ? getProject().files(getFregeCompilerJar())
                    : getProject().files(getFregeCompilerJar(), depsClasspath);
        });
    }

    @TaskAction
    public void fregeReplDependencies() {
        System.out.println(getClasspath().get().getAsPath());
    }
}
