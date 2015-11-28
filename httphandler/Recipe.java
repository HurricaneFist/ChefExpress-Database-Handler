package httphandler;

import java.util.*;

public class Recipe implements Comparable {

	public String cuisine, name, imageURL, instructions;
	public int cookingTime, relevance;
	public ArrayList<String> ingredients;

	public Recipe(String name, String imageURL, String instructions, ArrayList<String> ingredients, int cookingTime, String cuisine, int relevance) {
		this.name = name;
		this.imageURL = imageURL;
		this.instructions = instructions;
		this.ingredients = ingredients;
		this.cookingTime = cookingTime;
		this.cuisine = cuisine;
		this.relevance = relevance;
	}

  @Override
	public int compareTo(Recipe other) { 
		return relevance - other.relevance;
	}

	public String toString() {
		return this.name;
	}
}
