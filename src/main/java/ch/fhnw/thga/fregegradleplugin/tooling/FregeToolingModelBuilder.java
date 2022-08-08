package ch.fhnw.thga.fregegradleplugin.tooling;

import static ch.fhnw.thga.fregegradleplugin.FregePlugin.FREGE_CONFIGURATION_NAME;

import org.gradle.api.Project;
import org.gradle.tooling.provider.model.ToolingModelBuilder;

import ch.fhnw.thga.fregegradleplugin.FregeExtension;

public final class FregeToolingModelBuilder implements ToolingModelBuilder
{

    @Override
    public Object buildAll(String modelName, Project project)
    {
        String fregeMainSourceDir = project
        .getExtensions()
        .findByType(FregeExtension.class)
        .getMainSourceDir()
        .get()
        .getAsFile()
        .getAbsolutePath();
        
        String fregeClasspath     = project
        .getConfigurations()
        .getByName(FREGE_CONFIGURATION_NAME)
        .getAsPath();

        return new FregeProjectModel(fregeMainSourceDir, fregeClasspath);
    }

    @Override
    public boolean canBuild(String modelName)
    {
        return modelName.equals(FregeProjectInfo.class.getName());
    }
}
