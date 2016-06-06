package org.jakz.common;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SharedStore<K,V> 
{
	private ReentrantReadWriteLock lock;
	private IndexedMap<K, V> m;
	
	public SharedStore()
	{
		lock=new ReentrantReadWriteLock();
		m=new IndexedMap<K,V>();
	}
	
	public SharedStore<K,V> put(K nkey, V nvalue)
	{
		m.put(nkey, nvalue);
		return this;
	}
	
	public SharedStore<K,V> putL(K nkey, V nvalue)
	{
		lock.writeLock().lock();
			m.put(nkey, nvalue);
		lock.writeLock().unlock();
		return this;
	}
	
	public V get(K nkey)
	{
		return m.getValue(nkey);
	}
	
	public V getL(K nkey)
	{
		V r;
		lock.readLock().lock();
			r=m.getValue(nkey);
		lock.readLock().unlock();
		return r;
	}
	
	public void lockW()
	{
		lock.writeLock().lock();
	}
	
	public void unlockW()
	{
		lock.writeLock().unlock();
	}
	
	public void lockR()
	{
		lock.readLock().lock();
	}
	
	public void unlockR()
	{
		lock.readLock().unlock();
	}
	
	public int size()
	{
		return m.size();
	}
	
	public int sizeL(K nkey)
	{
		int r;
		lock.readLock().lock();
			r=m.size();
		lock.readLock().unlock();
		return r;
	}
}
