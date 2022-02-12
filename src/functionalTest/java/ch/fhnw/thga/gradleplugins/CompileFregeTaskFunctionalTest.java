package ch.fhnw.thga.gradleplugins;

import static ch.fhnw.thga.gradleplugins.FregeExtension.DEFAULT_RELATIVE_SOURCE_DIR;
import static ch.fhnw.thga.gradleplugins.FregePlugin.COMPILE_FREGE_TASK_NAME;
import static ch.fhnw.thga.gradleplugins.SharedFunctionalTestLogic.createFregeSection;
import static ch.fhnw.thga.gradleplugins.SharedFunctionalTestLogic.runAndFailGradleTask;
import static ch.fhnw.thga.gradleplugins.SharedFunctionalTestLogic.runGradleTask;
import static ch.fhnw.thga.gradleplugins.SharedFunctionalTestLogic.NEW_LINE;
import static org.gradle.testkit.runner.TaskOutcome.FAILED;
import static org.gradle.testkit.runner.TaskOutcome.FROM_CACHE;
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE;
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

public class CompileFregeTaskFunctionalTest
{
    private static final FregeSourceFile COMPLETION_FR = new FregeSourceFile(
        String.format("%s/%s",
            DEFAULT_RELATIVE_SOURCE_DIR,
            "ch/fhnw/thga/Completion.fr"),
        String.join
        (
            NEW_LINE,
            "module ch.fhnw.thga.Completion where",
            NEW_LINE,
            NEW_LINE,
            "  complete :: Int -> (Int, String)",
            NEW_LINE,
            "  complete i = (i, \"Frege rocks\")",
            NEW_LINE
        )
    );
    private static final String MINIMAL_BUILD_FILE_CONFIG = createFregeSection(
        FregeDTOBuilder
        .builder()
        .version("'3.25.84'")
        .release("'3.25alpha'")
        .build()
    );

    private static final boolean assertFileExists(
        File testProjectDir,
        String relativeFilePath)
    {
        return testProjectDir
        .toPath()
        .resolve(relativeFilePath)
        .toFile()
        .exists();
    }

    @Nested
    @IndicativeSentencesGeneration(
        separator = " -> ",
        generator = DisplayNameGenerator.ReplaceUnderscores.class
    )
    class Compile_frege_task_works {

