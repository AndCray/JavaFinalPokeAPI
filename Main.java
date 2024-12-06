import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Class representing a Pokémon
class Pokemon {
    public String name; // Pokémon name
    public double height; // Pokémon height
    public double weight; // Pokémon weight
    public int id; // Pokémon ID
    public TypeSlot[] types; // Array of type slots
}

// Class representing a type slot
class TypeSlot {
    public TypeInfo type; // Information about the type
}

// Class representing type information
class TypeInfo {
    public String name; // Name of the type
}

public class Main {
    // Create an instance of HttpClient for sending HTTP requests
    private static final HttpClient client = HttpClient.newHttpClient();

    public static void main(String[] args) throws Exception {
        // Create a Scanner object for reading user input
        Scanner scanner = new Scanner(System.in);

        // Display the main menu options to the user
        System.out.println("WARNING! Some Pokémon heights and weights may not be correct. This is due to an error within the source for the data on the Pokémon" +
                "\nPlease choose how you would like to find your Pokémon: " +
                "\n1. Name or Pokédex Number" +
                "\n2. List of all Pokémon with chosen type");

        // Read the user's choice
        int choice = Integer.parseInt(scanner.nextLine());

        // If the user chooses to search by name or ID
        if (choice == 1) {
            System.out.println("Enter a Pokémon name or id:");
            String nameOrId = scanner.nextLine();
            String url = "https://pokeapi.co/api/v2/pokemon/" + nameOrId.toLowerCase();

            try {
                // Create an HTTP GET request
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI(url))
                        .build();

                // Send the request and get the response
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                // If the response status is 200 (OK), parse and display the Pokémon data
                if (response.statusCode() == 200) {
                    Pokemon pokemon = parsePokemon(response.body());
                    System.out.println("Pokédex Number: " + pokemon.id);
                    System.out.println("Name: " + pokemon.name);
                    System.out.printf("Height: %.0f ft %.0f in\n", Math.floor((pokemon.height * 3.93700787) / 12), (pokemon.height * 3.93700787) % 12);
                    System.out.printf("Weight: %.1f lbs\n", pokemon.weight * 0.220462);
                    System.out.println("Types: " + String.join("/", getTypesAsStringList(pokemon.types)));
                } else {
                    System.out.println("Error: " + response.statusCode());
                }
            } catch (Exception e) {
                System.out.println("Exception Caught!");
                e.printStackTrace();
            }
        } else if (choice == 2) { // If the user chooses to search by type
            System.out.println("Enter the Pokémon type out of these types:" + "\n1. Normal" + "\n2. Fighting" + "\n3. Flying" + "\n4. Poison" + "\n5. Ground" + "\n6. Rock" + "\n7. Bug" + "\n8. Ghost" + "\n9. Steel" + "\n10. Fire" + "\n11. Water" + "\n12. Grass" + "\n13. Electric" + "\n14. Psychic" +"\n15. Ice" + "\n16. Dragon" + "\n17. Dark" + "\n18. Fairy");
            String typeName = scanner.nextLine().toLowerCase();

            List<String> pokemonList = getPokemonByType(typeName);

            if (pokemonList != null) {
                System.out.println("List of " + typeName + " type Pokémon:");
                for (String pokemon : pokemonList) {
                    System.out.println(pokemon);
                }
            } else {
                System.out.println("Failed to retrieve type information.");
            }
        }
    }

    // Method to parse Pokémon data from a response string using regex
    public static Pokemon parsePokemon(String responseBody) {
        Pokemon pokemon = new Pokemon();

        // Correctly extract the species name instead of abilities
        pokemon.name = extractWithRegex(responseBody, "\"species\":\\{\"name\":\"(.*?)\"");
        pokemon.height = Double.parseDouble(extractWithRegex(responseBody, "\"height\":(\\d+)"));
        pokemon.weight = Double.parseDouble(extractWithRegex(responseBody, "\"weight\":(\\d+)"));
        pokemon.id = Integer.parseInt(extractWithRegex(responseBody, "\"id\":(\\d+)"));

        String typesSection = extractWithRegex(responseBody, "\"types\":\\[(.*?)\\]");
        String[] typeEntries = typesSection.split("\\},\\{");

        pokemon.types = new TypeSlot[typeEntries.length];
        for (int i = 0; i < typeEntries.length; i++) {
            TypeSlot typeSlot = new TypeSlot();
            TypeInfo typeInfo = new TypeInfo();
            typeInfo.name = extractWithRegex(typeEntries[i], "\"type\":\\{\"name\":\"(.*?)\"");
            typeSlot.type = typeInfo;
            pokemon.types[i] = typeSlot;
        }

        return pokemon;
    }

    // Method to get a list of Pokémon names by type using regex
    public static List<String> getPokemonByType(String type) throws Exception {
        String url = "https://pokeapi.co/api/v2/type/" + type;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            List<String> pokemonList = new ArrayList<>();
            String pokemonSection = extractWithRegex(response.body(), "\"pokemon\":\\[(.*?)\\]");
            String[] pokemonEntries = pokemonSection.split("\\},\\{");

            for (String entry : pokemonEntries) {
                String nameEntry = extractWithRegex(entry, "\"pokemon\":\\{\"name\":\"(.*?)\"");
                if (!nameEntry.isEmpty()) {
                    pokemonList.add(nameEntry);
                }
            }

            return pokemonList;
        } else {
            System.out.println("Failed to retrieve data: " + response.statusCode());
            return null;
        }
    }

    // Method to get a list of type names as strings
    public static List<String> getTypesAsStringList(TypeSlot[] types) {
        List<String> typesList = new ArrayList<>();
        for (TypeSlot typeSlot : types) {
            typesList.add(typeSlot.type.name);
        }
        return typesList;
    }

    // Helper method to extract a substring using regex
    public static String extractWithRegex(String text, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }
}
