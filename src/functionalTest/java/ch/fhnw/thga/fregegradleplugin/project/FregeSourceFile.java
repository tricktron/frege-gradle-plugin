package ch.fhnw.thga.fregegradleplugin.project;

public class FregeSourceFile
{
    private final String modulePath;
    private final String sourceCode;

    public FregeSourceFile(String modulePath, String sourceCode)
    {
        this.modulePath = modulePath;
        this.sourceCode = sourceCode;   
    }

    public String getFregeModulePath()
    {
        return modulePath;
    }

    public String getFregeSourceCode()
    {
        return sourceCode;
    }
}
