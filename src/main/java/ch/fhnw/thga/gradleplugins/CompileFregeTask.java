package ch.fhnw.thga.gradleplugins;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;

@CacheableTask
public abstract class CompileFregeTask extends DefaultTask {
    private static final String FREGE_FILES_GLOB_PATTERN = "**/*.fr";
    private final JavaExec javaExec;

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract RegularFileProperty getFregeCompilerJar();

    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getFregeMainSourceDir();

    @Input
    public abstract ListProperty<String> getFregeCompilerFlags();

    @Input
    public abstract Property<String> getFregeDependencies();

    @Input
    @Option(option = "compileItem",
           description = "The absolute path to the frege file or the module name"
    )
    public abstract Property<String> getFregeCompileItem();

    @OutputDirectory
    public abstract DirectoryProperty getFregeOutputDir();

    @Internal
    public final Provider<String> getFregeMainSourcePath() {
        return getFregeMainSourceDir().map(srcDir -> srcDir.getAsFile().getAbsolutePath());
    }

    @Internal
    public final Provider<List<String>> getSourcePathArg() {
        return getFregeMainSourcePath().map(srcPath -> List.of("-sp", srcPath));
    }

    @Internal
    public final Provider<List<String>> getCompileItems() {
        return getFregeCompileItem()
            .map(name ->
            {
                return name.isEmpty() ? getFregeSourceFiles().get()
                                      : List.of(name);
            });
    }

    @Internal
    public final Provider<List<String>> getFregeSourceFiles() {
        return getFregeMainSourceDir()
            .map(srcDir -> srcDir.getAsFileTree())
            .map(tree -> tree.matching(pattern -> pattern.include(FREGE_FILES_GLOB_PATTERN)))
            .map(tree -> tree.getFiles().stream()
            .map(file -> file.getAbsolutePath())
            .collect(Collectors.toList())
        );
    }

    @Internal
    public final Provider<List<String>> getDependencyArg() {
        return getFregeDependencies().map(depsClasspath -> {
            return depsClasspath.isEmpty() ? Collections.emptyList()
                                           : List.of("-fp", depsClasspath);
        });
    }

    @Inject
    public CompileFregeTask(ObjectFactory objectFactory) {
        javaExec = objectFactory.newInstance(JavaExec.class);
    }

    private List<String> buildCompilerArgsFromProperties(List<String> targetDirectoryArg)
    {
        return Stream.of(
            getDependencyArg().get(),
            getFregeCompilerFlags().get(),
            targetDirectoryArg,
            getSourcePathArg().get(),
            getCompileItems().get())
        .filter(lists -> !lists.isEmpty())
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
    }

    @TaskAction
    public void compileFrege() {
        List<String> targetDirectoryArg = List.of(
            "-d",
            getFregeOutputDir().getAsFile().get().getAbsolutePath());

        javaExec.setClasspath(getProject().files(getFregeCompilerJar()))
                .setArgs(buildCompilerArgsFromProperties(targetDirectoryArg)).exec();
    }
}