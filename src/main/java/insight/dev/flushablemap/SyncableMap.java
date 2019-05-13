package insight.dev.flushablemap;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

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
public class SyncableMap<Key, Value> implements Syncable{

  private ConcurrentHashMap<Key, CopyOnWriteArrayList<Value>> map;

  public SyncableMap(){
    map = new ConcurrentHashMap<Key, CopyOnWriteArrayList<Value>>();
  }

  private SerializationFactory<Key, Value> factory;

  public SyncableMap setSerialiseFactory(SerializationFactory factory){
    this.factory = factory;
    return this;
  }

  public void put(Key key, Value value){
    CopyOnWriteArrayList<Value>  list = map.get(key);

    if (list == null){
      list = new CopyOnWriteArrayList<Value>();
      list.add(value);
      map.put(key, list);
    }
    else {
      list.add(value);
    }
  }
 
  
  public List<Value> getValue(Key key){
    return map.get(key);
  }

  private Map<Key, CopyOnWriteArrayList<Value>> deserialise(int[] serialisedMap){

    map = new ConcurrentHashMap<Key, CopyOnWriteArrayList<Value>>();

    int currentPos = 1;

    while (currentPos < serialisedMap.length){

      //read key
      int[] serialisedKey = new int[factory.keySize()];

      System.arraycopy(serialisedMap, currentPos, serialisedKey, 0, factory.keySize());

      currentPos = currentPos + factory.keySize();

      int valueSize = serialisedMap[currentPos];

      currentPos++;

      CopyOnWriteArrayList<Value> valueList = new CopyOnWriteArrayList<Value>();

      for (int i=0; i<valueSize; i++){

        int[] serialisedValue = new int[factory.valueSize()];

        System.arraycopy(serialisedMap, currentPos, serialisedValue,0, factory.valueSize());

        Value value = factory.makeValue(serialisedValue);

        valueList.add(value);

        currentPos+=factory.valueSize();
      }

      Key key = factory.makeKey(serialisedKey);

      map.put(key, valueList);
    }
    return null;
  }

  private synchronized int[] serialise(Map<Key, CopyOnWriteArrayList<Value>> map){
    int[] serialised = new int[]{map.size()};

    for (Map.Entry<Key, CopyOnWriteArrayList<Value>> entry: map.entrySet()){

      int[] serialisedKey = factory.serialiseKey(entry.getKey());

      serialised = ArrayUtil.append(serialisedKey, serialised);

      int[] serialisedList = serialiseList(entry.getValue());

      serialised = ArrayUtil.append(serialisedList, serialised);
    }

    return serialised;
  }
  
  public int size(){
	return (int) map.size();  
  }
  
  private synchronized int[] serialiseList(List<Value> list){

    int[] serialised = new int[]{list.size()};

    for (Value value:list){

      int[] serialisedValue = factory.serialiseValue(value);

      serialised = ArrayUtil.append(serialisedValue, serialised);
    }

    return serialised;

  }

  public synchronized void sync(String to) {
    FileChannel fileChannel = FileUtil.open(to, factory.getClass().getCanonicalName(),"rw");
    int[] serialised = serialise(map);

    ByteBuffer byteBuffer = ByteBuffer.allocate(serialised.length * 4);
    IntBuffer  intBuffer  = byteBuffer.asIntBuffer();
               intBuffer.put(serialised);

    byteBuffer.rewind();
    try {
      fileChannel.write(byteBuffer);
      fileChannel.force(true);
      fileChannel.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public synchronized void load(String from){
    try {
      FileChannel fileChannel = FileUtil.open(from, factory.getClass().getCanonicalName(), "rw");

      ByteBuffer byteBuffer = ByteBuffer.allocate((int) fileChannel.size());
      IntBuffer intBuffer = byteBuffer.asIntBuffer();
      fileChannel.read(byteBuffer);

      int[] serialiseMap = new int[intBuffer.capacity()];
      intBuffer.rewind();
      intBuffer.get(serialiseMap);
      deserialise(serialiseMap);

      fileChannel.close();

    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  @Override
  public String toString(){
    return map.toString();
  }
}