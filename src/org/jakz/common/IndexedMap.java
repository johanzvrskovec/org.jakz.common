package org.jakz.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

public class IndexedMap<K,V> //implements Map<K, V>
{
	public class IndexedValue
	{
		public final Integer index;
		public final V value;
		
		public IndexedValue(Integer nIndex, V nValue)
		{
			index=nIndex; value=nValue;
		}
	}
	
	public class KeyedValue
	{
		public final K key;
		public final V value;
		
		public KeyedValue(K nKey, V nValue)
		{
			key=nKey; value=nValue;
		}
	}
	
	private HashMap<K,V> mapKV;
	private HashMap<K,Integer> mapKI;
	private ArrayList<K> listK;
	
	public IndexedMap() 
	{
		mapKV=new HashMap<K,V>();
		mapKI=new HashMap<K,Integer>();
		listK=new ArrayList<K>();
	}
	
	
	public IndexedMap<K,V> put(K nkey, V nvalue)
	{
		return put(nkey,nvalue,null);
	}
	
	public IndexedMap<K,V> put(KeyedValue nKeyedValue)
	{
		return put(nKeyedValue.key,nKeyedValue.value,null);
	}
	
	public IndexedMap<K,V> put(K nkey, IndexedValue nvalue)
	{
		return put(nkey, nvalue.value, nvalue.index);
	}
	
	public IndexedMap<K,V> put(KeyedValue nKeyedValue, int index)
	{
		return put(nKeyedValue.key,nKeyedValue.value,index);
	}
	
	public IndexedMap<K,V> put(K nkey, V nvalue, Integer index)
	{	
		mapKV.put(nkey, nvalue);
		
		if(index==null)
		{
			listK.add(nkey);
			index=listK.size()-1;
		}
		else
		{
			listK.add(index, nkey);
		}
		
		mapKI.put(nkey, index);
		
		return this;
	}


