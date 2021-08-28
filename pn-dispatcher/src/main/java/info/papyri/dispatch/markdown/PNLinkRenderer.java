/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.papyri.dispatch.markdown;

import com.vladsch.flexmark.ast.AutoLink;
import com.vladsch.flexmark.html.renderer.DelegatingNodeRendererFactory;
import com.vladsch.flexmark.html.renderer.LinkType;
import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler;
import com.vladsch.flexmark.html.renderer.ResolvedLink;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.html.Attributes;
import java.util.HashSet;
import java.util.Set;
/**
 *
 * @author hac13
 */
public class PNLinkRenderer implements NodeRenderer {
  
  public static class Factory implements DelegatingNodeRendererFactory {

    @Override
    public Set<Class<?>> getDelegates() {
      return null;
    }

    @Override
    public NodeRenderer apply(DataHolder dh) {
      return new PNLinkRenderer();
    }
  }

  @Override
  public Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
    HashSet<NodeRenderingHandler<?>> set = new HashSet<>();
    set.add(new NodeRenderingHandler<>(AutoLink.class, (node, context, html) -> {
      String text = node.getText().toStringOrNull();
      if (text.startsWith("bib:") || text.startsWith("ddb:")) {
        ResolvedLink resolvedLink = context.resolveLink(LinkType.LINK, node.getUrl().unescape(), null);
        html.attr("href", resolvedLink.getUrl());
        html.withAttr().tag("a");
        html.text(text.substring(4));
        html.tag("/a");
      } else {
        context.delegateRender();
      }
    }));
    return set;
  }
  
}