        @Test
        void given_frege_code_in_default_source_dir_and_minimal_build_file_config(
            @TempDir File testProjectDir)
            throws Exception
        {
            Project project = FregeProjectBuilder
                .builder()
                .projectRoot(testProjectDir)
                .buildFile(MINIMAL_BUILD_FILE_CONFIG)
                .fregeSourceFiles(() -> Stream.of(COMPLETION_FR))
                .build();
            
            BuildResult result = runGradleTask(testProjectDir, COMPILE_FREGE_TASK_NAME);

            assertTrue(
                project
                .getTasks()
                .getByName(COMPILE_FREGE_TASK_NAME) 
                instanceof CompileFregeTask
            );
            assertEquals(
                SUCCESS,
                result.task(":" + COMPILE_FREGE_TASK_NAME).getOutcome()
            );
            assertFileExists(
                testProjectDir,
                "build/classes/main/frege/ch/fhnw/thga/Completion.java"
            );
            assertFileExists(
                testProjectDir,
                "build/classes/main/frege/ch/fhnw/thga/Completion.class"
            );
        }
        @Test
        void given_frege_code_and_many_compiler_flags(
            @TempDir File testProjectDir)
            throws Exception
        {
            String buildConfigWithCompilerFlags = createFregeSection(
                FregeDTOBuilder
                .builder()
                .version("'3.25.84'")
                .release("'3.25alpha'")
                .compilerFlags("['-v', '-make', '-O', '-hints']")
                .build()
            );
            Project project = FregeProjectBuilder
                .builder()
                .projectRoot(testProjectDir)
                .buildFile(buildConfigWithCompilerFlags)
                .fregeSourceFiles(() -> Stream.of(COMPLETION_FR))
                .build();
            
            BuildResult result = runGradleTask(testProjectDir, COMPILE_FREGE_TASK_NAME);

            assertTrue(
                project
                .getTasks()
                .getByName(COMPILE_FREGE_TASK_NAME) 
                instanceof CompileFregeTask
            );
            assertEquals(
                SUCCESS,
                result.task(":" + COMPILE_FREGE_TASK_NAME).getOutcome()
            );
            assertFileExists(
                testProjectDir,
                "build/classes/main/frege/ch/fhnw/thga/Completion.java"
            );
            assertFileExists(
                testProjectDir,
                "build/classes/main/frege/ch/fhnw/thga/Completion.class"
            );
        }
        @Test
        void given_frege_code_in_custom_source_and_output_dir_and_minimal_build_file_config(
            @TempDir File testProjectDir)
            throws Exception
        {
            String customSourceAndOutputBuildFileConfig = createFregeSection(
                FregeDTOBuilder
                .builder()
                .version("'3.25.84'")
                .release("'3.25alpha'")
                .mainSourceDir("layout.projectDirectory.dir('src/frege')")
                .outputDir("layout.buildDirectory.dir('frege')")
                .build()
            );
            Project project = FregeProjectBuilder
                .builder()
                .projectRoot(testProjectDir)
                .buildFile(customSourceAndOutputBuildFileConfig)
                .fregeSourceFiles(() -> Stream.of(new FregeSourceFile(
                    "src/frege/ch/fhnw/thga/Completion.fr",
                    COMPLETION_FR.getFregeSourceCode())))
                .build();

            BuildResult result = runGradleTask(testProjectDir, COMPILE_FREGE_TASK_NAME);
            assertTrue(
                project
                .getTasks()
                .getByName(COMPILE_FREGE_TASK_NAME)
                instanceof CompileFregeTask
            );
            assertEquals(
                SUCCESS,
                result.task(":" + COMPILE_FREGE_TASK_NAME).getOutcome()
            );
            assertFileExists(
                testProjectDir,
                "build/classes/main/frege/ch/fhnw/thga/Completion.java"
            );
            assertFileExists(
                testProjectDir,
                "build/classes/main/frege/ch/fhnw/thga/Completion.class"
            );
        }
        @Test
        void and_is_up_to_date_given_no_code_changes(
            @TempDir File testProjectDir)
            throws Exception
        {
            FregeProjectBuilder
                .builder()
                .projectRoot(testProjectDir)
                .buildFile(MINIMAL_BUILD_FILE_CONFIG)
                .fregeSourceFiles(() -> Stream.of(COMPLETION_FR))
                .build();
            
            BuildResult first = runGradleTask(testProjectDir, COMPILE_FREGE_TASK_NAME);
            
            assertEquals(
                SUCCESS, 
                first.task(":" + COMPILE_FREGE_TASK_NAME).getOutcome());

            BuildResult second = runGradleTask(testProjectDir, COMPILE_FREGE_TASK_NAME);
            
            assertEquals(
                UP_TO_DATE,
                second.task(":" + COMPILE_FREGE_TASK_NAME).getOutcome());
        }
        @Test
        void and_is_cached_given_cache_hit(
            @TempDir File testProjectDir) 
            throws Exception 
        {
            FregeProjectBuilder
                .builder()
                .projectRoot(testProjectDir)
                .buildFile(MINIMAL_BUILD_FILE_CONFIG)
                .fregeSourceFiles(() -> Stream.of(COMPLETION_FR))
                .build();

            BuildResult first = runGradleTask(
                testProjectDir,
                COMPILE_FREGE_TASK_NAME,
                "--build-cache"
            );
            
            assertEquals(
                SUCCESS, 
                first.task(":" + COMPILE_FREGE_TASK_NAME).getOutcome()
            );

            String codeChange = String.join(
                NEW_LINE, 
                "module ch.fhnw.thga.Completion where",
                NEW_LINE,
                NEW_LINE,
                "  frob :: Int -> (Int, String)",
                NEW_LINE,
                "  frob i = (i, \"Frege rocks\")",
                NEW_LINE
            );
            FregeProjectBuilder
                .builder()
                .projectRoot(testProjectDir)
                .buildFile(MINIMAL_BUILD_FILE_CONFIG)
                .fregeSourceFiles(() -> Stream.of(new FregeSourceFile(
                    COMPLETION_FR.getFregeModulePath(),
                    codeChange)))
                .build();

            BuildResult second = runGradleTask(
                testProjectDir,
                COMPILE_FREGE_TASK_NAME,
                "--build-cache"
            );
            
            assertEquals(
                SUCCESS,
                second.task(":" + COMPILE_FREGE_TASK_NAME).getOutcome()
            );

            FregeProjectBuilder
                .builder()
                .projectRoot(testProjectDir)
                .buildFile(MINIMAL_BUILD_FILE_CONFIG)
                .fregeSourceFiles(() -> Stream.of(COMPLETION_FR))
                .build();
            BuildResult third = runGradleTask(
                testProjectDir,
                COMPILE_FREGE_TASK_NAME,
                "--build-cache"
            );
            
            assertEquals(
                FROM_CACHE, 
                third.task(":" + COMPILE_FREGE_TASK_NAME).getOutcome());
        }
        @Test
        void given_two_dependent_frege_files_in_default_source_dir_and_minimal_build_file_config(
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
            Project project = FregeProjectBuilder
                .builder()
                .projectRoot(testProjectDir)
                .buildFile(MINIMAL_BUILD_FILE_CONFIG)
                .fregeSourceFiles(() -> Stream.of(COMPLETION_FR, new FregeSourceFile(
                    String.format(
                        "%s/%s",
                        DEFAULT_RELATIVE_SOURCE_DIR,
                        "ch/fhnw/thga/Frob.fr"
                    ),
                    frobCode)))
                .build();
            
            BuildResult result = runGradleTask(
                testProjectDir,
                COMPILE_FREGE_TASK_NAME,
                "--compileItem=ch.fhnw.thga.Frob"
            );

            assertTrue(
                project
                .getTasks()
                .getByName(COMPILE_FREGE_TASK_NAME) 
                instanceof CompileFregeTask
            );
            assertEquals(
                SUCCESS,
                result.task(":" + COMPILE_FREGE_TASK_NAME).getOutcome()
            );
            assertFileExists(
                testProjectDir,
                "build/classes/main/frege/ch/fhnw/thga/Completion.java"
            );
            assertFileExists(
                testProjectDir,
                "build/classes/main/frege/ch/fhnw/thga/Completion.class"
            );
            assertFileExists(
                testProjectDir,
                "build/classes/main/frege/ch/fhnw/thga/Frob.java"
            );
            assertFileExists(
                testProjectDir,
                "build/classes/main/frege/ch/fhnw/thga/Frob.class"
            );
        }
    }
    @Nested
    @IndicativeSentencesGeneration(
        separator = " -> ",
        generator = DisplayNameGenerator.ReplaceUnderscores.class
    )
    class Compile_frege_task_fails
    {
        @Test
        void given_frege_code_and_illegal_compiler_flags(
            @TempDir File testProjectDir)
            throws Exception
        {
            String buildConfigWithIllegalCompilerFlags = createFregeSection(
                FregeDTOBuilder
                .builder()
                .version("'3.25.84'")
                .release("'3.25alpha'")
                .compilerFlags("['-make', '-bla']")
                .build()
            );
            Project project = FregeProjectBuilder
                .builder()
                .projectRoot(testProjectDir)
                .buildFile(buildConfigWithIllegalCompilerFlags)
                .fregeSourceFiles(() -> Stream.of(COMPLETION_FR))
                .build();

            BuildResult result = runAndFailGradleTask(testProjectDir, COMPILE_FREGE_TASK_NAME);

            assertTrue(
                project
                .getTasks()
                .getByName(COMPILE_FREGE_TASK_NAME)
                instanceof CompileFregeTask
            );
            assertEquals(
                FAILED, 
                result.task(":" + COMPILE_FREGE_TASK_NAME).getOutcome()
            );
        }
        @Test
        void given_two_dependent_frege_files_in_default_source_dir_and_without_make_compiler_flag(
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
            String minimalBuildFileConfigWithoutMake = createFregeSection(
                FregeDTOBuilder
                .builder()
                .version("'3.25.84'")
                .release("'3.25alpha'")
                .compilerFlags("['-v']")
                .build()
            );
            Project project = FregeProjectBuilder
                .builder()
                .projectRoot(testProjectDir)
                .buildFile(minimalBuildFileConfigWithoutMake)
                .fregeSourceFiles(() -> Stream.of(COMPLETION_FR, new FregeSourceFile(
                    String.format(
                        "%s/%s",
                        DEFAULT_RELATIVE_SOURCE_DIR,
                        "ch/fhnw/thga/Frob.fr"
                    ),
                    frobCode)))
                .build();
            
            BuildResult result = runAndFailGradleTask(
                testProjectDir,
                COMPILE_FREGE_TASK_NAME,
                "--compileItem=ch.fhnw.thga.Frob"
            );

            assertTrue(
                project
                .getTasks()
                .getByName(COMPILE_FREGE_TASK_NAME) 
                instanceof CompileFregeTask
            );
            assertEquals(
                FAILED, 
                result.task(":" + COMPILE_FREGE_TASK_NAME).getOutcome()
            );
        }
    }
}
