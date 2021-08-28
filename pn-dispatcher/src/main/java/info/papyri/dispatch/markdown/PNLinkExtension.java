/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.papyri.dispatch.markdown;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html.HtmlRenderer.Builder;
import com.vladsch.flexmark.html.HtmlRenderer.HtmlRendererExtension;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import org.jetbrains.annotations.NotNull;
/**
 *
 * @author hac13
 */
public class PNLinkExtension implements HtmlRendererExtension {

  @Override
  public void rendererOptions(MutableDataHolder mdh) {
  }

  @Override
  public void extend(@NotNull Builder bldr, @NotNull String rendererType) {
    bldr.linkResolverFactory(new PNLinkResolver.Factory());
    bldr.nodeRendererFactory(new PNLinkRenderer.Factory());
  }
  
  public static PNLinkExtension create() {
    return new PNLinkExtension();
  }
}
