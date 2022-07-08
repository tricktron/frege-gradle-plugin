package ch.fhnw.thga.gradleplugins;

public final class FregeDTOBuilder implements Builder 
{
    private String version             = "";
    private String release             = "";
    private String compilerDownloadDir = "";
    private String mainSourceDir       = "";
    private String outputDir           = "";
    private String mainModule          = "";
    private String compilerFlags       = "";
    private String replModule          = "";
    private String compileItems        = "";

    private FregeDTOBuilder() {}

    public static FregeDTOBuilder builder()
    {
        return new FregeDTOBuilder();
    }

    public static Builder latestVersionBuilder()
    {
        return 
        builder()
        .version("'3.25.84'")
        .release("'3.25alpha'");
    }

    @Override
    public Builder version(String version) 
    {
        this.version = version;
        return this;
    }

    @Override
    public Builder release(String release) 
    {
        this.release = release;
        return this;
    }

    @Override
    public Builder compilerDownloadDir(String downloadDir) 
    {
        this.compilerDownloadDir = downloadDir;
        return this;
    }

    @Override
    public Builder mainSourceDir(String mainSourceDir) 
    {
        this.mainSourceDir = mainSourceDir;
        return this;
    }

    @Override
    public Builder outputDir(String outputDir) 
    {
        this.outputDir = outputDir;
        return this;
    }

    @Override
    public Builder mainModule(String mainModule) 
    {
        this.mainModule = mainModule;
        return this;
    }

    @Override
    public Builder compilerFlags(String compilerFlags) 
    {
        this.compilerFlags = compilerFlags;
        return this;
    }

    @Override
    public Builder replModule(String replModule)
    {
        this.replModule = replModule;
        return this;
    }

    @Override
    public Builder compileItems(String compileItems)
    {
        this.compileItems = compileItems;
        return this;
    }

    public FregeDTO build() 
    {
        return new FregeDTO
        (
            version,
            release,
            compilerDownloadDir,
            mainSourceDir,
            outputDir,
            mainModule,
            compilerFlags,
            replModule,
            compileItems
        );
    }
}
