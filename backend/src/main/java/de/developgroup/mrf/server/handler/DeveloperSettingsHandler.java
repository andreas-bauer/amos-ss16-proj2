package de.developgroup.mrf.server.handler;

import com.google.inject.Inject;
import de.developgroup.mrf.rover.collision.CollisionController;
import de.developgroup.mrf.server.ClientManager;
import de.developgroup.mrf.server.rpc.JsonRpc2Request;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class DeveloperSettingsHandler implements Observer{

    private static final Logger LOGGER = LoggerFactory
            .getLogger(DeveloperSettingsHandler.class);
    protected boolean killswitchEnabled = false;

    protected ClientManager clientManager;

    protected RoverHandler roverHandler;


    @Inject
    public DeveloperSettingsHandler(
                               ClientManager clientManager, RoverHandler roverHandler) {
        LOGGER.debug("Creating new instance of DeveloperSettingsHandler");
        this.clientManager = clientManager;
        this.roverHandler = roverHandler;
        // Ovserve clientManger about connected user changes
        clientManager.addObserver(this);
    }

    /**
     * Checks whether rover-actions are being blocked by a developer via the killswitch
     * If so, logs this to console
     *
     * @return returns true if actions are blocked, false otherwise
     */
    public boolean checkKillswitchEnabled() {
        if(killswitchEnabled) {
            LOGGER.trace("Developer blocked this action");
        }
        return killswitchEnabled;
    }

    /**
     * Simple Getter for killswitchState
     * @return killswitchState
     */
    public boolean isKillswitchEnabled() {
        return killswitchEnabled;
    }

    /**
     * Blocks or unblocks rover-actions
     *
     * @param newState if true, actions get blocked, if false, clients can steer the rover
     * @param notificationMessage message that should be broadcasted to the clients so they know
     *                            the killswitch was pressed
     */
    public void setKillswitchEnabled(Boolean newState, String notificationMessage) throws IOException {
        LOGGER.debug("Killswitch state is: " + newState);

        // determine whether client needs to get message
        // should always be the case but just to be safe
        boolean notifyClients = false;
        if(killswitchEnabled != newState.booleanValue()){
            notifyClients = true;
        }
        if (newState) {
            roverHandler.stop();
            killswitchEnabled = true;
        }
        else{
            killswitchEnabled = false;
        }
        notifyClientsAboutButtonState();
        if(notifyClients){
            notifyClientsAboutBlockingState(notificationMessage);
        }
    }

    /**
     * Changes the Switch in the client's developer view according the killswitch State
     */
    public void notifyClientsAboutButtonState() {

        // create JSON RPC object
        ArrayList<Object> params = new ArrayList<>();
        params.add(killswitchEnabled);

        JsonRpc2Request jsonRpc2Request = new JsonRpc2Request("updateKillswitchEnabled", params);

        LOGGER.debug("informing clients about Killswitch-State: "+ killswitchEnabled);
        clientManager.notifyAllClients(jsonRpc2Request);
    }

    /**
     * Changes the List in the client's developer view according the connectedUsersList
     */
    public void notifyClientsAboutConnectedUsers(String[] connectedUsers) {

        // create JSON RPC object
        ArrayList<Object> params = new ArrayList<>();
        params.add(connectedUsers);

        JsonRpc2Request jsonRpc2Request = new JsonRpc2Request("updateConnectedUsers", params);

        LOGGER.debug("informing clients about connected users: "+ connectedUsers);
        clientManager.notifyAllClients(jsonRpc2Request);
    }

    /**
     * Sends a message to all clients so they know the developer just
     * changed the killswitch state
     * @param message message text
     */
    protected void notifyClientsAboutBlockingState(String message){
        JsonRpc2Request notification = new JsonRpc2Request(
                "showAlertNotification", message);

        clientManager.notifyAllClients(notification);
    }

    /**
     * Sends a message to one specific client so he knows the killswitch is active and he can't interact with the rover
     * The method should be called if a new client connects to the server
     * @param clientId the client who should receive the message
     * @param message message text
     */
    public void notifyIfBlocked(int clientId, String message){
        if(!killswitchEnabled){
            return;
        }
        JsonRpc2Request notification = new JsonRpc2Request(
                "showAlertNotification", message);

        clientManager.notifyClientById(clientId, notification);
    }

    /**
     * This method gets called if information in the Client Manager change.
     * A list of connected users containing additional information is generated and send out to the clients
     * to be displayed in the developer view
     * @param o Observable, in this Case the clientManager
     * @param arg not used
     */
    @Override
    public void update(Observable o, Object arg) {
        Map<Integer, Session> sessions = clientManager.getSessions();
        Map<Integer, String> clientInfo = clientManager.getClientInformation();
        int numberOfConnectedClients = sessions.size();
        String[] connectedUsers = new String[numberOfConnectedClients];
        int count = 0;

        for (Map.Entry<Integer, Session> entry : sessions.entrySet()) {
            int clientId = entry.getKey();
            String ipAddress = entry.getValue().getRemoteAddress().getHostString();
            String additionalInformation = clientInfo.get(clientId);
            connectedUsers[count++] = "IP Address: "+ ipAddress + "   ClientID: "+clientId+
                    "  " + additionalInformation;
        }
        notifyClientsAboutConnectedUsers(connectedUsers);
    }
}
