package app.controllers;

import app.daos.CardDao;
import app.daos.UserDao;
import app.models.Card;
import app.models.User;
import app.models.UserWithCards;
import app.repositories.CardRepository;
import app.repositories.UserWithCardsRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import http.HttpStatus;
import http.ContentType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import server.Response;

import java.util.ArrayList;
import java.util.HashSet;

@Setter(AccessLevel.PRIVATE)
@Getter(AccessLevel.PRIVATE)
public class CardController extends Controller {
    private UserWithCardsRepository userWithCardsRepository;
    private CardRepository cardRepository;
    private UserDao userDao;
    private CardDao cardDao;

    public CardController(UserWithCardsRepository userWithCardsRepository, CardRepository cardRepository, CardDao cardDao, UserDao userDao) {
        setUserWithCardsRepository(userWithCardsRepository);
        setCardRepository(cardRepository);
        setCardDao(cardDao);
        setUserDao(userDao);
    }

    // POST /packages
    public Response createPackage(String request) {

        try {

            //we need the TypeReference so that the ObjectMapper knows how to read the JSON
            //in this case the JSON is an array of Card objects
            TypeReference<ArrayList<Card>> typeReference = new TypeReference<ArrayList<Card>>() {};
            ArrayList<Card> cards = getObjectMapper().readValue(request, typeReference);

            //check that the package has exactly 5 cards
            if (cards.size() != 5) {
                return new Response(
                        HttpStatus.BAD_REQUEST,
                        ContentType.JSON,
                        "{ \"error\": \"package must have exactly 5 cards\", \"data\": null }"
                );
            }

            //check that there are no duplicate card ids in the package
            HashSet<String> cardIds = new HashSet<>();
            for (Card card : cards) {
                if (cardIds.contains(card.getCardId())) {
                    return new Response(
                            HttpStatus.CONFLICT,
                            ContentType.JSON,
                            "{ \"error\": \"duplicate card ids in package\", \"data\": null }"
                    );
                }
                cardIds.add(card.getCardId());
            }

            //create the package
            if (!cardRepository.createCards(cards)) { //returns false if a card already exists
                return new Response(
                        HttpStatus.CONFLICT,
                        ContentType.JSON,
                        "{ \"error\": \"a card already exists\", \"data\": null }"
                );
            }

            return new Response(
                    HttpStatus.CREATED,
                    ContentType.JSON,
                    "{ \"data\": \"package successfully created\", \"error\": null }"
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

    // POST /transactions/packages
    public Response openPackage(String username) {

        try {
            UserWithCards user = getUserWithCardsRepository().getUser(username);

            if (user.getCoins() < 5) {
                return new Response(
                        HttpStatus.FORBIDDEN,
                        ContentType.JSON,
                        "{ \"error\": \"not enough coins to buy a package\", \"data\": null }"
                );
            }

            ArrayList<Card> newCards = getCardRepository().openPackage();
            if (newCards == null) {
                return new Response(
                        HttpStatus.NOT_FOUND,
                        ContentType.JSON,
                        "{ \"error\": \"there are no packages available to buy\", \"data\": null }"
                );
            }

            user.getStack().addAll(newCards);
            user.setCoins(user.getCoins() - 5);

            getUserWithCardsRepository().updateUser(user);

            String openedPackageJSON = getObjectMapper().writeValueAsString(newCards);

            return new Response(
                    HttpStatus.OK,
                    ContentType.JSON,
                    "{ \"data\": " + openedPackageJSON + ", \"error\": null }"
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

    // GET /cards
    public Response getUserCards(String username){
        try {
            UserWithCards user = getUserWithCardsRepository().getUser(username);

            ArrayList<Card> userStack = user.getStack();
            ArrayList<Card> userDeck = user.getDeck();
            ArrayList<Card> allUserCards = new ArrayList<>();
            allUserCards.addAll(userStack);
            allUserCards.addAll(userDeck);

            if(allUserCards.isEmpty()){
                return new Response(
                        HttpStatus.NOT_FOUND,
                        ContentType.JSON,
                        "{ \"error\": \"you don't have any cards\", \"data\": null }"
                );
            }

            String userCardsJSON = getObjectMapper().writeValueAsString(allUserCards);

            return new Response(
                    HttpStatus.OK,
                    ContentType.JSON,
                    "{ \"data\": " + userCardsJSON + ", \"error\": null }"
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

    // GET /decks
    public Response getUserDeck(String username){
        try {
            UserWithCards user = getUserWithCardsRepository().getUser(username);

            if(user == null){
                return new Response(
                        HttpStatus.NOT_FOUND,
                        ContentType.JSON,
                        "{ \"error\": \"user not found\", \"data\": null }"
                );
            }

            if(user.getDeck().isEmpty()){
                return new Response(
                        HttpStatus.NOT_FOUND,
                        ContentType.JSON,
                        "{ \"error\": \"you don't have any cards in your deck\", \"data\": null }"
                );
            }

            String userCardsJSON = getObjectMapper().writeValueAsString(user.getDeck());

            return new Response(
                    HttpStatus.OK,
                    ContentType.JSON,
                    "{ \"data\": " + userCardsJSON + ", \"error\": null }"
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

    // PUT /decks
    public Response createDeck(String username, String request) {

        try {

            //we need the TypeReference so that the ObjectMapper knows how to read the JSON
            //in this case the JSON is an array of Strings (the card ids)
            TypeReference<ArrayList<String>> typeReference = new TypeReference<ArrayList<String>>() {};
            ArrayList<String> cardIds = getObjectMapper().readValue(request, typeReference);

            if (cardIds.size() != 4) {
                return new Response(
                        HttpStatus.BAD_REQUEST,
                        ContentType.JSON,
                        "{ \"error\": \"a deck needs to be exactly 4 cards\", \"data\": null }"
                );
            }

            //check that the 4 card ids are unique
            HashSet<String> cardIdsSet = new HashSet<>(cardIds);
            if (cardIdsSet.size() != 4) {
                return new Response(
                        HttpStatus.BAD_REQUEST,
                        ContentType.JSON,
                        "{ \"error\": \"a deck needs to have 4 unique cards\", \"data\": null }"
                );
            }

            UserWithCards user = getUserWithCardsRepository().getUser(username);

            //get all user cards to see if user has all cards
            ArrayList<Card> userStack = user.getStack();
            ArrayList<Card> userDeck = user.getDeck();
            ArrayList<Card> allUserCards = new ArrayList<>();
            allUserCards.addAll(userStack);
            allUserCards.addAll(userDeck);

            //check if user has all cards
            for (String cardId : cardIds) {
                boolean hasCard = false;
                for (Card card : allUserCards) {
                    if (card.getCardId().equals(cardId)) {
                        hasCard = true;
                        break;
                    }
                }
                if (!hasCard) {
                    return new Response(
                            HttpStatus.FORBIDDEN,
                            ContentType.JSON,
                            "{ \"error\": \"you don't have all the cards\", \"data\": null }"
                    );
                }
            }

            //copy old deck to stack and clear deck
            userStack.addAll(userDeck);
            userDeck.clear();

            //add new cards to deck
            for (String cardId : cardIds) {
                for (Card card : userStack) {
                    if (card.getCardId().equals(cardId)) {
                        userStack.remove(card);
                        userDeck.add(card);
                        break;
                    }
                }
            }
            //update/save this deck
            getUserWithCardsRepository().updateUser(user);

            return new Response(
                    HttpStatus.CREATED,
                    ContentType.JSON,
                    "{ \"data\": \"deck successfully created\", \"error\": null }"
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
}
