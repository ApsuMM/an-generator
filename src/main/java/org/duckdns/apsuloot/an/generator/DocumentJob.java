package org.duckdns.apsuloot.an.generator;

import java.time.LocalDate;
import java.util.List;

public class DocumentJob extends BuisnessWeek {

    private List<LinkedActivity> linkedActivities;

    DocumentJob(LocalDate start, LocalDate end, int weekOfyear, int hours, List<LinkedActivity> activities)
    {
        super(start, end, weekOfyear, hours);
        this.linkedActivities = activities;
        // TODO Auto-generated constructor stub
    }

    public List<LinkedActivity> getLinkedActivities()
    {
        return linkedActivities;
    }

}
