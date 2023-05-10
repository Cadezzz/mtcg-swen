package app.repositories;

import app.daos.CardDao;
import app.daos.UserDao;
import app.models.Card;
import app.models.User;
import app.models.UserScoreboard;
import app.models.UserWithCards;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Objects;

public class CardRepository {
    private final CardDao cardDao;

    public CardRepository(CardDao cardDao) {
        this.cardDao = cardDao;
    }

    public boolean createCards(ArrayList<Card> cards) {

        //check that no card already exists
        for (Card card : cards) {
            if(cardDao.read(card.getCardId()) != null){
                return false; // a card already exists
            }
        }

        //if no card exists, create them
        for (Card card : cards) {
            cardDao.create(card);
        }

        return true; // all cards successfully created
    }

    public ArrayList<Card> openPackage() {

        //ArrayList to hold cards of opened package
        ArrayList<Card> cards = new ArrayList<>();

        //get all cards from db (LinkedHashMap so that they are in the same order for the curl script)
        LinkedHashMap<String, Card> allCards = cardDao.readAll();

        for (String cardId : allCards.keySet()) {
            //all cards that have no owner are in package
            if (allCards.get(cardId).getCardOwnerUsername() == null) {
                cards.add(allCards.get(cardId)); //add card to opened package
                //every package has 5 cards
                if(cards.size() == 5) {
                    return cards;
                }
            }
        }
        //if no packages are available
        return null;
    }
}
