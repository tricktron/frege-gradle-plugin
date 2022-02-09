package ch.fhnw.thga.gradleplugins.fregeproject;

import java.io.IOException;
import java.nio.file.Path;

import org.gradle.api.Project;

public interface Build
{
    Build fregeCompiler(Path fregeCompiler);
    Build settingsFile(String settingsFile);
    Project build() throws IOException;
}
