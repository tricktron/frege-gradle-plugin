package ch.fhnw.thga.gradleplugins;

import static ch.fhnw.thga.gradleplugins.FregePlugin.COMPILE_FREGE_TASK_NAME;
import static ch.fhnw.thga.gradleplugins.FregePlugin.SETUP_FREGE_TASK_NAME;
import static ch.fhnw.thga.gradleplugins.FregePlugin.FREGE_EXTENSION_NAME;
import static ch.fhnw.thga.gradleplugins.FregePlugin.FREGE_PLUGIN_ID;
import static ch.fhnw.thga.gradleplugins.FregePlugin.REPL_FREGE_TASK_NAME;
import static ch.fhnw.thga.gradleplugins.FregePlugin.RUN_FREGE_TASK_NAME;
import static ch.fhnw.thga.gradleplugins.FregeExtension.DEFAULT_RELATIVE_SOURCE_DIR;
import static ch.fhnw.thga.gradleplugins.GradleBuildFileConversionTest.createPluginsSection;
import static ch.fhnw.thga.gradleplugins.SharedFunctionalTestLogic.createFregeSection;
import static ch.fhnw.thga.gradleplugins.SharedFunctionalTestLogic.writeToFile;
import static ch.fhnw.thga.gradleplugins.SharedFunctionalTestLogic.createFregeGradleProject;
import static ch.fhnw.thga.gradleplugins.SharedFunctionalTestLogic.runGradleTask;
import static org.gradle.testkit.runner.TaskOutcome.FAILED;
import static org.gradle.testkit.runner.TaskOutcome.FROM_CACHE;
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.stream.Stream;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.IndicativeSentencesGeneration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class FregePluginFunctionalTest
{
    /*@Nested
    @IndicativeSentencesGeneration(separator = " -> ", generator = DisplayNameGenerator.ReplaceUnderscores.class)
    class Run_frege_task_works {
            @Test
            void given_frege_file_with_main_function_and_main_module_config() throws Exception {
                    String fregeCode = String.join(NEW_LINE, "module ch.fhnw.thga.Main where", NEW_LINE, NEW_LINE,
                                    "  main = do", NEW_LINE, "    println \"Frege rocks\"", NEW_LINE);
                    String mainFr = "Main.fr";
                    String buildFileConfig = createFregeSection(
                                    FREGE_BUILDER.version("'3.25.84'").release("'3.25alpha'")
                                                    .mainModule("'ch.fhnw.thga.Main'").build());
                    setupDefaultFregeProjectStructure(fregeCode, mainFr, buildFileConfig);

                    BuildResult result = runGradleTask(RUN_FREGE_TASK_NAME);
                    assertTrue(project.getTasks().getByName(RUN_FREGE_TASK_NAME) instanceof RunFregeTask);
                    assertEquals(SUCCESS, result.task(":" + RUN_FREGE_TASK_NAME).getOutcome());
                    assertTrue(result.getOutput().contains("Frege rocks"));
            }

            @Test
            void given_frege_file_without_main_function() throws Exception {
                    String completionFr = "Completion.fr";
                    String buildFileConfig = createFregeSection(
                                    FREGE_BUILDER.version("'3.25.84'").release("'3.25alpha'")
                                                    .mainModule("'ch.fhnw.thga.Completion'").build());
                    setupDefaultFregeProjectStructure(FREGE_COMPLETION_MODULE_CODE, completionFr, buildFileConfig);

                    BuildResult result = runAndFailGradleTask(RUN_FREGE_TASK_NAME);
                    assertTrue(project.getTasks().getByName(RUN_FREGE_TASK_NAME) instanceof RunFregeTask);
                    assertEquals(FAILED, result.task(":" + RUN_FREGE_TASK_NAME).getOutcome());
                    assertTrue(result.getOutput().contains("Main method not found"));
            }

            @Test
            void given_frege_file_with_main_function_and_main_module_command_line_option() throws Exception {
                    String fregeCode = String.join(NEW_LINE, "module ch.fhnw.thga.Main where", NEW_LINE, NEW_LINE,
                                    "  main = do", NEW_LINE, "    println \"Frege rocks\"", NEW_LINE);
                    String mainFr = "Main.fr";
                    String buildFileConfig = createFregeSection(
                                    FREGE_BUILDER.version("'3.25.84'").release("'3.25alpha'").build());
                    setupDefaultFregeProjectStructure(fregeCode, mainFr, buildFileConfig);

                    BuildResult result = runGradleTask(RUN_FREGE_TASK_NAME, "--mainModule=ch.fhnw.thga.Main");
                    assertTrue(project.getTasks().getByName(RUN_FREGE_TASK_NAME) instanceof RunFregeTask);
                    assertEquals(SUCCESS, result.task(":" + RUN_FREGE_TASK_NAME).getOutcome());
                    assertTrue(result.getOutput().contains("Frege rocks"));
            }
    }


    @Nested
    @IndicativeSentencesGeneration(
        separator = " -> ",
        generator = DisplayNameGenerator.ReplaceUnderscores.class)
    class Repl_frege_task_works
    {
        @Test
        void given_minimal_build_file_config_with_replModule() throws Exception
        {
            String completionFr            = "Completion.fr";
            String minimalReplModuleConfig = createFregeSection(
                FREGE_BUILDER
                .version("'3.25.84'")
                .release("'3.25alpha'")
                .replSource(String.format("'ch.fhnw.thga.Completion'"))
                .build());
            setupDefaultFregeProjectStructure(
                FREGE_COMPLETION_MODULE_CODE,
                completionFr,
                minimalReplModuleConfig);

            BuildResult result = runGradleTask(REPL_FREGE_TASK_NAME);

            assertTrue(
                project.getTasks().getByName(REPL_FREGE_TASK_NAME)
                instanceof ReplFregeTask);
            assertEquals(SUCCESS, result.task(":" + REPL_FREGE_TASK_NAME).getOutcome());
            assertTrue(result.getOutput().contains("java -cp"));
            assertTrue(result.getOutput().contains("frege3.25.84.jar"));
            assertTrue(result.getOutput().contains("Completion.java"));
        }
    }

    @Nested
    @IndicativeSentencesGeneration(
        separator = " -> ",
        generator = DisplayNameGenerator.ReplaceUnderscores.class)
    class Repl_frege_task_fails
    {
        @Test
        void given_minimal_build_file_config_without_repl_module() throws Exception
        {
            String completionFr           = "Completion.fr";
            String minimalBuildFileConfig = createFregeSection(
                FREGE_BUILDER
                .version("'3.25.84'")
                .release("'3.25alpha'")
                .build());
            setupDefaultFregeProjectStructure(
                FREGE_COMPLETION_MODULE_CODE,
                completionFr,
                minimalBuildFileConfig);

            BuildResult result = runAndFailGradleTask(REPL_FREGE_TASK_NAME);

            assertTrue(
                project.getTasks().getByName(REPL_FREGE_TASK_NAME)
                instanceof ReplFregeTask);
            assertEquals(FAILED, result.task(":" + COMPILE_FREGE_TASK_NAME).getOutcome());
        }
    }*/
}