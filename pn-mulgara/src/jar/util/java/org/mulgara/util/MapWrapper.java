/*
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is the Kowari Metadata Store.
 *
 * The Initial Developer of the Original Code is Plugged In Software Pty
 * Ltd (http://www.pisoftware.com, mailto:info@pisoftware.com). Portions
 * created by Plugged In Software Pty Ltd are Copyright (C) 2001,2002
 * Plugged In Software Pty Ltd. All Rights Reserved.
 *
 * Contributor(s): N/A.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.util;

// Java 2 standard packages
import java.util.*;

/**
 * Generic wrapper for a {@link Map}, delegating all functions to the wrapped
 * instance.
 *
 * @author <a href="http://staff.pisoftware.com/raboczi/">Simon Raboczi</a>
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class MapWrapper implements Map {

  /**
   * The wrapped instance.
   *
   */
  protected Map map;

  /**
   * Constructor.
   *
   * @param map the wrapped instance
   */
  public MapWrapper(Map map) {

    if (map == null) {

      throw new IllegalArgumentException("Null \"map\" parameter");
    }

    this.map = map;
  }

  /**
   * Gets the Empty attribute of the MapWrapper object
   *
   * @return The Empty value
   */
  public boolean isEmpty() {

    return map.isEmpty();
  }

  /**
   * METHOD TO DO
   *
   * @param key PARAMETER TO DO
   * @return RETURNED VALUE TO DO
   */
  public Object get(Object key) {

    return map.get(key);
  }

  //
  // Methods implementing the Map interface
  //

  /**
   * METHOD TO DO
   *
   * @return RETURNED VALUE TO DO
   */
  public int size() {

    return map.size();
  }

  /**
   * METHOD TO DO
   *
   * @param key PARAMETER TO DO
   * @return RETURNED VALUE TO DO
   */
  public boolean containsKey(Object key) {

    return map.containsKey(key);
  }

  /**
   * METHOD TO DO
   *
   * @param value PARAMETER TO DO
   * @return RETURNED VALUE TO DO
   */
  public boolean containsValue(Object value) {

    return map.containsValue(value);
  }

  /**
   * METHOD TO DO
   *
   * @param key PARAMETER TO DO
   * @param value PARAMETER TO DO
   * @return RETURNED VALUE TO DO
   */
  public Object put(Object key, Object value) {

    return map.put(key, value);
  }

  /**
   * METHOD TO DO
   *
   * @param key PARAMETER TO DO
   * @return RETURNED VALUE TO DO
   */
  public Object remove(Object key) {

    return map.remove(key);
  }

  /**
   * METHOD TO DO
   *
   * @param t PARAMETER TO DO
   */
  public void putAll(Map t) {

    map.putAll(t);
  }

  /**
   * METHOD TO DO
   *
   */
  public void clear() {

    map.clear();
  }

  /**
   * METHOD TO DO
   *
   * @return RETURNED VALUE TO DO
   */
  public Set keySet() {

    return map.keySet();
  }

  /**
   * METHOD TO DO
   *
   * @return RETURNED VALUE TO DO
   */
  public Collection values() {

    return map.values();
  }

  /**
   * METHOD TO DO
   *
   * @return RETURNED VALUE TO DO
   */
  public Set entrySet() {

    return map.entrySet();
  }

  /**
   * METHOD TO DO
   *
   * @param o PARAMETER TO DO
   * @return RETURNED VALUE TO DO
   */
  public boolean equals(Object o) {

    return map.equals(o);
  }

  /**
   * METHOD TO DO
   *
   * @return RETURNED VALUE TO DO
   */
  public int hashCode() {

    return map.hashCode();
  }
}
