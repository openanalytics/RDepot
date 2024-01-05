/**
 * R Depot
 *
 * Copyright (C) 2012-2024 Open Analytics NV
 *
 * ===========================================================================
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Apache License as published by
 * The Apache Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.rdepot.repo.collection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Map that contains a blocking queue of multiple elements under certain keys in a specific order.
 */
public class QueueMap<K,V> {
	private List<K> keys;
	private HashMap<K, BlockingQueue<V>> map;
	
	public QueueMap() {
		keys = new ArrayList<>();
		map = new LinkedHashMap<>();
	}
	
	/**
	 * Creates a queue under a specific key
	 * @param key
	 */
	public void createQueue(K key) {
		keys.add(key);
		BlockingQueue<V> queue = new LinkedBlockingDeque<V>();
		map.put(key, queue);
	}
	
	/**
	 * This method adds a value to a queue under given key.
	 * If this is the first element under the key, queue is created.
	 * @param key
	 * @param value
	 * @throws InterruptedException
	 */
	public void put(K key, V value) throws InterruptedException {
		if(!keys.contains(key)) {
			createQueue(key);
		} 
		
		BlockingQueue<V> queue = map.get(key);
		queue.put(value);
	}
	
//	public V getLastItem(K key, long timeout, TimeUnit timeUnit) {
//		return map.get(key).poll(timeout, timeUnit);
//	}
//	
	/**
	 * This method fetches the last element of the given key
	 * @param key
	 * @return
	 * @throws InterruptedException
	 */
	public V getLastItem(K key) throws InterruptedException {
		return map.get(key).take();
	}
	
	/**
	 * This method fetches the last element of the last key.
	 * The last key is the one whose first element was added the earliest.
	 * @return
	 * @throws InterruptedException
	 */
	public V getLastItem() throws InterruptedException {
		if(!keys.isEmpty()) {
			K key = keys.get(0);
			return map.get(key).take();
		} else {
			return null;
		}
	}
	
	public boolean isEmpty() {
		return keys.isEmpty();
	}
	
	public void clear() {
		map.clear();
		keys.clear();
	}
	
	public void remove(K key) {
		keys.remove(key);
		map.remove(key);
	}
	
	/**
	 * This method removes the last key including its elements.
	 * The last key is the one whose first element was added the earliest.
	 */
	public void removeLast() {
		if(!keys.isEmpty()) {
			K lastKey = keys.remove(0);
			map.remove(lastKey);
		}
	}
	
	public boolean containsKey(K key) {
		return keys.contains(key);
	}
}
