package combinedprojects.chat;

import combinedprojects.Mapping;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;


@ServerEndpoint(value = "/chat")
public class WebSocketChat {

    private static final Logger log = LogManager.getLogger();

    private static final String GUEST_PREFIX = "Guest";
    private static final AtomicInteger connectionIds = new AtomicInteger(0);
    private static final Set<WebSocketChat> connections = new CopyOnWriteArraySet<>();

    private final String nickname;
    private Session session;
    private final String login;

    public WebSocketChat() {
        login = Mapping.keymap.get("login");
        if (login != null) {
            nickname = login;
        } else {
            nickname = GUEST_PREFIX + connectionIds.getAndIncrement();
        }
    }

    @OnOpen
    public void start(Session session) {
        this.session = session;
        connections.add(this);
        String message = String.format("* %s %s", nickname, "has joined.");
        log.info(String.format("* %s %s", nickname, "has joined."));
        broadcast(message);
    }

    @OnClose
    public void end() {
        connections.remove(this);
        String message = String.format("* %s %s", nickname, "has disconnected.");
        log.info(String.format("* %s %s", nickname, "has disconnected."));
        broadcast(message);
    }

    @OnMessage
    public void incoming(String message) {
        // Never trust the client
        String filteredMessage = String.format("%s: %s",nickname, HTMLFilter.filter(message.toString()));
        log.info(String.format("%s: %s", nickname, HTMLFilter.filter(message.toString())));
        broadcast(filteredMessage);
    }

    @OnError
    public void onError(Throwable t) throws Throwable {
        log.error("Chat Error: " + t.toString(), t);
    }


    private static void broadcast(String msg) {
        for (WebSocketChat client : connections) {
            try {
                synchronized (client) {
                    client.session.getBasicRemote().sendText(msg);
                }
            } catch (IOException e) {
                log.debug("Chat Error: Failed to send message to client", e);
                connections.remove(client);
                try {
                    client.session.close();
                } catch (IOException e1) {
                    // Ignore
                }
                String message = String.format("* %s %s",
                        client.nickname, "has been disconnected.");
                broadcast(message);
            }
        }
    }
}