	public IndexedMap<K,V> clear() 
	{
		mapKV.clear();
		mapKI.clear();
		listK.clear();
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public IndexedMap<K,V> copy()
	{
		IndexedMap<K,V> r = new IndexedMap<K, V>();
		r.mapKV=(HashMap<K, V>) mapKV.clone();
		r.mapKI=(HashMap<K, Integer>) mapKI.clone();
		r.listK=(ArrayList<K>) listK.clone();
		return r;
	}


	public boolean containsKey(K key) 
	{
		return mapKV.containsKey(key);
	}


	public boolean containsValue(V value) 
	{
		return mapKV.containsValue(value);
	}

	/*
	public Set<Entry<K, V>> entrySet() 
	{
		return mapKV.entrySet();
	}
	 */

	public IndexedValue get(K key) 
	{
		return new IndexedValue(mapKI.get(key),mapKV.get(key));
	}
	
	public V getValue(K key) 
	{
		return mapKV.get(key);
	}
	
	public KeyedValue getAt(int index) 
	{
		return new KeyedValue(listK.get(index),mapKV.get(listK.get(index)));
	}
	
	public K getKeyAt(int index)
	{
		return listK.get(index);
	}
	
	public V getValueAt(int index)
	{
		return mapKV.get(listK.get(index));
	}

	public boolean isEmpty() 
	{
		return mapKV.isEmpty();
	}

	public Set<K> keySet() 
	{
		return mapKV.keySet();
	}
	
	public ArrayList<K> keys()
	{
		return new ArrayList<K>(listK);
	}


	public IndexedMap<K, V> putAll(Map<K,V> arg0) 
	{
		Set<Entry<K,V>> entry = arg0.entrySet();
		Iterator<Entry<K,V>> entryIt = entry.iterator();
		Entry<K,V> currentEntry;
		while( entryIt.hasNext())
		{
			currentEntry = entryIt.next();
			put(currentEntry.getKey(),currentEntry.getValue());
		}
		return this;
	}
	
	public IndexedMap<K,V> putAll(IndexedMap<K,V> arg0)
	{
		Iterator<K> keyIt = arg0.listK.iterator();
		K currentKey;
		while(keyIt.hasNext())
		{
			currentKey = keyIt.next();
			put(currentKey,arg0.mapKV.get(currentKey));
		}
		return this;
	}


	public IndexedValue remove(K key) 
	{
		listK.remove(mapKI.get(key));
		Integer retI = mapKI.remove(key);
		V retV = mapKV.remove(key);
		return new IndexedValue(retI,retV);
	}
	
	public KeyedValue removeAt(int index) 
	{
		K key = listK.get(index);
		listK.remove(index);
		mapKI.remove(key);
		V retV = mapKV.remove(key);
		return new KeyedValue(key,retV);
	}

	public IndexedMap<K,V> moveAt(int fromOldIndex, int toNewIndex)
	{
		return put(removeAt(fromOldIndex),toNewIndex);
	}
	
	public int size() 
	{
		return mapKV.size();
	}


	public ArrayList<KeyedValue> keyValuePairs() 
	{
		ArrayList<KeyedValue> toReturn = new ArrayList<KeyedValue>();
		Iterator<K> keyIt = listK.iterator();
		K currentKey;
		while(keyIt.hasNext())
		{
			currentKey = keyIt.next();
			KeyedValue kv = new KeyedValue(currentKey, mapKV.get(currentKey));
			toReturn.add(kv);
		}
		
		return toReturn;
	}
	
	public Set<IndexedValue> indexedValues() 
	{
		HashSet<IndexedValue> toReturn = new HashSet<IndexedValue>();
		Iterator<K> keyIt = listK.iterator();
		Integer currentIndex=0;
		K currentKey;
		while(keyIt.hasNext())
		{
			currentKey = keyIt.next();
			IndexedValue iv = new IndexedValue(currentIndex++, mapKV.get(currentKey));
			toReturn.add(iv);
		}
		
		return toReturn;
	}
	
	public ArrayList<V> values()
	{
		ArrayList<V> toReturn = new ArrayList<V>();
		Iterator<K> keyIt = listK.iterator();
		K currentKey;
		while(keyIt.hasNext())
		{
			currentKey = keyIt.next();
			toReturn.add(mapKV.get(currentKey));
		}
		return toReturn;
	}
	
	public IndexedMap<K,V> subList(int fromIndex, int toIndex)
	{
		IndexedMap<K,V> toReturn = new IndexedMap<K, V>();
		List<K> sl = listK.subList(fromIndex, toIndex);
		K currentKey;
		Iterator<K> keyIt = sl.iterator();
		while(keyIt.hasNext())
		{
			currentKey = keyIt.next();
			toReturn.put(currentKey, mapKV.get(currentKey));
		}
		return toReturn;
	}
	
	public IndexedMap<K,V> subList(int fromIndex)
	{
		return subList(fromIndex, listK.size());
	}
	
	public JSONObject toJSON()
	{
		JSONObject toReturn = new JSONObject();
		JSONArray jsonListK = new JSONArray();
		JSONObject jsonMapKV = new JSONObject();
		JSONObject jsonMapKI = new JSONObject();
		
		
		//The entries should be added in order
		Iterator<K> keyIt = listK.iterator();
		K currentKey;
		V currentValue;
		int currentIndex =0;
		while(keyIt.hasNext())
		{
			currentKey = keyIt.next();
			currentValue = mapKV.get(currentKey);
			jsonListK.put(currentKey);
			jsonMapKV.put(currentKey.toString(),currentValue);
			jsonMapKI.put(currentKey.toString(), currentIndex++);
		}
		toReturn.put("listK", jsonListK);
		toReturn.put("mapKV", jsonMapKV);
		toReturn.put("mapKI", jsonMapKI);
		
		return toReturn;
	}
	
	
	@SuppressWarnings("unchecked")
	public IndexedMap<K,V> fromJSON(JSONObject json)
	{
		clear();
		if(json!=null)
		{
			JSONArray json_listK = json.getJSONArray("listK");
			JSONObject json_mapKV = json.getJSONObject("mapKV");
			JSONObject json_mapKI = json.getJSONObject("mapKI");
			try
			{
				for(int i=0; i<json_listK.length(); i++)
				{
						K key = (K)json_listK.get(i);
						V value = (V) json_mapKV.get(key.toString());
						
						if(json_mapKI.getInt(key.toString())!=i)
							throw new java.lang.IllegalArgumentException("The mapped index of this key does not represent the index of the key in the internal list.");
						
						put(key, value);
				}
			}
			catch (Exception e)
			{
				throw new java.lang.IllegalArgumentException("The argument is of the wrong format or has an incoherent internal structure.",e);
			}
		}
		return this;
	}
	
	
	public String toString()
	{
		return toJSON().toString();
	}

}
