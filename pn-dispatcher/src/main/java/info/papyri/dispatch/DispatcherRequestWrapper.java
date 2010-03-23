/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.papyri.dispatch;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author hcayless
 */
public class DispatcherRequestWrapper extends HttpServletRequestWrapper {

  public DispatcherRequestWrapper(HttpServletRequest request) {
    super(request);
    for (String key : request.getParameterMap().keySet()) {
      params.put(key, request.getParameterValues(key));
    }
  }

  private Map<String, String[]> params = new HashMap<String, String[]>();

  public void setParameter(String key, String value) {
    params.put(key, new String[]{value});
  }

  @Override
  public String getParameter(String key) {
    String[] result = params.get(key);
    if (result != null) {
      return result[0];
    } else {
      return null;
    }
  }

  @Override
  public java.util.Enumeration<java.lang.String> getParameterNames() {
    return new ParameterEnumeration(params.keySet().iterator());
  }

  @Override
  public java.lang.String[] getParameterValues(String name) {
    return params.get(name);
  }

  @Override
  public java.util.Map<java.lang.String,java.lang.String[]> getParameterMap() {
    return params;
  }

  public class ParameterEnumeration implements Enumeration {

    public ParameterEnumeration(Iterator it) {
      this.it = it;
    }

    private Iterator it;

    @Override
    public boolean hasMoreElements() {
      return it.hasNext();
    }

    @Override
    public Object nextElement() {
      return it.next();
    }
  }

}
