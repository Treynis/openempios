package org.openhie.openempi.cluster;

import java.io.Serializable;

public class CommandRequest implements Serializable
{
    private static final long serialVersionUID = 2925735103911208737L;
    private ServiceName serviceName;
    private String requestName;
    private boolean hasResponse;
    private Serializable response;
    private Throwable exception;
    private boolean hasFailed;
    private Object[] params;

    public CommandRequest(ServiceName serviceName, String requestName, boolean hasResponse, final Serializable... args)
    {
        this.serviceName = serviceName;
        this.requestName = requestName;
        this.hasResponse = hasResponse;
        if (args.length > 0) {
            params = args;
        }
        this.hasFailed = false;
    }

    public ServiceName getServiceName() {
        return serviceName;
    }

    public void setServiceName(ServiceName serviceName) {
        this.serviceName = serviceName;
    }

    public String getRequestName() {
        return requestName;
    }

    public void setRequestName(String requestName) {
        this.requestName = requestName;
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    public boolean isHasResponse() {
        return hasResponse;
    }

    public void setHasResponse(boolean hasResponse) {
        this.hasResponse = hasResponse;
    }

    public Serializable getResponse() {
        return response;
    }

    public void setResponse(Serializable response) {
        this.response = response;
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
        setHasFailed(true);
    }

    public boolean isHasFailed() {
        return hasFailed;
    }

    public void setHasFailed(boolean hasFailed) {
        this.hasFailed = hasFailed;
    }
}
