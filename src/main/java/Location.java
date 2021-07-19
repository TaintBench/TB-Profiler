import soot.SootMethod;
import soot.Unit;

/** @author Linghui Luo */
public class Location {
  private SootMethod method;
  private Unit unit;

  public Location(SootMethod method, Unit unit) {
    super();
    this.method = method;
    this.unit = unit;
  }

  @Override
  public String toString() {
    return "method:" + method.getSignature() + " unit:" + unit + "]";
  }
}
