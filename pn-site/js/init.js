function init() {
    jQuery("div#hd h1").click(function() {window.location = "/"});
    jQuery("li.dialog").each(function(i) {
        jQuery(this).after("<li><a href=\"#\" onclick=\"javascript:jQuery('#" + this.id + "c').dialog({height:100,modal:true})\">" + this.title + "</a></li>");
        jQuery(this).hide();
    });
    jQuery("ul.nav li").not(".dialog").not(jQuery("#footer ul.nav li")).not(".current").has("a").hover(function() {
      jQuery(this).css('background-color', '#F8F6F4');
    },
    function() {
      jQuery(this).css('background-color', 'transparent');
    });
    jQuery("div.controls input").each(function() {
        if (!this.checked) {
            jQuery("." + this.name).css('display', 'none');
        }
    });
    if (jQuery(".translation").length == 0 && jQuery(".image").length == 0) {
        jQuery(".transcription").css('width', '98.8%');
    }
    if (jQuery("#image").length > 0) {
        initImage();
    }
    jQuery("#tmgo").button();
    jQuery("span.term").each( function (i, elt) {
        jQuery(elt).CreateBubblePopup({
            innerHtml: jQuery(elt).find("span.gloss").html(),
            position: "top",
            themePath: "/jquerybubblepopup-template/",
            selectable: "true",
            width: 200,
            closingDelay: 500
        });
    });
    jQuery.ajax({
      type: "GET",
      url: "/editor/user/info", 
      dataType: "json",
      success: function(data, status, xhr) {
        if (data.user) {
         jQuery("#login").html("<a href=\"/editor/user/user_dashboard\">home</a> | " + data.user.name + " | <a href=\"/editor/user/signout\">sign out</a>");
        }
      },
      error: function (data, status, xhr) {
        jQuery("#login").html("Editor not available.");
      },
      timeout: 10000
    });
    jQuery.getJSON("/mulgara/sparql/?query="
        + encodeURIComponent("prefix dc: <http://purl.org/dc/terms/> "
        + "select ?subject "
        + "from <rmi://localhost/papyri.info#pi> "
        + "where { ?subject dc:references <http://papyri.info" + getPath().replace(/\/$/, "") + "/source>}")
        + "&format=json", function(data) {
            if (data.results.bindings.length > 0) {
                jQuery("#controls").append('<div id="related" class="ui-widget-content ui-corner-all" style="margin-left:2em"><h4>related resources</h4></div>')
                jQuery.each(data.results.bindings, function(i, row) {
                    var val = row.subject.value;
                    jQuery("#related").append('<a href="'+ val + '" style="margin-left:1em" target="_blank">GLRT</a>');
                })
            }
    });
    
    addLinearBrowseControls();
}

function getPath() {
    var result = window.location.href.substring(window.location.href.indexOf(window.location.pathname));
    if (window.location.search.length > 0) {
        result = result.substring(0, result.indexOf(window.location.search));
    }
    if (window.location.hash.length > 0 && result.indexOf("#") > 0) {
        result = result.substring(0, result.indexOf(window.location.hash));
    }
    return result;
}

function addLinearBrowseControls(){

	var position = $(document).getUrlParam("p");
	var total = $(document).getUrlParam("t");
	var rows = $(document).getUrlParam("rows");
	var qs = buildSolrQueryString();
	querySolrServer(qs, position, total, rows, qs);
	
}

function buildSolrQueryString(){

	var querystring = window.location.search;
	querystring = querystring.replace(/[&?]t=\d+/, "");
	querystring = querystring.replace(/[&?]p=\d+/, "");
	var highlightstring = $(document).getUrlParam("q");
	if(highlightstring == null || highlightstring == ""){
	
		querystring = querystring += "&q=*:*";
	
	}
	else{
	
		querystring = querystring.replace(highlightstring, "*:*");
	
	}
	if(querystring.charAt(0) == "?") querystring = querystring.substring(1);
	return querystring;

}

function querySolrServer(query, position, total, rows, querystring){

	var that = this;
	var serverUrl = "http://localhost/solr/select/";
	$.get(	serverUrl,
			query, 
			function(data){ that.addLinearBrowseHTML(data, position, total, rows, querystring); }, 
			"xml");

}

function addLinearBrowseHTML(xmldoc, position, total, rows, querystring){

	var xml = $(xmldoc);
	var prevRecord = (position == 0 && rows == 2) ? null : xml.find("doc")[0];
	var nextRecord = (position == total && rows == 2) ? null : xml.find("doc")[xml.find("doc").length - 1];	
	var htmlWrapper = $("<div id=\"linear-browse-wrapper\"></div>");
	addPrevRecordHTML(htmlWrapper, prevRecord, position, total, rows, querystring);
	addBackToFacetBrowse(htmlWrapper)
	addNextRecordHTML(htmlWrapper, nextRecord, position, total, rows, querystring);
	var spacer = $("<div style='height: 1px; width: 100%; clear: both;'></div>");
	$("#controls").before(htmlWrapper);

}

function addPrevRecordHTML(wrapper, record, position, total, rows, querystring){

	var arrowWrapper = $("<div id=\"linear-previous-record\"></div>");
	var msg = "<< Previous record";
	if(record == null){
	
		var deadlink = $("<span class='deadlink'></span>");
		deadlink.text(msg);
		arrowWrapper.append(deadlink);
		
	} 
	else{
	
		var link = $("<a></a>");
		link.text(msg);
		var id = $($(record).children()[0]).text().substring("http://papyri.info".length);
		var title = $($(record).children()[1]).text();
		var href = id + "?" + buildSolrQueryLinkString("prev", querystring, position, total, rows);
		link.attr("title", title);
		link.attr("href", href);
		arrowWrapper.append(link);

	}
	wrapper.append(arrowWrapper);
	
}

