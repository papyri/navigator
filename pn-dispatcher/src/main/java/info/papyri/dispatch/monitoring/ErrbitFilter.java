package info.papyri.dispatch.monitoring;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;

public class ErrbitFilter implements Filter {

  @Override
  public void init(FilterConfig filterConfig) {
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    try {
      chain.doFilter(request, response);
    } catch (IOException | ServletException | RuntimeException e) {
      DispatchErrbitConfigProvider.report(e);
      throw e;
    }
  }

  @Override
  public void destroy() {
  }
}
