package ch.fhnw.thga.gradleplugins;

import org.gradle.api.Plugin;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.tasks.TaskProvider;

public class FregePlugin implements Plugin<Project>
{
    public static final String SETUP_FREGE_TASK_NAME      = "setupFrege";
    public static final String COMPILE_FREGE_TASK_NAME    = "compileFrege";
    public static final String RUN_FREGE_TASK_NAME        = "runFrege";
    public static final String REPL_FREGE_TASK_NAME       = "replFrege";
    public static final String DEPS_FREGE_TASK_NAME       = "depsFrege";
    public static final String FREGE_PLUGIN_ID            = "ch.fhnw.thga.frege";
    public static final String FREGE_EXTENSION_NAME       = "frege";
    public static final String FREGE_IMPLEMENTATION_SCOPE = "implementation";

    @Override
    public void apply(Project project) {
        Configuration implementation = project
                                       .getConfigurations()
                                       .create(FREGE_IMPLEMENTATION_SCOPE);

        FregeExtension extension     = project
                                       .getExtensions()
                                       .create(
                                       FREGE_EXTENSION_NAME,
                                       FregeExtension.class);
        
        project.getPlugins().apply(BasePlugin.class);

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
                    task.getFregeDependencies().set(implementation.getAsPath());
                }
            );

        project.getTasks().register(
            RUN_FREGE_TASK_NAME,
            RunFregeTask.class,
            task ->
            {
                task.getMainModule().set(extension.getMainModule());
                task.dependsOn(compileFregeTask.map(
                    compileTask ->
                    {
                        compileTask.getFregeCompileItem().set(task.getMainModule());
                        return compileTask;
                    }
                ).get());
                task.getFregeCompilerJar().set(
                    setupFregeCompilerTask.get().getFregeCompilerOutputPath());
                task.getFregeOutputDir().set(extension.getOutputDir());
                task.getFregeDependencies().set(implementation.getAsPath());
            }
        );

        project.getTasks().register(
            REPL_FREGE_TASK_NAME,
            ReplFregeTask.class,
            task ->
            {
                task.getReplModule().set(extension.getReplModule());
                task.dependsOn(compileFregeTask.map(
                    compileTask ->
                    {
                        compileTask.getFregeCompileItem().set(extension.getReplModule());
                        compileTask.getLogging().captureStandardOutput(LogLevel.LIFECYCLE);
                        compileTask.getLogging().captureStandardError(LogLevel.LIFECYCLE);
                        return compileTask;
                    }
                ).get());
                task.getFregeCompilerJar().set(
                    setupFregeCompilerTask.get().getFregeCompilerOutputPath());
                task.getFregeOutputDir().set(extension.getOutputDir());
                task.getFregeDependencies().set(implementation.getAsPath());
                task.getFregeMainSourceDir().set(extension.getMainSourceDir());
            }
        );
    }
}
