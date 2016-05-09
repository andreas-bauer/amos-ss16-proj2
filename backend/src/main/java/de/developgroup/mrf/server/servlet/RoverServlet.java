package de.developgroup.mrf.server.servlet;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import com.google.inject.Singleton;

import de.developgroup.mrf.server.socket.RoverSocket;

@Singleton
public class RoverServlet extends WebSocketServlet {
	@Override
	public void configure(WebSocketServletFactory webSocketServletFactory) {
		webSocketServletFactory.register(RoverSocket.class);
	}
}
