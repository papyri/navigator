<%@page language="java" session="false" contentType="text/html" import="info.papyri.metadata.*,org.apache.lucene.search.*,org.apache.lucene.search.highlight.*,org.apache.lucene.index.*,org.apache.lucene.document.*,edu.columbia.apis.*,info.papyri.index.LuceneIndex,util.jsp.el.Functions,java.util.*,javax.portlet.*,info.papyri.navigator.portlet.*" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%><%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %><%@taglib uri="tld/el-functions.tld" prefix="custom"%>
<portlet:defineObjects/>
<%!
Term cnTemplate = new Term(CoreMetadataFields.DOC_ID,"");
String APIS_LINK_ICON = "<img src=\"decorations/images/external.gif\" alt=\"link to metadata at APIS\" />";
String HGV_LINK_ICON = "<img src=\"decorations/images/external.gif\" alt=\"link to metadata at HGV\" />";
String DDB_LINK_ICON = "<img src=\"decorations/images/external.gif\" alt=\"link to Duke databank transcription\" />";
%>
<%
                    Document xrefDoc = (Document)request.getAttribute(MetadataSearchPortlet.XREF_DOC);
                    Object docNo = request.getAttribute(MetadataSearchPortlet.XREF_PAGE_DOC_NUMBER);
                    String detailURL = request.getAttribute(NavigatorPortlet.XREF_REQ_URL).toString();
                    if (detailURL.indexOf(".psml") == -1){
                        detailURL = detailURL + "/apisfull.psml";
                    }
                    else detailURL = detailURL.replaceAll("\\/[\\w-]+\\.psml","/apisfull.psml");
                    int pageDocIndex = (docNo == null)?0:(Integer)docNo;
                    String docId = xrefDoc.get(CoreMetadataFields.DOC_ID);
                    String apisDisplay = XREFPortlet.getDisplay(docId);
                    TreeSet<String> hgvs = new TreeSet<String>();
                    TreeSet<String> ddbs = new TreeSet<String>();
                    String [] xrefs = xrefDoc.getValues(CoreMetadataFields.XREFS);
                    if(xrefs==null)xrefs=new String[0];
                    String docInv = "";
                    for(String xref:xrefs){
                        if(xref.startsWith(NamespacePrefixes.HGV))hgvs.add(xref);
                        if(xref.startsWith(NamespacePrefixes.DDBDP))ddbs.add(xref);
                        if(xref.startsWith(NamespacePrefixes.INV))docInv = xref.substring(NamespacePrefixes.INV.length());
                    }
                    String [] hgvId = hgvs.toArray(new String[0]);
                    String [] ddbId = ddbs.toArray(new String[0]);
                    String [] publication = xrefDoc.getValues(CoreMetadataFields.BIBL_PUB);
                    if (publication == null)publication = new String[0];
                    Document [] hgvDoc = new Document [hgvId.length];
                    String detailId = docId;
                    if(docId.indexOf(":hgv:") != -1){ 
                        detailId = docId;
                    }
                    detailId = Functions.encode(detailId);
                    String title = "";
                    String archive = "";
                    boolean apisImg = CoreMetadataFields.SORTABLE_YES_VALUE.equals(xrefDoc.get(CoreMetadataFields.SORT_HAS_IMG));
                    boolean hgvImg = false;
                    StringBuffer docProvenance = new StringBuffer();
                    ArrayList<String> langs = new ArrayList<String>();
                    ArrayList<String> dates = new ArrayList<String>();
                    ArrayList<String> bl = new ArrayList<String>();
                    if (xrefDoc.get(CoreMetadataFields.TITLE) != null) title = xrefDoc.get(CoreMetadataFields.TITLE);
                    if (xrefDoc.get(CoreMetadataFields.PROVENANCE) != null) docProvenance.append(xrefDoc.get(CoreMetadataFields.PROVENANCE));
                    if (xrefDoc.get(CoreMetadataFields.LANG) != null){
                        for (String lang:xrefDoc.getValues(CoreMetadataFields.LANG)){
                            if (!langs.contains(lang)) langs.add(lang);
                        }
                    }
                    if (xrefDoc.get(CoreMetadataFields.INV) != null) docInv = xrefDoc.get(CoreMetadataFields.INV);
                    else docInv = "&nbsp;";
                    if (xrefDoc.get(CoreMetadataFields.IMG_URL) != null) apisImg = true;
                    if(xrefDoc.get(CoreMetadataFields.DATE1_D) != null){
                        if(xrefDoc.get(CoreMetadataFields.DATE2_D) != null){
                            dates.add(xrefDoc.get(CoreMetadataFields.DATE1_D) + " - " + xrefDoc.get(CoreMetadataFields.DATE2_D));
                        }
                        else{
                            dates.add(xrefDoc.get(CoreMetadataFields.DATE1_D));
                        }
                    }
                    if(docId.indexOf(":hgv:") != -1){
                        String pl = docId.substring(NamespacePrefixes.HGV.length()).replaceAll("%20"," ").replaceAll(":"," ");
                        for (int i = 0; i < publication.length; i++){
                            if (pl.equals(publication[i])){
                                publication[i] = "<b class=\"preferred-pub\"><a href=\"" + detailURL + "?controlName=" + Functions.encode(docId) + "\">"+  publication[i] + "</a></b>";
                                break;
                            }
                        }
                    }
                    
                    for (int j=0;j<hgvId.length;j++){
                        if(!hgvId[j].startsWith(NamespacePrefixes.HGV)) continue;
                        Document hgv = ((Hit)LuceneIndex.SEARCH_HGV.search(new TermQuery(cnTemplate.createTerm(hgvId[j]))).iterator().next()).getDocument();
                            if(title==null || "".equals(title)) title = hgv.get(CoreMetadataFields.TITLE);
                            String id = hgv.get(CoreMetadataFields.DOC_ID);
                            String pl = id.substring(NamespacePrefixes.HGV.length()).replaceAll("%20"," ");
                            if (hgv.get(CoreMetadataFields.IMG_URL) != null){
                                hgvImg = true;
                            }
                            for (int i = 0; i < publication.length; i++){
                                if (pl.equals(publication[i])){
                                    publication[i] = "<b class=\"preferred-pub\"><a href=\"" + detailURL + "?controlName=" + Functions.encode(id) + "\">"+  publication[i] + "</a></b>";
                                    break;
                                }
                            }
                    }
                    
                %>
                        <tbody class="searchresult" <%=(pageDocIndex % 2 != 0)?"style=\"background-color:#f6f5e0;color:#393929;\"":"" %>>
	                    <tr>
	                      <td>
	                      <%if (apisImg || hgvImg){ %><img src="/pn-portals/iavail.gif" alt="Image available" /><%} %>
	                      </td>
	                      <td><a href="<%=detailURL + "?controlName=" + detailId %>">[view]</a><br/>
	                      </td>
	                      <td class="metadatalinks">
	                        <span><%=apisDisplay %></span>
                          </td>
                          <td><%=title %>
	                      </td>
	                      <td class="publication">
	                           <% if (publication.length == 0){ %>&nbsp;
	                           <% }
	                           for (int j =0;j<publication.length;j++){ %>
	                           <span><%=publication[j] %></span><br/>
	                           <%} %>
	                      </td>
                          <td class="ids">
                              <span><%=docInv %></span>
                          </td>
	                      <td class="date">
	                      <% Iterator<String> datesIter = dates.iterator();
	                      if (!datesIter.hasNext()){ %>&nbsp;
	                      <% }
	                      while(datesIter.hasNext()){%><span><%=datesIter.next() %></span><br/>
	                      <%} %></td>
	                      <td class="provenance"><span><%=(docProvenance.length() == 0)?"&nbsp;":docProvenance.toString() %></span></td>
                          <td class="lang">
                          <%
                          Iterator<String> langIter = langs.iterator();
                          while(langIter.hasNext()){
                          %>
                          <span><%=langIter.next() %><br/></span>
                          <%} %>
                          </td>
                          <td class="bl">
                          <%Iterator<String> blIter = bl.iterator();
                          if (!blIter.hasNext()){%>
                          &nbsp;
                          <% }
                          while(blIter.hasNext()){%><span><%=blIter.next() %></span><br/>
                          <%} %>
                          </td>
                        </tr>
                        <%String keywords = request.getParameter("keyword");
                        if(keywords != null && !"".equals(keywords)){
                            %>
                        <tr>
                          <td class="keyword-in-context" colspan="10">
                          <% 
                          String [] kw = keywords.replaceAll("\"","").split("[\\s\\|]");
                          WeightedTerm [] terms = new WeightedTerm [kw.length];
                          for (int i=0;i<kw.length;i++){
                              terms[i] = new WeightedTerm(1,kw[i].toLowerCase());
                          }
                          Highlighter hl = new Highlighter(new QueryScorer(terms));
                          String field = ("on".equals(request.getParameter("chkIncludeTranslations")))?CoreMetadataFields.ALL:CoreMetadataFields.ALL_NO_TRANS;
                          String allNoTrans = xrefDoc.get(field);
                          if (allNoTrans == null) allNoTrans = "";
                          String fragment = hl.getBestFragment(new org.apache.lucene.analysis.standard.StandardAnalyzer(), CoreMetadataFields.ALL_NO_TRANS, allNoTrans);
                          if (fragment == null || "".equals(fragment.trim())) fragment = "&nbsp;";
                          %><%=fragment %>
                          </td>
                        </tr>
                        <%} %>
                    </tbody>