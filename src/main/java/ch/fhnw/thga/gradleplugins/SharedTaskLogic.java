package ch.fhnw.thga.gradleplugins;

import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.Property;

public final class SharedTaskLogic
{
    private SharedTaskLogic() {};

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
}
