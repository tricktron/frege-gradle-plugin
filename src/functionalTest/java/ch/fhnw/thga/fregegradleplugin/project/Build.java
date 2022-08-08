package ch.fhnw.thga.fregegradleplugin.project;

import java.io.IOException;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.gradle.api.Project;

public interface Build
{
    Build useLocalFregeCompiler(boolean useLocalFregeCompiler);
    Build settingsFile(String settingsFile);
    Build fregeSourceFiles(Supplier<Stream<FregeSourceFile>> fregeSourceFiles);
    Project build() throws IOException;
}
