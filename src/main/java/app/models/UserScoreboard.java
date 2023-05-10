package app.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserScoreboard {
    @JsonProperty("Name")
    String displayName;
    @JsonProperty("Elo")
    int elo;
    @JsonProperty("Wins")
    int battlesWon;
    @JsonProperty("Losses")
    int battlesLost;
}
