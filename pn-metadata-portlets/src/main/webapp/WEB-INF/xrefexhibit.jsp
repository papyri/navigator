<%@ page language="java"%>
<%@ page session="false" contentType="text/html" import="org.apache.lucene.document.*,java.util.*,javax.portlet.*" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%!

%>
<portlet:defineObjects/>
<portlet:defineObjects/>
    <script type="text/javascript">
          //alert(APIS_NS_DATA.items[0].label);
    </script>
    <table width="100%">
        <tr valign="top">
            <td>
                <div id="exhibit-control-panel"></div>
                <div id="exhibit-view-panel">
                    <div ex:role="exhibit-view"
                        ex:viewClass="Exhibit.TabularView"
                        ex:label="Table"
                        ex:columns=".label, .publication, .apis, .hgv, .ddbdp"
                        ex:columnLabels="label, publication, apis, hgv, ddbdp"
                        ex:columnFormats="list, list, list, list, list"
                        ex:sortColumn="1"
                        ex:sortAscending="true"
                        ></div>                </div>
            </td>
            <td width="25%">
                <div id="exhibit-browse-panel" ex:facets=".publication-series, .apis-collection"></div>
            </td>
        </tr>
    </table>
