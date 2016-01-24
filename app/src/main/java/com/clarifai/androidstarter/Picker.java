package com.clarifai.androidstarter;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;


public class Picker {
    String test = "";
    public static ArrayList<Recipe> pickRecipes(List<String> tags, ArrayList<Recipe> recipes) {
	ArrayList<Recipe> theChosen = new ArrayList<>();
	for ( Recipe recipe : recipes ) {
	    boolean hasIngredient = true;
	    for ( String ingredient : recipe.getIngredients() ) {
		if ( !tags.contains(ingredient) ) {
		    hasIngredient = false;
		}
	    }
	    if ( hasIngredient ) {
		theChosen.add(recipe);
	    }
	}
	return theChosen;
    }

    public static void main(String [] args) {
	System.out.println("TEST");
	ArrayList<Recipe> recipes = getSomeSampleRecipes();
	for ( Recipe recipe : pickRecipes(Arrays.asList("apple"),recipes) ) {
	    System.out.println("Recipe name : " + recipe.getName());
	    System.out.println("RECIPE  URL : " + recipe.getURL());
	}

	System.out.println("TEST");
	for ( Recipe recipe : pickRecipes(Arrays.asList("banana"),recipes) ) {
	    System.out.println("Recipe name : " + recipe.getName());
	    System.out.println("RECIPE  URL : " + recipe.getURL());
	}
	System.out.println("TEST");

	for ( Recipe recipe : pickRecipes(Arrays.asList("egg","cheese"),recipes) ) {
	    System.out.println("Recipe name : " + recipe.getName());
	    System.out.println("RECIPE  URL : " + recipe.getURL());
	}
	System.out.println("TEST");

	for ( Recipe recipe : pickRecipes(Arrays.asList("apple","pear","banana"),recipes) ) {
	    System.out.println("Recipe name : " + recipe.getName());
	    System.out.println("RECIPE  URL : " + recipe.getURL());
	}
	System.out.println("TEST");


    }
    public static ArrayList<Recipe> getSomeSampleRecipes() {
	ArrayList<Recipe> recipes = new ArrayList<>();
	//Cheese is the only ingredient we have with no recipe for just itself.
	recipes.add(new Recipe("Apple Sauce","Smash an apple and you get this.",
			       "http://www.food.com/recipe/applesauce-for-canning-98859",
			       new String[]{"apple"}));
	recipes.add(new Recipe("Banana FROZEN Smash","Smash a banana, FREEZE it",
			       "http://www.thekitchn.com/how-to-make-creamy-ice-cream-with-just-one-ingredient-cooking-lessons-from-the-kitchn-93414",new String[]{"banana"}));
	recipes.add(new Recipe("Banana Apple Smoothie","Blend it all in a blender, and then drink and enjoy.",
			       "http://www.instructables.com/id/How-do-you-make-a-Apple-Banana-Smoothie-2-3-peo/",new String[]{"apple","pear","banana"}));
	recipes.add(new Recipe("Steak", "Smash the apples into apple sauce, and then let the steak soak in this for 24 hours, then cook the steak in the oven until it is very well done.",
			       "http://www.foodnetwork.com/recipes/bistecca-fiorentina-recipe.html",new String[]{"beef"}));

	recipes.add(new Recipe("Boiled Egg","It's a boiled egg","http://www.food.com/recipe/easy-peeling-boiled-eggs-185013",new String[]{"egg"}));

	recipes.add(new Recipe("Egg in some fancy cheese dish.", "Someone tooks eggs, and put some cheese and shit in them.", "http://www.food.com/recipe/egg-in-cheese-meringue-nest-477895",new String[]{"egg","cheese"}));

	recipes.add(new Recipe("Brocoli and Cheese","Delicious Brocoli and Cheese","http://allrecipes.com/recipe/21155/quick-and-simple-broccoli-and-cheese/",
			       new String[]{"broccoli","cheese"})); //This also uses butter....
	recipes.add(new Recipe("Omelet","A broccoli, cheese, and egg omelet","http://www.incredibleegg.org/recipe/broccoli-cheddar-omelet/",
			       new String[]{"broccoli","egg","cheese"}));
	recipes.add(new Recipe("broccoli","5 ways to cook broccoli","http://www.thekitchn.com/how-to-cook-broccoli-5-ways-167323",new String[]{"broccoli"}));
	return recipes;

    }

}
