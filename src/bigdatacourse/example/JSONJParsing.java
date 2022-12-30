package bigdatacourse.example;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
//import org.apache.commons.io.IOUtils;


public class JSONJParsing {

    public static void main(String[] args) throws FileNotFoundException {
        // you will find here a few examples to handle JSON.Org
        System.out.println("you will find here a few examples to handle JSON.org");
        String path = "C:\\Users\\Administrator\\IdeaProjects\\big_big_data\\data\\meta_Office_Products.json";
        InputStream is = new FileInputStream(path);
//        FileReader reader = new FileReader("C:\\Users\\Administrator\\IdeaProjects\\big_big_data\\file.json");
        JSONTokener tokener = new JSONTokener(is);
        JSONObject json		=	new JSONObject(tokener);								// initialize empty object
        System.out.println("you will find here a few examples to handle JSON.org");
//        JSONObject json		=	new JSONObject();								// initialize empty object
//        json				=	new JSONObject("{\"phone\":\"05212345678\"}");	// initialize from string
//
//        // adding attributes
//        json.put("street", "Einstein");
//        json.put("number", 3);
//        json.put("city", "Tel Aviv");
//        System.out.println(json);					// prints single line
//        System.out.println(json.toString(4));		// prints "easy reading"
//
//        // adding inner objects
//        JSONObject main = new JSONObject();
//        main.put("address", json);
//        main.put("name", "Rubi Boim");
//        System.out.println(main.toString(4));
//
//        // adding array (1)
//        JSONArray views = new JSONArray();
//        views.put(1);
//        views.put(2);
//        views.put(3);
//        main.put("views-simple", views);
//
//        // adding array (2)
//        JSONArray viewsExtend = new JSONArray();
//        viewsExtend.put(new JSONObject().put("movieName", "American Pie").put("viewPercentage", 72));
//        viewsExtend.put(new JSONObject().put("movieName", "Top Gun").put("viewPercentage", 100));
//        viewsExtend.put(new JSONObject().put("movieName", "Bad Boys").put("viewPercentage", 87));
//        main.put("views-extend", viewsExtend);
//
//        System.out.println(main);
//        System.out.println(main.toString(4));


    }

}
