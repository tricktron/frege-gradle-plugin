package ch.fhnw.thga.gradleplugins;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

public abstract class FregeExtension
{
    public static final String DEFAULT_RELATIVE_COMPILER_DOWNLOAD_DIR   = "lib";
    public static final String DEFAULT_RELATIVE_OUTPUT_DIR              = "classes/main/frege";
    public static final String DEFAULT_RELATIVE_SOURCE_DIR              = "src/main/frege";
    public static final List<String> DEFAULT_COMPILER_FLAGS             = List.of("-O", "-make");
    public static final List<String> DEFAULT_COMPILE_ITEMS              = Collections.emptyList();
    public static final List<String> DEFAULT_TEST_MODULES               = Collections.emptyList();

    public abstract Property<String> getVersion();

    public abstract Property<String> getRelease();

    public abstract Property<String> getMainModule();

    public abstract DirectoryProperty getCompilerDownloadDir();

    public abstract DirectoryProperty getMainSourceDir();

    public abstract DirectoryProperty getOutputDir();

    public abstract ListProperty<String> getCompilerFlags();

    public abstract Property<String> getReplModule();

    public abstract ListProperty<String> getCompileItems();
    
    public abstract ListProperty<String> getTestModules();

    @Inject
    public FregeExtension(ProjectLayout projectLayout)
    {
        getCompilerDownloadDir()
        .convention(projectLayout.getProjectDirectory().dir(DEFAULT_RELATIVE_COMPILER_DOWNLOAD_DIR));

        getMainSourceDir()
        .convention(projectLayout.getProjectDirectory().dir(DEFAULT_RELATIVE_SOURCE_DIR));

        getOutputDir()
        .convention(projectLayout.getBuildDirectory().dir(DEFAULT_RELATIVE_OUTPUT_DIR));

        getCompilerFlags().convention(DEFAULT_COMPILER_FLAGS);

        getCompileItems().convention(DEFAULT_COMPILE_ITEMS);
        
        getTestModules().convention(DEFAULT_TEST_MODULES);
    }
}
