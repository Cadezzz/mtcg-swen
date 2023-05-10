CREATE TABLE "users" (
                         "username" text PRIMARY KEY,
                         "password" text,
                         "display_name" text,
                         "bio" text,
                         "image" text,
                         "coins" int,
                         "elo" int,
                         "battles_won" int,
                         "battles_lost" int
);

CREATE TABLE "cards" (
                         "card_id" text PRIMARY KEY,
                         "name" text,
                         "damage" float,
                         "card_owner_username" text,
                         "is_in_deck" bool,
                         "timestamp" timestamp default current_timestamp
);

ALTER TABLE "cards" ADD FOREIGN KEY ("card_owner_username") REFERENCES "users" ("username");
