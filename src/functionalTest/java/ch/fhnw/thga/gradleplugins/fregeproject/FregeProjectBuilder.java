package ch.fhnw.thga.gradleplugins.fregeproject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;

import ch.fhnw.thga.gradleplugins.FregeDTO;
import static ch.fhnw.thga.gradleplugins.FregePlugin.FREGE_EXTENSION_NAME;
import static ch.fhnw.thga.gradleplugins.FregePlugin.FREGE_PLUGIN_ID;
import static ch.fhnw.thga.gradleplugins.GradleBuildFileConversionTest.createPluginsSection;
import static ch.fhnw.thga.gradleplugins.FregeExtension.DEFAULT_RELATIVE_COMPILER_DOWNLOAD_DIR;

public final class FregeProjectBuilder implements ProjectRoot, BuildFile, Build
{
    private static final String LATEST_COMPILER_VERSION = "3.25.84";
    private static final Path LOCAL_COMPILER_PATH       =
        Paths.get(String.format(
            "src/functionalTest/resources/frege%s.jar",
            LATEST_COMPILER_VERSION)
        );

    private File projectRoot;
    private String buildFile                            = createPluginsSection(
        Stream.of(FREGE_PLUGIN_ID)
        );
    private boolean useLocalFregeCompiler               = true;
    private String settingsFile                         = "rootProject.name='frege-plugin'";
    private Supplier<Stream<FregeSourceFile>> fregeSourceFiles    = () -> Stream.empty();

    
    public static ProjectRoot builder()
    {
        return new FregeProjectBuilder();
    }
 
     private static void writeFile(
         File destination,
         String content,
         boolean append) 
         throws IOException
     {
         try (BufferedWriter output = new BufferedWriter(new FileWriter(destination, append)))
         {
                 output.write(content);
         }
     
     }
 
     private static File writeToFile(File destination, String content) throws IOException
     {
         writeFile(destination, content, false);
         return destination;
     }

     private File writeToFile(FregeSourceFile fregeFile)
     {
         Path fregeFilePath = projectRoot.toPath().resolve(fregeFile.getFregeModulePath());
         try
         {
            Files.createDirectories(
            fregeFilePath
            .getParent()
        );
            return writeToFile(
                fregeFilePath.toFile(),
                fregeFile.getFregeSourceCode()
            );
         } catch (IOException e)
         {
             throw new RuntimeException(e.getMessage(), e.getCause());
         }
     }
 
     private static File appendToFile(File destination, String content) throws IOException
     {
         writeFile(destination, System.lineSeparator() + content, true);
         return destination;
     }
     
     private File setupLocalFregeCompilerInDefaultPath() throws IOException
     {
         Files.createDirectories(
             projectRoot
             .toPath()
             .resolve(DEFAULT_RELATIVE_COMPILER_DOWNLOAD_DIR));
            return Files.copy(
                LOCAL_COMPILER_PATH,
                projectRoot
                    .toPath()
                    .resolve(DEFAULT_RELATIVE_COMPILER_DOWNLOAD_DIR)
                    .resolve(LOCAL_COMPILER_PATH.getFileName())
            ).toFile();
     }

    private FregeProjectBuilder() {}


    @Override
    public Build useLocalFregeCompiler(boolean useLocalFregeCompiler)
    {
        this.useLocalFregeCompiler = useLocalFregeCompiler;
        return this;
    }

    @Override
    public Build settingsFile(String settingsFile)
    {
        this.settingsFile = String.join(
            System.lineSeparator(),
            this.settingsFile,
            settingsFile
        );
        return this;
    }

    @Override
    public Build buildFile(String buildFile)
    {
        this.buildFile = String.join(
            System.lineSeparator(),
            this.buildFile,
            buildFile
        );
        return this;
    }

    @Override
    public BuildFile projectRoot(File projectRoot)
    {
        this.projectRoot = projectRoot;
        return this;
    }

    @Override
    public Build fregeSourceFiles(Supplier<Stream<FregeSourceFile>> fregeSourceFiles)
    {
        this.fregeSourceFiles = fregeSourceFiles;
        return this;
    }

    private File createGradleFile(String filename, String content) throws IOException
    {
        return writeToFile(
            new File(projectRoot, filename),
            content
        );
    }

    @Override
    public Project build() throws IOException
    {
        createGradleFile("settings.gradle", settingsFile);
        createGradleFile("build.gradle", buildFile);
        if (useLocalFregeCompiler) setupLocalFregeCompilerInDefaultPath();
        fregeSourceFiles.get().map(this::writeToFile).findFirst();
        Project project = ProjectBuilder
            .builder()
            .withProjectDir(projectRoot)
            .build();
        project.getPluginManager().apply(FREGE_PLUGIN_ID);
        return project;
    }
}
