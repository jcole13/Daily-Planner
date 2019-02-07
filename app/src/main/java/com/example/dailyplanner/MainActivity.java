package com.example.dailyplanner;

//Need to fix the weathertask subclass, make it so only sentence, weather, icon are what is changed
//make sure city infor displayed correctly
//dont know why R is not recognized
//Make sure asynctask runs as expected

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.icu.text.DateFormat;
import android.icu.text.DecimalFormat;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.dailyplanner.db.TaskContract;
import com.example.dailyplanner.db.TaskDbHelper;

import java.util.ArrayList;
import java.util.Date;

import data.CityPreferences;
import data.JSONWeatherParser;
import data.WeatherhttpClient;
import model.Weather;


public class MainActivity extends AppCompatActivity {

    //instantiate all private fields in the activity
    private static final String TAG = "MainActivity";
    private TaskDbHelper mHelper;
    private ListView mTaskListView;
    private ArrayAdapter<String> mAdapter;
    private TextView cityName;
    private TextView temp;
    private TextView iconView;
    private TextView updated;
    private TextView sentenceView;

    private String units;

    Typeface weatherFont;

    //instantiate weather so you can use itst methods
    Weather weather = new Weather();




    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); //load what was on page before closed
        setContentView(R.layout.activity_main);

        weatherFont = Typeface.createFromAsset(getAssets(), "fonts/weather.ttf"); //special symbols for weather icons
        cityName = (TextView) findViewById(R.id.cityText);
        iconView = (TextView) findViewById(R.id.thumbnailIcon);
        temp = (TextView) findViewById(R.id.tempText);
        sentenceView = (TextView) findViewById(R.id.sentence);
        //updated = (TextView) findViewById(R.id.updatedtext);

        iconView.setTypeface(weatherFont);
        //city saved outside of the activity
        CityPreferences cityPreference = new CityPreferences(MainActivity.this);


        //initial rendering using data that was in the app on its last closing
        renderWeatherData(cityPreference.getCity(), "metric");
        units = "metric";
        //Database is accessed

        mHelper = new TaskDbHelper(this); //DB used to save tasks for later
        mTaskListView = (ListView) findViewById(R.id.list_todo);

        //list UI is changed on startup
        updateUI();

    }
    public void renderWeatherData(String city, String measure) {
        //calls subclass weathertask to run in the background
        WeatherTask weatherTask = new WeatherTask();
        //Takes in string array with a single spot filled, that is the end of the url
        weatherTask.execute(new String[]{city + "&units=" + measure});
    }
    //change units of measure
    public void toImperial(View view){
        CityPreferences cityPreferences = new CityPreferences(MainActivity.this);
        String newCity = cityPreferences.getCity();
        renderWeatherData(newCity, "imperial");
        units = "imperial";

    }
    public void toMetric(View view){
        CityPreferences cityPreferences = new CityPreferences(MainActivity.this);
        String newCity = cityPreferences.getCity();
        renderWeatherData(newCity, "metric"); //re-render
        units = "metric";

    }


    private class WeatherTask extends AsyncTask<String, Void, Weather> { //runs in the background
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override

        //when the task is finished executing
        protected void onPostExecute(Weather weather) { //after the data is collected
            super.onPostExecute(weather);

            DateFormat df = DateFormat.getTimeInstance();


            //Goes through all the text and assigns based on values from the JSON object in the API

            String sunriseDate = df.format(new Date(weather.place.getSunrise()));
            String sunsetDate = df.format(new Date(weather.place.getSunset()));
            String updateDate = df.format(new Date(weather.place.getLastupdate()));

            DecimalFormat decimalFormat = new DecimalFormat("#.#");
            String temperatureformat = decimalFormat.format(weather.currentcondition.getTemperature());

            cityName.setText(weather.place.getCity() + ", " + weather.place.getCountry());
            if(units.equals("metric")) {
                temp.setText(temperatureformat + "℃"); //celsius
                String num = temp.toString();
                //int parser = Integer.parseInt(num.substring(0, num.length() - 1));
                int parser = (int) Double.parseDouble(temperatureformat);
                String weathercode = weather.currentcondition.getIcon();
                //checking through weather codes, pretty mundane
                //Although these if/else statements are basically AI
                if (parser < 10) {
                    if (weathercode.substring(0, 2).equals("13"))
                        sentenceView.setText("Go for a heavy jacket, or something that has a hood and long pants. Maybe a hat and gloves as well");
                    if (weathercode.substring(0, 2).equals("10"))
                        sentenceView.setText("Go for a medium jacket and long pants, or something with a hood");
                    else sentenceView.setText("Go for a medium jacket, it is a little chilly");
                } else if (parser < 20) {
                    if (weathercode.substring(0, 2).equals("10"))
                        sentenceView.setText("Go for a light jacket, or something with a hood");
                    else
                        sentenceView.setText("Go for a light jacket, there's a chance that you will be cold");

                } else if (parser > 22) {
                    if (weathercode.substring(0, 2).equals("10"))
                        sentenceView.setText("It's very warm out, so go for short sleeves, but beware of possible rain");
                    else
                        sentenceView.setText("No rain, go for short sleeves. Maybe even a hat with sunglasses");

                } else {
                    if (weathercode.substring(0, 2).equals("10"))
                        sentenceView.setText("It's pretty warm out. Depending on preference, go for short sleeves or long sleeves, but beware of possible rain");
                    else
                        sentenceView.setText("No rain, and it should be a nice day out today. Go for short sleeves, and lightweight pants");

                }
            }
            else{
                temp.setText(temperatureformat + "℉" ); //farenheight
                String num = temp.toString();
                //int parser = Integer.parseInt(num.substring(0, num.length() - 1));
                int parser = (int) Double.parseDouble(temperatureformat);
                String weathercode = weather.currentcondition.getIcon();
                if (parser < 50) {
                    if (weathercode.substring(0, 2).equals("13"))
                        sentenceView.setText("Go for a heavy jacket, or something that has a hood and long pants. Maybe a hat and gloves as well");
                    if (weathercode.substring(0, 2).equals("10"))
                        sentenceView.setText("Go for a medium jacket and long pants, or something with a hood");
                    else sentenceView.setText("Go for a medium jacket, it is a little chilly");
                } else if (parser < 70) {
                    if (weathercode.substring(0, 2).equals("10"))
                        sentenceView.setText("Go for a light jacket, or something with a hood");
                    else
                        sentenceView.setText("Go for a light jacket, there's a chance that you will be cold");

                } else if (parser > 75) {
                    if (weathercode.substring(0, 2).equals("10"))
                        sentenceView.setText("It's very warm out, so go for short sleeves, but beware of possible rain");
                    else
                        sentenceView.setText("No rain, go for short sleeves. Maybe even a hat with sunglasses");

                } else {
                    if (weathercode.substring(0, 2).equals("10"))
                        sentenceView.setText("It's pretty warm out. Depending on preference, go for short sleeves or long sleeves, but beware of possible rain");
                    else
                        sentenceView.setText("No rain, and it should be a nice day out today. Go for short sleeves, and lightweight pants");

                }

            }
            //humidity.setText("Humidity: " + weather.currentcondition.getHumidity() + " %");
            //pressure.setText("Pressure: " + weather.currentcondition.getPressure() + " hPa");

            //sunrise.setText("Sunrise: " + sunriseDate);
            //sunset.setText("Sunset: " + sunsetDate);
            //updated.setText("Last Updated: " + updateDate);
            //description.setText("Condition: " + weather.currentcondition.getCondition() + " (" + weather.currentcondition.getDescription() + ")" );


            String icon = "";
            String code = weather.currentcondition.getIcon();
            if(weather.currentcondition.getIcon().charAt(2) == 'n'){ //checking for what code to update the big icon
                //these are in the special font
                if(code.substring(0, 2).equals("13")){
                    icon= getApplicationContext().getString(R.string.weather_snowy);
                }
                else if(code.substring(0, 2).equals("10")){
                    icon= getApplicationContext().getString(R.string.weather_drizzle);
                }
                else if(code.substring(0, 2).equals("03") || code.substring(0, 2).equals("04")){
                    icon= getApplicationContext().getString(R.string.weather_cloudy);
                }
                else if(code.substring(0, 2).equals("50")){
                    icon= getApplicationContext().getString(R.string.weather_foggy);
                }
                else{
                    icon= getApplicationContext().getString(R.string.weather_clear_night);
                    //Log.v("Substring: ", code.substring(0, 2));
                }

            }
            else{
                if(code.substring(0, 2).equals("13")){
                    icon= getApplicationContext().getString(R.string.weather_snowy);
                }
                else if(code.substring(0, 2).equals("10")){
                    icon= getApplicationContext().getString(R.string.weather_drizzle);
                }
                else if(code.substring(0, 2).equals("03") || code.substring(0, 2).equals("04")){
                    icon= getApplicationContext().getString(R.string.weather_cloudy);
                }
                else if(code.substring(0, 2).equals("50")){
                    icon= getApplicationContext().getString(R.string.weather_foggy);
                }
                else{
                    icon= getApplicationContext().getString(R.string.weather_sunny);
                    //Log.v("Substring: ", code.substring(0, 2));
                }
            }
            iconView.setText(icon);





        }
        //getWeather() returns a weather object that has been filled in with values, onPostExecute() uses this object
        @Override
        protected Weather doInBackground(String... params) {

            String data = ( (new WeatherhttpClient()).getWeatherData(params[0]));

            weather = JSONWeatherParser.getWeather(data);
            //Log.v("Description: ", weather.currentcondition.getDescription());
            //Log.v("Icon Code: ", weather.currentcondition.getIcon());

            return weather; //all collected weather data which holds instances of each type of weather that I am collecting


        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override

    //First condition opens up menu for adding an item to todolist
    //second changes city
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_add_task) {
            final EditText taskEditText = new EditText(this);
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Add a new task")
                    .setMessage("What do you want to do next?")
                    .setView(taskEditText)
                    .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String task = String.valueOf(taskEditText.getText());
                            //updateUI();
                            SQLiteDatabase db = mHelper.getWritableDatabase(); //sets up place to store tasks
                            //Utilizes arrayadapter to write to database
                            ContentValues values = new ContentValues();
                            values.put(TaskContract.TaskEntry.COL_TASK_TITLE, task);
                            db.insertWithOnConflict(TaskContract.TaskEntry.TABLE,
                                    null,
                                    values,
                                    SQLiteDatabase.CONFLICT_REPLACE);
                            db.close(); //close
                            updateUI(); //refresh the tasks
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .create();
            dialog.show();
            return true;
        }
            if(id == R.id.change_cityId) {
                //used a method here, a little cleaner
                showInputDialog();
            }
                return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    //as it is called, refreshes the screen (from the DB)
    private void updateUI() {
        ArrayList<String> taskList = new ArrayList<>(); //start as an arraylist for middleman
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.query(TaskContract.TaskEntry.TABLE,
                new String[]{TaskContract.TaskEntry._ID, TaskContract.TaskEntry.COL_TASK_TITLE},
                null, null, null, null, null);
        while (cursor.moveToNext()) {
            int idx = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_TITLE);
            taskList.add(cursor.getString(idx)); //add to arraylist each task in DB
        }

        //Writes to the screen

        if (mAdapter == null) { //create a new object if null
            mAdapter = new ArrayAdapter<>(this,
                    R.layout.item_todo,
                    R.id.task_title,
                    taskList);
            mTaskListView.setAdapter(mAdapter);
        } else { //add the task to existing mAdapter
            mAdapter.clear();
            mAdapter.addAll(taskList);
            mAdapter.notifyDataSetChanged();
        }

        cursor.close();
        db.close();
    }
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    //when done is clicked, the task is removed from database, cleared on updateUI()
    public void deleteTask(View view) {
        View parent = (View) view.getParent();
        TextView taskTextView = (TextView) parent.findViewById(R.id.task_title);
        String task = String.valueOf(taskTextView.getText());
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.delete(TaskContract.TaskEntry.TABLE,
                TaskContract.TaskEntry.COL_TASK_TITLE + " = ?",
                new String[]{task});
        db.close();
        updateUI();
    }
    //if weather is selected on the drop down menu
    //inputdialog is where to type in new city in form (City,US)
    private void showInputDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Change City");

        final EditText cityInput = new EditText(MainActivity.this);
        cityInput.setInputType(InputType.TYPE_CLASS_TEXT);
        cityInput.setHint("Chicago,US"); //this is the only form that works
        builder.setView(cityInput);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                CityPreferences cityPreferences = new CityPreferences(MainActivity.this); //updates the city, refreshes the weatherdata
                cityPreferences.setCity(cityInput.getText().toString());

                String newcity = cityPreferences.getCity();

                renderWeatherData(newcity, units);

            }
        });
        builder.show(); //boilerplate to show the input dialog
    }
}



