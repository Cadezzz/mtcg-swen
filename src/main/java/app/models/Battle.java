package app.models;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

@Getter
@Setter
public class Battle {
    private UserWithCards user1;
    private UserWithCards user2;
    CompletableFuture<String> battleLogFuture = new CompletableFuture<>();
    String battleLog = "";

    public Battle(){

    }

    public void startBattle() {

        battleLog += "\"Battle started between " + user1.getUsername() + " and " + user2.getUsername() + "!\n";

        for (int round = 1; round <= 100; round++) {

            Card user1Card = getRandomCardFromDeck(user1);
            Card user2Card = getRandomCardFromDeck(user2);

            battleLog += "\n\n" + user1.getUsername() + ": " + user1Card.getName() + "(" + user1Card.getDamage() + " Damage) vs " + user2.getUsername() + ": " + user2Card.getName() + "(" + user2Card.getDamage() + " Damage)";

            Card winningCard = determineWinningCard(user1Card, user2Card);

            if(winningCard == user1Card) {
                user2.getDeck().remove(user2Card);
                user1.getDeck().add(user2Card);
            } else if(winningCard == user2Card) {
                user1.getDeck().remove(user1Card);
                user2.getDeck().add(user1Card);
            }

            if(user1.getDeck().size() == 0 || user2.getDeck().size() == 0) {
                break;
            }

        }

        if(user1.getDeck().size() > user2.getDeck().size()) {
            //user1 won the battle
            battleLog += "\n\n" + user1.getUsername() + " won the battle!";

            user1.setBattlesWon(user1.getBattlesWon() + 1);
            user2.setBattlesLost(user2.getBattlesLost() + 1);
            user1.setElo(user1.getElo() + 3);
            user2.setElo(user2.getElo() - 5);

        } else if (user2.getDeck().size() > user1.getDeck().size()){
            //user2 won the battle
            battleLog += "\n\n" + user2.getUsername() + " won the battle!";

            user2.setBattlesWon(user2.getBattlesWon() + 1);
            user1.setBattlesLost(user1.getBattlesLost() + 1);
            user2.setElo(user2.getElo() + 3);
            user1.setElo(user1.getElo() - 5);

        } else {
            //draw
            battleLog += "\n\nThe battle ends in a draw!";
        }

        battleLogFuture.complete(battleLog);
    }
    
    public Card determineWinningCard(Card card1, Card card2){

        float card1Damage = card1.getDamage();
        float card2Damage = card2.getDamage();

        if(card1.getName().contains("Spell") || card2.getName().contains("Spell")) {
            //Fight with Spell vs. Monster or Spell vs. Spell

            battleLog += " => " + card1.getDamage() + " VS " + card2.getDamage();

            //knights get drowned by water spells
            if(card1.getName().contains("Knight") && card2.getName().contains("Water")) {
                card1Damage = 0;
            }
            if(card1.getName().contains("Water") && card2.getName().contains("Knight")) {
                card2Damage = 0;
            }

            //the kraken is immune to spells
            if(card1.getName().contains("Kraken")) {
                card2Damage = 0;
            }
            if(card2.getName().contains("Kraken")) {
                card1Damage = 0;
            }

            //element effect calculation

            //fire is effective against normal
            if(card1.getName().contains("Fire") && !card2.getName().contains("Water") && !card2.getName().contains("Fire")) {
                card1Damage *= 2;
                card2Damage *= 0.5;
            }

            //normal is ineffective against fire
            if(!card1.getName().contains("Water") && !card1.getName().contains("Fire") && card2.getName().contains("Fire")) {
                card1Damage *= 0.5;
                card2Damage *= 2;
            }

            //water is effective against fire
            if(card1.getName().contains("Water") && card2.getName().contains("Fire")) {
                card1Damage *= 2;
                card2Damage *= 0.5;
            }

            //fire is ineffective against water
            if(card1.getName().contains("Fire") && card2.getName().contains("Water")) {
                card1Damage *= 0.5;
                card2Damage *= 2;
            }

            //normal is effective against water
            if(!card1.getName().contains("Water") && !card1.getName().contains("Fire") && card2.getName().contains("Water")) {
                card1Damage *= 2;
                card2Damage *= 0.5;
            }

            //water is ineffective against normal
            if(card1.getName().contains("Water") && !card2.getName().contains("Water") && !card2.getName().contains("Fire")) {
                card1Damage *= 0.5;
                card2Damage *= 2;
            }

            battleLog += " -> " + card1Damage + " VS " + card2Damage;

        } else {
            //pure monster fight (elements don't matter)

            //goblins are afraid of dragons
            if(card1.getName().contains("Goblin") && card2.getName().contains("Dragon")) {
                card1Damage = 0;
            }
            if(card1.getName().contains("Dragon") && card2.getName().contains("Goblin")) {
                card2Damage = 0;
            }

            //wizards can control orcs
            if(card1.getName().contains("Ork") && card2.getName().contains("Wizard")) {
                card1Damage = 0;
            }
            if(card1.getName().contains("Wizard") && card2.getName().contains("Ork")) {
                card2Damage = 0;
            }

            //fire elves can dodge the attacks of dragons
            if(card1.getName().contains("Dragon") && card2.getName().contains("FireElf")) {
                card1Damage = 0;
            }
            if(card1.getName().contains("FireElf") && card2.getName().contains("Dragon")) {
                card2Damage = 0;
            }
        }

        if(card1Damage > card2Damage) {
            battleLog += " => " + card1.getName() + " wins";
            return card1;
        }

        if(card2Damage > card1Damage) {
            battleLog += " => " + card2.getName() + " wins";
            return card2;
        }
        battleLog += " => Draw";
        return null; //in case of a draw
    }

    public Card getRandomCardFromDeck(UserWithCards user){
        //choose random card from deck
        int randomNum = ThreadLocalRandom.current().nextInt(0, user.getDeck().size());
        return user.getDeck().get(randomNum);
    }
}
