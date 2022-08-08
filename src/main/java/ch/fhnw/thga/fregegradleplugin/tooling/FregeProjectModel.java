package ch.fhnw.thga.fregegradleplugin.tooling;

import java.io.Serializable;

public class FregeProjectModel implements Serializable, FregeProjectInfo
{

    private final String mainSourceDir;
    private final String classpath;

    public FregeProjectModel(String mainSourceDir, String classpath)
    {
        this.mainSourceDir = mainSourceDir;
        this.classpath     = classpath;
    }

    @Override
    public String getMainSourceDir()
    {
        return mainSourceDir;
    }

    @Override
    public String getClasspath()
    {
        return classpath;
    }
}
