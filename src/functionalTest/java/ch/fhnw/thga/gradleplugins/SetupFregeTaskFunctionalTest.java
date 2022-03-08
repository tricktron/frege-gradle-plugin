package ch.fhnw.thga.gradleplugins;

import static ch.fhnw.thga.gradleplugins.FregeExtension.DEFAULT_RELATIVE_COMPILER_DOWNLOAD_DIR;
import static ch.fhnw.thga.gradleplugins.FregePlugin.SETUP_FREGE_TASK_NAME;
import static ch.fhnw.thga.gradleplugins.SharedFunctionalTestLogic.assertFileExists;
import static ch.fhnw.thga.gradleplugins.SharedFunctionalTestLogic.createFregeSection;
import static ch.fhnw.thga.gradleplugins.SharedFunctionalTestLogic.runGradleTask;
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Paths;

import org.gradle.api.Project;
import org.gradle.testkit.runner.BuildResult;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.IndicativeSentencesGeneration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ch.fhnw.thga.gradleplugins.fregeproject.FregeProjectBuilder;
import ch.fhnw.thga.gradleplugins.fregeproject.ProjectRoot;

@Tag("network")
class SetupFregeTaskFunctionalTest
{
    private static FregeDTOBuilder FREGE_BUILDER     = FregeDTOBuilder.builder();
    private static ProjectRoot FREGE_PROJECT_BUILDER = FregeProjectBuilder.builder();

    @Nested
    @IndicativeSentencesGeneration(
        separator = " -> ",
        generator = DisplayNameGenerator.ReplaceUnderscores.class)
    class Setup_frege_task_works {
        @Test
        void given_minimal_build_file_config(@TempDir File testProjectDir) throws Exception
        {
            String minimalBuildFileConfig = createFregeSection(
                FREGE_BUILDER
                .version("'3.25.84'")
                .release("'3.25alpha'")
                .build()
            );

            Project project = FREGE_PROJECT_BUILDER
                .projectRoot(testProjectDir)
                .buildFile(minimalBuildFileConfig)
                .useLocalFregeCompiler(false)
                .build();

            BuildResult result = runGradleTask(testProjectDir, SETUP_FREGE_TASK_NAME);
    
            assertTrue(
                project
                .getTasks()
                .getByName(SETUP_FREGE_TASK_NAME) instanceof SetupFregeTask
            );
            assertEquals(SUCCESS, result.task(":" + SETUP_FREGE_TASK_NAME).getOutcome());
            assertFileExists(
                testProjectDir,
                Paths.get(DEFAULT_RELATIVE_COMPILER_DOWNLOAD_DIR, "frege3.25.84.jar").toString()
            );
        }
    
        @Test
        void given_custom_frege_compiler_download_directory_in_build_file_config(
            @TempDir File testProjectDir) 
            throws Exception 
        {
            String buildFileConfigWithCustomDownloadDir = createFregeSection(
                FREGE_BUILDER
                    .version("'3.25.84'")
                    .release("'3.25alpha'")
                    .compilerDownloadDir("layout.projectDirectory.dir('dist')")
                    .build()
            );

            Project project = FREGE_PROJECT_BUILDER
                .projectRoot(testProjectDir)
                .buildFile(buildFileConfigWithCustomDownloadDir)
                .useLocalFregeCompiler(false)
                .build();
    
            BuildResult result = runGradleTask(testProjectDir, SETUP_FREGE_TASK_NAME);
            
            assertTrue(
                project
                .getTasks()
                .getByName(SETUP_FREGE_TASK_NAME) instanceof SetupFregeTask
            );
            assertEquals(SUCCESS, result.task(":" + SETUP_FREGE_TASK_NAME).getOutcome());
            assertFileExists(
                testProjectDir,
                Paths.get("dist", "frege3.25.84.jar").toString()
            );
        }
    }
}