import io.kamax.matrix.json.RoomMessageFormattedTextPutBody;
import io.kamax.matrix.json.GsonUtil;
import com.google.gson.JsonObject;

public class TestEmoticon {
    public static void main(String[] args) {
        String rawFallback = "Raremote";
        String html = "<img src=\"mxc://matrix.local/vaqAwlPGRjSoyCjUuGVAxhHc\" width=\"16\" height=\"16\" style=\"vertical-align:middle; margin-right: 4px; border-radius: 2px;\" alt=\"Raremote\" title=\"Raremote\" data-mx-emoticon=\"true\" />";
        RoomMessageFormattedTextPutBody body = new RoomMessageFormattedTextPutBody(rawFallback, html);
        
        JsonObject json = GsonUtil.makeObj(body);
        System.out.println("Full JSON:");
        System.out.println(GsonUtil.getPretty().toJson(json));
        
        System.out.println("\nformatted_body value:");
        System.out.println(json.get("formatted_body").getAsString());
        
        // Check if data-mx-emoticon is present
        String formatted = json.get("formatted_body").getAsString();
        if (formatted.contains("data-mx-emoticon")) {
            System.out.println("✓ data-mx-emoticon attribute preserved");
        } else {
            System.out.println("✗ data-mx-emoticon attribute missing!");
        }
    }
}