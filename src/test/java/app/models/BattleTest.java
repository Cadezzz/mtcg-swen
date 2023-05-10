package app.models;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class BattleTest {

    Battle battle;
    UserWithCards user1;
    UserWithCards user2;

    @BeforeEach
    void beforeEach() {
        battle = new Battle();

        user1 = new UserWithCards();
        user1.setUsername("user1");
        user1.setElo(100);
        user1.setBattlesWon(0);
        user1.setBattlesLost(0);
        user1.setDeck(new ArrayList<Card>());
        user1.setStack(new ArrayList<Card>());

        user2 = new UserWithCards();
        user2.setUsername("user2");
        user2.setElo(100);
        user2.setBattlesWon(0);
        user2.setBattlesLost(0);
        user2.setDeck(new ArrayList<Card>());
        user2.setStack(new ArrayList<Card>());

        battle.setUser1(user1);
        battle.setUser2(user2);
    }

    @Test
    @DisplayName("Test battle where user1 should win (because of stronger cards")
    void testStartBattle() {

        //arrange

        // user1 cards are strong
        Card card1 = new Card();
        card1.setCardId("1");
        card1.setName("WaterGoblin");
        card1.setDamage(100.0f);
        card1.setCardOwnerUsername(null);
        card1.setInDeck(false);

        Card card2 = new Card();
        card2.setCardId("2");
        card2.setName("Dragon");
        card2.setDamage(500.0f);
        card2.setCardOwnerUsername(null);
        card2.setInDeck(false);

        Card card3 = new Card();
        card3.setCardId("3");
        card3.setName("WaterSpell");
        card3.setDamage(200.0f);
        card3.setCardOwnerUsername(null);
        card3.setInDeck(false);

        Card card4 = new Card();
        card4.setCardId("4");
        card4.setName("Ork");
        card4.setDamage(450.0f);
        card4.setCardOwnerUsername(null);
        card4.setInDeck(false);

        user1.getDeck().add(card1);
        user1.getDeck().add(card2);
        user1.getDeck().add(card3);
        user1.getDeck().add(card4);

        //user2 cards are weak
        Card card5 = new Card();
        card5.setCardId("5");
        card5.setName("WaterGoblin");
        card5.setDamage(1.0f);
        card5.setCardOwnerUsername(null);
        card5.setInDeck(false);

        Card card6 = new Card();
        card6.setCardId("6");
        card6.setName("Dragon");
        card6.setDamage(5.0f);
        card6.setCardOwnerUsername(null);
        card6.setInDeck(false);

        Card card7 = new Card();
        card7.setCardId("7");
        card7.setName("WaterSpell");
        card7.setDamage(2.0f);
        card7.setCardOwnerUsername(null);
        card7.setInDeck(false);

        Card card8 = new Card();
        card8.setCardId("8");
        card8.setName("Ork");
        card8.setDamage(4.0f);
        card8.setCardOwnerUsername(null);
        card8.setInDeck(false);

        user2.getDeck().add(card5);
        user2.getDeck().add(card6);
        user2.getDeck().add(card7);
        user2.getDeck().add(card8);

        //act

        //start battle
        battle.startBattle();

        System.out.println(battle.getBattleLog());

        //assert

        //check that user1 has all the cards after battle and user2 lost all cards
        assertEquals(user1.getDeck().size(), 8);
        assertEquals(user2.getDeck().size(), 0);

        //check that user1 has one win and user2 has one loss
        assertEquals(user1.getBattlesWon(), 1);
        assertEquals(user2.getBattlesLost(), 1);

        //check that user1 gained 3 elo and user2 lost 5 elo
        assertEquals(user1.getElo(), 103);
        assertEquals(user2.getElo(), 95);

    }

    @Test
    @DisplayName("Test determine winning card")
    void testDetermineWinningCard() {

        //arrange

        // card1 is strong and should win
        Card card1 = new Card();
        card1.setCardId("1");
        card1.setName("Dragon");
        card1.setDamage(100.0f);
        card1.setCardOwnerUsername(null);
        card1.setInDeck(false);

        // card2 is weak and should lose
        Card card2 = new Card();
        card2.setCardId("2");
        card2.setName("Dragon");
        card2.setDamage(1.0f);
        card2.setCardOwnerUsername(null);
        card2.setInDeck(false);

        //act
        Card winningCard = battle.determineWinningCard(card1, card2);

        //assert
        assertEquals(winningCard, card1);
    }

    @Test
    @DisplayName("Test effectiveness win")
    void testDetermineWinningCardWithEffectiveness() {

        //arrange

        //both cards have same damage, but water should win against fire

        // card1 is fire
        Card card1 = new Card();
        card1.setCardId("1");
        card1.setName("WaterSpell");
        card1.setDamage(10.0f);
        card1.setCardOwnerUsername(null);
        card1.setInDeck(false);

        // card2 is weak and should lose
        Card card2 = new Card();
        card2.setCardId("2");
        card2.setName("FireDragon");
        card2.setDamage(15.0f);
        card2.setCardOwnerUsername(null);
        card2.setInDeck(false);

        //act
        Card winningCard = battle.determineWinningCard(card1, card2);

        //assert
        assertEquals(winningCard, card1);
    }

    @Test
    @DisplayName("Test effectiveness calculation")
    void testDetermineWinningCardEffectivenessCalculation() {

        //arrange

        //One card is stronger but water effectiveness should halve the damage of the damage card and double the own damage

        // card1 is fire
        Card card1 = new Card();
        card1.setCardId("1");
        card1.setName("WaterSpell");
        card1.setDamage(7.0f);
        card1.setCardOwnerUsername(null);
        card1.setInDeck(false);

        // card2 is weak and should lose
        Card card2 = new Card();
        card2.setCardId("2");
        card2.setName("FireDragon");
        card2.setDamage(20.0f);
        card2.setCardOwnerUsername(null);
        card2.setInDeck(false);

        //act
        Card winningCard = battle.determineWinningCard(card1, card2);

        //assert
        assertEquals(winningCard, card1);
    }

    @Test
    @DisplayName("Test effectiveness pure monster fight")
    void testDetermineWinningCardEffectivenessPureMonsterFight() {

        //arrange

        //Effectiveness should not matter for pure monster fights so card with higher damage should win

        // card1 is fire
        Card card1 = new Card();
        card1.setCardId("1");
        card1.setName("WaterDragon");
        card1.setDamage(7.0f);
        card1.setCardOwnerUsername(null);
        card1.setInDeck(false);

        // card2 is weak and should lose
        Card card2 = new Card();
        card2.setCardId("2");
        card2.setName("FireDragon");
        card2.setDamage(20.0f);
        card2.setCardOwnerUsername(null);
        card2.setInDeck(false);

        //act
        Card winningCard = battle.determineWinningCard(card1, card2);

        //assert
        assertEquals(winningCard, card2);
    }

    @Test
    @DisplayName("Test draw")
    void testDetermineWinningCardDraw() {

        //arrange

        //both cards are the same and should draw

        // card1 is fire
        Card card1 = new Card();
        card1.setCardId("1");
        card1.setName("WaterSpell");
        card1.setDamage(10.0f);
        card1.setCardOwnerUsername(null);
        card1.setInDeck(false);

        // card2 is weak and should lose
        Card card2 = new Card();
        card2.setCardId("2");
        card2.setName("WaterSpell");
        card2.setDamage(10.0f);
        card2.setCardOwnerUsername(null);
        card2.setInDeck(false);

        //act
        Card winningCard = battle.determineWinningCard(card1, card2);

        //assert
        assertNull(winningCard);
    }
}