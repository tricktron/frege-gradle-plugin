package ch.fhnw.thga.gradleplugins;

import static ch.fhnw.thga.gradleplugins.FregePlugin.COMPILE_FREGE_TASK_NAME;
import static ch.fhnw.thga.gradleplugins.SharedFunctionalTestLogic.createFregeSection;
import static ch.fhnw.thga.gradleplugins.SharedFunctionalTestLogic.runGradleTask;
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
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ch.fhnw.thga.gradleplugins.fregeproject.FregeProjectBuilder;
import ch.fhnw.thga.gradleplugins.fregeproject.FregeSourceFile;
import ch.fhnw.thga.gradleplugins.fregeproject.ProjectRoot;

public class CompileFregeTaskFunctionalTest
{
    private static final String NEW_LINE               = System.lineSeparator();
    private static final FregeSourceFile COMPLETION_FR = new FregeSourceFile(
        "src/main/frege/ch/fhnw/thga/Completion.fr",
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
            String minimalBuildFileConfig = createFregeSection(
                FregeDTOBuilder
                .builder()
                .version("'3.25.84'")
                .release("'3.25alpha'")
                .build()
            );

            Project project = FregeProjectBuilder
                .builder()
                .projectRoot(testProjectDir)
                .buildFile(minimalBuildFileConfig)
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
            assertTrue(testProjectDir
                .toPath()
                .resolve("build/classes/main/frege/ch/fhnw/thga/Completion.java")
                .toFile()
                .exists());
            assertTrue(testProjectDir
                .toPath()
                .resolve("build/classes/main/frege/ch/fhnw/thga/Completion.class")
                .toFile()
                .exists());
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
            assertTrue(testProjectDir
                .toPath()
                .resolve("build/classes/main/frege/ch/fhnw/thga/Completion.java")
                .toFile()
                .exists()
            );
            assertTrue(testProjectDir
                .toPath()
                .resolve("build/classes/main/frege/ch/fhnw/thga/Completion.class")
                .toFile()
                .exists()
            );
        }
        @Test
        void given_frege_code_in_custom_source_and_output_dir_and_minimal_build_file_config(
            @TempDir File testProjectDir
        )
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
            System.out.println(result.getOutput());
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
            assertTrue(testProjectDir
                .toPath()
                .resolve("build/frege/ch/fhnw/thga/Completion.java")
                .toFile()
                .exists()
            );
            assertTrue(testProjectDir
                .toPath()
                .resolve("build/frege/ch/fhnw/thga/Completion.class")
                .toFile()
                .exists()
            );
        }
    } 
}
