import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.HashMap;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;

public class HeadlineData {

    private ArrayList<RealHeadline> realHeadlines = new ArrayList<RealHeadline>();

    public HeadlineData() throws Exception {
        FileReader fileReader = new FileReader("assets/RealHeadlines.json");
        // parsing file "RealHeadlines.json"
        JSONObject realHeadlinesJSON = (JSONObject) new JSONParser().parse(fileReader);
        JSONArray realHeadlines = (JSONArray) realHeadlinesJSON.get("headlines");
        Iterator arrayItr = realHeadlines.iterator();
        while (arrayItr.hasNext()) {
            HashMap<String, String> headlineMap = (HashMap) arrayItr.next();
            String text = headlineMap.get("text");
            String link = headlineMap.get("link");
            RealHeadline realHeadline = new RealHeadline(text, link);
            realHeadlines.add(realHeadline);
        }
    }

    public RealHeadline GetRealHeadline(){
        int randIndex = (int) Math.floor(Math.random() * realHeadlines.size());
        return realHeadlines.get(randIndex);
    }
}