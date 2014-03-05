/*
 * Copyright 2014 org.pegdown.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package info.papyri.dispatch.pegdown.ast;

import java.util.List;
import org.parboiled.common.ImmutableList;
import org.pegdown.ast.AbstractNode;
import org.pegdown.ast.Node;
import org.pegdown.ast.Visitor;

/**
 *
 * @author hcayless
 */
public class DDbLinkNode extends AbstractNode {
  private final StringBuilder sb;
  
  public DDbLinkNode(String text) {
    this.sb = new StringBuilder(text);
  }
  
  @Override
  public void accept(Visitor visitor) {
    visitor.visit((Node)this);
  }

  @Override
  public List<Node> getChildren() {
    return ImmutableList.of();
  }
  
  public String getText() {
    return sb.toString();
  }

}
