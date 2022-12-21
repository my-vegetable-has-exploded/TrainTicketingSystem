/*
 * WFSnapshot.java
 *
 * Created on January 19, 2006, 9:20 PM
 *
 * From "Multiprocessor Synchronization and Concurrent Data Structures",
 * by Maurice Herlihy and Nir Shavit.
 * Copyright 2006 Elsevier Inc. All rights reserved.
 */

package ticketingsystem;

import java.util.ArrayList;

/**
 * Wait-free snapshot.
 * 
 * @author Maurice Herlihy
 */
public class WFSnapshot<T> implements Snapshot<T> {
	private ArrayList<StampedSnap<T>> a_table; // array of MRSW atomic registers

	public WFSnapshot(int capacity, T init) {
		a_table = new ArrayList<StampedSnap<T>>(capacity);
		for (int i = 0; i < capacity; i++) {
			a_table.add( new StampedSnap<T>(init));
		}
	}

	public void update(T value) {
		int me = ThreadID.get();
		ArrayList<T> snap = this.scan();
		StampedSnap<T> oldValue = a_table.get(me);
		StampedSnap<T> newValue = new StampedSnap<T>(oldValue.stamp + 1, value, snap);
		a_table.set(me, newValue);
	}

	private ArrayList<StampedSnap<T>> collect() {
		ArrayList<StampedSnap<T>> copy = new ArrayList<StampedSnap<T>>(a_table.size());
		for (int j = 0; j < a_table.size(); j++)
			copy.add(a_table.get(j));
		return copy;
	}

	public ArrayList<T> scan() {
		ArrayList<StampedSnap<T>> oldCopy;
		ArrayList<StampedSnap<T>> newCopy;
		boolean[] moved = new boolean[a_table.size()];
		oldCopy = collect();
		collect: while (true) {
			newCopy = collect();
			for (int j = 0; j < a_table.size(); j++) {
				// did any thread move?
				if (oldCopy.get(j).stamp != newCopy.get(j).stamp) {
					if (moved[j]) { // second move
						return oldCopy.get(j).snap;
					} else {
						moved[j] = true;
						oldCopy = newCopy;
						continue collect;
					}
				}
			}
			// clean collect
			ArrayList<T> result = new ArrayList<T>(a_table.size());
			for (int j = 0; j < a_table.size(); j++)
				result.add(newCopy.get(j).value);
			return result;
		}
	}
}
