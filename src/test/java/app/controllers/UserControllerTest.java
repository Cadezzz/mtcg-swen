package app.controllers;

import app.daos.CardDao;
import app.daos.UserDao;
import app.models.User;
import app.models.UserScoreboard;
import app.models.UserWithCards;
import app.repositories.UserWithCardsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import server.Request;
import server.Response;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    private UserDao userDaoMock;
    private CardDao cardDaoMock;
    private UserController userController;
    private UserWithCardsRepository userWithCardsRepositoryMock;

    @BeforeEach
    void beforeEach(){
        userDaoMock = mock(UserDao.class);
        cardDaoMock = mock(CardDao.class);
        userWithCardsRepositoryMock = mock(UserWithCardsRepository.class);
        userController = new UserController(userWithCardsRepositoryMock, cardDaoMock, userDaoMock);
    }

    @Test
    @DisplayName("Test that password is hashed when new user is registered")
    void testUserCreatePasswordIsHashedOnRegister() throws IllegalAccessException {

        //arrange
        String request = """
                {
                    "Username": "admin",
                    "Password": "pw123"
                }
                """;

        //act
        userController.createUser(request);

        //assert
        //create captor to capture user object passed to repository
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        //verify that repository .createUser function is called and capture passed user object
        verify(userWithCardsRepositoryMock).createUser(userCaptor.capture());

        //get captured user object from captor
        User user = userCaptor.getValue();

        //verify that password was hashed before saving user to repository
        assertNotEquals(user.getPassword(), "pw123");

    }

    @Test
    @DisplayName("Test for correct response on user create")
    void testUserCreateResponseSuccessful() throws IllegalAccessException {

        //arrange
        String request = """
                {
                    "Username": "admin",
                    "Password": "pw123"
                }
                """;

        //act
        Response response = userController.createUser(request);

        //assert
        assertEquals(response.getStatusCode(), 201); //HTTP STATUS: CREATED
    }

    @Test
    @DisplayName("Test for error response on duplicate user create")
    void testUserCreateResponseDuplicate() throws IllegalAccessException {

        //arrange
        doThrow(new IllegalAccessException()).when(userWithCardsRepositoryMock).createUser(any(User.class));

        String request = """
                {
                    "Username": "admin",
                    "Password": "pw123"
                }
                """;

        //act
        Response response = userController.createUser(request);

        //assert
        assertEquals(response.getStatusCode(), 409); //HTTP STATUS: CONFLICT
    }

    @Test
    @DisplayName("Test for error on invalid JSON request")
    void testUserCreateResponseBadRequest() throws IllegalAccessException {

        //arrange
        //JSON has typo in password field
        String request = """
                {
                    "Username": "admin",
                    "password": "pw123"
                }
                """;

        //act
        Response response = userController.createUser(request);

        //assert
        assertEquals(response.getStatusCode(), 400); //HTTP STATUS: BAD REQUEST
    }

    @Test
    @DisplayName("Test valid login")
    void testLogin() {

        //arrange

        //user in database
        User user = new User();
        user.setUsername("testUser");
        user.setPassword("#�tE����x�E�k��t�'�\u0010����\u0087[�0��%"); //hash of "pw123"

        when(userDaoMock.read(any())).thenReturn(user);

        String request = """
                {
                    "Username": "admin",
                    "Password": "pw123"
                }
                """;

        //act
        Response response = userController.loginUser(request);

        //assert
        assertEquals(response.getStatusCode(), 200); //HTTP STATUS: OK
        assertEquals(response.getContent(), """
                { "data": "testUser-mtcgToken", "error": null }""");

    }

    @Test
    @DisplayName("Test valid login wrong username")
    void testLoginWrongUsername() {

        //arrange

        //JSON has wrong username
        String request = """
                {
                    "Username": "admin1",
                    "Password": "pw123"
                }
                """;

        //act
        Response response = userController.loginUser(request);

        //assert
        assertEquals(response.getStatusCode(), 401); //HTTP STATUS: UNAUTHORIZED
    }

    @Test
    @DisplayName("Test login wrong password")
    void testLoginWrongPassword() {

        //arrange

        //user in database
        User user = new User();
        user.setUsername("testUser");
        user.setPassword("#�tE����x�E�k��t�'�\u0010����\u0087[�0��%"); //hash of "pw123"

        when(userDaoMock.read(any())).thenReturn(user);

        //JSON has wrong password
        String request = """
                {
                    "Username": "admin",
                    "Password": "pw1234"
                }
                """;

        //act
        Response response = userController.loginUser(request);

        //assert
        assertEquals(response.getStatusCode(), 401); //HTTP STATUS: UNAUTHORIZED

    }

    @Test
    @DisplayName("Test getting user info")
    void testGetUserInfo() {

        User cadeUser = new User();
        cadeUser.setUsername("cade");
        cadeUser.setDisplayName("Cade");
        cadeUser.setBio("Test");
        cadeUser.setImage("Test");

        when(userDaoMock.read(any())).thenReturn(cadeUser);

        //act
        Response response = userController.getUserByUsername("cade");

        //assert
        assertEquals(response.getContent(), """
                { "data": {"Name":"Cade","Bio":"Test","Image":"Test"}, "error": null }""");
    }

    @Test
    @DisplayName("Test updating user info")
    void testUpdateUserInfo() {

        UserWithCards cadeUser = new UserWithCards();
        cadeUser.setUsername("cade");
        cadeUser.setDisplayName("Test");
        cadeUser.setBio("Test");
        cadeUser.setImage("Test");

        when(userWithCardsRepositoryMock.getUser(any())).thenReturn(cadeUser);

        //arrange
        String request = """
                {
                     "Name": "Cade",
                     "Bio": "You know me as Cade",
                     "Image": "PuppyEyes"
                 }
                """;

        //act
        userController.updateUser("test", request);

        //assert
        //create captor to capture user object passed to repository
        ArgumentCaptor<UserWithCards> userCaptor = ArgumentCaptor.forClass(UserWithCards.class);

        //verify that repository .createUser function is called and capture passed user object
        verify(userWithCardsRepositoryMock).updateUser(userCaptor.capture());

        //get captured user object from captor
        UserWithCards updatedUser = userCaptor.getValue();

        //verify that the user data from JSON has been written correctly to the user object before it is passed
        assertEquals(updatedUser.getDisplayName(), "Cade");
        assertEquals(updatedUser.getBio(), "You know me as Cade");
        assertEquals(updatedUser.getImage(), "PuppyEyes");
    }

    @Test
    @DisplayName("Test updating user info with broken JSON")
    void testUpdateUserInfoBadRequest() {


        //arrange
        //wrong JSON request
        String request = """
                {
                     "name": "Cade",
                     "Bio": "You know me as Cade",
                     "Image": "PuppyEyes"
                 }
                """;

        //act

        Response response = userController.updateUser("test", request);

        //assert
        assertEquals(response.getStatusCode(), 400); //HTTP STATUS: BAD REQUEST

    }

    @Test
    @DisplayName("Test for correct response on user scoreboard")
    void testGetUserScoreboard() {

        UserScoreboard cadeUserScoreboard = new UserScoreboard();
        cadeUserScoreboard.setDisplayName("Cade");
        cadeUserScoreboard.setElo(150);
        cadeUserScoreboard.setBattlesWon(5);
        cadeUserScoreboard.setBattlesLost(1);

        when(userWithCardsRepositoryMock.getScoreboard(any())).thenReturn(cadeUserScoreboard);

        //act
        Response response = userController.getUserScoreboard("cade");

        //verify that the user data from JSON has been written correctly to the user object before it is passed
        assertEquals(response.getContent(), """
                { "data": {"Name":"Cade","Elo":150,"Wins":5,"Losses":1}, "error": null }""");
    }

    @Test
    @DisplayName("Test for correct response on getting all user scoreboard")
    void testGetAllScoreboards() {
        UserScoreboard cadeUserScoreboard = new UserScoreboard();
        cadeUserScoreboard.setDisplayName("Cade");
        cadeUserScoreboard.setElo(180);
        cadeUserScoreboard.setBattlesWon(5);
        cadeUserScoreboard.setBattlesLost(1);

        UserScoreboard bobUserScoreboard = new UserScoreboard();
        bobUserScoreboard.setDisplayName("Bob");
        bobUserScoreboard.setElo(170);
        bobUserScoreboard.setBattlesWon(10);
        bobUserScoreboard.setBattlesLost(2);

        UserScoreboard adminUserScoreboard = new UserScoreboard();
        adminUserScoreboard.setDisplayName("Admin");
        adminUserScoreboard.setElo(80);
        adminUserScoreboard.setBattlesWon(2);
        adminUserScoreboard.setBattlesLost(5);

        ArrayList<UserScoreboard> userScoreboardArrayList = new ArrayList<>();
        userScoreboardArrayList.add(cadeUserScoreboard);
        userScoreboardArrayList.add(bobUserScoreboard);
        userScoreboardArrayList.add(adminUserScoreboard);

        when(userWithCardsRepositoryMock.getAllScoreboards()).thenReturn(userScoreboardArrayList);

        //act
        Response response = userController.getAllScoreboards();

        //verify that the user data from JSON has been written correctly to the user object before it is passed
        assertEquals(response.getContent(), """
                { "data": [{"Name":"Cade","Elo":180,"Wins":5,"Losses":1},{"Name":"Bob","Elo":170,"Wins":10,"Losses":2},{"Name":"Admin","Elo":80,"Wins":2,"Losses":5}], "error": null }""");
    }

}