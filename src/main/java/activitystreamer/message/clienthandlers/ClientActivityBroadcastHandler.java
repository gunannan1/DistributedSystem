package activitystreamer.message.clienthandlers;

import activitystreamer.client.ClientSkeleton;
import activitystreamer.message.MessageHandler;
import activitystreamer.server.Connection;
import com.google.gson.JsonObject;

/*
author Yiru Pan
date 14/4/18
 */
public class ClientActivityBroadcastHandler extends MessageHandler {
    private final ClientSkeleton clientSkeleton;

    public ClientActivityBroadcastHandler(ClientSkeleton clientSkeleton)
    {
        this.clientSkeleton=clientSkeleton;
    }

    @Override
    public boolean processMessage(JsonObject json, Connection connection) {
       clientSkeleton.log.info("Client received broadcastToAll activity from server");

        return true;
    }
}
