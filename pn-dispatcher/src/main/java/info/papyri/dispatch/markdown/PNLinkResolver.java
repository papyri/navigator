/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.papyri.dispatch.markdown;

import com.vladsch.flexmark.html.LinkResolver;
import com.vladsch.flexmark.html.LinkResolverFactory;
import com.vladsch.flexmark.html.renderer.*;
import com.vladsch.flexmark.util.ast.Node;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Set;
/**
 *
 * @author hac13
 */
public class PNLinkResolver implements LinkResolver {
  
  public PNLinkResolver(LinkResolverBasicContext context){}
  
  @NotNull
  @Override
  public ResolvedLink resolveLink(@NotNull Node node, @NotNull LinkResolverBasicContext context, @NotNull ResolvedLink link) {
    String url = link.getUrl();
    if (url.startsWith("ddb:")) {
      return link.withStatus(LinkStatus.VALID).withUrl("/ddbdp/" + url.substring(4));
    } 
    if (url.startsWith("bib:")) {
      return link.withStatus(LinkStatus.VALID).withUrl("/biblio/" + url.substring(4));
    } 
    return link;
  }
  
  public static class Factory implements LinkResolverFactory {
            @Nullable
            @Override
            public Set<Class<?>> getAfterDependents() {
                return null;
            }

            @Nullable
            @Override
            public Set<Class<?>> getBeforeDependents() {
                return null;
            }

            @Override
            public boolean affectsGlobalScope() {
                return false;
            }

            @NotNull
            @Override
            public LinkResolver apply(@NotNull LinkResolverBasicContext context) {
                return new PNLinkResolver(context);
            }
        }
}
