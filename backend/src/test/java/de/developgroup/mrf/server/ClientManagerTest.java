/**
 * This file is part of Mobile Robot Framework.
 * Mobile Robot Framework is free software under the terms of GNU AFFERO GENERAL PUBLIC LICENSE.
 */
package de.developgroup.mrf.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.inject.Injector;
import de.developgroup.mrf.server.handler.ClientInformationHandler;
import de.developgroup.mrf.server.handler.ClientInformationHandlerImpl;
import de.developgroup.mrf.server.handler.SingleDriverHandler;
import de.developgroup.mrf.server.rpc.JsonRpc2Request;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ClientManagerTest {

    private Injector injector;
    private static ClientManagerImpl clientManager;
    private ClientInformationHandler clientInformationHandler = mock(ClientInformationHandlerImpl.class);
	private SingleDriverHandler singleDriverHandler = mock(SingleDriverHandler.class);

	private static Session session;
	private static RemoteEndpoint remoteEndpoint;

	private String sendFirstClientMsg = "{\"method\":\"setClientId\",\"params\":[5000],\"jsonrpc\":\"2.0\"}";
	private String sendSecondClientMsg = "{\"method\":\"setClientId\",\"params\":[5000],\"jsonrpc\":\"2.0\"}";

    @BeforeClass
    public static void setUpBeforeAll() throws IOException {
    }

	@Before
	public void setUp() throws Exception {
		clientManager = new ClientManagerImpl(clientInformationHandler, singleDriverHandler);

		// mocking session
		session = mock(Session.class);
		remoteEndpoint = mock(RemoteEndpoint.class);
		when(session.getRemote()).thenReturn(remoteEndpoint);
		when(session.getRemoteAddress()).thenReturn(new InetSocketAddress(0));
		when(session.isOpen()).thenReturn(false);
	}

	@After
	public void tearDown() throws Exception {
		clientManager.removeClosedSessions();
		injector = null;
	}

	@Test
	public void testConnectedCllientCountIsEmpty() {
		int size = clientManager.getConnectedClientsCount();
		assertEquals(0, size);
	}

	@Test
	public void testNoClientConnectedAtBegin() {
		assertTrue(clientManager.isNoClientConnected());
	}

	@Test
	public void testAddSessions(){
		clientManager.addClient(session);
		assertEquals(1,clientManager.getSessions().size());
	}

	@Test
	public void testAddAndRemoveClients() throws IOException {
		clientManager.addClient(session);
		verify(remoteEndpoint).sendString(sendFirstClientMsg);
		assertEquals(1, clientManager.getConnectedClientsCount());

		clientManager.addClient(session);
		verify(remoteEndpoint).sendString(sendSecondClientMsg);
		assertEquals(2, clientManager.getConnectedClientsCount());

		// remove all session
		verify(remoteEndpoint, atLeastOnce()).sendString(anyString());
		clientManager.removeClosedSessions();
		assertEquals(0, clientManager.getConnectedClientsCount());
	}

	@Test
	public void testNotifyClientByIdText() throws IOException {
		clientManager.addClient(session);
		// send id to client
		verify(remoteEndpoint, atLeastOnce()).sendString(sendFirstClientMsg);
		// notification that a new client has connected to the server
		verify(remoteEndpoint, atLeastOnce()).sendString(anyString());

		clientManager.notifyClientById(5000, "Test Another Notification");
		String notificationMsg = "{\"method\":\"incomingNotification\",\"params\":[\"Test Another Notification\"],\"jsonrpc\":\"2.0\"}";
		verify(remoteEndpoint).sendString(notificationMsg);

	}

	@Test
	public void testNotifyClientById() throws IOException {
		clientManager.addClient(session);
		// send id to client
		verify(remoteEndpoint, atLeastOnce()).sendString(sendFirstClientMsg);
		// notification that a new client has connected to the server
		verify(remoteEndpoint, atLeastOnce()).sendString(anyString());

		List<Object> params = new ArrayList<>();
		params.add("testParam");
		JsonRpc2Request notification = new JsonRpc2Request("Notification 123",
				params);
		clientManager.notifyClientById(5000, notification);

		String notificationMsg = "{\"method\":\"Notification 123\",\"params\":[\"testParam\"],\"jsonrpc\":\"2.0\"}";
		verify(remoteEndpoint, atLeastOnce()).sendString(notificationMsg);

	}

	@Test
	public void testNotifyAllClientsText() throws IOException {
		clientManager.addClient(session);
		clientManager.addClient(session);

		clientManager.notifyAllClients("Test Notification");
		String notificationMsg = "{\"method\":\"incomingNotification\",\"params\":[\"Test Notification\"],\"jsonrpc\":\"2.0\"}";
		verify(remoteEndpoint, times(2)).sendString(notificationMsg);
	}

	@Test
	public void testNotifyAllClients() throws IOException {
		clientManager.addClient(session);
		clientManager.addClient(session);

		List<Object> params = new ArrayList<>();
		params.add("testParam");
		JsonRpc2Request notification = new JsonRpc2Request("Notification 456",
				params);

		clientManager.notifyAllClients(notification);
		String notificationMsg = "{\"method\":\"Notification 456\",\"params\":[\"testParam\"],\"jsonrpc\":\"2.0\"}";
		verify(remoteEndpoint, times(2)).sendString(notificationMsg);
	}

	@Test
	public void testIsClientConnected() {
		clientManager.addClient(session);
		assertTrue(clientManager.isClientConnected(5000));
	}

    @Test
    public void testGetUnblockedConnections() {

        clientManager.getUnblockedConnections();
		verify(clientInformationHandler).getUnblockedConnections();
    }

	@Test
	public void testGetBlockedConnections() {

		clientManager.getBlockedConnections();
		verify(clientInformationHandler).getBlockedConnections();
	}

    @Test
    public void testSetClientInformationContent() {
        clientManager.setClientInformation(5000, "Firefox", "Windows");

        verify(clientInformationHandler).addClientInformation(5000, "Firefox", "Windows");
    }

	@Test
	public void testBlockIp(){
		String ipAddress = "123.456.789";
		clientManager.blockIp(ipAddress);
		verify(clientInformationHandler).blockIp(ipAddress);
	}

	@Test
	public void testUnblockIp(){
		String ipAddress = "123.456.789";
		clientManager.unblockIp(ipAddress);
		verify(clientInformationHandler).unblockIp(ipAddress);
	}

	@Test
	public void testClientIsBlocked(){
		String ipAddress = "123.456.789";
		clientManager.clientIsBlocked(ipAddress);
		verify(clientInformationHandler).isBlocked(ipAddress);
	}

	@Test
	public void testClientIdIsBlocked(){
		int clientId = 1337;
		clientManager.clientIdIsBlocked(clientId);
		verify(clientInformationHandler).isBlocked(clientId);
	}

	@Test
	public void testReleaseDriverIfBlocked(){
		clientManager.releaseDriverIfBlocked();
		verify(clientInformationHandler).releaseDriverIfBlocked();
	}

	@Test
	public void testReleaseDriver() throws IOException {
		when(singleDriverHandler.getCurrentDriverId()).thenReturn(42);
		singleDriverHandler.releaseDriver(42);
		verify(singleDriverHandler).releaseDriver(42);
	}
}
