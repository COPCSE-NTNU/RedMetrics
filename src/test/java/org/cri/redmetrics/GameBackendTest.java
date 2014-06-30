package org.cri.redmetrics;

import com.google.api.client.http.HttpResponseException;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;

public class GameBackendTest extends HttpBackendTest<TestGame> {

    static final String GAME_NAME = "Asteroids";
    static final String UPDATED_GAME_NAME = "Gasteroids";

    TestGame createdGame;

    public GameBackendTest() {
        super("game/", TestGame.class);
    }

    @Override
    public void init() throws IOException {
        resetCreatedGame();
    }

    void resetCreatedGame() throws IOException {
        TestGame game = new TestGame();
        game.setName(GAME_NAME);
        createdGame = post(game);
    }

    // CREATE

    @Test
    public void canCreateGame() throws IOException {
        assertThat(createdGame.getId()).isNotNull().isNotEqualTo(0);
        assertThat(createdGame.getApiKey()).isNotNull();
        assertThat(createdGame.getName()).isEqualTo(GAME_NAME);
    }

    @Test
    public void shouldForbidUnnamedGameCreation() throws IOException {
        try {
            post(new TestGame());
            failBecauseExceptionWasNotThrown(HttpResponseException.class);
        } catch (HttpResponseException e) {
            assertThat(e.getStatusCode()).isEqualTo(400);
        }
    }

    // READ

    @Test
    public void canReadGame() throws IOException {
        TestGame readGame = get(createdGame.getId());
        assertThat(readGame.getName()).isEqualTo(GAME_NAME);
    }

    @Test
    void shouldFailWhenReadingUnknownId() throws IOException {
        try {
            get(randomId());
            failBecauseExceptionWasNotThrown(HttpResponseException.class);
        } catch (HttpResponseException e) {
            assertThat(e.getStatusCode()).isEqualTo(404);
        }
    }

    // UPDATE

    @Test
    public void canUpdateGame() throws IOException {
        createdGame.setName(UPDATED_GAME_NAME);
        TestGame updatedGame = put(createdGame);
        assertThat(updatedGame.getName()).isEqualTo(UPDATED_GAME_NAME);
    }

    @Test
    public void shouldFailWhenUpdatingWithUrlIdDifferentThanContentId() throws IOException {
        try {
            put(path + randomId(), createdGame);
            failBecauseExceptionWasNotThrown(HttpResponseException.class);
        } catch (HttpResponseException e) {
            assertThat(e.getStatusCode()).isEqualTo(400);
        }
    }

    @Test
    public void shouldFailWhenUpdatingNegativeId() throws IOException {
        try {
            int id = -1;
            createdGame.setId(id);
            TestGame updatedGame = put(path + id, createdGame);
            failBecauseExceptionWasNotThrown(HttpResponseException.class);
        } catch (HttpResponseException e) {
            assertThat(e.getStatusCode()).isEqualTo(400);
        }
    }

    // DELETE

    @Test
    public void canDeleteGame() throws IOException {
        TestGame deletedGame = delete(createdGame.getId());
        resetCreatedGame();
        try {
            get(deletedGame.getId());
            failBecauseExceptionWasNotThrown(HttpResponseException.class);
        } catch (HttpResponseException e) {
            assertThat(e.getStatusCode()).isEqualTo(404);
        }
    }


}
