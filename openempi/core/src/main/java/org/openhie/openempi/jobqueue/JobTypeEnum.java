package org.openhie.openempi.jobqueue;

public enum JobTypeEnum
{
    FILE_IMPORT(1, "File Import"),
    BLOCKING_INITIALIZATION(2, "Blocking Initialization"),
    MATCHING_INITIALIZATION(3, "Matching Initialization"),
    MATCH_REPOSITORY_DATA(4, "Match Repository Data"),
    ENTITY_IMPORT(5, "Entity Import"),
    DATA_PROFILING(6, "Data Profiling"),
    GENERATE_CUSTOM_FIELDS(7, "Generate Custom Fields"),
    ASSIGN_GLOBAL_IDENTIFIERS(8, "Assign Global Identifiers");
    
    private int code;
    private String name;
    
    private JobTypeEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getName() {
        return name;
    }
}
