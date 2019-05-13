package insight.dev.flushablemap;

/**
 * insight.dev.flushablemap
 * <p>
 * TODO: Add class description
 * <p>
 * Author:  Anh Le-Tuan
 * <p>
 * Email:   anh.letuan@insight-centre.org
 * <p>
 * Date:  05/01/19.
 */
public class PathCache {
  private int src;
  private int target;
  private boolean passOrFail;

  public PathCache(int src, int target, boolean passOrFail){
    this.src = src;
    this.target = target;
    this.passOrFail = passOrFail;
  }

  public int getSrc() {
    return src;
  }

  public int getTarget() {
    return target;
  }

  public boolean isPassOrFail() {
    return passOrFail;
  }

  public int[] serialise(){
    return new int[]{src, target, (passOrFail? 1:0)};
  }
}
