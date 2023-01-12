package org.duckdns.apsuloot.an.generator;

import java.util.List;

public class Activity {

    private String name;
    private List<String> contents;
    private ActivityProperties properties;

    
    Activity(String name, List<String> contents, ActivityProperties properties)
    {
        this.name = name;
        this.contents = contents;
        this.properties = properties;
    }

    public String getName()
    {
        return name;
    }

    public List<String> getContents()
    {
        return contents;
    }

    public ActivityProperties getProperties()
    {
        return properties;
    }
}
