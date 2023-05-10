package app.controllers;

import app.daos.CardDao;
import app.daos.UserDao;
import app.models.Card;
import app.models.User;
import app.models.UserWithCards;
import app.repositories.CardRepository;
import app.repositories.UserWithCardsRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import server.Response;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CardControllerTest {

    private UserDao userDaoMock;
    private CardDao cardDaoMock;
    private CardRepository cardRepositoryMock;
    private CardController cardController;
    private UserWithCardsRepository userWithCardsRepositoryMock;

    @BeforeEach
    void beforeEach() {
        userDaoMock = mock(UserDao.class);
        cardDaoMock = mock(CardDao.class);
        cardRepositoryMock = mock(CardRepository.class);
        userWithCardsRepositoryMock = mock(UserWithCardsRepository.class);
        cardController = new CardController(userWithCardsRepositoryMock, cardRepositoryMock, cardDaoMock, userDaoMock);
    }

    @Test
    @DisplayName("Test creating new package with cards")
    void testCreatePackage() {

        //arrange

        String request = """
                [
                     {
                         "Id": "n15f0dc7-37d0-426e-994e-43fc3ac83c1812",
                         "Name": "WaterGoblin",
                         "Damage": 10.0
                     },
                     {
                         "Id": "k1f8f8dc-e25e-4a95-aa2c-782823f36e3a12",
                         "Name": "Dragon",
                         "Damage": 50.0
                     },
                     {
                         "Id": "h15e8976-7c86-4d06-9a80-641c2019a70f12",
                         "Name": "WaterSpell",
                         "Damage": 20.0
                     },
                     {
                         "Id": "p1b6aj86-bdb2-47e5-b6e4-68c5ab38933942",
                         "Name": "Ork",
                         "Damage": 45.0
                     },
                     {
                         "Id": "l1dd758f-649c-40f9-ba3a-8657f4b3430f12",
                         "Name": "FireSpell",
                         "Damage": 25.0
                     }
                 ]
                """;

        when(cardRepositoryMock.createCards(any())).thenReturn(true);

        //act
        cardController.createPackage(request);

        //assert
        //create captor to capture card array passed to repository
        ArgumentCaptor<ArrayList<Card>> cardCaptor = ArgumentCaptor.forClass(ArrayList.class);

        //verify that repository .createPackage function was called and capture passed arraylist
        verify(cardRepositoryMock).createCards(cardCaptor.capture());

        //get captured user object from captor
        ArrayList<Card> cards = cardCaptor.getValue();

        //verify that the cards are in the ArrayList with the correct values
        assertEquals(cards.get(0).getCardId(), "n15f0dc7-37d0-426e-994e-43fc3ac83c1812");
        assertEquals(cards.get(4).getCardId(), "l1dd758f-649c-40f9-ba3a-8657f4b3430f12");
        assertEquals(cards.get(0).getName(), "WaterGoblin");
        assertEquals(cards.get(4).getName(), "FireSpell");
        assertEquals(cards.get(0).getDamage(), 10.0);
        assertEquals(cards.get(4).getDamage(), 25.0);
        assertNull(cards.get(0).getCardOwnerUsername());
        assertNull(cards.get(4).getCardOwnerUsername());
        assertFalse(cards.get(0).isInDeck());
        assertFalse(cards.get(4).isInDeck());
    }

    @Test
    @DisplayName("Test creating new package with wrong number of cards")
    void testCreatePackageFourCards() {

        //arrange

        String request = """
                [
                     {
                         "Id": "n15f0dc7-37d0-426e-994e-43fc3ac83c1812",
                         "Name": "WaterGoblin",
                         "Damage": 10.0
                     },
                     {
                         "Id": "k1f8f8dc-e25e-4a95-aa2c-782823f36e3a12",
                         "Name": "Dragon",
                         "Damage": 50.0
                     },
                     {
                         "Id": "h15e8976-7c86-4d06-9a80-641c2019a70f12",
                         "Name": "WaterSpell",
                         "Damage": 20.0
                     },
                     {
                         "Id": "p1b6aj86-bdb2-47e5-b6e4-68c5ab38933942",
                         "Name": "Ork",
                         "Damage": 45.0
                     }
                 ]
                """;

        when(cardRepositoryMock.createCards(any())).thenReturn(true);

        //act
        Response response = cardController.createPackage(request);

        //assert
        assertEquals(response.getStatusCode(), 400); //bad request
    }

    @Test
    @DisplayName("Test creating new package with bad JSON")
    void testCreatePackageBadRequest() {

        //arrange

        //request field "Id" is misspelled as "id"
        String request = """
                [
                     {
                         "id": "n15f0dc7-37d0-426e-994e-43fc3ac83c1812",
                         "Name": "WaterGoblin",
                         "Damage": 10.0
                     },
                     {
                         "Id": "k1f8f8dc-e25e-4a95-aa2c-782823f36e3a12",
                         "Name": "Dragon",
                         "Damage": 50.0
                     },
                     {
                         "Id": "h15e8976-7c86-4d06-9a80-641c2019a70f12",
                         "Name": "WaterSpell",
                         "Damage": 20.0
                     },
                     {
                         "Id": "p1b6aj86-bdb2-47e5-b6e4-68c5ab38933942",
                         "Name": "Ork",
                         "Damage": 45.0
                     },
                     {
                         "Id": "l1dd758f-649c-40f9-ba3a-8657f4b3430f12",
                         "Name": "FireSpell",
                         "Damage": 25.0
                     }
                 ]
                """;


        //act
        Response response = cardController.createPackage(request);

        //assert
        assertEquals(response.getStatusCode(), 400); //bad request
    }

    @Test
    @DisplayName("Test creating new package with duplicate cards")
    void testCreatePackageDuplicateCards() {

        //arrange

        //first two cards have same id
        String request = """
                [
                     {
                         "Id": "n15f0dc7-37d0-426e-994e-43fc3ac83c1812",
                         "Name": "WaterGoblin",
                         "Damage": 10.0
                     },
                     {
                         "Id": "n15f0dc7-37d0-426e-994e-43fc3ac83c1812",
                         "Name": "Dragon",
                         "Damage": 50.0
                     },
                     {
                         "Id": "h15e8976-7c86-4d06-9a80-641c2019a70f12",
                         "Name": "WaterSpell",
                         "Damage": 20.0
                     },
                     {
                         "Id": "p1b6aj86-bdb2-47e5-b6e4-68c5ab38933942",
                         "Name": "Ork",
                         "Damage": 45.0
                     },
                     {
                         "Id": "l1dd758f-649c-40f9-ba3a-8657f4b3430f12",
                         "Name": "FireSpell",
                         "Damage": 25.0
                     }
                 ]
                """;

        when(cardRepositoryMock.createCards(any())).thenReturn(true);

        //act
        Response response = cardController.createPackage(request);

        //assert
        assertEquals(response.getStatusCode(), 409); //conflict
    }

    @Test
    @DisplayName("Test buying/opening existing package with cards")
    void testOpenPackage() {

        //arrange

        UserWithCards cadeUser = new UserWithCards();
        cadeUser.setUsername("cade");
        cadeUser.setCoins(20);
        cadeUser.setStack(new ArrayList<>());
        cadeUser.setDeck(new ArrayList<>());

        //5 cards for pack
        Card card1 = new Card();
        card1.setCardId("1");
        card1.setName("WaterGoblin");
        card1.setDamage(10.0f);
        card1.setCardOwnerUsername(null);
        card1.setInDeck(false);

        Card card2 = new Card();
        card2.setCardId("2");
        card2.setName("Dragon");
        card2.setDamage(50.0f);
        card2.setCardOwnerUsername(null);
        card2.setInDeck(false);

        Card card3 = new Card();
        card3.setCardId("3");
        card3.setName("WaterSpell");
        card3.setDamage(20.0f);
        card3.setCardOwnerUsername(null);
        card3.setInDeck(false);

        Card card4 = new Card();
        card4.setCardId("4");
        card4.setName("Ork");
        card4.setDamage(45.0f);
        card4.setCardOwnerUsername(null);
        card4.setInDeck(false);

        Card card5 = new Card();
        card5.setCardId("5");
        card5.setName("FireSpell");
        card5.setDamage(25.0f);
        card5.setCardOwnerUsername(null);
        card5.setInDeck(false);

        ArrayList<Card> cardsFromPack = new ArrayList<>();
        cardsFromPack.add(card1);
        cardsFromPack.add(card2);
        cardsFromPack.add(card3);
        cardsFromPack.add(card4);
        cardsFromPack.add(card5);

        when(userWithCardsRepositoryMock.getUser(any())).thenReturn(cadeUser);
        when(cardRepositoryMock.openPackage()).thenReturn(cardsFromPack);

        //act
        cardController.openPackage("cade");

        //assert
        ArgumentCaptor<UserWithCards> userCaptor = ArgumentCaptor.forClass(UserWithCards.class);
        verify(userWithCardsRepositoryMock).updateUser(userCaptor.capture());
        UserWithCards updatedUser = userCaptor.getValue();

        //verify that user now has the 5 cards
        assertEquals(updatedUser.getStack().size(), 5);

        //verify that user lost 5 coins
        assertEquals(updatedUser.getCoins(), 15);

    }

    @Test
    @DisplayName("Test buying/opening existing package when user has not enough coins")
    void testOpenPackageWithoutEnoughCoins() {

        //arrange
        UserWithCards cadeUser = new UserWithCards();
        cadeUser.setUsername("cade");
        cadeUser.setCoins(4);
        cadeUser.setStack(new ArrayList<>());
        cadeUser.setDeck(new ArrayList<>());

        when(userWithCardsRepositoryMock.getUser(any())).thenReturn(cadeUser);

        //act
        Response response = cardController.openPackage("cade");

        //assert
        assertEquals(response.getStatusCode(), 403); //forbidden

    }

    @Test
    @DisplayName("Test buying/opening existing package there are no packs left")
    void testOpenPackageWithoutAvailablePackages() {

        //arrange
        UserWithCards cadeUser = new UserWithCards();
        cadeUser.setUsername("cade");
        cadeUser.setCoins(20);
        cadeUser.setStack(new ArrayList<>());
        cadeUser.setDeck(new ArrayList<>());

        when(userWithCardsRepositoryMock.getUser(any())).thenReturn(cadeUser);

        //no packs left
        when(cardRepositoryMock.openPackage()).thenReturn(null);

        //act
        Response response = cardController.openPackage("cade");

        //assert
        assertEquals(response.getStatusCode(), 404); //not found

    }

    @Test
    @DisplayName("Test getting all user cards (should show cards from both deck and stack)")
    void getUserCards() {
        //arrange
        UserWithCards cadeUser = new UserWithCards();
        cadeUser.setUsername("cade");
        cadeUser.setCoins(20);
        cadeUser.setStack(new ArrayList<>());
        cadeUser.setDeck(new ArrayList<>());

        //2 cards for deck
        Card card1 = new Card();
        card1.setCardId("1");
        card1.setName("WaterGoblin");
        card1.setDamage(10.0f);
        card1.setCardOwnerUsername(null);
        card1.setInDeck(false);

        Card card2 = new Card();
        card2.setCardId("2");
        card2.setName("Dragon");
        card2.setDamage(50.0f);
        card2.setCardOwnerUsername(null);
        card2.setInDeck(false);

        cadeUser.getDeck().add(card1);
        cadeUser.getDeck().add(card2);

        //2 cards for stack
        Card card3 = new Card();
        card3.setCardId("3");
        card3.setName("WaterSpell");
        card3.setDamage(20.0f);
        card3.setCardOwnerUsername(null);
        card3.setInDeck(false);

        Card card4 = new Card();
        card4.setCardId("4");
        card4.setName("Ork");
        card4.setDamage(45.0f);
        card4.setCardOwnerUsername(null);
        card4.setInDeck(false);

        cadeUser.getStack().add(card3);
        cadeUser.getStack().add(card4);

        when(userWithCardsRepositoryMock.getUser(any())).thenReturn(cadeUser);

        //act
        Response response = cardController.getUserCards("cade");

        //assert that response includes both cards from deck and stack
        assertEquals(response.getContent(), """
                { "data": [{"Id":"3","Name":"WaterSpell","Damage":20.0},{"Id":"4","Name":"Ork","Damage":45.0},{"Id":"1","Name":"WaterGoblin","Damage":10.0},{"Id":"2","Name":"Dragon","Damage":50.0}], "error": null }""");
    }

    @Test
    @DisplayName("Test getting user deck (should only show cards from deck and not stack)")
    void getUserDeck() {
        //arrange
        UserWithCards cadeUser = new UserWithCards();
        cadeUser.setUsername("cade");
        cadeUser.setCoins(20);
        cadeUser.setStack(new ArrayList<>());
        cadeUser.setDeck(new ArrayList<>());

        //2 cards for deck
        Card card1 = new Card();
        card1.setCardId("1");
        card1.setName("WaterGoblin");
        card1.setDamage(10.0f);
        card1.setCardOwnerUsername(null);
        card1.setInDeck(false);

        Card card2 = new Card();
        card2.setCardId("2");
        card2.setName("Dragon");
        card2.setDamage(50.0f);
        card2.setCardOwnerUsername(null);
        card2.setInDeck(false);

        cadeUser.getDeck().add(card1);
        cadeUser.getDeck().add(card2);

        //2 cards for stack
        Card card3 = new Card();
        card3.setCardId("3");
        card3.setName("WaterSpell");
        card3.setDamage(20.0f);
        card3.setCardOwnerUsername(null);
        card3.setInDeck(false);

        Card card4 = new Card();
        card4.setCardId("4");
        card4.setName("Ork");
        card4.setDamage(45.0f);
        card4.setCardOwnerUsername(null);
        card4.setInDeck(false);

        cadeUser.getStack().add(card3);
        cadeUser.getStack().add(card4);

        when(userWithCardsRepositoryMock.getUser(any())).thenReturn(cadeUser);

        //act
        Response response = cardController.getUserDeck("cade");

        //assert that response includes cards from deck only
        assertEquals(response.getContent(), """
                { "data": [{"Id":"1","Name":"WaterGoblin","Damage":10.0},{"Id":"2","Name":"Dragon","Damage":50.0}], "error": null }""");
    }

}