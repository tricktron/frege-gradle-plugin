package ch.fhnw.thga.gradleplugins;

import ch.fhnw.thga.gradleplugins.internal.DependencyFregeTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.TaskProvider;

public class FregePlugin implements Plugin<Project> {
    public static final String SETUP_FREGE_TASK_NAME = "setupFrege";
    public static final String COMPILE_FREGE_TASK_NAME = "compileFrege";
    public static final String RUN_FREGE_TASK_NAME = "runFrege";
    public static final String RUN_FREGE_TASK_NAME_ALTERNATIVE = "fregeRun";
    public static final String RUN_FREGE_TASK_MODULE_OVERRIDE = "class_name"; // Used by intellij-frege to run arbitary files
    public static final String REPL_FREGE_TASK_NAME = "replFrege";
    public static final String DEPS_FREGE_TASK_NAME = "depsFrege";
    public static final String FREGE_PLUGIN_ID = "ch.fhnw.thga.frege";
    public static final String FREGE_EXTENSION_NAME = "frege";
    public static final String FREGE_DEPENDENCY_SCOPE = "implementation";
    public static final String FREGE_RESOLVED_SCOPE = "fregeResolved";

    @Override
    public void apply(Project project) {
        Configuration implementation = project.getConfigurations().maybeCreate(FREGE_DEPENDENCY_SCOPE);
        Configuration resolvedImplementation = project.getConfigurations().maybeCreate("fregeResolvedImplementation");
        resolvedImplementation.extendsFrom(implementation);
        Configuration fregeResolved = project.getConfigurations().create(FREGE_RESOLVED_SCOPE);
        implementation.extendsFrom(fregeResolved);

        FregeExtension extension     = project
                                       .getExtensions()
                                       .create(
                                       FREGE_EXTENSION_NAME,
                                       FregeExtension.class);

        TaskProvider<SetupFregeTask> setupFregeCompilerTask =
            project.getTasks().register(
                SETUP_FREGE_TASK_NAME,
                SetupFregeTask.class,
                task ->
                {
                    task.getVersion().set(extension.getVersion());
                    task.getRelease().set(extension.getRelease());
                    task.getDownloadDir().set(extension.getCompilerDownloadDir());
                });

        TaskProvider<CompileFregeTask> compileFregeTask =
            project.getTasks().register(
                COMPILE_FREGE_TASK_NAME,
                CompileFregeTask.class,
                task ->
                {
                    task.dependsOn(setupFregeCompilerTask);
                    task.getFregeCompilerJar().set(
                        setupFregeCompilerTask.get().getFregeCompilerOutputPath());
                    task.getFregeMainSourceDir().set(extension.getMainSourceDir());
                    task.getFregeOutputDir().set(extension.getOutputDir());
                    task.getFregeCompilerFlags().set(extension.getCompilerFlags());
                    task.getFregeDependencies().set(resolvedImplementation.getAsPath());
                    task.getReplSource().set("");
                }
            );

        TaskProvider<RunFregeTask> runFregeTask = project.getTasks().register(
                RUN_FREGE_TASK_NAME,
                RunFregeTask.class,
                task ->
                {
                    task.dependsOn(compileFregeTask);
                    task.getFregeCompilerJar().set(
                            setupFregeCompilerTask.get().getFregeCompilerOutputPath());
                    task.getFregeOutputDir().set(extension.getOutputDir());
                    if(project.hasProperty(RUN_FREGE_TASK_MODULE_OVERRIDE)) {
                        task.getMainModule().set(project.findProperty(RUN_FREGE_TASK_MODULE_OVERRIDE).toString());
                    }else {
                        task.getMainModule().set(extension.getMainModule());
                    }
                    task.getFregeDependencies().set(resolvedImplementation.getAsPath());
                }
        );

        project.getTasks().register(
                RUN_FREGE_TASK_NAME_ALTERNATIVE,
                task -> {
                    task.dependsOn(runFregeTask);
                }
        );

        project.getTasks().register(
            DEPS_FREGE_TASK_NAME,
            DependencyFregeTask.class,
            task ->
            {
                task.dependsOn(compileFregeTask.map(
                    compileTask ->
                    {
                        compileTask.getReplSource().set(task.getReplSource());
                        return compileTask;
                    })
                .get());
                task.dependsOn(compileFregeTask);
                task.getFregeCompilerJar().set(
                    setupFregeCompilerTask.get().getFregeCompilerOutputPath());
                task.getFregeOutputDir().set(extension.getOutputDir());
                task.getFregeDependencies().set(resolvedImplementation.getAsPath());
            }
        );

        project.getTasks().register(
            REPL_FREGE_TASK_NAME,
            ReplFregeTask.class,
            task ->
            {
                task.getReplSource().set(extension.getReplSource());
                task.dependsOn(compileFregeTask.map(
                    compileTask ->
                    {
                        compileTask.getReplSource().set(task.getReplSource());
                        return compileTask;
                    })
                .get());
                task.getFregeCompilerJar().set(
                    setupFregeCompilerTask.get().getFregeCompilerOutputPath());
                task.getFregeOutputDir().set(extension.getOutputDir());
                task.getFregeDependencies().set(resolvedImplementation.getAsPath());
            }
        );


        project.afterEvaluate(ignored -> {
            project.getDependencies().add(FREGE_RESOLVED_SCOPE, setupFregeCompilerTask.map(SetupFregeTask::getFregeCompilerOutputPath).map(project::files));
        });
    }
}