function addBackToFacetBrowse(wrapper){

	var searchstring = getCookie("lbpersist");
	if(searchstring == null) return;
	var prevpageURL = window.location.protocol + "//" + window.location.host + "/search" + searchstring;
	var linkwrapper = $("<div id='linear-back'></div>");
	var link = $("<a href='" + prevpageURL + "' title='Back to search page'>Back to search results</a>");	
	linkwrapper.append(link);
	wrapper.append(linkwrapper);
}

function addNextRecordHTML(wrapper, record, position, total, rows, querystring){

	var arrowWrapper = $("<div id=\"linear-next-record\"></div>");
	var msg = "Next record >>"
	if(record == null){
	
		var deadlink = $("<span class='deadlink'></span>");
		deadlink.text(msg);
		arrowWrapper.append(deadlink);
	
	}
	else{
	
		var link = $("<a></a>");
		link.text(msg);
		var id = $($(record).children()[0]).text().substring("http://papyri.info".length);
		var title = $($(record).children()[1]).text();
		var href = id + "?" + buildSolrQueryLinkString("next", querystring, position, total, rows);
		link.attr("title", title);
		link.attr("href", href);
		arrowWrapper.append(link);
		
	}
	wrapper.append(arrowWrapper);
	
}

function buildSolrQueryLinkString(direction, querystring, position, total, rows){

	var offset = $(document).getUrlParam("start") / 1;
	position = position / 1;
	total = total / 1;
	rows = rows / 1;
	var new_offset = offset;
	var new_position = position;

	if(direction == "next"){
	
		if(position + 1 == total){
			
			new_rows = 2;
			
		}
		else{
		
			new_rows = 3;
		
		}
		new_position += 1;
		if(position != 0) new_offset += 1;
	
	}
	else{					// i.e., if direction = "prev"
	
		if(position - 1 == 0){
		
			new_rows = 2;
			new_offset = 0;
		
		}
		else{ 
		
			new_offset -= 1; 
			new_rows = 3;	
		}
		new_position -= 1;
	
	}

	var reOffset = new RegExp("start=" + offset);
	var reRows = new RegExp("rows=" + rows);
	querystring = querystring.replace(reOffset, "start=" + new_offset);
	querystring = querystring.replace(reRows, "rows=" + new_rows);
	querystring = querystring + "&p=" + new_position;
	querystring = querystring + "&t=" + $(document).getUrlParam("t");
	querystring = querystring.replace(/[&]?q=\*:\*/, "");
	if($(document).getUrlParam("q") != null && $(document).getUrlParam("q") != ""){
	
		querystring = querystring + "&q=" + $(document).getUrlParam("q");
	
	}
	return querystring;
	
}


// TODO: Should probably hive this off into a separate file

/* Copyright (c) 2006-2007 Mathias Bank (http://www.mathias-bank.de)
 * Dual licensed under the MIT (http://www.opensource.org/licenses/mit-license.php) 
 * and GPL (http://www.opensource.org/licenses/gpl-license.php) licenses.
 * 
 * Version 2.1
 * 
 * Thanks to 
 * Hinnerk Ruemenapf - http://hinnerk.ruemenapf.de/ for bug reporting and fixing.
 * Tom Leonard for some improvements
 * 
 */
jQuery.fn.extend({
/**
* Returns get parameters.
*
* If the desired param does not exist, null will be returned
*
* To get the document params:
* @example value = $(document).getUrlParam("paramName");
* 
* To get the params of a html-attribut (uses src attribute)
* @example value = $('#imgLink').getUrlParam("paramName");
*/ 
 getUrlParam: function(strParamName){
	  strParamName = escape(unescape(strParamName));
	  
	  var returnVal = new Array();
	  var qString = null;
	  
	  if ($(this).attr("nodeName")=="#document") {
	  	//document-handler
		
		if (window.location.search.search(strParamName) > -1 ){
			
			qString = window.location.search.substr(1,window.location.search.length).split("&");
		}
			
	  } else if ($(this).attr("src")!="undefined") {
	  	
	  	var strHref = $(this).attr("src")
	  	if ( strHref.indexOf("?") > -1 ){
	    	var strQueryString = strHref.substr(strHref.indexOf("?")+1);
	  		qString = strQueryString.split("&");
	  	}
	  } else if ($(this).attr("href")!="undefined") {
	  	
	  	var strHref = $(this).attr("href")
	  	if ( strHref.indexOf("?") > -1 ){
	    	var strQueryString = strHref.substr(strHref.indexOf("?")+1);
	  		qString = strQueryString.split("&");
	  	}
	  } else {
	  	return null;
	  }
	  	
	  
	  if (qString==null) return null;
	  
	  
	  for (var i=0;i<qString.length; i++){
			if (escape(unescape(qString[i].split("=")[0])) == strParamName){
				returnVal.push(qString[i].split("=")[1]);
			}
			
	  }
	  
	  
	  if (returnVal.length==0) return null;
	  else if (returnVal.length==1) return returnVal[0];
	  else return returnVal;
	}
});


function getCookie(name) {
    var nameEQ = name + "=";
    var ca = document.cookie.split(';');
    for(var i=0;i < ca.length;i++) {
        var c = ca[i];
        while (c.charAt(0)==' ') c = c.substring(1,c.length);
        if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
    }
    return null;
}
