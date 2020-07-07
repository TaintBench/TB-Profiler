import java.util.List;
import soot.SootMethod;

public class TaintFlowInfo {
  protected SourceInfo sourceInfo;
  protected String componentType;
  protected String componentName;
  protected int stmtDistance;
  protected int callDistance;
  protected List<SootMethod> callbacks;
  protected int pathConditions;
}
