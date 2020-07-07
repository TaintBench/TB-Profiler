import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FindingInfo {
  protected int ID;
  protected SourceOrSinkInfo sourceInfo;
  protected SourceOrSinkInfo sinkInfo;
  protected List<IntermediateFlowInfo> interFlows;
  protected Set<String> attributes;

  public FindingInfo() {
    this.sourceInfo = new SourceOrSinkInfo();
    this.sinkInfo = new SourceOrSinkInfo();
  }

  @Override
  public String toString() {
    StringBuilder str = new StringBuilder();
    str.append("\nID:" + ID + "\n");
    str.append("Source:\n" + sourceInfo.toString());
    str.append("\nSink:\n" + sinkInfo.toString());
    str.append("\nIntermediates:\n");
    interFlows.forEach(e -> str.append(e));
    return str.toString();
  }

  public List<LocationInJimple> getLocationsInJimple() {
    List<LocationInJimple> list = new ArrayList<>();
    list.add(sourceInfo.getLocationInJimple());
    list.add(sinkInfo.getLocationInJimple());
    for (IntermediateFlowInfo flow : interFlows) list.add(flow.getLocationInJimple());
    return list;
  }

  public boolean isLeftSideOfSourceField() {
    String[] tokens = sourceInfo.statement.split("=");
    if (tokens.length <= 1) return false;
    else {
      String left = tokens[0];
      boolean isField = left.contains(".");
      return isField;
    }
  }

  public String getStatement(String id) {
    if (sourceInfo.ID.equals(id)) return sourceInfo.statement;
    if (sinkInfo.ID.equals(id)) return sinkInfo.statement;
    for (IntermediateFlowInfo inter : interFlows) {
      if (inter.ID.equals(id)) return inter.statement;
    }
    return null;
  }
}
