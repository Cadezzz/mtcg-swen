package app.models;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Card {
    @JsonAlias("Id")
    @JsonProperty("Id")
    private String cardId;
    @JsonAlias("Name")
    @JsonProperty("Name")
    private String name;
    @JsonAlias("Damage")
    @JsonProperty("Damage")
    private float damage;
    @JsonIgnore
    private String cardOwnerUsername = null;
    @JsonIgnore
    private boolean isInDeck = false;

    public Card() {}
}
