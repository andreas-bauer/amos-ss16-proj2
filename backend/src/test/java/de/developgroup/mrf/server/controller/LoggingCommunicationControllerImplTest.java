/**
 * This file is part of Mobile Robot Framework.
 * Mobile Robot Framework is free software under the terms of GNU AFFERO GENERAL PUBLIC LICENSE.
 */
package de.developgroup.mrf.server.controller;

import de.developgroup.mrf.server.ClientManager;
import de.developgroup.mrf.server.rpc.JsonRpc2Request;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class LoggingCommunicationControllerImplTest {

	ClientManager clientManager;
	LoggingCommunicationControllerImpl logCtrl;

	@Before
	public void setUp() throws Exception {
		clientManager = mock(ClientManager.class);
		logCtrl = new LoggingCommunicationControllerImpl(clientManager);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void sendLogEntriesToClientTest() throws Exception {
		List<String> list = new ArrayList<>();
		list.add("Test Log Entry");
		List<Object> resultList = new ArrayList<>();
		resultList.add(true);
		resultList.add(list.get(0));
		JsonRpc2Request notification = new JsonRpc2Request("incomingLogEntries",resultList);
		logCtrl.sendLogEntriesToClient(5002, list);
		verify(clientManager).notifyClientById(5002, notification);
	}

	@Test
	public void sendEmptyLogEntriesToClientTest() throws Exception {
		logCtrl.sendLogEntriesToClient(5002, null);
		verify(clientManager).notifyClientById(5002, getNoNewEntriesOrErrorRequest());
	}

	@Test
	public void handleNoEntryFoundTest() throws Exception {
		logCtrl.handleNoEntryFound(5002, "Test Entry");
		verify(clientManager).notifyClientById(5002, getNoNewEntriesOrErrorRequest());
	}

	@Test
    public void handleSystemUpTimeExceptionTest() {
        Exception exception = new Exception();
        logCtrl.handleSystemUpTimeException(5002, exception);
        verify(clientManager).notifyClientById(5002, getUpTimeErrorRequest());
    }

    @Test
    public void sendUpTimeToClientTest() {
        String upTimeString = "Test upTime";
        logCtrl.sendUpTimeToClient(5002, upTimeString);
        List<Object> params = new ArrayList<>();
        params.add(upTimeString);
        JsonRpc2Request request = new JsonRpc2Request("incomingSystemUpTime", params);
        verify(clientManager).notifyClientById(5002, request);
    }

    @Test
    public void getSystemUpTimeStringTest() {
        try {
            String upTimeString = logCtrl.getSystemUpTimeString();
            assert(!upTimeString.isEmpty());
        } catch (Exception ex) {
            //Test fails if an exception occurs
            assert(false);
        }

    }

	private JsonRpc2Request getNoNewEntriesOrErrorRequest() {
		List<Object> resultList = new ArrayList<>();
		resultList.add(false);
		return new JsonRpc2Request("incomingLogEntries",resultList);
	}

    private JsonRpc2Request getUpTimeErrorRequest() {
        List<Object> params = new ArrayList<>();
        params.add("ERROR");
        return new JsonRpc2Request("incomingSystemUpTime", params);
    }

}
