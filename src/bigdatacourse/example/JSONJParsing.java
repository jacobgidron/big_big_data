package bigdatacourse.example;

import org.json.*;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
//import org.apache.commons.io.IOUtils;


public class JSONJParsing {

    public static void main(String[] args) throws IOException {
        // you will find here a few examples to handle JSON.Org
        System.out.println("you will find here a few examples to handle JSON.org");
        String path = "C:\\Users\\Administrator\\IdeaProjects\\big_big_data\\data\\meta_Office_Products.json";
//        String path = "C:\\Users\\Administrator\\IdeaProjects\\big_big_data\\data\\tmp_rev.json";
//        String path = "C:\\Users\\Administrator\\IdeaProjects\\big_big_data\\data\\reviews_Office_Products.json";
        InputStream is = new FileInputStream(path);
        FileReader reader = new FileReader(path);
        BufferedReader br = new BufferedReader(reader);
        List<String> l2 = new ArrayList<String>();
        String line;
        List<JSONObject> l = new ArrayList<JSONObject>();
        TreeSet<String> set = new TreeSet<>();
        JSONObject j;
        while ((line = br.readLine()) != null) {
            TreeSet<String> categories_set = new TreeSet<String>();
            j = new JSONObject(line);
            l.add(new JSONObject(line));
            System.out.println("not set:");
            for (Object categories : j.getJSONArray("categories")) {
                JSONArray inner = (JSONArray) categories;
                for (Object cat : inner) {
                    String s = (String) cat;
                    categories_set.add(s);
                }



//
//                JSONArray inner = (JSONArray) categories;
//                String s = inner.toString();
////                System.out.println(s);
//                String replace = s.replaceAll("^\\[|]$|\"", "");
//                categories_set.addAll(Arrays.asList(replace.split(",")));
            }
            System.out.println("set :"+ categories_set + "\n\n");

//            System.out.println(((JSONArray)(l.get(0).get("categories"))).get(0));
            l2.add(line);
        }
        
        System.out.println("you will find here a few examples to handle JSON.org");


//        JSONTokener tokener = new JSONTokener(reader);
//        JSONObject json		=	new JSONObject(tokener);
//        try {
//            while (!tokener.end()) {
////                System.out.println("befor "+tokener.end());
//                l.add(new JSONObject(tokener));
////                System.out.println("after "+tokener.end());
//            }
//        }
//        catch(Exception e){
//            System.out.println("you will find here a few examples to handle JSON.org");
//        }


        // initialize empty object
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
