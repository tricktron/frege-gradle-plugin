package ch.fhnw.thga.gradleplugins;

import static ch.fhnw.thga.gradleplugins.FregeExtension.DEFAULT_RELATIVE_SOURCE_DIR;
import static ch.fhnw.thga.gradleplugins.FregePlugin.COMPILE_FREGE_TASK_NAME;
import static ch.fhnw.thga.gradleplugins.FregePlugin.INIT_FREGE_TASK_NAME;
import static ch.fhnw.thga.gradleplugins.SharedFunctionalTestLogic.MINIMAL_BUILD_FILE_CONFIG;
import static ch.fhnw.thga.gradleplugins.SharedFunctionalTestLogic.assertFileExists;
import static ch.fhnw.thga.gradleplugins.SharedFunctionalTestLogic.runGradleTask;
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.gradle.api.Project;
import org.gradle.testkit.runner.BuildResult;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.IndicativeSentencesGeneration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ch.fhnw.thga.gradleplugins.fregeproject.FregeProjectBuilder;
import ch.fhnw.thga.gradleplugins.fregeproject.ProjectRoot;

public class InitFregeTaskFunctionalTest
{
    private static ProjectRoot FREGE_PROJECT_BUILDER = FregeProjectBuilder.builder();

    @Nested
    @IndicativeSentencesGeneration(
        separator = " -> ",
        generator = DisplayNameGenerator.ReplaceUnderscores.class)
    class Init_frege_task_works {
        @Test
        void given_minimal_build_file_config(
            @TempDir File testProjectDir)
            throws Exception
        {
            Project project = FREGE_PROJECT_BUILDER
                .projectRoot(testProjectDir)
                .buildFile(MINIMAL_BUILD_FILE_CONFIG)
                .build();

            BuildResult result = runGradleTask(testProjectDir, INIT_FREGE_TASK_NAME);
    
            assertTrue(
                project
                .getTasks()
                .getByName(INIT_FREGE_TASK_NAME) instanceof InitFregeTask
            );
            assertEquals(
                SUCCESS,
                result.task(":" + INIT_FREGE_TASK_NAME).getOutcome()
            );
            assertFileExists(
                testProjectDir.toPath().resolve(DEFAULT_RELATIVE_SOURCE_DIR).toFile(),
                "examples/HelloFrege.fr"
            );

            BuildResult compileHelloFrege = runGradleTask(testProjectDir, COMPILE_FREGE_TASK_NAME);
            assertEquals(SUCCESS, compileHelloFrege.task(":" + COMPILE_FREGE_TASK_NAME).getOutcome());
        }

        @Test
        void given_custom_module_name_on_command_line(
            @TempDir File testProjectDir)
            throws Exception
        {
            Project project = FREGE_PROJECT_BUILDER
                .projectRoot(testProjectDir)
                .buildFile(MINIMAL_BUILD_FILE_CONFIG)
                .build();

            BuildResult result = runGradleTask(
                testProjectDir,
                INIT_FREGE_TASK_NAME,
                "--moduleName=ch.fhnw.thga.HelloFrege"
            );
    
            assertTrue(
                project
                .getTasks()
                .getByName(INIT_FREGE_TASK_NAME) instanceof InitFregeTask
            );
            assertEquals(
                SUCCESS,
                result.task(":" + INIT_FREGE_TASK_NAME).getOutcome()
            );
            assertFileExists(
                testProjectDir.toPath().resolve(DEFAULT_RELATIVE_SOURCE_DIR).toFile(),
                "ch/fhnw/thga/HelloFrege.fr"
            );
        }
    }
}
