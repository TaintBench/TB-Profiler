import java.util.ArrayList;
import java.util.List;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;

public class LocationInJimple {
  protected List<Unit> statements = new ArrayList<Unit>();
  protected SootMethod method;
  protected SootClass klass;
  protected String ID;

  public void addStatement(Unit u) {
    if (statements != null) this.statements.add(u);
  }

  @Override
  public String toString() {
    StringBuilder str = new StringBuilder();
    str.append("statements:" + "\n");
    statements.forEach(e -> str.append("\t" + e.toString() + "\n"));
    if (method != null) str.append("methodName:\t" + method.getSignature() + "\n");
    if (klass != null) str.append("className:\t" + klass.getName() + "\n");
    return str.toString();
  }

  public Unit getTarget(String targetName) {
    for (Unit u : statements) if (u.toString().contains(targetName)) return u;
    System.err.println("Couldn't find target " + targetName + " in " + method.getSignature());
    return null;
  }

  public boolean isSource() {
    return ID.endsWith("source");
  }
}
