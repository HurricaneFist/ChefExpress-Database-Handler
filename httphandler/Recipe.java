package httphandler;

import java.util.*;

public class Recipe implements Comparable<Recipe> {

	// Name of the recipe, a link to an image of it, and the instructions for it
	public String name, imageURL, instructions;

    // Relevance is a property of each recipe which determines its position in the search results
	public int relevance;
	
	// Approximately how long a recipe takes to prepare, in seconds
	public long cookingTime;
	
	// The ingredients for a recipe
	public ArrayList<String> ingredients, 
	
	// Each cuisine a recipe belongs to
	cuisines;

	public Recipe(String name, String imageURL, String instructions, ArrayList<String> ingredients, long cookingTime, ArrayList<String> cuisines, int relevance) {
		this.name = name;
		this.imageURL = imageURL;
		this.instructions = instructions;
		this.ingredients = ingredients;
		this.cookingTime = cookingTime;
		this.cuisines = cuisines;
		this.relevance = relevance;
	}

	public int compareTo(Recipe other) { 
		return other.relevance - relevance;
	}

	public String toString() {
		return this.name;
	}
}
