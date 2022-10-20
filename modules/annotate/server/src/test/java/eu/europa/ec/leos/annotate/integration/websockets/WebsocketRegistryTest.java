package eu.europa.ec.leos.annotate.integration.websockets;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import eu.europa.ec.leos.annotate.model.UserInformation;
import eu.europa.ec.leos.annotate.websockets.WebSessionRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.socket.WebSocketSession;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
public class WebsocketRegistryTest {

    private static final String SESSIONNAME = "testSession";
    
    @InjectMocks
    private WebSessionRegistry registry;

    @Before
    public void setUp() {
        // nothing to do
    }

    @Test
    public void testRegister() {
        //setup
        final WebSocketSession mockSession = Mockito.mock(WebSocketSession.class);
        Mockito.when(mockSession.getId()).thenReturn(SESSIONNAME);
        final UserInformation userInformation = Mockito.mock(UserInformation.class);

        //call
        registry.registerSession(mockSession, userInformation);

        //verify
        assertEquals(mockSession, registry.getSession(SESSIONNAME));
    }


    @Test
    public void testUnRegister() {
        //setup
        final WebSocketSession mockSession = Mockito.mock(WebSocketSession.class);
        Mockito.when(mockSession.getId()).thenReturn(SESSIONNAME);
        final UserInformation userInformation = Mockito.mock(UserInformation.class);
        registry.registerSession(mockSession, userInformation);
        assertEquals(mockSession, registry.getSession(SESSIONNAME));

        registry.unregisterSession(SESSIONNAME);

        //verify
        assertNull(registry.getSession(SESSIONNAME));
    }

    @Test
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    public void testUserInfo() {
        //setup
        final WebSocketSession mockSession = Mockito.mock(WebSocketSession.class);
        Mockito.when(mockSession.getId()).thenReturn(SESSIONNAME);
        final UserInformation userInformation = Mockito.mock(UserInformation.class);
        registry.registerSession(mockSession, userInformation);
        assertEquals(mockSession, registry.getSession(SESSIONNAME));

        //verify
        assertEquals(userInformation, registry.getUserInfo(SESSIONNAME));
    }

}