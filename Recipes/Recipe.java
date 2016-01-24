import java.util.ArrayList;
import java.net.URL;
public class Recipe {
    private String[] ingredients;
    private String description = null;
    private String name = null;
    private URL url = null;

    public String getDecription() {
	return description;
    }

    public URL getURL(){
	return url;
    }

    public String getName() {
	return name;
    }

    public String[] getIngredients() {
	return ingredients;
    }

    public Recipe(String name,String description,String url,String[] ingredients) {
	this.description = description;
	this.name = name;
	this.ingredients = ingredients;
	try{
	this.url = new URL(url);
	} catch(Exception e) {
	    System.out.println("ERROR INVALID URL");
	}
    }
}
