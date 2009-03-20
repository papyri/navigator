<%@page language="java" session="false" contentType="text/html" import="info.papyri.navigator.portlet.*,org.apache.lucene.search.*,org.apache.lucene.index.*,org.apache.lucene.document.*,edu.columbia.apis.*,info.papyri.index.*,util.jsp.el.Functions,java.util.*,javax.portlet.*" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%><%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %><%@taglib uri="tld/el-functions.tld" prefix="custom"%><%@page import="info.papyri.metadata.CoreMetadataFields"%>
<%@page import="info.papyri.metadata.NamespacePrefixes"%>
<portlet:defineObjects/>
<%!
Term cnTemplate = new Term(CoreMetadataFields.DOC_ID,"");
String APIS_LINK_ICON = "<img src=\"decorations/images/external.gif\" alt=\"link to metadata at APIS\" />";
String HGV_LINK_ICON = "<img src=\"decorations/images/external.gif\" alt=\"link to metadata at HGV\" />";
String DDB_LINK_ICON = "<img src=\"decorations/images/external.gif\" alt=\"link to Duke databank transcription\" />";
%>
<%
                    org.apache.lucene.document.Document doc = (Document)request.getAttribute(MetadataSearchPortlet.XREF_DOC);
                    int pageDocIndex = (Integer)request.getAttribute(MetadataSearchPortlet.XREF_PAGE_DOC_NUMBER);
                    String detailURL = request.getAttribute(NavigatorPortlet.XREF_REQ_URL).toString();
                    if (detailURL.indexOf(".psml") == -1){
                        detailURL = detailURL + "/apisfull.psml";
                    }
                    else detailURL = detailURL.replaceAll("\\/[\\w-]+\\.psml","/apisfull.psml");
                    String apisId = doc.get(CoreMetadataFields.DOC_ID);
                    String apisDisplay = apisId;
                    TreeSet<String> hgvs = new TreeSet<String>();
                    TreeSet<String> ddbs = new TreeSet<String>();
                    String [] xrefs = doc.getValues(CoreMetadataFields.XREFS);
                    if(xrefs==null)xrefs=new String[0];
                    String docInv = "";
                    for(String xref:xrefs){
                        if(xref.startsWith(NamespacePrefixes.HGV))hgvs.add(xref);
                        if(xref.startsWith(NamespacePrefixes.DDBDP))ddbs.add(xref);
                        if(xref.startsWith(NamespacePrefixes.INV))docInv = xref.substring(NamespacePrefixes.INV.length());
                    }
                    String [] hgvId = hgvs.toArray(new String[0]);
                    String [] ddbId = ddbs.toArray(new String[0]);
                    String [] publication = doc.getValues(CoreMetadataFields.BIBL_PUB);
                    if (publication == null)publication = new String[0];
                    Document apisDoc = null;
                    Document [] hgvDoc = new Document [hgvId.length];
                    String title = "";
                    String archive = "";
                    boolean apisImg = false;
                    boolean hgvImg = false;
                    ArrayList<String> imgNoteLinks = new ArrayList<String>();
                    StringBuffer docProvenance = new StringBuffer();
                    ArrayList<String> langs = new ArrayList<String>();
                    //ArrayList<String> dates = new ArrayList<String>();
                    ArrayList<String> apisDates = new ArrayList<String>();
                    ArrayList<String> hgvDates = new ArrayList<String>();
                    ArrayList<String> bl = new ArrayList<String>();
                    if (apisId == null || apisId.startsWith("none")){
                        apisDisplay = "none"; 
                        apisDoc = new Document();
                    } else {
                        apisDoc = ((Hit)LuceneIndex.SEARCH_COL.search(new TermQuery(cnTemplate.createTerm(apisId))).iterator().next()).getDocument();
                        if (apisDoc.get(CoreMetadataFields.TITLE) != null) title = apisDoc.get(CoreMetadataFields.TITLE);
                        if (apisDoc.get(CoreMetadataFields.PROVENANCE) != null) docProvenance.append(apisDoc.get(CoreMetadataFields.PROVENANCE));
                        if (apisDoc.get(CoreMetadataFields.LANG) != null){
                            for (String lang: apisDoc.getValues(CoreMetadataFields.LANG)){
                                if (!langs.contains(lang)) langs.add(lang);
                            }
                        }
                        if (apisDoc.get(CoreMetadataFields.INV) != null) docInv = apisDoc.get(CoreMetadataFields.INV);
                        if (apisDoc.get(CoreMetadataFields.IMG_URL) != null){
                            apisImg = true;
                            imgNoteLinks.add("<a href=\"" + XREFPortlet.getAPISlink(apisId) + "\" target=\"_new\">APIS" + APIS_LINK_ICON + "</a>" );
                        }
                        if(apisDoc.get(CoreMetadataFields.DATE1_D) != null){
                            if(apisDoc.get(CoreMetadataFields.DATE2_D) != null){
                                apisDates.add(apisDoc.get(CoreMetadataFields.DATE1_D) + " - " + apisDoc.get(CoreMetadataFields.DATE2_D) + " (apis)");
                            }
                            else{
                                apisDates.add(apisDoc.get(CoreMetadataFields.DATE1_D) + " (apis)");
                            }
                        }
                    }
                    
                    
                    for (int j=0;j<hgvId.length;j++){
                        hgvDoc[j] = ((Hit)LuceneIndex.SEARCH_HGV.search(new TermQuery(cnTemplate.createTerm(hgvId[j]))).iterator().next()).getDocument();
                        
                    }
                    
                    if (hgvDoc.length != 0){
                        title = hgvDoc[0].get(CoreMetadataFields.TITLE);
                        for (Document hgv: hgvDoc){
                            String p = hgv.get(CoreMetadataFields.PROVENANCE);
                            String l = hgv.get(CoreMetadataFields.LANG);
                            String hgvName = hgv.get(CoreMetadataFields.DOC_ID);
                            String pl = hgvName.substring(NamespacePrefixes.HGV.length());
                            pl = pl.replaceAll(":"," ").replaceAll("%20"," ");
                            for (int i = 0; i < publication.length; i++){
                                if (pl.equals(publication[i])){
                                    publication[i] = "<b class=\"preferred-pub\"><a href=\"" + detailURL + "?controlName=" + hgvName + "\">"+  publication[i] + "</a></b>";
                                    break;
                                }
                            }
                            if ( p != null && docProvenance.indexOf(p) == -1){
                              if (docProvenance.length() > 0) docProvenance.append(';');
                              docProvenance.append(hgv.get(CoreMetadataFields.PROVENANCE));
                            }
                            if (hgv.get(CoreMetadataFields.IMG_URL) != null){
                                hgvImg = true;
                                imgNoteLinks.add("<a href=\"" + XREFPortlet.getHGVlink(pl) + "\" target=\"_new\">HGV" + HGV_LINK_ICON + "</a>" );
                            }
                            if (l != null){
                              for(String lang:hgv.getValues(CoreMetadataFields.LANG)){
                                 if (!langs.contains(lang)) langs.add(lang); 
                              }
                            }
                            String [] date1s = hgv.getValues(CoreMetadataFields.DATE1_D);
                            if (date1s == null) date1s = new String[0];
                            String [] date2s = hgv.getValues(CoreMetadataFields.DATE2_D);
                            if (date2s == null) date2s = new String[0];
                            for (int i = 0; i < date1s.length; i++){
                                if(hgv.get(CoreMetadataFields.DATE2_D) != null && !date1s[i].equals(date2s[i])){
                                    hgvDates.add(date1s[i] + " - " + date2s[i] + " (hgv);");
                                }
                                else{
                                    hgvDates.add(date1s[i] + " (hgv);");
                                }
                            }
                            if (hgv.get(CoreMetadataFields.BIBL_CORR) != null){
                                bl.add(hgv.get(CoreMetadataFields.BIBL_CORR) + ";");
                            }
                        }
                    }
                    
                %>
                        <tbody class="searchresult" <%=(pageDocIndex % 2 != 0)?"style=\"background-color:#f6f5e0;color:#393929;\"":"" %>>
	                    <tr>
	                      <td rowspan="2">
	                      <%if (apisImg || hgvImg){ %><img src="/pn-portals/iavail.gif" alt="Image available" /><%} %>
	                      </td>
	                      <td rowspan="2">
	                      <%
	                      String detailId = apisId;
	                      if(apisId.startsWith(NamespacePrefixes.APIS + "none")){ 
	                          detailId = hgvId[0];
	                      }
	                      detailId = Functions.encode(detailId);
	                      %>
	                      <a href="<%=detailURL + "?controlName=" + detailId %>">[view]</a>
	                      </td>
	                      <td class="metadatalinks" rowspan="2">
	                        <b>Identifiers:</b>
	                        <ul>
	                          <%if (!apisId.startsWith("none")){ %><li style="white-space:nowrap;"><%=apisId %>(apis)<a href="<%=XREFPortlet.getAPISlink(apisId) %>" target="_new"><%=APIS_LINK_ICON %></a></li><%} %>
	                           <% for (int j =0;j<hgvId.length;j++){ %>
	                          <li style="white-space:nowrap;">
	                           <%=hgvId[j] %>(hgv)<a href="<%=XREFPortlet.getHGVlink(hgvId[j]) %>" target="_new"><%=HGV_LINK_ICON %></a><br/>
	                           </li>
	                           <%} %>
	                           <% for (int j =0;j<ddbId.length;j++){ %>
	                          <li style="white-space:nowrap;">
	                           <%=ddbId[j] %>(ddb)<a href="<%=XREFPortlet.getDDBDPlink(ddbId[j]) %>" target="_new"><%=DDB_LINK_ICON %></a><br/>
	                           </li>
	                           <%} %>
	                        </ul>
	                      </td>
	                      <td class="publication">
	                        <b>Publication:</b>
	                        <br/>
	                           <% for (int j =0;j<publication.length;j++){ %>
	                           <span><%=publication[j] %></span><br/>
	                           <%} %>
	                      </td>
	                      <td class="date"><b>Date:</b><br/>
	                      <% Iterator<String> datesIter = (hgvDates.size() > 0)?hgvDates.iterator():apisDates.iterator();
	                      while(datesIter.hasNext()){%><span><%=datesIter.next() %></span><br/>
	                      <%} %></td>
	                      <td class="provenance"><b>Provenance:</b><br/><span><%=docProvenance %></span></td>
	                      <td class="title"><b>Title:</b><br/><span><%=title %></span></td>
	                      <td class="translation"><b>Translation(s):</b><br/><span><%=(apisDoc != null && apisDoc.get(CoreMetadataFields.BIBL_TRANS) != null)?"<a href=\"" + XREFPortlet.getAPISlink(apisId) + "\">English" + APIS_LINK_ICON + "</a>":"none" %></span></td>
	                    </tr>
                        <tr>
                          <td><b style="color:#666">Author/Archive:</b></td>
                          <td class="ids">
                          <b style="color:#666">Inventory:</b>
                          <br/><span><%=docInv %></span></td>
                          <td><b style="color:#666">Language:</b><br/>
                          <% Iterator<String> langIter = langs.iterator();
                          while(langIter.hasNext()){%>
                          <span><%=langIter.next() %></span><br/>
                          <%} %>
                          </td>
                          <td class="image"><b style="color:#666">Image(s):</b><br/>
                            <%
                          
                          Iterator<String> links = imgNoteLinks.iterator();
                            while (links.hasNext()){%>
                            <%=links.next() %><br/>
                            <%} %>
                            
                          </td>
                          <td class="bl"><b style="color:#666">BL Post-Concordance:</b><br/>
                          <%Iterator<String> blIter = bl.iterator();
                          while(blIter.hasNext()){%><span><%=blIter.next() %></span><br/>
                          <%} %>
                          </td>
                        </tr>
                    </tbody>