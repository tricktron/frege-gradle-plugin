package ch.fhnw.thga.gradleplugins;

import static ch.fhnw.thga.gradleplugins.FregeExtension.DEFAULT_RELATIVE_SOURCE_DIR;
import static ch.fhnw.thga.gradleplugins.FregePlugin.RUN_FREGE_TASK_NAME;
import static ch.fhnw.thga.gradleplugins.SharedFunctionalTestLogic.NEW_LINE;
import static ch.fhnw.thga.gradleplugins.SharedFunctionalTestLogic.createFregeSection;
import static ch.fhnw.thga.gradleplugins.SharedFunctionalTestLogic.runAndFailGradleTask;
import static ch.fhnw.thga.gradleplugins.SharedFunctionalTestLogic.runGradleTask;
import static ch.fhnw.thga.gradleplugins.SharedFunctionalTestLogic.MINIMAL_BUILD_FILE_CONFIG;
import static ch.fhnw.thga.gradleplugins.SharedFunctionalTestLogic.COMPLETION_FR;
import static ch.fhnw.thga.gradleplugins.SharedFunctionalTestLogic.assertFileDoesNotExist;
import static org.gradle.testkit.runner.TaskOutcome.FAILED;
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import ch.fhnw.thga.gradleplugins.fregeproject.FregeSourceFile;

public class RunFregeTaskFunctionalTest
{
    private static final FregeSourceFile MAIN_FR = new FregeSourceFile(
        String.format(
            "%s/%s",
            DEFAULT_RELATIVE_SOURCE_DIR,
            "ch/fhnw/thga/Main.fr"
        ),
        String.join(
            NEW_LINE,
            "module ch.fhnw.thga.Main where",
            NEW_LINE,
            NEW_LINE,
            "  main = do",
            NEW_LINE,
            "    println \"Frege rocks\"",
            NEW_LINE
        )
    );
    @Nested
    @IndicativeSentencesGeneration(
        separator = " -> ",
        generator = DisplayNameGenerator.ReplaceUnderscores.class
    )
    class Run_frege_task_works
    {
        @Test
        void given_frege_file_with_main_function_and_main_module_config(
            @TempDir File testProjectDir)
            throws Exception 
        {
            String mainBuildConfig = createFregeSection(
                FregeDTOBuilder
                .builder()
                .version("'3.25.84'")
                .release("'3.25alpha'")
                .mainModule("'ch.fhnw.thga.Main'")
                .build()
            );
            Project project = FregeProjectBuilder
                .builder()
                .projectRoot(testProjectDir)
                .buildFile(mainBuildConfig)
                .fregeSourceFiles(() -> Stream.of(MAIN_FR))
                .build();
            
            BuildResult result = runGradleTask(testProjectDir, RUN_FREGE_TASK_NAME);
            
            assertTrue(
                project
                .getTasks()
                .getByName(RUN_FREGE_TASK_NAME) 
                instanceof RunFregeTask
            );
            assertEquals(
                SUCCESS,
                result.task(":" + RUN_FREGE_TASK_NAME).getOutcome()
            );
            assertTrue(result.getOutput().contains("Frege rocks"));
        }

        @Test
        void given_frege_file_with_main_function_and_main_module_command_line_option(
            @TempDir File testProjectDir)
            throws Exception
        {
            Project project = FregeProjectBuilder
                .builder()
                .projectRoot(testProjectDir)
                .buildFile(MINIMAL_BUILD_FILE_CONFIG)
                .fregeSourceFiles(() -> Stream.of(MAIN_FR))
                .build();
            
            BuildResult result = runGradleTask(
                testProjectDir,
                RUN_FREGE_TASK_NAME,
                "--mainModule=ch.fhnw.thga.Main");
            
            assertTrue(
                project
                .getTasks()
                .getByName(RUN_FREGE_TASK_NAME) 
                instanceof RunFregeTask
            );
            assertEquals(
                SUCCESS,
                result.task(":" + RUN_FREGE_TASK_NAME).getOutcome()
            );
            assertTrue(result.getOutput().contains("Frege rocks"));
        }

        @Test
        void given_two_frege_files_then_only_the_specified_main_module_is_compiled(
            @TempDir File testProjectDir)
            throws Exception
        {
            Project project = FregeProjectBuilder
                .builder()
                .projectRoot(testProjectDir)
                .buildFile(MINIMAL_BUILD_FILE_CONFIG)
                .fregeSourceFiles(() -> Stream.of(MAIN_FR, COMPLETION_FR))
                .build();
            
            BuildResult result = runGradleTask(
                testProjectDir,
                RUN_FREGE_TASK_NAME,
                "--mainModule=ch.fhnw.thga.Main");
            
            assertTrue(
                project
                .getTasks()
                .getByName(RUN_FREGE_TASK_NAME) 
                instanceof RunFregeTask
            );
            assertEquals(
                SUCCESS,
                result.task(":" + RUN_FREGE_TASK_NAME).getOutcome()
            );
            assertTrue(result.getOutput().contains("Frege rocks"));
            assertFileDoesNotExist(
                testProjectDir,
                "build/classes/main/frege/ch/fhnw/thga/Completion.java"
            );
            assertFileDoesNotExist(
                testProjectDir,
                "build/classes/main/frege/ch/fhnw/thga/Completion.class"
            );
        }
    }
    
    @Nested
    @IndicativeSentencesGeneration(
        separator = " -> ",
        generator = DisplayNameGenerator.ReplaceUnderscores.class
    )
    class Run_frege_task_fails
    {
        @Test
        void given_frege_file_without_main_function(
            @TempDir File testProjectDir)
            throws Exception
        {
            String mainBuildConfig = createFregeSection(
                FregeDTOBuilder
                .builder()
                .version("'3.25.84'")
                .release("'3.25alpha'")
                .mainModule("'ch.fhnw.thga.Main'")
                .build()
            );
            Project project = FregeProjectBuilder
                .builder()
                .projectRoot(testProjectDir)
                .buildFile(mainBuildConfig)
                .fregeSourceFiles(() -> Stream.of(new FregeSourceFile(
                    String.format(
                        "%s/%s",
                        DEFAULT_RELATIVE_SOURCE_DIR,
                        "ch/fhnw/thga/Main.fr"
                    ),
                    String.join(
                        NEW_LINE,
                        "module ch.fhnw.thga.Main where",
                        NEW_LINE,
                        NEW_LINE,
                        "  add a b = a + b",
                        NEW_LINE
                    )
                )))
                .build();

            BuildResult result = runAndFailGradleTask(
                testProjectDir,
                RUN_FREGE_TASK_NAME
            );

            assertTrue(
                project
                .getTasks()
                .getByName(RUN_FREGE_TASK_NAME) 
                instanceof RunFregeTask
            );
            assertEquals(
                FAILED,
                result.task(":" + RUN_FREGE_TASK_NAME).getOutcome()
            );
            assertTrue(result.getOutput().contains("Main method not found"));
        }
    }
}
