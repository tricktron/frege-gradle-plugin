package ch.fhnw.thga.gradleplugins;

import java.io.Serializable;

public class FregeProjectModel implements Serializable, FregeProjectInfo
{

    private final String fregeMainSourceDir;
    private final String fregeDependenciesClasspath;

    public FregeProjectModel(String fregeMainSourceDir, String fregeDependenciesClasspath)
    {
        this.fregeMainSourceDir = fregeMainSourceDir;
        this.fregeDependenciesClasspath = fregeDependenciesClasspath;
    }

    @Override
    public String getFregeMainSourceDir() {
        return fregeMainSourceDir;
    }

    @Override
    public String getFregeDependenciesClasspath() {
        return fregeDependenciesClasspath;
    }


}
