package ch.fhnw.thga.gradleplugins;

import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;

public class FregePlugin implements Plugin<Project>
{
    public static final String SETUP_FREGE_TASK_NAME              = "setupFrege";
    public static final String COMPILE_FREGE_TASK_NAME            = "compileFrege";
    public static final String RUN_FREGE_TASK_NAME                = "runFrege";
    public static final String RUN_FREGE_ALT_TASK_NAME            = "fregeRun";
    public static final String TEST_FREGE_TASK_NAME               = "testFrege";
    public static final String REPL_FREGE_TASK_NAME               = "replFrege";
    public static final String INIT_FREGE_TASK_NAME               = "initFrege";
    public static final String FREGE_PLUGIN_ID                    = "ch.fhnw.thga.frege";
    public static final String FREGE_EXTENSION_NAME               = "frege";
    public static final String FREGE_CONFIGURATION_NAME           = FREGE_EXTENSION_NAME;
    public static final String HELLO_FREGE_DEFAULT_MODULE_NAME    = "examples.HelloFrege";
    public static final String FREGE_TEST_MODULE_NAME             = "frege.tools.Quick";
    public static final String FREGE_TEST_DEFAULT_ARGS            = "-v";
    public static final String RUN_FREGE_TASK_MODULE_OVERRIDE     = "class_name"; // Used by the intellij-frege plugin to run arbitary files

    @Override
    public void apply(Project project)
    {
        NamedDomainObjectProvider<Configuration> fregeConfiguration = project
            .getConfigurations()
            .register(
                FREGE_CONFIGURATION_NAME,
                config ->
                    {
                        config.setCanBeResolved(true);
                        config.setCanBeConsumed(true);
                    }
            );
        Configuration implementation = project.getConfigurations().maybeCreate("implementation");
        NamedDomainObjectProvider<Configuration> fregeResolved = project.getConfigurations().register("fregeResolved");
        implementation.extendsFrom(fregeConfiguration.get());
        implementation.extendsFrom(fregeResolved.get());

        FregeExtension extension = project
            .getExtensions()
            .create(
            FREGE_EXTENSION_NAME,
            FregeExtension.class);

        project.getPlugins().apply(BasePlugin.class);

        project.getTasks().register(
            INIT_FREGE_TASK_NAME,
            InitFregeTask.class,
            task ->
            {
                task.getFregeMainSourceDir().set(extension.getMainSourceDir());
                task.getFregeModuleName().set(HELLO_FREGE_DEFAULT_MODULE_NAME);
            }
        );

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
                    task.getFregeDependencies().set(fregeConfiguration.get().getAsPath());
                }
            );

        TaskProvider<RunFregeTask> runTask = project.getTasks().register(
                RUN_FREGE_TASK_NAME,
                RunFregeTask.class,
                task ->
                {
                    if (project.hasProperty(RUN_FREGE_TASK_MODULE_OVERRIDE))
                        task.getMainModule().set(project.findProperty(RUN_FREGE_TASK_MODULE_OVERRIDE).toString());
                    else
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
                    task.getFregeDependencies().set(fregeConfiguration.get().getAsPath());
                }
        );

        project.getTasks().register(RUN_FREGE_ALT_TASK_NAME, task -> {
            task.dependsOn(runTask);
        });

        project.getTasks().register(
            TEST_FREGE_TASK_NAME,
            TestFregeTask.class,
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
                    task.getFregeDependencies().set(fregeConfiguration.get().getAsPath());
                    task.getFregeArgs().set(FREGE_TEST_DEFAULT_ARGS);
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
                task.getFregeDependencies().set(fregeConfiguration.get().getAsPath());
                task.getFregeMainSourceDir().set(extension.getMainSourceDir());
            }
        );
        project.afterEvaluate(ignored -> {
            project.getDependencies().add("implementation", setupFregeCompilerTask.map(SetupFregeTask::getFregeCompilerOutputPath).map(project::files));

            JavaPluginExtension javaPluginExtension = project.getExtensions().findByType(JavaPluginExtension.class);
            if(javaPluginExtension != null) {
                SourceSet main = javaPluginExtension.getSourceSets().findByName("main");
                if(main != null) {
                    main.getOutput().dir(compileFregeTask.get().getOutputs());
                }
            }
            Task classes = project.getTasks().findByName("classes");
            if(classes != null) {
                classes.dependsOn(compileFregeTask);
            }
        });
    }
}
