package app;

import app.controllers.BattleController;
import app.controllers.CardController;
import app.controllers.UserController;
import app.daos.CardDao;
import app.daos.UserDao;
import app.repositories.CardRepository;
import app.repositories.UserWithCardsRepository;
import app.services.DatabaseConnection;
import http.ContentType;
import http.HttpStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import server.Request;
import server.Response;
import server.ServerApp;

import java.sql.Connection;


@Setter(AccessLevel.PRIVATE)
@Getter(AccessLevel.PRIVATE)
public class App implements ServerApp {


    private Connection connection;

    private UserController userController;
    private CardController cardController;
    private BattleController battleController;

    private CardDao cardDao;
    private UserDao userDao;

    private UserWithCardsRepository userWithCardsRepository;
    private CardRepository cardRepository;

    public App() {
        setConnection(new DatabaseConnection().getConnection());

        setUserDao(new UserDao(getConnection()));
        setCardDao(new CardDao(getConnection()));

        setUserWithCardsRepository(new UserWithCardsRepository(getUserDao(), getCardDao()));
        setCardRepository(new CardRepository(getCardDao()));

        setUserController(new UserController(getUserWithCardsRepository(), getCardDao() ,getUserDao()));
        setCardController(new CardController(getUserWithCardsRepository(), getCardRepository(), getCardDao(), getUserDao()));
        setBattleController(new BattleController(getUserWithCardsRepository(), getCardRepository(), getCardDao(), getUserDao()));
    }

    public Response handleRequest(Request request) {

        String token = request.getToken();
        if(token != null && !getUserWithCardsRepository().validateToken(token)) {
            return this.returnUnauthorizedResponse();
        }

        switch (request.getMethod()) {
            case POST: {
                //creates a new user
                if(request.getPathname().matches("/users")) {
                    return this.getUserController().createUser(request.getBody());
                }
                //login with existing user
                if(request.getPathname().matches("/sessions")) {
                    return this.getUserController().loginUser(request.getBody());
                }
                //create package
                if(request.getPathname().matches("/packages")) {
                    if(token == null || !token.equals("admin")) {
                        return this.returnUnauthorizedResponse();
                    } //only admin is allowed to create packages
                    return this.getCardController().createPackage(request.getBody());
                }
                //buy package
                if(request.getPathname().matches("/transactions/packages")) {
                    if(token == null) {
                        return this.returnUnauthorizedResponse();
                    } //only logged-in users are allowed to buy packages
                    return this.getCardController().openPackage(token);
                }
                //enter battle
                if(request.getPathname().matches("/battles")) {
                    if(token == null) {
                        return this.returnUnauthorizedResponse();
                    } //only logged-in users are allowed to enter a battle
                    return this.getBattleController().startBattle(token);
                }

            }
            case GET: {
                //retrieves user data for the given username
                if(request.getPathname().matches("/users/.+")) {
                    String username = request.getPathname().replace("/users/", "");
                    if(token == null || (!token.equals(username) && !token.equals("admin"))) {
                        return this.returnUnauthorizedResponse();
                    }
                    return this.getUserController().getUserByUsername(username);
                }
                //retrieves the stats for the user itself
                if(request.getPathname().matches("/stats")) {
                    if(token == null) {
                        return this.returnUnauthorizedResponse();
                    }
                    return this.getUserController().getUserScoreboard(token);
                }
                //retrieves all users scoreboard ordered by the ELO
                if(request.getPathname().matches("/scores")) {
                    if(token == null) {
                        return this.returnUnauthorizedResponse();
                    }
                    return this.getUserController().getAllScoreboards();
                }
                //shows user's card stack and deck
                if(request.getPathname().matches("/cards")) {
                    if(token == null) {
                        return this.returnUnauthorizedResponse();
                    }
                    return this.getCardController().getUserCards(token);
                }
                //shows own user's deck
                if(request.getPathname().matches("/decks")) {
                    if(token == null) {
                        return this.returnUnauthorizedResponse();
                    }
                    return this.getCardController().getUserDeck(token);
                }
                //unique feature shows other user's deck
                if(request.getPathname().matches("/decks/.+")) {
                    if(token == null) {
                        return this.returnUnauthorizedResponse();
                    }
                    String username = request.getPathname().replace("/decks/", "");
                    return this.getCardController().getUserDeck(username);
                }
            }
            case PUT: {
                //updates user data for the given username
                if(request.getPathname().matches("/users/.+")) {
                    String username = request.getPathname().replace("/users/", "");
                    if(token == null || (!token.equals(username) && !token.equals("admin"))) {
                        return this.returnUnauthorizedResponse();
                    }
                    return this.getUserController().updateUser(username, request.getBody());
                }
                //create user deck
                if(request.getPathname().matches("/decks")) {
                    if(token == null) {
                        return this.returnUnauthorizedResponse();
                    }
                    return this.getCardController().createDeck(token, request.getBody());
                }
            }
        }

        return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, "{ \"error\": \"not found\", \"data\": null }");
    }

    private Response returnUnauthorizedResponse() {
        return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{ \"error\": \"not authorized for this action\", \"data\": null }");
    }
}
