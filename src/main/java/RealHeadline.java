import java.util.HashMap;

public class RealHeadline {

    private String text;
    private HashMap<String, String> components;
    private String link;

    public RealHeadline(){
        text = null;
        components = new HashMap<String, String>();
        link = null;
    }

    public String GetText()
    {
        return this.text;
    }

    public void AddComponent(String type, String text){
        components.put(type, text);
        if (this.text == null){
            this.text = text + " ";
        } else {
            this.text += text + " ";
        }
    }

    public HashMap<String, String> GetComponents()
    {
        return components;
    }

    public void SetLink(String link){
        this.link = link;
    }

    public String GetLink(){
        return this.link;
    }
}
