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

// compile with: javac -cp httphandler/json-simple-1.1.1.jar httphandler/*.java

public class HTTPHandler {

    // for now you'll need to manually enter these
	final static String ID;
	final static String KEY;

    // max number of results returned
	final static int MAX_RESULTS = 100;

	// simply calling getRecipes with no specification for testing purposes
	public static ArrayList<Recipe> getRecipes(ArrayList<String> ingredients) throws Exception {
		return getRecipes(ingredients, "", -1, -1, -1, -1, -1);
	}

    // the highly customizable version of getRecipes
	public static ArrayList<Recipe> getRecipes(ArrayList<String> ingredients, String cuisine, int kCalMin, int kCalMax, int carbLimit, int proteinMin,
	                                           int timeLimit) throws Exception {

		boolean cuisineSpecified = cuisine.equals("") ? false : true; // if an empty string is entered as an argument for cuisine, then cuisine is unspecified
		boolean kCalMinSpecified = (kCalMin < 0) ? false : true; // if integer parameters for mins/maxs are inputted as negative, they are unspecified
		boolean kCalMaxSpecified = (kCalMax < 0) ? false : true;
		boolean carbLimitSpecified = (carbLimit < 0) ? false : true;
		boolean proteinMinSpecified = (proteinMin < 0) ? false : true;
		boolean timeLimitSpecified = (timeLimit < 0) ? false : true;

		if (kCalMinSpecified && kCalMaxSpecified && kCalMin > kCalMax)
			throw new Exception("ERROR: Minimum calories specified cannot be greater than maximum calories specified.");

        // initializing the ArrayList of recipes to be returned
		ArrayList<Recipe> recipes = new ArrayList<Recipe>();

        // parameters for the GET call
		String params = "";
		for (int i = 0; i < ingredients.size(); i++)
		  // the ingredients argument is an arraylist of ingredients, i.e. new ArrayList(Arrays.asList("cinnamon", "apple", "butter"));
			params += ("&allowedIngredient[]=" + ingredients.get(i));

	    // kCalMin is an integer in kilocalories (AKA food calories)
		if (kCalMinSpecified)
			params += "&nutrition.ENERC_KCAL.min=" + kCalMin;
   
        // kCalMax is same as above
		if (kCalMaxSpecified)
			params += "&nutrition.ENERC_KCAL.max=" + kCalMax;

		// the cuisine argument should be a demonym like "american", "french", "mediterranean", etc.
		if (cuisineSpecified)
			params += "&allowedCuisine[]=cuisine^cuisine-" + cuisine;

		// the carbLimit argument should be an integer in grams
		if (carbLimitSpecified)
			params += "&nutrition.CHOCDF.max=" + carbLimit;

		// the proteinMin argument should be an integer in grams
		if (proteinMinSpecified)
			params += "&nutrition.PROCNT.min=" + proteinMin;

        // a limit on how long the meal should take to prepare, in seconds
		if (timeLimitSpecified)
			params += "&maxTotalTimeInSeconds=" + timeLimit;

		URL url = new URL("http://api.yummly.com/v1/api/recipes?_app_id=" + ID + "&_app_key=" + KEY + params + "&maxResult=" + MAX_RESULTS);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		conn.setRequestMethod("GET");

		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

		StringBuffer responseJSON = new StringBuffer(in.readLine());

		JSONParser parser = new JSONParser();
		JSONObject json = (JSONObject) parser.parse(responseJSON.toString());
		JSONArray data = (JSONArray) json.get("matches");

		for (int i = 0; i < data.size(); i++) {
			JSONObject jsonRecipe = (JSONObject) data.get(i);
			JSONArray jsonIngredients = (JSONArray) jsonRecipe.get("ingredients");
			ArrayList<String> returnedIngredients = new ArrayList<String>();

			for (int j = 0; j < jsonIngredients.size(); j++)
				returnedIngredients.add((String) jsonIngredients.get(j)); // load the ingredients from the JSON obj into the returned obj

			// the more ingredients the recipe has that the user didn't enter, the lower the relevance
			int relevance = -returnedIngredients.size();

            // getting the data for the returned recipe object from the JSON, TODO: test it!
			recipes.add(new Recipe(
				(String) jsonRecipe.get("recipeName"),
				(String) jsonRecipe.get("smallImageUrls"),
				"FILLER INSTRUCTIONS",  // TODO: find a method of getting instructions for the recipe, if it exists
				returnedIngredients,
				Integer.parseInt((String) jsonRecipe.get("totalTimeInSeconds")),
				(String) ((JSONObject) jsonRecipe.get("attributes")).get("cuisine"),
				relevance
			));
		}

		in.close();
		Collections.sort(recipes); // TODO: check if sorting works properly
		return recipes;
	}

	public static void main(String[] args) throws Exception {

		/*
		Test code

		ArrayList<String> input = new ArrayList<String>();
		input.add("cinnamon");
		input.add("apple");

		System.out.println(getRecipes(input));

		*/
	}

}
