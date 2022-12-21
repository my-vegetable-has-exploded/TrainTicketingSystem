/*
 * StampedSnap.java
 *
 * Created on January 19, 2006, 9:03 PM
 *
 * From "Multiprocessor Synchronization and Concurrent Data Structures",
 * by Maurice Herlihy and Nir Shavit.
 * Copyright 2006 Elsevier Inc. All rights reserved.
 */

 package ticketingsystem;

import java.util.ArrayList;

/**
 * Labeled snapshot value.
 * @param T object type
 * @author Maurice Herlihy
 */
public class StampedSnap<T> extends StampedValue<T> {
  public ArrayList<T> snap; 
  public StampedSnap(T value) {
    super(value);
    snap  = new ArrayList<T>();
  }
  /**
   * Constructor.
   * @param stamp timestamp
   * @param value object value
   * @param snap 
   */
  public StampedSnap(long stamp, T value, ArrayList<T> snap) {
    super(stamp, value);
	this.snap=snap;
  }
}