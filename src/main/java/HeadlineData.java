import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class HeadlineData {

    public static final String[] componentTypes =
            {"actor", "action", "object", "descriptor"};

    private ArrayList<RealHeadline> realHeadlineList;
    private HashMap<String, ArrayList<String>> components;

    public HeadlineData() throws IOException, ParseException {
        realHeadlineList = new ArrayList<RealHeadline>();
        components = new HashMap<String, ArrayList<String>>();
        FileReader fileReader = new FileReader("assets/RealHeadlines.json");
        // parsing file "RealHeadlines.json"
        JSONObject realHeadlinesJSON = (JSONObject) new JSONParser().parse(fileReader);
        JSONArray realHeadlines = (JSONArray) realHeadlinesJSON.get("headlines");

        for (Object headline : realHeadlines) {
            // save the data about the headline in a map
            JSONObject headlineMap = (JSONObject) headline;

            // transfer data from map to headline object and component dictionary
            RealHeadline realHeadline = new RealHeadline();
            for (String componentType : componentTypes) {
                String componentText = (String) headlineMap.get(componentType);
                if (componentText != null) {
                    realHeadline.AddComponent(componentType, componentText);
                    ArrayList<String> componentsOfType = components.get(componentType);
                    if (componentsOfType == null) {
                        componentsOfType = new ArrayList<String>();
                    }
                    componentsOfType.add(componentText);
                    components.put(componentType, componentsOfType);
                }
            }
            String link = (String) headlineMap.get("link");
            if (link != null) {
                realHeadline.SetLink(link);
            }

            // add headline object to list
            realHeadlineList.add(realHeadline);
        }
    }

    public RealHeadline GetRandomRealHeadline() {
        int randIndex = (int) Math.floor(Math.random() * realHeadlineList.size());
        return realHeadlineList.get(randIndex);
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