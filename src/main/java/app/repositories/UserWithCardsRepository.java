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

public class UserWithCardsRepository {
    private final UserDao userDao;
    private final CardDao cardDao;

    public UserWithCardsRepository(UserDao userDao, CardDao cardDao) {
        this.userDao = userDao;
        this.cardDao = cardDao;
    }

    public UserWithCards getUser(String username) {
        User user = userDao.read(username);

        //if user does not exist
        if(user == null){
            return null;
        }

        UserWithCards userWithCards = new UserWithCards();
        userWithCards.setUsername(user.getUsername());
        userWithCards.setPassword(user.getPassword());
        userWithCards.setDisplayName(user.getDisplayName());
        userWithCards.setBio(user.getBio());
        userWithCards.setImage(user.getImage());
        userWithCards.setCoins(user.getCoins());
        userWithCards.setElo(user.getElo());
        userWithCards.setBattlesWon(user.getBattlesWon());
        userWithCards.setBattlesLost(user.getBattlesLost());
        userWithCards.setDeck(new ArrayList<>());
        userWithCards.setStack(new ArrayList<>());

        LinkedHashMap<String, Card> allCards = cardDao.readAll();
        for (String cardId : allCards.keySet()) {
            if (Objects.equals(allCards.get(cardId).getCardOwnerUsername(), userWithCards.getUsername())) {
                if (allCards.get(cardId).isInDeck()) {
                    userWithCards.getDeck().add(allCards.get(cardId));
                } else {
                    userWithCards.getStack().add(allCards.get(cardId));
                }
            }
        }

        return userWithCards;
    }

    public boolean validateToken(String token){
        return(!(userDao.read(token) == null));
    }


    public void createUser (User user) throws IllegalAccessException {
        userDao.create(user);
    }

    public void updateUser(UserWithCards userWithCards) {
        User user = new User(
                userWithCards.getUsername(),
                userWithCards.getPassword(),
                userWithCards.getDisplayName(),
                userWithCards.getBio(),
                userWithCards.getImage(),
                userWithCards.getCoins(),
                userWithCards.getElo(),
                userWithCards.getBattlesWon(),
                userWithCards.getBattlesLost()
        );
        userDao.update(user);

        for(Card card : userWithCards.getDeck()) {
            card.setCardOwnerUsername(userWithCards.getUsername());
            card.setInDeck(true);
            cardDao.update(card);
        }

        for(Card card : userWithCards.getStack()) {
            card.setCardOwnerUsername(userWithCards.getUsername());
            card.setInDeck(false);
            cardDao.update(card);
        }
    }

    public UserScoreboard getScoreboard(String username) {
        User user = userDao.read(username);

        if(user == null) {
            return null;
        }

        UserScoreboard userScoreboard = new UserScoreboard();
        userScoreboard.setDisplayName(user.getDisplayName());
        userScoreboard.setElo(user.getElo());
        userScoreboard.setBattlesWon(user.getBattlesWon());
        userScoreboard.setBattlesLost(user.getBattlesLost());

        return userScoreboard;
    }

    public ArrayList<UserScoreboard> getAllScoreboards() {
        ArrayList<User> users = userDao.readAll();

        if(users == null || users.isEmpty()) {
            return null;
        }

        ArrayList<UserScoreboard> scoreboards = new ArrayList<>();

        for(User user : users) {
            UserScoreboard userScoreboard = new UserScoreboard();
            userScoreboard.setDisplayName(user.getDisplayName());
            userScoreboard.setElo(user.getElo());
            userScoreboard.setBattlesWon(user.getBattlesWon());
            userScoreboard.setBattlesLost(user.getBattlesLost());
            scoreboards.add(userScoreboard);
        }

        //sort scoreboards by elo descending
        scoreboards.sort((o1, o2) -> o2.getElo() - o1.getElo());

        return scoreboards;
    }

    public void deleteUser(UserWithCards userWithCards) {
        User user = new User();
        user.setUsername(userWithCards.getUsername());
        userDao.delete(user);

        for(Card card : userWithCards.getDeck()) {
            cardDao.delete(card);
        }

        for(Card card : userWithCards.getStack()) {
            cardDao.delete(card);
        }
    }
}
