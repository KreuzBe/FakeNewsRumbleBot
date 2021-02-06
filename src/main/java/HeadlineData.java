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

    public static final String[] componentTypes =
            {"actor", "action", "object", "descriptor"};

    private ArrayList<RealHeadline> realHeadlines;
    private HashMap<String, ArrayList<String>> components;

    public HeadlineData() throws IOException, ParseException {
        realHeadlines = new ArrayList<RealHeadline>();
        components = new HashMap<String, ArrayList<String>>();
        FileReader fileReader = new FileReader("assets/RealHeadlines.json");
        // parsing file "RealHeadlines.json"
        JSONObject realHeadlinesJSON = (JSONObject) new JSONParser().parse(fileReader);
        JSONArray realHeadlines = (JSONArray) realHeadlinesJSON.get("headlines");
        Iterator arrayItr = realHeadlines.iterator();
        while (arrayItr.hasNext()) {
            // save the data about the headline in a map            
            HashMap<String, String> headlineMap = (HashMap) arrayItr.next();

            // transfer data from map to headline object and component dictionary
            RealHeadline realHeadline = new RealHeadline();
            for (int i = 0; i < componentTypes.length; i++) {
                String componentText = headlineMap.get(componentTypes[i]);
                if (componentText != null) {
                    realHeadline.AddComponent(componentTypes[i], componentText);
                    ArrayList<String> componentsOfType = components.get(componentTypes[i]);
                    if (componentsOfType == null) {
                        componentsOfType = new ArrayList<String>();
                    }
                    componentsOfType.add(componentText);
                    components.put(componentTypes[i], componentsOfType);
                }
            }
            String link = headlineMap.get("link");
            if (link != null) {
                realHeadline.SetLink(link);
            }

            // add headline object to list
            realHeadlines.add(realHeadline);
        }
    }

    public RealHeadline GetRandomRealHeadline() {
        int randIndex = (int) Math.floor(Math.random() * realHeadlines.size());
        return realHeadlines.get(randIndex);
    }

    public String GetRandomHeadlineComponent(String componentType) {
        ArrayList<String> componentsOfReqType = components.get(componentType);
        if (componentsOfReqType == null) {
            return null;
        }
        int randIndex = (int) Math.floor(Math.random() * componentsOfReqType.size());
        return componentsOfReqType.get(randIndex);
    }
}