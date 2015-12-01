package httphandler;

import java.io.*;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;

import java.net.*;
import javax.net.ssl.HttpsURLConnection;
import java.util.ArrayList;
import java.util.Collections;

public class HTTPHandler {

    // API ID and API key needed to access the API
	final static String ID;
	final static String KEY;

    // The max number of results to have the API return to the user
	final static int MAX_RESULTS = 100;
	
	/* Attribution requirements for using Yummly's API, can only be
	 * used after getRecipes() has already been called
	 */
	static ArrayList<String> attribution = new ArrayList<String>();

	/**
	 * Takes an ArrayList<String> of ingredients, and returns an ArrayList<Recipe> of recipes
	 * which contain the ingredients provided.
	 * @param ingredients              a list of ingredients
	 * @return An ArrayList of Recipe objects.
	 * @throws Exception
	 */
	public static ArrayList<Recipe> getRecipes(ArrayList<String> ingredients) throws Exception {
		return getRecipes(ingredients, "", -1, -1, -1, -1, -1);
	}

    /**
     * Same as the above method, but allows for much more user customization for things such 
     * as dietary restrictions, sodium limits, calories, protein, preparation time, etc.
     * 
     * If an empty string is entered as an argument for any String parameter, that parameter
     * is not specified for. Likewise, if a negative number is entered as an argument for 
     * any Integer parameter, that parameter is not specified for.
     * 
     * @param ingredients              a list of ingredients
     * @param cuisine                  the type of cuisine (i.e. american, french, mediterranean, etc.)
     * @param kCalMin                  minimum calories 
     * @param kCalMax                  maximum calories 
     * @param carbLimit                minimum carbohydrates in grams
     * @param proteinMin               minimum protein in grams
     * @param timeLimit                maximum time to prepare recipe in seconds
     * @return
     * @throws Exception
     */
	public static ArrayList<Recipe> getRecipes(ArrayList<String> ingredients, String cuisine, int kCalMin, int kCalMax, int carbLimit, int proteinMin,
	                                           int timeLimit) throws Exception {

		// Check if each parameter was specified for by the user
		boolean cuisineSpecified = cuisine.equals("") ? false : true; 
		boolean kCalMinSpecified = (kCalMin < 0) ? false : true; 
		boolean kCalMaxSpecified = (kCalMax < 0) ? false : true;
		boolean carbLimitSpecified = (carbLimit < 0) ? false : true;
		boolean proteinMinSpecified = (proteinMin < 0) ? false : true;
		boolean timeLimitSpecified = (timeLimit < 0) ? false : true;

		if (kCalMinSpecified && kCalMaxSpecified && kCalMin > kCalMax)
			throw new Exception("ERROR: Minimum calories specified cannot be greater than maximum calories specified.");

        // Initializing the ArrayList of recipes to be returned
		ArrayList<Recipe> recipes = new ArrayList<Recipe>();

        // Processing parameters for the URL used in the GET call
		String params = "";
		for (int i = 0; i < ingredients.size(); i++)
			params += ("&allowedIngredient[]=" + ingredients.get(i));

		if (kCalMinSpecified)
			params += "&nutrition.ENERC_KCAL.min=" + kCalMin;
   
		if (kCalMaxSpecified)
			params += "&nutrition.ENERC_KCAL.max=" + kCalMax;

		if (cuisineSpecified)
			params += "&allowedCuisine[]=cuisine^cuisine-" + cuisine;

		if (carbLimitSpecified)
			params += "&nutrition.CHOCDF.max=" + carbLimit;

		if (proteinMinSpecified)
			params += "&nutrition.PROCNT.min=" + proteinMin;

		if (timeLimitSpecified)
			params += "&maxTotalTimeInSeconds=" + timeLimit;

		// Initializing the HTTP URL connection
		URL url = new URL("http://api.yummly.com/v1/api/recipes?_app_id=" + ID + "&_app_key=" + KEY + params + "&maxResult=" + MAX_RESULTS);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");

		// Acquiring the JSON from the GET call
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		StringBuffer responseJSON = new StringBuffer(in.readLine());

		// Parsing the JSON returned from the GET call
		JSONParser parser = new JSONParser();
		JSONObject json = (JSONObject) parser.parse(responseJSON.toString());
		JSONArray data = (JSONArray) json.get("matches");
		
		// Get the attribution
		JSONObject rawAttribution = (JSONObject) json.get("attribution");
		attribution.add((String) rawAttribution.get("html"));
		attribution.add((String) rawAttribution.get("url"));
		attribution.add((String) rawAttribution.get("text"));
		attribution.add((String) rawAttribution.get("logo"));

		/* Looping through each recipe, converting it to a Recipe object, and adding it to the 
		 * ArrayList<Recipe> which will be returned by getRecipes
		 */
		for (int i = 0; i < data.size(); i++) {
			JSONObject jsonRecipe = (JSONObject) data.get(i);
			JSONArray jsonIngredients = (JSONArray) jsonRecipe.get("ingredients");
			
			ArrayList<String> returnedIngredients = new ArrayList<String>();
			for (int j = 0; j < jsonIngredients.size(); j++)
				returnedIngredients.add((String) jsonIngredients.get(j)); 

			// Relevance of a recipe is inversely proportional to the number of ingredients it contains
			int relevance = -returnedIngredients.size();

			// Set the time needed to prepare each recipe, if the user specified timeLimit
			long returnedTime;
			if (jsonRecipe.get("totalTimeInSeconds") == null)
				returnedTime = -1;
			else
				try {
					returnedTime = (Long) jsonRecipe.get("totalTimeInSeconds");
				} catch (Exception e) {
					returnedTime = -1;
				}
			
			/* Set all cuisines the recipe is in (apparently a recipe can be in multiple cuisines),
			 * if the user specified cuisine
			 */
			ArrayList<String> returnedCuisines = new ArrayList<String>();
			if (cuisineSpecified) {
				JSONObject rawAttributes = (JSONObject) jsonRecipe.get("attributes");
				JSONArray rawCuisines = (JSONArray) rawAttributes.get("cuisine");
				for (int j = 0; j < rawCuisines.size(); j++)
					returnedCuisines.add((String) rawCuisines.get(j));
			}
			
			// Check if image URL exists, if it doesn't then put some filler there for it
			String imageURL = "NO IMAGE URL FOUND";
			if ((jsonRecipe.get("smallImageUrls")) != null)
				imageURL = (String) ((JSONArray) jsonRecipe.get("smallImageUrls")).get(0);
			
			// Create a new Recipe object for each recipe in the array, and add that to the ArrayList<Recipe>
			recipes.add(new Recipe(
				(String) jsonRecipe.get("recipeName"),
				imageURL,
				"FILLER INSTRUCTIONS",  // TODO: find a method of getting instructions for the recipe, if it exists
				returnedIngredients,
				returnedTime,
				returnedCuisines,
				relevance
			));
		}

		in.close();
		
		// Sort the ArrayList<Recipe> based on relevance, and return it
		Collections.sort(recipes); 
		return recipes;
	}

	public static void main(String[] args) throws Exception {

		/* USAGE INSTRUCTIONS
		
		// Example input: Search for recipes containing cinnamon and apple.
		
		ArrayList<String> input = new ArrayList<String>() {{
			add("cinnamon");
			add("apple");
		}};
		
		// Output: Returns a list of 100 recipes containing cinnamon and apple in them
		// The first (most relevant) result returned is applesauce, which can be made with only cinnamon and apple
		// The last (least relevant) result returned may be roasted turkey, which needs many more ingredients than provided
		
		ArrayList<Recipe> list = getRecipes(input);

		for (Recipe recipe: list)
			System.out.println(recipe);
		
		// After calling getRecipes(), it is required to display the attribution to Yummly's API
		
		System.out.println(attribution);
		
		*/
	}
}
