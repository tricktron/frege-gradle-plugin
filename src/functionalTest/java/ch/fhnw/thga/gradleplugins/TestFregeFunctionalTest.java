package ch.fhnw.thga.gradleplugins;

import static ch.fhnw.thga.gradleplugins.FregeExtension.DEFAULT_RELATIVE_SOURCE_DIR;
import static ch.fhnw.thga.gradleplugins.FregePlugin.TEST_FREGE_TASK_NAME;
import static ch.fhnw.thga.gradleplugins.SharedFunctionalTestLogic.createFregeSection;
import static ch.fhnw.thga.gradleplugins.SharedFunctionalTestLogic.runAndFailGradleTask;
import static ch.fhnw.thga.gradleplugins.SharedFunctionalTestLogic.runGradleTask;
import static ch.fhnw.thga.gradleplugins.SharedTaskLogic.EMPTY_LINE;
import static ch.fhnw.thga.gradleplugins.SharedTaskLogic.NEW_LINE;
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

public class TestFregeFunctionalTest
{
    private static final FregeSourceFile PROPERTY_TESTS_FR = new FregeSourceFile(
        String.format(
            "%s/%s",
            DEFAULT_RELATIVE_SOURCE_DIR,
            "ch/fhnw/thga/PropertyTests.fr"
        ),
        String.join(NEW_LINE,
            "module ch.fhnw.thga.PropertyTests where",
            EMPTY_LINE,
            "import Test.QuickCheck",
            EMPTY_LINE,
            "p_pass = property $ \\(n::Integer) -> odd n ^^ even n",
            "p_fail = property $ \\(n::Integer) -> even n"
        )
    );

    @Nested
    @IndicativeSentencesGeneration(
        separator = " -> ",
        generator = DisplayNameGenerator.ReplaceUnderscores.class
    )
    class Test_frege_task_works
    {
        @Test
        void given_frege_code_with_true_quick_check_property(
            @TempDir File testProjectDir)
            throws Exception 
        {
            String mainBuildConfig = createFregeSection(
                FregeDTOBuilder
                .builder()
                .version("'3.25.84'")
                .release("'3.25alpha'")
                .mainModule("'ch.fhnw.thga.PropertyTests'")
                .build()
            );

            Project project = FregeProjectBuilder
                .builder()
                .projectRoot(testProjectDir)
                .buildFile(mainBuildConfig)
                .fregeSourceFiles(() -> Stream.of(PROPERTY_TESTS_FR))
                .build();
            
            BuildResult result = runGradleTask(
                testProjectDir,
                TEST_FREGE_TASK_NAME,
                "--args=-v -p p_pass"
            );
            
            assertTrue(
                project
                .getTasks()
                .getByName(TEST_FREGE_TASK_NAME) 
                instanceof TestFregeTask
            );
            assertEquals(
                SUCCESS,
                result.task(":" + TEST_FREGE_TASK_NAME).getOutcome()
            );
            assertTrue(result.getOutput().contains("OK"));
            assertTrue(result.getOutput().contains("Properties passed: 1, failed: 0"));
        }
    }

    @Nested
    @IndicativeSentencesGeneration(
        separator = " -> ",
        generator = DisplayNameGenerator.ReplaceUnderscores.class
    )
    class Test_frege_task_fails
    {
        @Test
        void given_frege_code_with_false_quick_check_property(
            @TempDir File testProjectDir)
            throws Exception 
        {
            String mainBuildConfig = createFregeSection(
                FregeDTOBuilder
                .builder()
                .version("'3.25.84'")
                .release("'3.25alpha'")
                .mainModule("'ch.fhnw.thga.PropertyTests'")
                .build()
            );

            Project project = FregeProjectBuilder
                .builder()
                .projectRoot(testProjectDir)
                .buildFile(mainBuildConfig)
                .fregeSourceFiles(() -> Stream.of(PROPERTY_TESTS_FR))
                .build();
            
            BuildResult result = runAndFailGradleTask(
                testProjectDir,
                TEST_FREGE_TASK_NAME,
                "--args=-v -p p_fail"
            );
            
            assertTrue(
                project
                .getTasks()
                .getByName(TEST_FREGE_TASK_NAME) 
                instanceof TestFregeTask
            );
            assertEquals(
                FAILED,
                result.task(":" + TEST_FREGE_TASK_NAME).getOutcome()
            );
            assertTrue(result.getOutput().contains("Failed"));
            assertTrue(result.getOutput().contains("Properties passed: 0, failed: 1"));
        }
    }
}
