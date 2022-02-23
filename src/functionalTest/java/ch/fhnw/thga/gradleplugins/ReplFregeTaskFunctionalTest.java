package ch.fhnw.thga.gradleplugins;

import static ch.fhnw.thga.gradleplugins.FregePlugin.REPL_FREGE_TASK_NAME;
import static ch.fhnw.thga.gradleplugins.SharedFunctionalTestLogic.COMPLETION_FR;
import static ch.fhnw.thga.gradleplugins.SharedFunctionalTestLogic.MINIMAL_BUILD_FILE_CONFIG;
import static ch.fhnw.thga.gradleplugins.SharedFunctionalTestLogic.createFregeSection;
import static ch.fhnw.thga.gradleplugins.SharedFunctionalTestLogic.runAndFailGradleTask;
import static ch.fhnw.thga.gradleplugins.SharedFunctionalTestLogic.runGradleTask;
import static org.gradle.testkit.runner.TaskOutcome.FAILED;
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.stream.Stream;

import org.gradle.api.Project;
import org.gradle.testkit.runner.BuildResult;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.IndicativeSentencesGeneration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ch.fhnw.thga.gradleplugins.fregeproject.FregeProjectBuilder;

public class ReplFregeTaskFunctionalTest
{
    @Nested
    @IndicativeSentencesGeneration(
        generator = DisplayNameGenerator.ReplaceUnderscores.class)
    class Repl_frege_task_works
    {
        @Test
        void given_minimal_build_file_config_with_repl_module(
            @TempDir File testProjectDir)
            throws Exception
        {
            String replModuleConfig = createFregeSection(
                FregeDTOBuilder
                .builder()
                .version("'3.25.84'")
                .release("'3.25alpha'")
                .replModule("'ch.fhnw.thga.Completion'")
                .build()
            );
            Project project = FregeProjectBuilder
                .builder()
                .projectRoot(testProjectDir)
                .buildFile(replModuleConfig)
                .fregeSourceFiles(() -> Stream.of(COMPLETION_FR))
                .build();
                                                                                         
            BuildResult result = runGradleTask(testProjectDir, REPL_FREGE_TASK_NAME);
                                                                                         
            assertTrue(
                project
                .getTasks()
                .getByName(REPL_FREGE_TASK_NAME)
                instanceof ReplFregeTask);
            assertEquals(
                SUCCESS,
                result.task(":" + REPL_FREGE_TASK_NAME).getOutcome());
            assertTrue(result.getOutput().contains("java -cp"));
            assertTrue(result.getOutput().contains("frege3.25.84.jar"));
            assertFalse(result.getOutput().contains("Completion.class"));
        }
    }

    @Nested
    @IndicativeSentencesGeneration(
        generator = DisplayNameGenerator.ReplaceUnderscores.class)
    class Repl_frege_task_fails
    {
        @Test
        void given_minimal_build_file_config_without_repl_module(
            @TempDir File testProjectDir)
            throws Exception
        {
            Project project = FregeProjectBuilder
                .builder()
                .projectRoot(testProjectDir)
                .buildFile(MINIMAL_BUILD_FILE_CONFIG)
                .fregeSourceFiles(() -> Stream.of(COMPLETION_FR))
                .build();
                                                                                         
            BuildResult result = runAndFailGradleTask(testProjectDir, REPL_FREGE_TASK_NAME);

            assertTrue(
                project
                .getTasks()
                .getByName(REPL_FREGE_TASK_NAME)
                instanceof ReplFregeTask
            );
            assertEquals(
                FAILED,
                result.task(":" + REPL_FREGE_TASK_NAME).getOutcome()
            );
        }
    }
}