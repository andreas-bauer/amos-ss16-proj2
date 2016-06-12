package de.developgroup.mrf.server.socket;

import java.io.IOException;
import java.util.Date;

import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import de.developgroup.mrf.server.ClientManager;
import de.developgroup.mrf.server.handler.DeveloperSettingsHandler;
import de.developgroup.mrf.server.handler.NotificationHandler;
import de.developgroup.mrf.server.handler.RoverHandler;
import de.developgroup.mrf.server.handler.SingleDriverHandler;
import de.developgroup.mrf.server.rpc.JsonRpc2Socket;

public class RoverSocket extends JsonRpc2Socket {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(RoverSocket.class);

	@Inject
	static RoverHandler roverHandler;

	@Inject
	static DeveloperSettingsHandler developerSettingsHandler;

	@Inject
	static SingleDriverHandler singleDriverHandler;

	@Inject
	static NotificationHandler notificationHandler;

	@Inject
	static ClientManager clientManager;

	public RoverSocket() {
	}

	@Override
	public void onWebSocketConnect(final Session sess) {
		super.onWebSocketConnect(sess);
		int newClientId = clientManager.addClient(sess);
		// if killswitch is enabled, notify the newly connected user
		developerSettingsHandler.notifyIfBlocked(newClientId,
				"Interactions with the rover are blocked at the moment");
	}

	@Override
	public void onWebSocketClose(int statusCode, String reason) {
		super.onWebSocketClose(statusCode, reason);
		clientManager.removeClosedSessions();
		singleDriverHandler.verifyDriverAvailability();
	}

	public String ping(Number sqn) {
		LOGGER.trace("ping({})", sqn);
		return roverHandler.handlePing(sqn.intValue());
	}

	public void driveForward(Number desiredSpeed) throws IOException {
		if (developerSettingsHandler.checkKillswitchEnabled()) {
			return;
		}
		LOGGER.trace("driveForeward({})", desiredSpeed);
		roverHandler.driveForward(desiredSpeed.intValue());
	}

	public void driveBackward(Number desiredSpeed) throws IOException {
		if (developerSettingsHandler.checkKillswitchEnabled()) {
			return;
		}
		LOGGER.trace("driveBackward({})", desiredSpeed);
		roverHandler.driveBackward(desiredSpeed.intValue());
	}

	public void stop() throws IOException {
		if (developerSettingsHandler.checkKillswitchEnabled()) {
			return;
		}
		LOGGER.trace("stop()");
		roverHandler.stop();
	}

	public void turnLeft(Number turnRate) throws IOException {
		if (developerSettingsHandler.checkKillswitchEnabled()) {
			return;
		}
		LOGGER.trace("turnLeft({})", turnRate);
		roverHandler.turnLeft(turnRate.intValue());
	}

	public void turnRight(Number turnRate) throws IOException {
		if (developerSettingsHandler.checkKillswitchEnabled()) {
			return;
		}
		LOGGER.trace("turnRight({})", turnRate);
		roverHandler.turnRight(turnRate.intValue());
	}

	public void turnHeadUp(Number angle) throws IOException {
		if (developerSettingsHandler.checkKillswitchEnabled()) {
			return;
		}
		LOGGER.trace("turnHeadUp({})", angle);
		roverHandler.turnHeadUp(angle.intValue());
	}

	public void turnHeadDown(Number angle) throws IOException {
		if (developerSettingsHandler.checkKillswitchEnabled()) {
			return;
		}
		LOGGER.trace("turnHeadDown({})", angle);
		roverHandler.turnHeadDown(angle.intValue());
	}

	public void turnHeadLeft(Number angle) throws IOException {
		if (developerSettingsHandler.checkKillswitchEnabled()) {
			return;
		}
		LOGGER.trace("turnHeadLeft({})", angle);
		roverHandler.turnHeadLeft(angle.intValue());
	}

	public void turnHeadRight(Number angle) throws IOException {
		if (developerSettingsHandler.checkKillswitchEnabled()) {
			return;
		}
		LOGGER.trace("turnHeadRight({})", angle);
		roverHandler.turnHeadRight(angle.intValue());
	}

	public void resetHeadPosition() throws IOException {
		if (developerSettingsHandler.checkKillswitchEnabled()) {
			return;
		}
		LOGGER.trace("resetHeadPosition()");
		roverHandler.resetHeadPosition();
	}

	public void setKillswitch(Boolean killswitchEnabled,
			String notificationMessage) throws IOException {
		developerSettingsHandler.setKillswitchEnabled(killswitchEnabled,
				notificationMessage);
	}

	public void getCameraSnapshot(Number clientId) throws IOException {
		if (developerSettingsHandler.checkKillswitchEnabled()) {
			return;
		}
		LOGGER.trace("getCameraSnapshot()");
		roverHandler.getCameraSnapshot(clientId.intValue());
	}

	public void getLoggingEntries(Number clientId, String lastEntry) throws IOException {
		LOGGER.trace("getLoggingEntries()");
		roverHandler.getLoggingEntries(clientId.intValue(), lastEntry);
	}

	// TODO: Delete if not needed
	public Boolean getKillswitchState() {
		return developerSettingsHandler.isKillswitchEnabled();
	}

	// TODO: Clarify: unused in frontend --> method gets never called?
	public void sendKillswitchState() {
		developerSettingsHandler.notifyClientsAboutButtonState();
	}

	public void distributeAlertNotification(String alertMsg) {
		notificationHandler.distributeAlertNotification(alertMsg);
	}

	public void enterDriverMode(Number clientId) {
		singleDriverHandler.acquireDriver(clientId.intValue());
	}

	public void exitDriverMode(Number clientId) {
		singleDriverHandler.releaseDriver(clientId.intValue());
	}

}
