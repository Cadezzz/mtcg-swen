package app.controllers;

import app.daos.CardDao;
import app.daos.UserDao;
import app.models.Battle;
import app.models.UserWithCards;
import app.repositories.CardRepository;
import app.repositories.UserWithCardsRepository;
import http.ContentType;
import http.HttpStatus;
import lombok.Getter;
import lombok.Setter;
import server.Response;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Getter
@Setter
public class BattleController extends Controller{

    public static final ArrayBlockingQueue<Battle> battleQueue = new ArrayBlockingQueue<>(2);

    private UserWithCardsRepository userWithCardsRepository;
    private CardRepository cardRepository;
    private UserDao userDao;
    private CardDao cardDao;

    public BattleController(UserWithCardsRepository userWithCardsRepository, CardRepository cardRepository, CardDao cardDao, UserDao userDao) {
        setUserWithCardsRepository(userWithCardsRepository);
        setCardRepository(cardRepository);
        setCardDao(cardDao);
        setUserDao(userDao);
    }

    public Response startBattle(String username) {
        try {
            UserWithCards user = getUserWithCardsRepository().getUser(username);

            if(user.getDeck().size() != 4) {
                return new Response(
                        HttpStatus.FORBIDDEN,
                        ContentType.JSON,
                        "{ \"error\": \"your deck needs to have exactly 4 cards to battle\", \"data\": null }"
                );
            }

            Battle battle = battleQueue.poll(); //load battle from battle queue (return null if queue is empty)
            if(battle != null){ // there is already a battle in the queue
                battle.setUser2(user); // we are the second user to enter the battle
                battle.startBattle();
            } else { // there is no battle in the queue
                battle = new Battle(); // we need to create a new battle
                battle.setUser1(user); // we are the first user to enter the battle
                battleQueue.put(battle); // put the battle in the queue
            }

            CompletableFuture<String> battleLog = battle.getBattleLogFuture(); // battleLog is a completable future so we can wait for it to complete
            String battleLogString = battleLog.get(); // wait for the battle to complete

            //save user to database (cards, elo and wins/losses have changed)
            getUserWithCardsRepository().updateUser(user);

            return new Response(
                    HttpStatus.OK,
                    ContentType.JSON,
                    battleLogString
            );

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.JSON,
                    "{ \"error\": \"internal server error\", \"data\": null }"
            );
        }

    }

}
