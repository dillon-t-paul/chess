package service;

import dataaccess.MemoryDataAccess;
import datamodels.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import services.ServiceActions;
import services.ServiceException;

import static org.junit.jupiter.api.Assertions.*;

class ServiceActionsTest {
    private ServiceActions serviceActions;

    @BeforeEach
    void setUp() {
        serviceActions = new ServiceActions(new MemoryDataAccess());
    }

    @Test
    void registerCreatesUserAndAuthToken() throws Exception {
        AuthData auth = register("Billy");

        assertEquals("Billy", auth.username());
        assertNotNull(auth.authToken());
        assertFalse(auth.authToken().isBlank());
    }

    @Test
    void registerRejectsMissingOrBlankFields() {
        assertStatus(400, () ->
                serviceActions.registerUser(null, "password", "example@example.com"));
        assertStatus(400, () ->
                serviceActions.registerUser("Billy", " ", "example@example.com"));
        assertStatus(400, () ->
                serviceActions.registerUser("Billy", "password", null));
    }

    @Test
    void registerRejectsDuplicateUsername() throws Exception {
        register("Billy");

        assertStatus(403, () ->
                serviceActions.registerUser(
                        "Billy", "different-password", "other@example.com"));
    }

    @Test
    void loginAcceptsCorrectPasswordAndCreatesToken() throws Exception {
        register("Billy");

        AuthData auth = serviceActions.loginUser("Billy", "password");

        assertEquals("Billy", auth.username());
        assertNotNull(auth.authToken());
        assertFalse(auth.authToken().isBlank());
    }

    @Test
    void loginRejectsUnknownUserAndWrongPassword() throws Exception {
        register("Billy");

        assertStatus(401, () ->
                serviceActions.loginUser("unknown", "password"));
        assertStatus(401, () ->
                serviceActions.loginUser("Billy", "wrong-password"));
    }

    @Test
    void loginRejectsMissingFields() {
        assertStatus(400, () ->
                serviceActions.loginUser(null, "password"));
        assertStatus(400, () ->
                serviceActions.loginUser("Billy", ""));
    }

    @Test
    void logoutInvalidatesAuthToken() throws Exception {
        AuthData auth = register("Billy");

        serviceActions.logoutUser(auth.authToken());

        // A second logout checks that the first one removed the token.
        assertStatus(401, () ->
                serviceActions.logoutUser(auth.authToken()));
    }

    @Test
    void resetDBClearsUsersAndTokens() throws Exception {
        AuthData oldAuth = register("Billy");

        serviceActions.resetDB();

        assertStatus(401, () ->
                serviceActions.logoutUser(oldAuth.authToken()));

        // Registering Billy again confirms the old user was cleared.
        AuthData newAuth = register("Billy");
        assertEquals("Billy", newAuth.username());
    }

    private AuthData register(String username) throws Exception {
        return serviceActions.registerUser(
                username,
                "password",
                username + "@example.com");
    }

    private void assertStatus(int expectedStatus, Executable action) {
        ServiceException exception =
                assertThrows(ServiceException.class, action);
        assertEquals(expectedStatus, exception.status());
    }
}