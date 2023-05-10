package app.daos;

import app.models.Card;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.LinkedHashMap;

public class CardDao {

    private final Connection connection;

    public CardDao(Connection connection) {
        this.connection = connection;
    }

    public void create(Card card) {
        String sql = "INSERT INTO cards (card_id, name, damage, card_owner_username, is_in_deck) VALUES (?, ?, ?, ?, ?)";
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, card.getCardId());
            stmt.setString(2, card.getName());
            stmt.setFloat(3, card.getDamage());
            stmt.setString(4, card.getCardOwnerUsername());
            stmt.setBoolean(5, card.isInDeck());
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Card read(String cardId) {
        String sql = "SELECT * FROM cards WHERE card_id = ?;";
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, cardId);
            var result = stmt.executeQuery();
            if (result.next()) {
                return new Card(
                        result.getString("card_id"),
                        result.getString("name"),
                        result.getFloat("damage"),
                        result.getString("card_owner_username"),
                        result.getBoolean("is_in_deck")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public LinkedHashMap<String, Card> readAll() {
        LinkedHashMap<String, Card> allCards = new LinkedHashMap<>();
        String sql = "SELECT * FROM cards ORDER BY timestamp ASC;";
        try (var stmt = connection.prepareStatement(sql)) {
            ResultSet allCardsResults = stmt.executeQuery();
            while (allCardsResults.next()) {
                Card card = new Card(
                        allCardsResults.getString("card_id"),
                        allCardsResults.getString("name"),
                        allCardsResults.getFloat("damage"),
                        allCardsResults.getString("card_owner_username"),
                        allCardsResults.getBoolean("is_in_deck")
                );
                allCards.put(card.getCardId(), card);
            }
            return allCards;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void update(Card card) {
        String sql = "UPDATE cards SET name = ?, damage = ?, card_owner_username = ?, is_in_deck = ? WHERE card_id = ?;";
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, card.getName());
            stmt.setFloat(2, card.getDamage());
            stmt.setString(3, card.getCardOwnerUsername());
            stmt.setBoolean(4, card.isInDeck());
            stmt.setString(5, card.getCardId());
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void delete(Card card) {
        String sql = "DELETE FROM cards WHERE card_id = ?;";
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, card.getCardId());
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
