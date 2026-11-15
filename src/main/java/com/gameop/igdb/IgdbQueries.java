package com.gameop.igdb;

import java.util.List;

public final class IgdbQueries {

    private IgdbQueries() {}

    /** Consoles pedidos + PC para multiplataforma. */
    public static final String PLATFORMS =
            "5,6,9,12,41,48,49,130,167,169";

    public static final List<String> PRIORITY_FRANCHISES = List.of(
            "God of War",
            "The Last of Us",
            "Uncharted",
            "Spider-Man",
            "Ghost of Tsushima",
            "Ghost of Yotei",
            "Red Dead Redemption",
            "Grand Theft Auto",
            "Assassin's Creed",
            "Tomb Raider",
            "Batman: Arkham",
            "Horizon",
            "Resident Evil",
            "Devil May Cry",
            "Call of Duty",
            "Battlefield",
            "Counter-Strike",
            "DOOM",
            "Halo",
            "Far Cry",
            "Apex Legends",
            "Fortnite",
            "Overwatch",
            "Forza Horizon",
            "Forza Motorsport",
            "Need for Speed",
            "Gran Turismo",
            "Assetto Corsa",
            "The Crew",
            "The Witcher",
            "Elden Ring",
            "Dark Souls",
            "Bloodborne",
            "Final Fantasy",
            "Persona",
            "Dragon Quest",
            "Baldur's Gate",
            "The Elder Scrolls",
            "Cyberpunk 2077",
            "Monster Hunter",
            "The Legend of Zelda",
            "Super Mario",
            "Mario Kart",
            "Pokémon",
            "Metroid",
            "Donkey Kong",
            "Animal Crossing",
            "Super Smash Bros",
            "Silent Hill",
            "Outlast",
            "Dead Space",
            "Alan Wake",
            "Until Dawn",
            "The Evil Within",
            "Amnesia",
            "Minecraft",
            "Terraria",
            "Hogwarts Legacy",
            "Black Myth: Wukong",
            "It Takes Two",
            "Sekiro",
            "Death Stranding",
            "Days Gone",
            "Stellar Blade");

    public static String popularQuery(int limit, int offset) {
        return "where cover != null & version_parent = null & category = 0"
                + " & platforms = (" + PLATFORMS + ")"
                + " & total_rating_count > 15"
                + "; sort total_rating_count desc;"
                + " limit " + limit + "; offset " + offset + ";";
    }

    public static String franchiseQuery(List<String> names, int limit) {
        String orNames = names.stream()
                .map(name -> "name ~ *\"" + name.replace("\"", "") + "\"*")
                .reduce((a, b) -> a + " | " + b)
                .orElse("name != null");
        return "where cover != null & version_parent = null & category = 0"
                + " & platforms = (" + PLATFORMS + ")"
                + " & (" + orNames + ")"
                + "; sort total_rating_count desc;"
                + " limit " + limit + ";";
    }

    public static String searchQuery(String term, int limit) {
        String safe = term.replace("\"", "").replace(";", "");
        return "search \"" + safe + "\";"
                + " where cover != null & version_parent = null & category = 0;"
                + " limit " + limit + ";";
    }

    public static String byIdQuery(long id) {
        return "where id = " + id + "; limit 1;";
    }
}
