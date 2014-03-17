package org.openhie.openempi.cluster;

import java.io.Serializable;
import java.util.concurrent.Callable;

import org.openhie.openempi.context.Context;

public class RemoteRequestTask implements Callable<CommandRequest>, Serializable
{
    private static final long serialVersionUID = 2925735103911208737L;
    public CommandRequest request;

    public RemoteRequestTask(CommandRequest request) {
        this.request = request;
    }
    
    public CommandRequest call() throws Exception {
        Context.getClusterManager().processRequest(request);
        return request;
    }
}
