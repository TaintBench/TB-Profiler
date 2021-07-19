import soot.SootMethod;
import soot.Unit;

/** @author Linghui Luo */
public class SourceInfo {

  protected String stmt;
  protected String methodName;
  protected String className;
  protected int lineNo;
  protected String targetName;
  protected int targetNo;
  protected int ID;

  public SourceInfo(
      String stmt,
      String methodName,
      String className,
      int lineNo,
      String targetName,
      int targetNo,
      int ID) {
    this.stmt = stmt;
    this.methodName = methodName;
    this.className = className;
    this.lineNo = lineNo;
    this.targetName = targetName;
    this.targetNo = targetNo;
    this.ID = ID;
  }

  public boolean match(SootMethod method, Unit unit, boolean compaireLineNo) {
    String cName = method.getDeclaringClass().getName();
    String mName = method.getName();
    if (cName.contains("$")) {
      cName = cName.replace("$", "."); // handle inner & anonymous class
      if (Character.isDigit(cName.charAt(cName.length() - 1))) {
        String prefix = cName.substring(0, cName.length() - 1);
        cName = prefix + "AnonymousClass" + cName.charAt(cName.length() - 1);
      }
    }

    if (cName.equals(className) && methodName.contains(mName)) {
      if (unit.toString().contains(targetName)) {
        if (compaireLineNo) {
          return unit.getJavaSourceStartLineNumber() == lineNo;
        } else {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ID;
    result = prime * result + ((className == null) ? 0 : className.hashCode());
    result = prime * result + lineNo;
    result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
    result = prime * result + ((stmt == null) ? 0 : stmt.hashCode());
    result = prime * result + ((targetName == null) ? 0 : targetName.hashCode());
    result = prime * result + targetNo;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    SourceInfo other = (SourceInfo) obj;
    if (ID != other.ID) return false;
    if (className == null) {
      if (other.className != null) return false;
    } else if (!className.equals(other.className)) return false;
    if (lineNo != other.lineNo) return false;
    if (methodName == null) {
      if (other.methodName != null) return false;
    } else if (!methodName.equals(other.methodName)) return false;
    if (stmt == null) {
      if (other.stmt != null) return false;
    } else if (!stmt.equals(other.stmt)) return false;
    if (targetName == null) {
      if (other.targetName != null) return false;
    } else if (!targetName.equals(other.targetName)) return false;
    if (targetNo != other.targetNo) return false;
    return true;
  }
}
