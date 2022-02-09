package ch.fhnw.thga.gradleplugins.fregeproject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;

import ch.fhnw.thga.gradleplugins.FregeDTO;
import static ch.fhnw.thga.gradleplugins.FregePlugin.FREGE_EXTENSION_NAME;
import static ch.fhnw.thga.gradleplugins.FregePlugin.FREGE_PLUGIN_ID;
import static ch.fhnw.thga.gradleplugins.GradleBuildFileConversionTest.createPluginsSection;

public final class FregeProjectBuilder implements ProjectRoot, BuildFile, Build
{
    private File projectRoot;
    private String buildFile    = createPluginsSection(Stream.of(FREGE_PLUGIN_ID));
    private Path fregeCompiler  = Paths.get("src/functionalTest/resources/frege3.25.84.jar");
    private String settingsFile = "rootProject.name='frege-plugin'";

    private static volatile FregeProjectBuilder instance;

    private static String createFregeSection(FregeDTO fregeDTO) 
    {
         return String.format(
             "%s {%s  %s%s}",
             FREGE_EXTENSION_NAME,
             System.lineSeparator(),
             fregeDTO.toBuildFile(),
             System.lineSeparator());
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
 
     private static File appendToFile(File destination, String content) throws IOException
     {
         writeFile(destination, System.lineSeparator() + content, true);
         return destination;
     } 

    private FregeProjectBuilder() {}

    public static ProjectRoot getInstance()
    {
        FregeProjectBuilder result = instance;
        if (result != null) return result;
        synchronized (FregeProjectBuilder.class)
        {
            return instance == null ? new FregeProjectBuilder()
                                    : instance;
        }
    }

    @Override
    public Build fregeCompiler(Path fregeCompiler)
    {
        this.fregeCompiler = fregeCompiler;
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
    public Project build() throws IOException {
        File settingsGradle = new File(projectRoot, "settings.gradle");
        writeToFile(settingsGradle, settingsFile);
        File buildGradle = new File(projectRoot, "build.gradle");
        writeToFile(buildGradle, buildFile);
        Project project = ProjectBuilder
            .builder()
            .withProjectDir(projectRoot)
            .build();
        project.getPluginManager().apply(FREGE_PLUGIN_ID);
        return project;
    }
}
