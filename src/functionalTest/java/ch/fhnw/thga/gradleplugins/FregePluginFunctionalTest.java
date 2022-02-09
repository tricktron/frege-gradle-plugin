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
    private static final String NEW_LINE                     = System.lineSeparator();
    private static FregeDTOBuilder FREGE_BUILDER             = FregeDTOBuilder.getInstance();
    private static final String FREGE_COMPLETION_MODULE_CODE = 
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
        );



    /*private BuildResult runAndFailGradleTask(String taskName, String... args) {
            return GradleRunner.create().withProjectDir(testProjectDir).withPluginClasspath()
                            .withArguments(taskName)
                            .buildAndFail();
    }*/

    static File createFregeSourceFile(
        Path fregeFilePath,
        String fregeSourceCode)
        throws IOException
    {
        Files.createDirectories(
            fregeFilePath
            .getParent()
        );
        File fregeFile = fregeFilePath.toFile();
        writeToFile(fregeFile, fregeSourceCode);
        return fregeFile;
    }

    static File setupLocalFregeCompiler(File testProjectDir) throws IOException
    {
        Path fregeCompiler = Paths.get("src/functionalTest/resources/frege3.25.84.jar");
        Files.createDirectories(testProjectDir.toPath().resolve("lib"));
        return Files.copy(
            fregeCompiler,
            testProjectDir.toPath().resolve("lib/frege3.25.84.jar")
        ).toFile();
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
            String minimalBuildFileConfig = createFregeSection(
                FREGE_BUILDER.version("'3.25.84'").release("'3.25alpha'").build()
            );
            Project project = createFregeGradleProject(
                testProjectDir,
                minimalBuildFileConfig
            );
            Path completionFr =
                testProjectDir
                .toPath()
                .resolve(Paths.get(DEFAULT_RELATIVE_SOURCE_DIR, "ch/fhnw/thga/Completion.fr")
            );
            createFregeSourceFile(completionFr, FREGE_COMPLETION_MODULE_CODE);
            setupLocalFregeCompiler(testProjectDir);
            
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
                @TempDir File testProjectDir
            ) throws Exception
            {
                String buildConfigWithCompilerFlags = createFregeSection(
                    FREGE_BUILDER
                    .version("'3.25.84'")
                    .release("'3.25alpha'")
                    .compilerFlags("['-v', '-make', '-O', '-hints']")
                    .build()
                );
                Project project = createFregeGradleProject(
                testProjectDir,
                buildConfigWithCompilerFlags
                );
                Path completionFr =
                    testProjectDir
                    .toPath()
                    .resolve(Paths.get(DEFAULT_RELATIVE_SOURCE_DIR, "ch/fhnw/thga/Completion.fr")
                );
                createFregeSourceFile(completionFr, FREGE_COMPLETION_MODULE_CODE);
                setupLocalFregeCompiler(testProjectDir);
                
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

            /*@Test
            void given_frege_code_in_custom_source_dir_and_custom_output_dir_and_minimal_build_file_config()
                            throws Exception {
                    Path customMainSourceDir = testProjectDir.toPath().resolve(Paths.get("src", "frege"));
                    Files.createDirectories(customMainSourceDir);
                    File completionFr = customMainSourceDir.resolve("Completion.fr").toFile();
                    writeToFile(completionFr, FREGE_COMPLETION_MODULE_CODE);
                    String minimalBuildFileConfig = createFregeSection(
                                    FREGE_BUILDER.version("'3.25.84'").release("'3.25alpha'")
                                                    .mainSourceDir("layout.projectDirectory.dir('src/frege')")
                                                    .outputDir("layout.buildDirectory.dir('frege')").build());
                    appendToFile(buildFile, minimalBuildFileConfig);
                    System.out.println(Files.readString(buildFile.toPath()));

                    BuildResult result = runGradleTask(COMPILE_FREGE_TASK_NAME);

                    assertTrue(project.getTasks().getByName(COMPILE_FREGE_TASK_NAME) instanceof CompileFregeTask);
                    assertEquals(SUCCESS, result.task(":" + COMPILE_FREGE_TASK_NAME).getOutcome());
                    assertTrue(
                                    new File(testProjectDir.getAbsolutePath()
                                                    + "/build/frege/ch/fhnw/thga/Completion.java").exists());
                    assertTrue(
                                    new File(testProjectDir.getAbsolutePath()
                                                    + "/build/frege/ch/fhnw/thga/Completion.class").exists());
            }

            @Test
            void and_is_up_to_date_given_no_code_changes() throws Exception {
                    String completionFr = "Completion.fr";
                    String minimalBuildFileConfig = createFregeSection(
                                    FREGE_BUILDER.version("'3.25.84'").release("'3.25alpha'").build());
                    setupDefaultFregeProjectStructure(FREGE_COMPLETION_MODULE_CODE, completionFr, minimalBuildFileConfig);

                    System.out.println(Files.readString(buildFile.toPath()));
                    BuildResult first = runGradleTask(COMPILE_FREGE_TASK_NAME);
                    assertEquals(SUCCESS, first.task(":" + COMPILE_FREGE_TASK_NAME).getOutcome());

                    BuildResult second = runGradleTask(COMPILE_FREGE_TASK_NAME);
                    assertEquals(UP_TO_DATE, second.task(":" + COMPILE_FREGE_TASK_NAME).getOutcome());
            }

            @Test
            void and_is_cached_given_cache_hit() throws Exception {
                    String completionFr = "Completion.fr";
                    String minimalBuildFileConfig = createFregeSection(
                                    FREGE_BUILDER.version("'3.25.84'").release("'3.25alpha'").build());
                    setupDefaultFregeProjectStructure(FREGE_COMPLETION_MODULE_CODE, completionFr, minimalBuildFileConfig);

                    BuildResult first = runGradleTask(COMPILE_FREGE_TASK_NAME, "--build-cache");
                    assertEquals(SUCCESS, first.task(":" + COMPILE_FREGE_TASK_NAME).getOutcome());

                    String codeChange = String.join(NEW_LINE, "module ch.fhnw.thga.Completion where", NEW_LINE,
                                    NEW_LINE,
                                    "  frob :: Int -> (Int, String)", NEW_LINE, "  frob i = (i, \"Frege rocks\")",
                                    NEW_LINE);
                    setupDefaultFregeProjectStructure(codeChange, completionFr, "");

                    System.out.println(Files.readString(buildFile.toPath()));
                    BuildResult second = runGradleTask(COMPILE_FREGE_TASK_NAME, "--build-cache");
                    assertEquals(SUCCESS, second.task(":" + COMPILE_FREGE_TASK_NAME).getOutcome());

                    setupDefaultFregeProjectStructure(FREGE_COMPLETION_MODULE_CODE, completionFr, "");
                    BuildResult third = runGradleTask(COMPILE_FREGE_TASK_NAME, "--build-cache");
                    assertEquals(FROM_CACHE, third.task(":" + COMPILE_FREGE_TASK_NAME).getOutcome());
            }

            @Test
            void given_two_dependent_frege_files_in_default_source_dir_and_minimal_build_file_config()
                            throws Exception {
                    String completionFr = "Completion.fr";
                    String frobFr = "Frob.fr";
                    String frobCode = String.join(NEW_LINE, "module ch.fhnw.thga.Frob where", NEW_LINE, NEW_LINE,
                                    "import ch.fhnw.thga.Completion (complete)", NEW_LINE,
                                    "frob i = complete $ i + i", NEW_LINE);

                    String minimalBuildFileConfig = createFregeSection(
                                    FREGE_BUILDER.version("'3.25.84'").release("'3.25alpha'").build());
                    setupDefaultFregeProjectStructure(FREGE_COMPLETION_MODULE_CODE, completionFr, minimalBuildFileConfig);
                    setupDefaultFregeProjectStructure(frobCode, frobFr, "");

                    System.out.println(Files.readString(buildFile.toPath()));
                    BuildResult result = runGradleTask(COMPILE_FREGE_TASK_NAME);

                    assertTrue(project.getTasks().getByName(COMPILE_FREGE_TASK_NAME) instanceof CompileFregeTask);
                    assertEquals(SUCCESS, result.task(":" + COMPILE_FREGE_TASK_NAME).getOutcome());
                    assertTrue(new File(
                                    testProjectDir.getAbsolutePath()
                                                    + "/build/classes/main/frege/ch/fhnw/thga/Completion.java")
                                                                    .exists());
                    assertTrue(new File(
                                    testProjectDir.getAbsolutePath()
                                                    + "/build/classes/main/frege/ch/fhnw/thga/Completion.class")
                                                                    .exists());
                    assertTrue(new File(testProjectDir.getAbsolutePath()
                                    + "/build/classes/main/frege/ch/fhnw/thga/Frob.java")
                                                    .exists());
                    assertTrue(new File(testProjectDir.getAbsolutePath()
                                    + "/build/classes/main/frege/ch/fhnw/thga/Frob.class")
                                                    .exists());
            }
    }

    @Nested
    @IndicativeSentencesGeneration(separator = " -> ", generator = DisplayNameGenerator.ReplaceUnderscores.class)
    class Compile_frege_task_fails {
            @Test
            void given_frege_code_and_illegal_compiler_flags() throws Exception {
                    String completionFr = "Completion.fr";
                    String buildConfigWithIllegalCompilerFlags = createFregeSection(FREGE_BUILDER
                                    .version("'3.25.84'")
                                    .release("'3.25alpha'").compilerFlags("['-make', '-bla']").build());
                    setupDefaultFregeProjectStructure(FREGE_COMPLETION_MODULE_CODE, completionFr,
                                    buildConfigWithIllegalCompilerFlags);

                    BuildResult result = runAndFailGradleTask(COMPILE_FREGE_TASK_NAME);

                    assertTrue(project.getTasks().getByName(COMPILE_FREGE_TASK_NAME) instanceof CompileFregeTask);
                    assertEquals(FAILED, result.task(":" + COMPILE_FREGE_TASK_NAME).getOutcome());
            }

            @Test
            void given_two_dependent_frege_files_in_default_source_dir_and_without_make_compiler_flag()
                            throws Exception {
                    String completionFr = "Completion.fr";
                    String frobFr = "Frob.fr";
                    String frobCode = String.join(NEW_LINE, "module ch.fhnw.thga.Frob where", NEW_LINE, NEW_LINE,
                                    "import ch.fhnw.thga.Completion (complete)", NEW_LINE,
                                    "frob i = complete $ i + i", NEW_LINE);

                    String minimalBuildFileConfigWithoutMake = createFregeSection(
                                    FREGE_BUILDER.version("'3.25.84'").release("'3.25alpha'").compilerFlags("['-v']")
                                                    .build());
                    setupDefaultFregeProjectStructure(FREGE_COMPLETION_MODULE_CODE, completionFr,
                                    minimalBuildFileConfigWithoutMake);
                    setupDefaultFregeProjectStructure(frobCode, frobFr, "");
                    
                    System.out.println("Build File: " + Files.readString(buildFile.toPath()));

                    BuildResult result = runAndFailGradleTask(COMPILE_FREGE_TASK_NAME);


                    assertTrue(project.getTasks().getByName(COMPILE_FREGE_TASK_NAME) instanceof CompileFregeTask);
                    assertEquals(FAILED, result.task(":" + COMPILE_FREGE_TASK_NAME).getOutcome());
            }
    }

    @Nested
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
        }*/
    }
}