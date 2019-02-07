package data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import Util.Utils;

/**
 * Created by Jared Cole on 11/27/2016.
 */


//accesses API
public class WeatherhttpClient {
    public String getWeatherData(String place){
        HttpURLConnection connection = null; //initialize values
        InputStream inputstream = null;

        //only works for USA
        if(!place.substring(place.indexOf('&')-2,place.indexOf('&')).toLowerCase().equals("us")) place = "New York,us&units=metric";


        try {

            //Establish Connection
            connection = (HttpURLConnection) (new URL(Utils.BASE_URL + place)).openConnection();
            connection.addRequestProperty("x-api-key","1023b2d5947227099d9c1043ed22f012"); //api key
            connection.setRequestMethod("GET"); //get request
            connection.setDoInput(true);
            connection.setDoOutput(true);

            connection.connect();

            //Read the response
            StringBuffer stringbuffer = new StringBuffer(); //this is what collects the data
            inputstream = connection.getInputStream(); //comes in as a stream
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputstream)); //reads the stream
            String line = null;
            while((line = bufferedReader.readLine()) != null){
                stringbuffer.append(line + "\r\n");//each line added to the string
            }
            inputstream.close();
            connection.disconnect();
            return stringbuffer.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
