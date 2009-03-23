package info.papyri.ddbdp.servlet;

public interface IndexEventListener extends java.util.EventListener {
      public void replaceDocReader(IndexEvent event);
      public void replaceSearchers(SearcherEvent event);
}
