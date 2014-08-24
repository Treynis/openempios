package org.openhie.openempi.loader;

public class FileLoaderResults
{
    private int recordProcessed;
    private int recordsLoaded;
    private int recordsErrored;
    private String errorMessage;
    private boolean loadingFailed = false;
    
    public String getErrorMessage() {
        return errorMessage;
    }
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    public boolean isLoadingFailed() {
        return loadingFailed;
    }
    public void setLoadingFailed(boolean loadingFailed) {
        this.loadingFailed = loadingFailed;
    }
    public int getRecordProcessed() {
        return recordProcessed;
    }
    public void setRecordProcessed(int recordProcessed) {
        this.recordProcessed = recordProcessed;
    }
    public int getRecordsLoaded() {
        return recordsLoaded;
    }
    public void setRecordsLoaded(int recordsLoaded) {
        this.recordsLoaded = recordsLoaded;
    }
    public int getRecordsErrored() {
        return recordsErrored;
    }
    public void setRecordsErrored(int recordsErrored) {
        this.recordsErrored = recordsErrored;
    }
}
