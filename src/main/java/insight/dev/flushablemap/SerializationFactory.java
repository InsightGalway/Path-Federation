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
public interface SerializationFactory<Key, Value> {

  public int[] serialiseKey(Key key);

  public int[] serialiseValue(Value value);

  public Key makeKey(int[] key);

  public Value makeValue(int[] value);

  public int keySize();

  public int valueSize();
}
