package ch.fhnw.thga.gradleplugins.fregeproject;

import java.io.File;
import java.util.List;

public class FregeProject
{
    public final File settingsFile;
    public final File buildFile;
    public final File fregeCompiler;
    public final List<File> fregeSourceFiles;

    public FregeProject(
        File settingsFile,
        File buildFile,
        File fregeCompiler,
        List<File> fregeSourceFiles
        
    )
    {
        this.settingsFile     = settingsFile;
        this.buildFile        = buildFile;
        this.fregeCompiler    = fregeCompiler;
        this.fregeSourceFiles = fregeSourceFiles;
    }
}
