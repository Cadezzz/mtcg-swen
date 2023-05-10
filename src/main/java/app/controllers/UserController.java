package app.controllers;

import app.daos.CardDao;
import app.daos.UserDao;
import app.models.User;
import app.models.UserScoreboard;
import app.models.UserWithCards;
import app.repositories.UserWithCardsRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import http.ContentType;
import http.HttpStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import server.Response;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

@Setter(AccessLevel.PRIVATE)
@Getter(AccessLevel.PRIVATE)
public class UserController extends Controller {

    private UserWithCardsRepository userWithCardsRepository;
    private UserDao userDao;
    private CardDao cardDao;

    public UserController(UserWithCardsRepository userWithCardsRepository, CardDao cardDao, UserDao userDao) {
        setUserWithCardsRepository(userWithCardsRepository);
        setCardDao(cardDao);
        setUserDao(userDao);
    }

    // GET /users/username
    public Response getUserByUsername(String username) {
        try {
            User user = userDao.read(username);

            if (user == null){
                return new Response(
                    HttpStatus.NOT_FOUND,
                    ContentType.JSON,
                    "{ \"error\": \"username not found\", \"data\": null }"
                );
            }

            String userJSON = getObjectMapper().writeValueAsString(user);
            return new Response(
                HttpStatus.OK,
                ContentType.JSON,
                "{ \"data\": " + userJSON + ", \"error\": null }"
            );

        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new Response(
                    HttpStatus.BAD_REQUEST,
                    ContentType.JSON,
                    "{ \"error\": \"bad request\", \"data\": null }"

            );
        }
    }

    // UPDATE /users/username
    public Response updateUser(String username, String request) {
        try {
            //read new user data from json request and save to user object
            User userWithNewData = getObjectMapper().readValue(request, User.class);

            //read old user from database
            UserWithCards user = getUserWithCardsRepository().getUser(username);

            user.setDisplayName(userWithNewData.getDisplayName());
            user.setBio(userWithNewData.getBio());
            user.setImage(userWithNewData.getImage());

            getUserWithCardsRepository().updateUser(user);

            return new Response(
                    HttpStatus.OK,
                    ContentType.JSON,
                    "{ \"data\": \"successfully updated user\", \"error\": null }"
            );

        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new Response(
                    HttpStatus.BAD_REQUEST,
                    ContentType.JSON,
                    "{ \"error\": \"bad request\", \"data\": null }"


            );
        }
    }

    // POST /users
    public Response createUser(String request) {
        try {
            //read username and password from json request
            User user = getObjectMapper().readValue(request, User.class);

            if(user.getUsername().isEmpty() || user.getPassword().isEmpty()) {
                return new Response(
                        HttpStatus.BAD_REQUEST,
                        ContentType.JSON,
                        "{ \"error\": \"bad request\", \"data\": null }"
                );
            }

            //hash password
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(user.getPassword().getBytes());
            user.setPassword(new String(hash).replace("\0", ""));

            getUserWithCardsRepository().createUser(user);

            return new Response(
                HttpStatus.CREATED,
                ContentType.JSON,
                "{ \"data\": \"successfully created user\", \"error\": null }"
            );

        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new Response(
                    HttpStatus.BAD_REQUEST,
                    ContentType.JSON,
                    "{ \"error\": \"bad request\", \"data\": null }"
            );
        } catch (IllegalAccessException e) {
            return new Response(
                    HttpStatus.CONFLICT,
                    ContentType.JSON,
                    "{ \"error\": \"duplicate username is not allowed\", \"data\": null }"
            );
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public Response getUserScoreboard(String username) {
        try {
            UserScoreboard scoreboard = getUserWithCardsRepository().getScoreboard(username);

            if (scoreboard == null) {
                return new Response(
                        HttpStatus.UNAUTHORIZED,
                        ContentType.JSON,
                        "{ \"error\": \"unauthorized\", \"data\": null }"
                );
            }

            String scoreboardJSON = getObjectMapper().writeValueAsString(scoreboard);

            return new Response(
                    HttpStatus.OK,
                    ContentType.JSON,
                    "{ \"data\": " + scoreboardJSON + ", \"error\": null }"
            );

        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.JSON,
                    "{ \"error\": \"error\", \"data\": null }"
            );
        }
    }

    public Response getAllScoreboards() {
        try {
            ArrayList<UserScoreboard> allScoreboards = getUserWithCardsRepository().getAllScoreboards();

            String scoreboardsJSON = getObjectMapper().writeValueAsString(allScoreboards);

            return new Response(
                    HttpStatus.OK,
                    ContentType.JSON,
                    "{ \"data\": " + scoreboardsJSON + ", \"error\": null }"
            );

        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.JSON,
                    "{ \"error\": \"error\", \"data\": null }"

            );
        }
    }

    public Response loginUser(String request) {
        try {
            //read username and password from json request
            User loginUser = getObjectMapper().readValue(request, User.class);

            User user = getUserDao().read(loginUser.getUsername());

            if(user == null){
                return new Response(
                        HttpStatus.UNAUTHORIZED,
                        ContentType.JSON,
                        "{ \"data\": \"invalid username or password combination\", \"error\": null }"
                );
            }

            //hash password
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(loginUser.getPassword().getBytes());
            loginUser.setPassword(new String(hash).replace("\0", ""));

            if(user.getPassword().equals(loginUser.getPassword())){
                return new Response(
                        HttpStatus.OK,
                        ContentType.JSON,
                        "{ \"data\": \"" + user.getUsername() + "-mtcgToken\", \"error\": null }"
                );
            }

            return new Response(
                    HttpStatus.UNAUTHORIZED,
                    ContentType.JSON,
                    "{ \"data\": \"invalid username or password combination\", \"error\": null }"
            );

        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new Response(
                    HttpStatus.BAD_REQUEST,
                    ContentType.JSON,
                    "{ \"error\": \"bad request\", \"data\": null }"


            );
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
