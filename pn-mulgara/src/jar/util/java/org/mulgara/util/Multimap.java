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
 * A wrapper for {@link Map}s which makes it convenient to deal with keys that
 * map to multiple values.
 *
 * @author <a href="http://staff.pisoftware.com/raboczi/">Simon Raboczi</a>
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class Multimap extends MapWrapper {

  /**
   * Constructor.
   *
   * @param map the underlying {@link Map}, whose values will be {@link Set}s of
   *      values
   */
  public Multimap(Map map) {
    super(map);
  }

  //
  // Methods overriding the MapWrapper
  //
  // TODO - the performance is linear in the number of keys; could improve

  /**
   * METHOD TO DO
   *
   * @param value PARAMETER TO DO
   * @return RETURNED VALUE TO DO
   */
  public boolean containsValue(Object value) {

    for (Iterator i = map.values().iterator(); i.hasNext();) {

      if (((Collection) i.next()).contains(value)) {

        return true;
      }
    }

    return false;
  }

  /**
   * METHOD TO DO
   *
   * @param key PARAMETER TO DO
   * @param value PARAMETER TO DO
   * @return RETURNED VALUE TO DO
   */
  public Object put(Object key, Object value) {

    Set values = (Set) map.get(key);

    if (values == null) {

      values = new HashSet();
    }

    values.add(value);

    return map.put(key, values);

    // FIXME: the result value is incorrect
  }

  /**
   * Remove an object from being mapped to a specified key.
   *
   * @param key the key to remove the mapping from
   * @param value the value to remove from the mapping
   * @return whether the object existed and was unmapped
   */
  public boolean remove(Object key, Object value) {

    Set values = (Set) map.get(key);

    if (values != null) {

      boolean removed = values.remove(value);

      if (values.isEmpty()) {

        map.remove(key);
      }

      return removed;
    }
    else {

      return false;
    }
  }

  /**
   * METHOD TO DO
   *
   * @return RETURNED VALUE TO DO
   */
  public Collection values() {

    Set result = new HashSet();

    for (Iterator i = map.values().iterator(); i.hasNext();) {

      result.addAll((Collection) i.next());
    }

    return result;
  }
}
