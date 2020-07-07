import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Witness {
  private Map<String, Set<String>> witnesses = new HashMap<>();

  public Witness() {
    this.witnesses = new HashMap<>();
  }

  public void put(String attribute, String locationID) {
    Set<String> ws = new HashSet<>();
    if (witnesses.containsKey(attribute)) {
      ws = witnesses.get(attribute);
    }
    ws.add(locationID);
    witnesses.put(attribute, ws);
  }

  public Set<String> get(String attribute) {
    return this.witnesses.get(attribute);
  }
}
