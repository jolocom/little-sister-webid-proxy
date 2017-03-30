package com.jolocom.webidproxy.websocket;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.Decoder;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
import javax.websocket.Extension;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketClientEndpoint extends javax.websocket.Endpoint {

	private static final Logger log = LoggerFactory.getLogger(WebSocketClientEndpoint.class);

	private Session session;

	public static WebSocketClientEndpoint connect(WebSocketClient webSocketClient, URI webSocketEndpointUri) throws Exception {

		// create client container

		WebSocketContainer webSocketContainer = ContainerProvider.getWebSocketContainer();

		// set default timeout

		long oldDefaultMaxSessionIdleTimeout = webSocketContainer.getDefaultMaxSessionIdleTimeout();
		long newDefaultMaxSessionIdleTimeout = 0;
		webSocketContainer.setDefaultMaxSessionIdleTimeout(newDefaultMaxSessionIdleTimeout);

		if (log.isDebugEnabled()) log.debug("Changed default max session idle timeout from " + oldDefaultMaxSessionIdleTimeout + " to " + newDefaultMaxSessionIdleTimeout);

		// connect

		return connect(webSocketContainer, webSocketClient, webSocketEndpointUri);
	}

	private static WebSocketClientEndpoint connect(WebSocketContainer webSocketContainer, WebSocketClient webSocketClient, URI webSocketEndpointUri) throws Exception {

		// init websocket endpoint

		List<String> preferredSubprotocols = Arrays.asList(new String[] { });
		List<Extension> extensions = null;
		List<Class<? extends Encoder>> encoders = null;
		List<Class<? extends Decoder>> decoders = null;

		ClientEndpointConfig.Configurator clientEndpointConfigConfigurator = new ClientEndpointConfig.Configurator() {

		};

		ClientEndpointConfig.Builder clientEndpointConfigBuilder = ClientEndpointConfig.Builder.create();

		clientEndpointConfigBuilder.preferredSubprotocols(preferredSubprotocols);
		clientEndpointConfigBuilder.extensions(extensions);
		clientEndpointConfigBuilder.encoders(encoders);
		clientEndpointConfigBuilder.decoders(decoders);
		clientEndpointConfigBuilder.configurator(clientEndpointConfigConfigurator);

		ClientEndpointConfig clientEndpointConfig = clientEndpointConfigBuilder.build();
		clientEndpointConfig.getUserProperties().put("WebSocketClient", webSocketClient);
		clientEndpointConfig.getUserProperties().put("WebSocketEndpointUri", webSocketEndpointUri);

		// connect websocket endpoint

		WebSocketClientEndpoint webSocketEndpoint = new WebSocketClientEndpoint();

		Session session = webSocketContainer.connectToServer(webSocketEndpoint, clientEndpointConfig, URI.create(webSocketEndpointUri.toString()));
		webSocketEndpoint.setSession(session);

		// done

		log.info("Connected WebSocket endpoint for " + webSocketEndpointUri + " with preferred subprotocols " + preferredSubprotocols);
		return webSocketEndpoint;
	}

	@Override
	public void onOpen(Session session, EndpointConfig endpointConfig) {

		// set timeout

		long oldMaxIdleTimeout = session.getMaxIdleTimeout();
		long newMaxIdleTimeout = 0;
		session.setMaxIdleTimeout(newMaxIdleTimeout);

		if (log.isDebugEnabled()) log.debug("Changed max idle timeout of session " + session.getId() + " from " + oldMaxIdleTimeout + " to " + newMaxIdleTimeout);

		// read properties

		ClientEndpointConfig clientEndpointConfig = (ClientEndpointConfig) endpointConfig;

		URI webSocketEndpointUri = (URI) clientEndpointConfig.getUserProperties().get("WebSocketEndpointUri");

		// init message handler

		WebSocketClientMessageHandler webSocketMessageHandler = new WebSocketClientMessageHandler(session);

		// init session

		log.info("WebSocket session " + session.getId() + " opened (" + webSocketEndpointUri + ").");

		session.addMessageHandler(webSocketMessageHandler);
		session.getUserProperties().putAll(clientEndpointConfig.getUserProperties());
	}

	@Override
	public void onClose(Session session, CloseReason closeReason) {

		log.info("WebSocket session " + session.getId() + " closed.");

		WebSocketClient webSocketClient = (WebSocketClient) session.getUserProperties().get("webSocketClient");
		webSocketClient.close();
	}

	@Override
	public void onError(Session session, Throwable throwable) {

		log.error("WebSocket session " + (session != null ? session.getId() : session) + " problem: " + throwable.getMessage(), throwable);

		if (session != null) {

			WebSocketClient webSocketClient = (WebSocketClient) session.getUserProperties().get("webSocketClient");
			webSocketClient.close();
		}
	}

	/*
	 * Getters and setters
	 */

	public Session getSession() {

		return this.session;
	}

	public void setSession(Session session) {

		this.session = session;
	}
}