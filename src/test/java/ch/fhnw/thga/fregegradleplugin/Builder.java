package ch.fhnw.thga.fregegradleplugin;

public interface Builder 
{
    Builder version(String version);

    Builder release(String release);

    Builder compilerDownloadDir(String downloadDir);

    Builder mainSourceDir(String mainSourceDir);

    Builder outputDir(String outputDir);
    
    Builder testOutputDir(String testOutputDir);

    Builder mainModule(String mainModule);

    Builder compilerFlags(String compilerFlags);

    Builder replModule(String replModule);
    
    Builder compileItems(String compileItems);
    
    Builder testModules(String testModules);

    FregeDTO build();
}
