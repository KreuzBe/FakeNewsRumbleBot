import java.util.ArrayList;
import java.util.HashMap;

public class RealHeadline {

    private String text;
    private HashMap<String, String> components;
    private ArrayList<String> typeOrder = new ArrayList<>();
    private String link;

    public RealHeadline() {
        text = null;
        components = new HashMap<String, String>();
        link = null;
    }

    public String GetText() {
        return this.text;
    }

    public void AddComponent(String type, String text) {
        components.put(type, text);
        typeOrder.add(type);
        if (this.text == null) {
            this.text = text + " ";
        } else {
            this.text += text + " ";
        }
    }

    public HashMap<String, String> GetComponents() {
        return components;
    }

    public ArrayList<String> getTypeOrder() {
        return typeOrder;
    }

    public void SetLink(String link) {
        this.link = link;
    }

    public String GetLink() {
        return this.link;
    }
}
