package ch.fhnw.thga.gradleplugins;

import static ch.fhnw.thga.gradleplugins.FregePlugin.FREGE_EXTENSION_NAME;
import static ch.fhnw.thga.gradleplugins.FregePlugin.FREGE_PLUGIN_ID;
import static ch.fhnw.thga.gradleplugins.GradleBuildFileConversionTest.createPluginsSection;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.stream.Stream;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;

public class SharedFunctionalTestLogic
{

   static String createFregeSection(FregeDTO fregeDTO) 
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

    static File writeToFile(File destination, String content) throws IOException
    {
        writeFile(destination, content, false);
        return destination;
    }

    static File appendToFile(File destination, String content) throws IOException
    {
        writeFile(destination, System.lineSeparator() + content, true);
        return destination;
    }

    static BuildResult runGradleTask(File testProjectDir, String... taskName)
    {
        return GradleRunner
            .create()
            .withProjectDir(testProjectDir)
            .withPluginClasspath()
            .withArguments(taskName)
            .build();
    }

    static File createSettingsFile(File testProjectDir) throws IOException 
        {
            File settingsFile = new File(testProjectDir, "settings.gradle");
            return writeToFile(settingsFile, "rootProject.name='frege-plugin'");
        }
    
        private static File createFregePluginBuildFile(
            File testProjectDir)
            throws IOException
        {
            File buildFile = new File(testProjectDir, "build.gradle");
            return writeToFile(
                buildFile,
                createPluginsSection(Stream.of(FREGE_PLUGIN_ID)));
        }
    
        static File createFregeBuildFile(
            File testProjectDir,
            String fregeBuildFileConfig)
            throws IOException
        {
            return appendToFile(
                createFregePluginBuildFile(
                    testProjectDir),
                    fregeBuildFileConfig);
        }
    
        static Project createFregeGradleProject(
            File testProjectDir,
            String fregeBuildFileConfig)
            throws Exception
        {
            createSettingsFile(testProjectDir);
            createFregeBuildFile(testProjectDir, fregeBuildFileConfig);
            Project project = ProjectBuilder
                .builder()
                .withProjectDir(testProjectDir)
                .build();
            project.getPluginManager().apply(FREGE_PLUGIN_ID);
            return project;
        }
}
