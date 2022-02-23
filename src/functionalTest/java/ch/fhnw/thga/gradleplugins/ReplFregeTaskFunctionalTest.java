package ch.fhnw.thga.gradleplugins;

import static ch.fhnw.thga.gradleplugins.FregeExtension.DEFAULT_RELATIVE_SOURCE_DIR;
import static ch.fhnw.thga.gradleplugins.FregePlugin.REPL_FREGE_TASK_NAME;
import static ch.fhnw.thga.gradleplugins.SharedFunctionalTestLogic.COMPLETION_FR;
import static ch.fhnw.thga.gradleplugins.SharedFunctionalTestLogic.MINIMAL_BUILD_FILE_CONFIG;
import static ch.fhnw.thga.gradleplugins.SharedFunctionalTestLogic.NEW_LINE;
import static ch.fhnw.thga.gradleplugins.SharedFunctionalTestLogic.assertFileDoesNotExist;
import static ch.fhnw.thga.gradleplugins.SharedFunctionalTestLogic.assertFileExists;
import static ch.fhnw.thga.gradleplugins.SharedFunctionalTestLogic.createFregeSection;
import static ch.fhnw.thga.gradleplugins.SharedFunctionalTestLogic.runAndFailGradleTask;
import static ch.fhnw.thga.gradleplugins.SharedFunctionalTestLogic.runGradleTask;
import static org.gradle.testkit.runner.TaskOutcome.FAILED;
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.gradle.api.Project;
import org.gradle.testkit.runner.BuildResult;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.IndicativeSentencesGeneration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ch.fhnw.thga.gradleplugins.fregeproject.FregeProjectBuilder;
import ch.fhnw.thga.gradleplugins.fregeproject.FregeSourceFile;

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
            assertTrue(result.getOutput().contains(
                Paths.get(COMPLETION_FR.getFregeModulePath()).normalize().toString())
            );
            assertFileDoesNotExist(
                testProjectDir,
                "build/classes/main/frege/ch/fhnw/thga/Completion.java"
            );
            assertFileDoesNotExist(
                testProjectDir,
                "build/classes/main/frege/ch/fhnw/thga/Completion.class"
            );
        }

        @Test
        void given_dependent_frege_files_with_command_line_repl_module_option(
            @TempDir File testProjectDir)
            throws Exception
        {
            String frobCode = String.join(
                NEW_LINE,
                "module ch.fhnw.thga.Frob where",
                NEW_LINE,
                NEW_LINE,
                "import ch.fhnw.thga.Completion (complete)",
                NEW_LINE,
                "frob i = complete $ i + i",
                NEW_LINE
            );
            FregeSourceFile frob_FR = new FregeSourceFile(
                String.format(
                    "%s/%s",
                    DEFAULT_RELATIVE_SOURCE_DIR,
                    "ch/fhnw/thga/Frob.fr"
                ),
                frobCode);
            Project project = FregeProjectBuilder
                .builder()
                .projectRoot(testProjectDir)
                .buildFile(MINIMAL_BUILD_FILE_CONFIG)
                .fregeSourceFiles(() -> Stream.of(COMPLETION_FR, frob_FR))
                .build();
            
            BuildResult result = runGradleTask(
                testProjectDir,
                REPL_FREGE_TASK_NAME,
                "--replModule=ch.fhnw.thga.Frob"
            );
            
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
            assertTrue(result.getOutput().contains(
                Paths.get(frob_FR.getFregeModulePath()).normalize().toString())
            );
            assertFileExists(
                testProjectDir,
                "build/classes/main/frege/ch/fhnw/thga/Completion.java"
            );
            assertFileExists(
                testProjectDir,
                "build/classes/main/frege/ch/fhnw/thga/Completion.class"
            );
            assertFileDoesNotExist(
                testProjectDir,
                "build/classes/main/frege/ch/fhnw/thga/Frob.java"
            );
            assertFileDoesNotExist(
                testProjectDir,
                "build/classes/main/frege/ch/fhnw/thga/Frob.class"
            );
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