package eu.europa.ec.leos.annotate.integration.websockets;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import eu.europa.ec.leos.annotate.helper.SpotBugsAnnotations;
import eu.europa.ec.leos.annotate.model.UserInformation;
import eu.europa.ec.leos.annotate.services.AuthenticationService;
import eu.europa.ec.leos.annotate.websockets.AnnotateWebSocketHandler;
import eu.europa.ec.leos.annotate.websockets.WebSessionRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.socket.*;

import java.net.URI;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AnnotateWebSocketHandlerTest {

    private final static String TESTSES = "testSession";
    
    @Mock
    private AuthenticationService authService;

    @Mock
    private WebSessionRegistry webSessionRegistry;

    @InjectMocks
    private AnnotateWebSocketHandler annotateWebSocketHandler;

    @Test
    public void testDecodeParamaters() {
        assertEquals(0, annotateWebSocketHandler.decodeQueryString("&&&").size());
        assertEquals(1, annotateWebSocketHandler.decodeQueryString("&&&a=b").size());
        assertEquals(3, annotateWebSocketHandler.decodeQueryString("a=b&c=%26%25==&utf8=%E2%9C%93").size());
        assertEquals(1, annotateWebSocketHandler.decodeQueryString("latin1=%FC").size());
    }

    @Test
    public void testPing() throws Exception {
        // setup
        final WebSocketSession mockSession = mock(WebSocketSession.class);
        when(mockSession.getId()).thenReturn(TESTSES);
        when(mockSession.isOpen()).thenReturn(true);

        final TextMessage req = new TextMessage("{\"type\":\"ping\",\"id\":1}", true);
        // call
        annotateWebSocketHandler.handleTextMessage(mockSession, req);

        // verify
        final ArgumentCaptor<TextMessage> argument = ArgumentCaptor.forClass(TextMessage.class);
        verify(mockSession).sendMessage(argument.capture());
        assertEquals("pong", new ObjectMapper().readValue(argument.getValue().getPayload(), Map.class).get("type"));
    }

    @Test
    public void testWhoAmI_Authenticated() throws Exception {
        // setup
        final WebSocketSession mockSession = mock(WebSocketSession.class);
        final UserInformation userInformation = mock(UserInformation.class);
        when(mockSession.getId()).thenReturn(TESTSES);
        when(mockSession.isOpen()).thenReturn(true);
        when(webSessionRegistry.getSession(TESTSES)).thenReturn(mockSession);
        when(webSessionRegistry.getUserInfo(TESTSES)).thenReturn(userInformation);
        final TextMessage req = new TextMessage("{\"type\":\"whoami\",\"id\":1}", true);

        // call
        annotateWebSocketHandler.handleTextMessage(mockSession, req);

        // verify
        final ArgumentCaptor<TextMessage> argument = ArgumentCaptor.forClass(TextMessage.class);
        verify(mockSession).sendMessage(argument.capture());
        @SuppressWarnings("unchecked")
        final Map<String, String> payload = new ObjectMapper().readValue(argument.getValue().getPayload(), Map.class);
        assertEquals("whoyouare", payload.get("type"));
    }

    @Test
    @SuppressFBWarnings(value = SpotBugsAnnotations.ReturnValueIgnored, justification = SpotBugsAnnotations.ReturnValueIgnoredReason)
    public void testWhoAmI_Unauthenticated() throws Exception {
        // setup
        final WebSocketSession mockSession = mock(WebSocketSession.class);
        when(mockSession.getId()).thenReturn(TESTSES);
        when(mockSession.isOpen()).thenReturn(true);
        // No auth session present
        when(webSessionRegistry.getSession(TESTSES)).thenReturn(null);
        final TextMessage req = new TextMessage("{\"type\":\"whoami\",\"id\":1}", true);

        // call
        annotateWebSocketHandler.handleTextMessage(mockSession, req);

        // verify
        verify(webSessionRegistry, never()).getUserInfo(any(String.class));
        verify(mockSession, times(1)).close(CloseStatus.PROTOCOL_ERROR);
    }

    @Test
    @SuppressFBWarnings(value = SpotBugsAnnotations.ReturnValueIgnored, justification = SpotBugsAnnotations.ReturnValueIgnoredReason)
    public void testClientIdMsg_Authenticated() throws Exception {
        // setup
        final WebSocketSession mockSession = mock(WebSocketSession.class);
        final UserInformation userInformation = mock(UserInformation.class);
        when(mockSession.getId()).thenReturn(TESTSES);
        when(mockSession.isOpen()).thenReturn(true);
        when(webSessionRegistry.getUserInfo(TESTSES)).thenReturn(userInformation);
        when(webSessionRegistry.getSession(TESTSES)).thenReturn(mockSession);

        final TextMessage req = new TextMessage("{\"messageType\":\"client_id\",\"value\":\"955c9960-b1d1-42b2-a730-6ff4d7afc0e1\"}", true);

        // call
        annotateWebSocketHandler.handleTextMessage(mockSession, req);

        // verify
        verify(webSessionRegistry).getUserInfo(any(String.class));
    }

    @Test
    @SuppressFBWarnings(value = SpotBugsAnnotations.ReturnValueIgnored, justification = SpotBugsAnnotations.ReturnValueIgnoredReason)
    public void testClientIdMsg_Unauthenticated() throws Exception {
        // setup
        final WebSocketSession mockSession = mock(WebSocketSession.class);
        when(mockSession.getId()).thenReturn(TESTSES);
        when(mockSession.isOpen()).thenReturn(true);
        when(webSessionRegistry.getSession(TESTSES)).thenReturn(null);// no registered session

        final TextMessage req = new TextMessage("{\"messageType\":\"client_id\",\"value\":\"955c9960-b1d1-42b2-a730-6ff4d7afc0e1\"}", true);

        // call
        annotateWebSocketHandler.handleTextMessage(mockSession, req);

        // verify
        verify(webSessionRegistry, never()).getUserInfo(any(String.class));
    }

    @Test
    @SuppressFBWarnings(value = SpotBugsAnnotations.ReturnValueIgnored, justification = SpotBugsAnnotations.ReturnValueIgnoredReason)
    public void testAfterConnectionEstablished_LoginFailed() throws Exception {
        // setup
        final WebSocketSession mockSession = mock(WebSocketSession.class);
        when(mockSession.getId()).thenReturn(TESTSES);
        final URI uri = new URI("ws/?access_token=5769");
        when(mockSession.getUri()).thenReturn(uri);
        when(webSessionRegistry.getSession(eq(TESTSES))).thenReturn(null);// not registered

        when(authService.findUserByAccessToken(eq("5769"))).thenReturn(null);

        // call
        annotateWebSocketHandler.afterConnectionEstablished(mockSession);

        // verify
        verify(webSessionRegistry).getSession(any());
        verifyNoMoreInteractions(webSessionRegistry);
    }

    @Test
    public void testAfterConnectionEstablished_LoginPassedViaURI() throws Exception {
        // setup
        final WebSocketSession mockSession = mock(WebSocketSession.class);
        when(mockSession.getId()).thenReturn(TESTSES);
        final URI uri = new URI("ws/?access_token=5768");
        when(mockSession.getUri()).thenReturn(uri);
        when(webSessionRegistry.getSession(eq(TESTSES))).thenReturn(null);// not registered

        final UserInformation userInformation = mock(UserInformation.class);
        when(userInformation.getLogin()).thenReturn("victor");
        when(authService.findUserByAccessToken(eq("5768"))).thenReturn(userInformation);

        // call
        annotateWebSocketHandler.afterConnectionEstablished(mockSession);

        // verify
        verify(webSessionRegistry).registerSession(same(mockSession), same(userInformation));
    }

    @Test
    public void testAfterConnectionEstablished_NoAccessTokenFound() throws Exception {
        // setup
        final WebSocketSession mockSession = mock(WebSocketSession.class);
        when(mockSession.getId()).thenReturn(TESTSES);
        final URI uri = new URI("/-5768");// weblogic 1213 send this kind of message
        when(mockSession.getUri()).thenReturn(uri);
        when(webSessionRegistry.getSession(eq(TESTSES))).thenReturn(null);// not registered

        // call
        annotateWebSocketHandler.afterConnectionEstablished(mockSession);

        // verify
        verify(webSessionRegistry, never()).registerSession(any(), any());
    }

    @Test
    public void testAccesstoken_Unauthenticated_userLoginFound() throws Exception {
        // setup
        final UserInformation userInformation = mock(UserInformation.class);
        when(userInformation.getLogin()).thenReturn("victor");
        when(authService.findUserByAccessToken(eq("5768"))).thenReturn(userInformation);

        final WebSocketSession mockSession = mock(WebSocketSession.class);
        when(mockSession.getId()).thenReturn(TESTSES);
        when(mockSession.isOpen()).thenReturn(true);
        when(webSessionRegistry.getSession(TESTSES)).thenReturn(null);

        final TextMessage req = new TextMessage("{\"type\":\"access-token\",\"value\":\"5768\"}", true);

        // call
        annotateWebSocketHandler.handleTextMessage(mockSession, req);

        // verify
        verify(webSessionRegistry).registerSession(same(mockSession), same(userInformation));
    }

    @Test
    public void testAccesstoken_Unauthenticated_userLoginNotFound() throws Exception {
        // setup
        when(authService.findUserByAccessToken(eq("xyz"))).thenReturn(null);

        final WebSocketSession mockSession = mock(WebSocketSession.class);
        when(mockSession.getId()).thenReturn(TESTSES);
        when(mockSession.isOpen()).thenReturn(true);
        when(webSessionRegistry.getSession(TESTSES)).thenReturn(null);

        final TextMessage req = new TextMessage("{\"type\":\"access-token\",\"value\":\"xyz\"}", true);

        // call
        annotateWebSocketHandler.handleTextMessage(mockSession, req);

        // verify
        verify(webSessionRegistry, never()).registerSession(any(), any());
    }

    @Test
    public void testAccesstoken_AlreadyAuthenticated_resendAccessToken() throws Exception {
        // setup
        final WebSocketSession mockSession = mock(WebSocketSession.class);
        when(mockSession.getId()).thenReturn(TESTSES);
        when(mockSession.isOpen()).thenReturn(true);
        when(webSessionRegistry.getSession(TESTSES)).thenReturn(mockSession);

        final TextMessage req = new TextMessage("{\"type\":\"access-token\",\"value\":\"xyz\"}", true);

        // call
        annotateWebSocketHandler.handleTextMessage(mockSession, req);

        // verify that no registration takes place
        verify(webSessionRegistry, never()).registerSession(any(), any());
    }
}