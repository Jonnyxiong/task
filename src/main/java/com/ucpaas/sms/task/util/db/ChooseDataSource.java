package com.ucpaas.sms.task.util.db;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class ChooseDataSource extends AbstractRoutingDataSource {

    @Override  
    protected Object determineCurrentLookupKey() {  
        return HandleDataSource.getDataSource();  
    }  
}
