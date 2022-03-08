package ch.fhnw.thga.gradleplugins;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.Property;

public final class SharedTaskLogic
{
    private SharedTaskLogic() {};

    public static final String NEW_LINE = System.lineSeparator();

    public static final Provider<FileCollection> setupClasspath(
        Project project,
        Property<String> dependencies,
        Object... paths)
    {
        return dependencies.map(depsClasspath ->
        {
            return depsClasspath.isEmpty() ? project.files(paths)
                                           : project.files(depsClasspath, paths);
        });
    }

    public static final String extractClassNameFromFregeModuleName(String moduleName)
    {
       return moduleName.substring(moduleName.lastIndexOf(".") + 1);
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
}
