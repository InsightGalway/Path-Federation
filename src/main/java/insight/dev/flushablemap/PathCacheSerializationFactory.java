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
public class PathCacheSerializationFactory implements SerializationFactory<Integer, PathCache> {

  public static PathCacheSerializationFactory instance = new PathCacheSerializationFactory();

  private PathCacheSerializationFactory(){}

  public int[] serialiseKey(Integer integer) {
    return new int[]{integer};
  }

  public int[] serialiseValue(PathCache pathCache) {
    return pathCache.serialise();
  }

  public Integer makeKey(int[] key) {
    return key[0];
  }

  public PathCache makeValue(int[] value) {
    return new PathCache(value[0], value[1], (value[2]==0? false:true));
  }

  public int keySize() {
    return 1;
  }

  public int valueSize() {
    return 3;
  }
}
