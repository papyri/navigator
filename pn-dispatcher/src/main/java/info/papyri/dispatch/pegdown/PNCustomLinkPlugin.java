/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.papyri.dispatch.pegdown;

import static org.parboiled.BaseParser.ANY;
import org.parboiled.Rule;
import info.papyri.dispatch.pegdown.ast.DDbLinkNode;
import info.papyri.dispatch.pegdown.ast.BibLinkNode;
import org.pegdown.Parser;
import org.pegdown.Printer;
import org.pegdown.ast.Node;
import org.pegdown.ast.Visitor;
import org.pegdown.plugins.InlinePluginParser;
import org.pegdown.plugins.ToHtmlSerializerPlugin;

/**
 *
 * @author hcayless
 */
public class PNCustomLinkPlugin extends Parser implements InlinePluginParser, ToHtmlSerializerPlugin {

  public PNCustomLinkPlugin() {
    super(ALL, 1000L, Parser.DefaultParseRunnerProvider);
  }

  public Rule DDbLink() {
    return Sequence(
            "{ddb:",
            OneOrMore(TestNot('}'), ANY),
            push(new DDbLinkNode(match())),
            "}");
  }
  
  public Rule BibLink() {
    return Sequence(
            "{bib:",
            OneOrMore(TestNot('}'), ANY),
            push(new BibLinkNode(match())),
            "}");
  }


  @Override
  public Rule[] inlinePluginRules() {
    return new Rule[]{DDbLink(),BibLink()};
  }

  @Override
  public boolean visit(Node node, Visitor visitor, Printer printer) {
    if(node instanceof DDbLinkNode) {
      DDbLinkNode n = (DDbLinkNode)node;
      printer.print("<a href=\"/ddbdp/")
              .print(n.getText())
              .print("\">")
              .print(n.getText())
              .print("</a>");
      return true;
    }
    if(node instanceof BibLinkNode) {
      BibLinkNode n = (BibLinkNode)node;
      printer.print("<a href=\"/biblio/")
              .print(n.getText())
              .print("\">")
              .print(n.getText())
              .print("</a>");
      return true;
    }
    return false;
  }
}
