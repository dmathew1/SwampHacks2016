package com.clarifai.androidstarter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.clarifai.api.ClarifaiClient;
import com.clarifai.api.RecognitionRequest;
import com.clarifai.api.RecognitionResult;
import com.clarifai.api.Tag;
import com.clarifai.api.exception.ClarifaiException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;


/**
 * A simple Activity that performs recognition using the Clarifai API.
 */
public class RecognitionActivity extends Activity {
	private static final String TAG = RecognitionActivity.class.getSimpleName();

	// IMPORTANT NOTE: you should replace these keys with your own App ID and secret.
	// These can be obtained at https://developer.clarifai.com/applications
	private static final String APP_ID = "ZPsnrk88k5PoOPh33vuo9MMhqlv7Xk2SUkJ5UZP4";
	private static final String APP_SECRET = "Y1EYgZh9gQe2WFZtzJJ8EJLgtKutg5jsBvEU5KLx";

	private static final ArrayList<String> foodBank = new ArrayList<String>(
			Arrays.asList("egg", "beef", "broccoli", "apple", "banana", "pear", "cheese", "tomato"));

	//Results
	private ArrayList<String> foodResults = new ArrayList<String>();

	private static final int CODE_PICK = 1;

	private final ClarifaiClient client = new ClarifaiClient(APP_ID, APP_SECRET);
	private Button cameraButton;
	private ImageView imageView;
	private TextView textView;
	private static final int CAM_REQUEST = 1313;
	private int count;
	private ArrayList<String> recipeList = new ArrayList<String>();
	ArrayList<Recipe> entireRecipeList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		goToHomeScreen();
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (requestCode == CAM_REQUEST) {
			// The user has taken an image. Send it to Clarifai for recognition.
			Log.d(TAG, "User picked image: " + intent.getData());
			Bitmap bitmap = loadBitmapFromUri(intent.getData());
			if (bitmap != null) {
				imageView.setImageBitmap(bitmap);
				textView.setText("Recognizing...");
				cameraButton.setEnabled(false);

				// Run recognition on a background thread since it makes a network call.
				new AsyncTask<Bitmap, Void, RecognitionResult>() {
					@Override
					protected RecognitionResult doInBackground(Bitmap... bitmaps) {
						return recognizeBitmap(bitmaps[0]);
					}
					@Override
					protected void onPostExecute(RecognitionResult result) {
						updateUIForResult(result);
					}
				}.execute(bitmap);
			} else {
				textView.setText("Unable to load selected image.");
			}
		}
	}

	public void goToHomeScreen(){
		setContentView(R.layout.landng_menu);
		TextView myText = (TextView) findViewById(R.id.button3 );
		Animation anim = new AlphaAnimation(0.0f, 1.0f);
		anim.setDuration(400);
		anim.setStartOffset(20);
		anim.setRepeatMode(Animation.REVERSE);
		anim.setRepeatCount(Animation.INFINITE);
		myText.startAnimation(anim);


		Button startButton = (Button) findViewById(R.id.button3);
		startButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				goToStartScreen();
			}
		});
	}

	public void goToStartScreen() {
		setContentView(R.layout.activity_recognition);

		imageView = (ImageView) findViewById(R.id.image_view);
		textView = (TextView) findViewById(R.id.text_view);
		cameraButton = (Button) findViewById(R.id.camera_button);
		cameraButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				findViewById(R.id.camera_button).setVisibility(View.INVISIBLE);
				findViewById(R.id.camera_text).setVisibility(View.INVISIBLE);
				findViewById(R.id.camera_down).setVisibility(View.INVISIBLE);
				findViewById(R.id.app_description).setVisibility(View.INVISIBLE);
				Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(cameraIntent, CAM_REQUEST);
			}
		});
	}

	public void goToListRecipe() {
		setContentView(R.layout.list_recipe);
		ArrayAdapter<TextView> itemsAdapter = new ArrayAdapter(this, R.layout.list_text_view, foodResults);
		ListView listView = (ListView) findViewById(R.id.listView);
		listView.setAdapter(itemsAdapter);

		//Creates the functionality of the button
		Button getRecipe = (Button) findViewById(R.id.get_recipe_list);
		getRecipe.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getRecipeList();

			}
		});

		Button getIngredients = (Button) findViewById(R.id.back);
		getIngredients.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				goToStartScreen();

			}
		});

	}

	/**
	 * Loads a Bitmap from a content URI returned by the media picker.
	 */
	private Bitmap loadBitmapFromUri(Uri uri) {
		try {
			// The image may be large. Load an image that is sized for display. This follows best
			// practices from http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, opts);
			int sampleSize = 1;
			while (opts.outWidth / (2 * sampleSize) >= imageView.getWidth() &&
					opts.outHeight / (2 * sampleSize) >= imageView.getHeight()) {
				sampleSize *= 2;
			}

			opts = new BitmapFactory.Options();
			opts.inSampleSize = sampleSize;
			return BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, opts);
		} catch (IOException e) {
			Log.e(TAG, "Error loading image: " + uri, e);
		}
		return null;
	}

	/**
	 * Sends the given bitmap to Clarifai for recognition and returns the result.
	 */
	private RecognitionResult recognizeBitmap(Bitmap bitmap) {
		try {
			// Scale down the image. This step is optional. However, sending large images over the
			// network is slow and  does not significantly improve recognition performance.
			Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 320,
					320 * bitmap.getHeight() / bitmap.getWidth(), true);

			// Compress the image as a JPEG.
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			scaled.compress(Bitmap.CompressFormat.JPEG, 90, out);
			byte[] jpeg = out.toByteArray();

			// Send the JPEG to Clarifai and return the result.
			return client.recognize(new RecognitionRequest(jpeg)).get(0);
		} catch (ClarifaiException e) {
			Log.e(TAG, "Clarifai error", e);
			return null;
		}
	}

	/**
	 * Updates the UI by displaying tags for the given result.
	 */
	private void updateUIForResult(RecognitionResult result) {
		//counter to see if any of the tags identified any ingredients
		count = 0;
		if (result != null) {
			if (result.getStatusCode() == RecognitionResult.StatusCode.OK) {
				// Display the list of tags in the UI.
				StringBuilder b = new StringBuilder();
				//for each tag in the Clarifai results, loop through the foodBank
				for (Tag tag : result.getTags()) {
					for (String food : foodBank) {
						if (tag.getName().equals(food) && checkArray(food)) {
							foodResults.add(tag.getName());
							textView.setText(food);
							count++;
						}
					}
				}

			} else {
				Log.e(TAG, "Clarifai: " + result.getStatusMessage());
				textView.setText("Sorry, there was an error recognizing your image.");
			}
		} else {
			textView.setText("Sorry, there was an error recognizing your image.");
		}
		if (count == 0) {
			Toast.makeText(this, "Could not identify the image", Toast.LENGTH_LONG).show();
			goToStartScreen();
		} else {
			//Makes the camera button invisible on the results
			if (count != 0) {
				//Creates the Button view items and sets them as visible
				findViewById(R.id.confirm_button).setVisibility(View.VISIBLE);
				findViewById(R.id.cancel_button).setVisibility(View.VISIBLE);
                findViewById(R.id.get_ingredients_list).setVisibility(View.VISIBLE);
			} else {
				Toast.makeText(getApplicationContext(), "Could not identify the image", Toast.LENGTH_LONG).show();
				// Execute some code after 2 seconds have passed
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					public void run() {
						goToStartScreen();
					}
				}, 2000);
			}
		}


		//Creates the functionality of the button
		Button confirmButton = (Button) findViewById(R.id.confirm_button);
		confirmButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String toast = foodResults.get(foodResults.size()-1) + " has been added to the ingredient list.";
				Toast t = Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_LONG);
				t.setGravity(Gravity.CENTER,0,0);
				t.show();
				goToStartScreen();
			}
		});

		//Creates the functionality of the button
		Button cancelButton = (Button) findViewById(R.id.cancel_button);
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//remove the enqueued item
				if (foodResults.size() == 0) {
					goToStartScreen();
				}

				foodResults.remove(foodResults.size() - 1);
				cameraButton.setEnabled(true);
				goToStartScreen();
			}
		});

		//Creates the functionality of the button
		Button getIngredients = (Button) findViewById(R.id.get_ingredients_list);
		getIngredients.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				goToListRecipe();

			}
		});
	}

	public void getRecipeList() {
		Picker pkr = new Picker();
		entireRecipeList = pkr.getSomeSampleRecipes();
		entireRecipeList = pkr.pickRecipes(foodResults, entireRecipeList);
		recipeList.clear();
		for (int a = 0; a < entireRecipeList.size(); a++) {
			recipeList.add(a, entireRecipeList.get(a).name);
		}
		displayRecipe(recipeList);
	}

	public boolean checkArray(String food){
		boolean canWeAdd = false;
		for(int a = 0; a < foodResults.size(); a++){
			if(foodResults.get(a).contains(food)){
				canWeAdd = false;
			}
		}
		return canWeAdd;
	}

	public void displayRecipe(ArrayList<String> arr) {
		setContentView(R.layout.recipe_display);

		Button getIngredients = (Button) findViewById(R.id.back);
		getIngredients.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				goToListRecipe();

			}
		});

		ArrayAdapter<TextView> itemsAdapter = new ArrayAdapter(this, R.layout.list_text_view, arr);
		ListView listView = (ListView) findViewById(R.id.listView);
		listView.setAdapter(itemsAdapter);

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// When clicked, show a toast with the TextView text or do whatever you need.
				String recipeName = ((TextView) view).getText().toString();
				int tempIndex = -1;
				for(int a = 0; a < entireRecipeList.size(); a++){
					if(entireRecipeList.get(a).name ==recipeName){
						tempIndex = a;
					}
				}
				String recipeDesc = "Name: " + entireRecipeList.get(tempIndex).name + "\n";
				recipeDesc = "Desc: " + entireRecipeList.get(tempIndex).description + "\n";
				for(int a = 0; a < entireRecipeList.get(tempIndex).ingredients.length; a++){
					recipeDesc += "Ingredients: " + entireRecipeList.get(tempIndex).ingredients[a];
					recipeDesc += ", ";
				}
				Toast.makeText(getApplicationContext(), recipeDesc, Toast.LENGTH_LONG).show();
			}
		});

	}
}