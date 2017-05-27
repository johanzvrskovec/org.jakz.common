package org.jakz.common;

/**
 * Tests functionality of classes in the common package
 * @author johkal
 *
 */
public class Test 
{
	public static void main(String[] args) throws TestFailedException
	{
		new Test().run();
	}
	
	public void run() throws TestFailedException
	{
		testIndexedMap();
	}
	
	public void testIndexedMap() throws TestFailedException
	{
		IndexedMap<String, String> indexedMap = new IndexedMap<String, String>();
		//put
		indexedMap.put(indexedMap.createKeyedValue("first", "val1"));
		indexedMap.put(indexedMap.createKeyedValue("second", "val2"));
		if(indexedMap.size()!=2)
			throw new TestFailedException("Indexed Map failed put test");
		if(!(indexedMap.containsKey("first")&&indexedMap.get("first").index==0&&indexedMap.getValue("first").equals("val1")))
			throw new TestFailedException("Indexed Map failed put test");
		if(!(indexedMap.containsKey("second")&&indexedMap.get("second").index==1)&&indexedMap.getValue("second").equals("val2"))
			throw new TestFailedException("Indexed Map failed put test");
		
		//put replace
		indexedMap.put(indexedMap.createKeyedValue("first", "val3"));
		if(indexedMap.size()!=2||!indexedMap.getAt(0).value.equals("val3"))
			throw new TestFailedException("Indexed Map failed put replace test");
		
		//opt
		indexedMap.opt(indexedMap.createKeyedValue("first", "nogoodvalue"));
		indexedMap.opt(indexedMap.createKeyedValue("third", "val4"));
		indexedMap.opti(indexedMap.createKeyedValue("nogoodkey", "nogoodvalue"),0);
		indexedMap.optk(indexedMap.createKeyedValue("third", "nogoodvalue"));
		if(indexedMap.size()!=3||!indexedMap.getValue("third").equals("val4")||!indexedMap.getValue("first").equals("val3"))
			throw new TestFailedException("Indexed Map failed opt test");
		
		//insert, delete test
		indexedMap.put("fourth", "val5", 1);
		if(indexedMap.size()!=4||!indexedMap.getValueAt(1).equals("val5")||!indexedMap.getValueAt(2).equals("val2"))
			throw new TestFailedException("Indexed Map failed insert test");
		
	}
	
}
