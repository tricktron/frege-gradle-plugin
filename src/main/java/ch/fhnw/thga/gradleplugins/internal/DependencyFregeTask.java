package ch.fhnw.thga.gradleplugins.internal;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.TaskAction;

public abstract class DependencyFregeTask extends DefaultTask {
    @InputFiles
    public abstract ConfigurableFileCollection getClasspath();

    @TaskAction
    public void fregeDependencies() {
        System.out.println(getClasspath().getAsPath());
    }
}
