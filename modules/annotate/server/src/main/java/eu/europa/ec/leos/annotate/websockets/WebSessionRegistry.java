package eu.europa.ec.leos.annotate.websockets;

import eu.europa.ec.leos.annotate.model.UserInformation;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class WebSessionRegistry {
    /* lookup that holds all users and their sessions */
    private final ConcurrentMap<String, UserInformation> sessionUserInfo = new ConcurrentHashMap<>();

    /* lookup across all sessions by id */
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    private final Object lock = new Object();

    public WebSocketSession getSession(final String sessionId) {
        return this.sessions.get(sessionId);
    }

    public UserInformation getUserInfo(final String sessionId) {
        return this.sessionUserInfo.get(sessionId);
    }

    public void registerSession(final WebSocketSession session, final UserInformation userInformation) {
        Assert.notNull(session, "Session ID must not be null");
        Assert.notNull(session.getId(), "Session ID must not be null");

        synchronized (this.lock) {
            final WebSocketSession ses = this.sessions.get(session.getId());
            if (ses == null) {
                this.sessions.put(session.getId(), session);
                this.sessionUserInfo.put(session.getId(), userInformation);
            }
            //else Do nothing. Don't register second time.
        }
    }

    public void unregisterSession(final String sessionId) {
        Assert.notNull(sessionId, "Session ID must not be null");
        synchronized (lock) {
            this.sessions.remove(sessionId);
            this.sessionUserInfo.remove(sessionId);
        }
    }
}
