import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;

public class HeadlineData {
    public HeadlineData() throws Exception
    {
        FileReader fileReader = new FileReader("assets/RealHeadlines.json");
        // parsing file "RealHeadlines.json"
        JSONObject realHeadlinesJSON = (JSONObject)new JSONParser().parse(fileReader);
        JSONArray realHeadlines = (JSONArray) realHeadlinesJSON.get("headlines");
        Iterator arrayItr = realHeadlines.iterator();
        while (arrayItr.hasNext())
        {
            Iterator<Map.Entry> mapItr = ((Map) arrayItr.next()).entrySet().iterator();
            while (mapItr.hasNext())
            {
                Map.Entry pair = mapItr.next();
                System.out.println(pair.getKey() + " : " + pair.getValue());
            }
        }
    }
}