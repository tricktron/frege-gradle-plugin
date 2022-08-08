package ch.fhnw.thga.fregegradleplugin.tasks;

import static ch.fhnw.thga.fregegradleplugin.FregeExtension.DEFAULT_RELATIVE_SOURCE_DIR;
import static ch.fhnw.thga.fregegradleplugin.FregePlugin.TEST_FREGE_TASK_NAME;
import static ch.fhnw.thga.fregegradleplugin.SharedFunctionalTestLogic.createFregeSection;
import static ch.fhnw.thga.fregegradleplugin.SharedFunctionalTestLogic.runAndFailGradleTask;
import static ch.fhnw.thga.fregegradleplugin.SharedFunctionalTestLogic.runGradleTask;
import static ch.fhnw.thga.fregegradleplugin.SharedTaskLogic.EMPTY_LINE;
import static ch.fhnw.thga.fregegradleplugin.SharedTaskLogic.NEW_LINE;
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

import ch.fhnw.thga.fregegradleplugin.FregeDTOBuilder;
import ch.fhnw.thga.fregegradleplugin.project.FregeProjectBuilder;
import ch.fhnw.thga.fregegradleplugin.project.FregeSourceFile;

public class TestFregeFunctionalTest
{
    private static final FregeSourceFile PROPERTY_TESTS_FR = new FregeSourceFile
    (
        String.format
        (
            "%s/%s",
            DEFAULT_RELATIVE_SOURCE_DIR,
            "ch/fhnw/thga/PropertyTests.fr"
        ),
        String.join
        (NEW_LINE,
            "module ch.fhnw.thga.PropertyTests where",
            EMPTY_LINE,
            "import Test.QuickCheck",
            EMPTY_LINE,
            "p_pass = property $ \\(n::Integer) -> odd n ^^ even n",
            "p_fail = property $ \\(n::Integer) -> even n"
        )
    );

    @Nested
    @IndicativeSentencesGeneration
    (
        separator = " -> ",
        generator = DisplayNameGenerator.ReplaceUnderscores.class
    )
    class Test_frege_task_works
    {
        @Test
        void given_single_frege_module_with_true_quick_check_property
            (@TempDir File testProjectDir)
            throws Exception 
        {
            String mainBuildConfig = createFregeSection
            (
                FregeDTOBuilder
                .latestVersionBuilder()
                .testModules("['ch.fhnw.thga.PropertyTests']")
                .build()
            );

            Project project = FregeProjectBuilder
            .builder()
            .projectRoot(testProjectDir)
            .buildFile(mainBuildConfig)
            .fregeSourceFiles(() -> Stream.of(PROPERTY_TESTS_FR))
            .build();
            
            BuildResult result = runGradleTask
            (
                testProjectDir,
                TEST_FREGE_TASK_NAME,
                "--args=-v -p p_pass"
            );
            
            assertTrue
            (
                project
                .getTasks()
                .getByName(TEST_FREGE_TASK_NAME) 
                instanceof TestFregeTask
            );
            assertEquals
            (
                SUCCESS,
                result.task(":" + TEST_FREGE_TASK_NAME).getOutcome()
            );
            assertTrue(result.getOutput().contains("OK"));
            assertTrue(result.getOutput().contains("Properties passed: 1, failed: 0"));
        }

        @Test
        void given_multiple_frege_modules_with_true_quick_check_properties
            (@TempDir File testProjectDir)
            throws Exception 
        {
            FregeSourceFile PROPERTY_TESTS1_FR = new FregeSourceFile
            (
                String.format
                (
                    "%s/%s",
                    DEFAULT_RELATIVE_SOURCE_DIR,
                    "ch/fhnw/thga/PropertyTests1.fr"
                ),
                String.join
                (
                    NEW_LINE,
                    "module ch.fhnw.thga.PropertyTests1 where",
                    EMPTY_LINE,
                    "import Test.QuickCheck",
                    EMPTY_LINE,
                    "p_pass1 = property $ \\(n::Integer) -> odd n ^^ even n"
                )
            );

            FregeSourceFile PROPERTY_TESTS2_FR = new FregeSourceFile
            (
                String.format
                (
                    "%s/%s",
                    DEFAULT_RELATIVE_SOURCE_DIR,
                    "ch/fhnw/thga/PropertyTests2.fr"
                ),
                String.join
                (
                    NEW_LINE,
                    "module ch.fhnw.thga.PropertyTests2 where",
                    EMPTY_LINE,
                    "import Test.QuickCheck",
                    EMPTY_LINE,
                    "f   = reverse",
                    "g x = show x ++ show x ++ show x",
                    "p_commutativity = property $ \\(xs :: [Int]) -> ( map g (f xs) == f (map g xs) )"
                )
            );
            String mainBuildConfig = createFregeSection
            (
                FregeDTOBuilder
                .latestVersionBuilder()
                .testModules("['ch.fhnw.thga.PropertyTests1', 'ch.fhnw.thga.PropertyTests2']")
                .build()
            );

            Project project = FregeProjectBuilder
            .builder()
            .projectRoot(testProjectDir)
            .buildFile(mainBuildConfig)
            .fregeSourceFiles(() -> Stream.of(PROPERTY_TESTS1_FR, PROPERTY_TESTS2_FR))
            .build();
            
            BuildResult result = runGradleTask
            (
                testProjectDir,
                TEST_FREGE_TASK_NAME
            );
            
            assertTrue
            (
                project
                .getTasks()
                .getByName(TEST_FREGE_TASK_NAME) 
                instanceof TestFregeTask
            );
            assertEquals
            (
                SUCCESS,
                result.task(":" + TEST_FREGE_TASK_NAME).getOutcome()
            );
            assertTrue(result.getOutput().contains("OK"));
            assertTrue(result.getOutput().contains("Properties passed: 2, failed: 0"));
        }
    }

    @Nested
    @IndicativeSentencesGeneration
    (
        separator = " -> ",
        generator = DisplayNameGenerator.ReplaceUnderscores.class
    )
    class Test_frege_task_fails
    {
        @Test
        void given_single_frege_code_with_false_quick_check_property
            (@TempDir File testProjectDir)
            throws Exception 
        {
            String mainBuildConfig = createFregeSection
            (
                FregeDTOBuilder
                .latestVersionBuilder()
                .testModules("['ch.fhnw.thga.PropertyTests']")
                .build()
            );

            Project project = FregeProjectBuilder
            .builder()
            .projectRoot(testProjectDir)
            .buildFile(mainBuildConfig)
            .fregeSourceFiles(() -> Stream.of(PROPERTY_TESTS_FR))
            .build();
            
            BuildResult result = runAndFailGradleTask
            (
                testProjectDir,
                TEST_FREGE_TASK_NAME,
                "--args=-v -p p_fail"
            );
            
            assertTrue
            (
                project
                .getTasks()
                .getByName(TEST_FREGE_TASK_NAME) 
                instanceof TestFregeTask
            );
            assertEquals
            (
                FAILED,
                result.task(":" + TEST_FREGE_TASK_NAME).getOutcome()
            );
            assertTrue(result.getOutput().contains("Failed"));
            assertTrue(result.getOutput().contains("Properties passed: 0, failed: 1"));
        }
    }
}
