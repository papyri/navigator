/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.papyri.dispatch.pegdown;

import java.util.ArrayList;
import java.util.List;
import org.pegdown.LinkRenderer;
import org.pegdown.ParsingTimeoutException;
import org.pegdown.PegDownProcessor;
import org.pegdown.ToHtmlSerializer;
import org.pegdown.ast.RootNode;
import org.pegdown.plugins.PegDownPlugins;
import org.pegdown.plugins.ToHtmlSerializerPlugin;

/**
 *
 * @author hcayless
 */
public class PNPegDownProcessor extends PegDownProcessor {
  
  public final List<ToHtmlSerializerPlugin> serializers = new ArrayList<ToHtmlSerializerPlugin>();
  
  public PNPegDownProcessor(int options, 
          long maxParsingTimeInMillis, 
          PegDownPlugins plugins, 
          ToHtmlSerializerPlugin serializer) {
    super(options, maxParsingTimeInMillis, plugins);
    this.serializers.add(serializer);
  }
  
  @Override
  public String markdownToHtml(String markdownSource) {
    try {
          RootNode astRoot = parseMarkdown(markdownSource.toCharArray());
          return new ToHtmlSerializer(new LinkRenderer(), serializers).toHtml(astRoot);
      } catch(ParsingTimeoutException e) {
          return null;
      }
  }
}
