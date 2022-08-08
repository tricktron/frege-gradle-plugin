package ch.fhnw.thga.gradleplugins;

import static ch.fhnw.thga.gradleplugins.FregePlugin.FREGE_CONFIGURATION_NAME;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.tooling.provider.model.ToolingModelBuilder;

public final class FregeToolingModelBuilder implements ToolingModelBuilder
{

    @Override
    public Object buildAll(String modelName, Project project)
    {
        ExtensionContainer extensions = project.getExtensions();
        FregeExtension fregeExtension = extensions.findByType(FregeExtension.class);
        String fregeMainSourceDir = fregeExtension.getMainSourceDir().get().getAsFile().getAbsolutePath();
        ConfigurationContainer configurations = project.getConfigurations();
        Configuration config = configurations.getByName(FREGE_CONFIGURATION_NAME);
        String fregeClasspath = config.getAsPath();
        return new FregeProjectModel(fregeMainSourceDir, fregeClasspath);
    }

    @Override
    public boolean canBuild(String modelName)
    {
        return modelName.equals(FregeProjectInfo.class.getName());
    }

}
