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
    alignRTL();
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
    var biblio = jQuery("div#bibliography li>a");
    if (biblio.length > 0) {
      var sparql = "/sparql?query="
        + encodeURIComponent("prefix dc: <http://purl.org/dc/terms/> "
        + "SELECT * "
        + "FROM <http://papyri.info/graph> "
        + "WHERE { ");
      jQuery("div#bibliography li>a").each( function (i) {
        sparql += encodeURIComponent("<"+this.href+"> dc:bibliographicCitation ?cite" + i + (i < biblio.length - 1?" . ":""));
      });
      sparql += encodeURIComponent(" }") + "&format=json";
      jQuery.getJSON(sparql, function(data) {
        jQuery("div#bibliography li>a").each( function(i) {
          jQuery(this).text(data.results.bindings[0]["cite" + i].value);
        });
      });
    }

    var bibl = jQuery("div#bibliography li>a");

    addLinearBrowseControls();
		getAlert();
		getCampaign();
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

/* The following functions are all concerned with linear browse - that is to say,
   allowing users to traverse to the next or previous record in the result set
   without first returning to the initial search page listing results */

/**
 * Initiates the sequence of events that queries the Solr server and adds
 * next, previous, and back-to-results controls as appropriate.
 *
 * Of the parameters passed in the query string, "p" refers to the records position
 * in the entire result set; "t" is the total number of records in the result set;
 * 'q' is the searched-for string used in highlighting (and thus must be filtered out
 * for search purposes); and the remainder are all parameters for direct use by the
 * Solr server
 *
 */
function addLinearBrowseControls(){

	var position = jQuery(document).getUrlParam("p");
	var total = jQuery(document).getUrlParam("t");
	var rows = jQuery(document).getUrlParam("rows");
	var qs = buildSolrQueryString();
	if(qs == "") return false;
	querySolrServer(qs, position, total, rows, qs);

}

/**
 * Transforms the passed querystring into a querystring usable in querying the Solr
 * server.
 *
 * The purpose of this query is to determine the next and previous records to which
 * the 'next' and 'previous' controls should point.
 *
 */

function buildSolrQueryString(){

	var querystring = window.location.search;
	if(!querystring.match(/fq=/)) return "";
	// get rid of values not used by Solr: t, d, and q (which is used only for highlighting)
	querystring = querystring.replace(/[&?]t=\d+/, "");
	querystring = querystring.replace(/[&?]p=\d+/, "");
	var highlightstring = jQuery(document).getUrlParam("q");
	// but *some* value for q is required, so the 'select all' wildcard (*:*) is given
	if(highlightstring == null || highlightstring == ""){

		querystring = querystring += "&q=*:*";

	}
	else{

		querystring = querystring.replace(highlightstring, "*:*");

	}
	if(querystring.charAt(0) == "?") querystring = querystring.substring(1);
	return querystring + "&wt=xml";

}
/**
 * Queries the Solr server
 */

function querySolrServer(query, position, total, rows, querystring){

	var that = this;
	var serverUrl = "https://" + location.host + "/pn-search/select/";
	jQuery.get(	serverUrl,
			query,
			function(data){ that.addLinearBrowseHTML(data, position, total, rows, querystring); },
			"xml");

}

/**
 * Manager function for generating the 'Previous', 'Next', and 'Back to search results'
 * HTML controls.
 */


function addLinearBrowseHTML(xmldoc, position, total, rows, querystring){

	var xml = jQuery(xmldoc);
	var prevRecord = (position == 0 && rows == 2) ? null : xml.find("doc")[0];
	var nextRecord = (position == total && rows == 2) ? null : xml.find("doc")[xml.find("doc").length - 1];
	var htmlWrapper = jQuery("<div id=\"linear-browse-wrapper\"></div>");
	addPrevRecordHTML(htmlWrapper, prevRecord, position, total, rows, querystring);
	addBackToFacetBrowse(htmlWrapper)
	addNextRecordHTML(htmlWrapper, nextRecord, position, total, rows, querystring);
	var spacer = jQuery("<div style='height: 1px; width: 100%; clear: both;'></div>");
	jQuery("#controls").before(htmlWrapper);

}

function addPrevRecordHTML(wrapper, record, position, total, rows, querystring){

	var arrowWrapper = jQuery("<div id=\"linear-previous-record\"></div>");
	var msg = "<< Previous record";
	if(record == null){

		var deadlink = jQuery("<span class='deadlink'></span>");
		deadlink.text(msg);
		arrowWrapper.append(deadlink);

	}
	else{

		var link = jQuery("<a></a>");
		link.text(msg);
		var id = jQuery(jQuery(record).children()[0]).text().substring("http://papyri.info".length);
		var title = jQuery(jQuery(record).children()[1]).text();
		var href = id + "?" + buildSolrQueryLinkString("prev", querystring, position, total, rows);
		link.attr("title", title);
		link.attr("href", href);
		arrowWrapper.append(link);

	}
	wrapper.append(arrowWrapper);

}

function addBackToFacetBrowse(wrapper){

	// note that unlike the 'previous' and 'next' HTML controls, persistence here is
	// achieved not by passing a query string around, but through
	// a cookie ("lbpersist")

	var searchstring = getCookie("lbpersist");
	if(searchstring == null) return;
	var prevpageURL = window.location.protocol + "//" + window.location.host + "/search" + searchstring;
	var linkwrapper = jQuery("<div id='linear-back'></div>");
	var link = jQuery("<a href='" + prevpageURL + "' title='Back to search page'>Back to search results</a>");
	linkwrapper.append(link);
	wrapper.append(linkwrapper);
}

function addNextRecordHTML(wrapper, record, position, total, rows, querystring){

	var arrowWrapper = jQuery("<div id=\"linear-next-record\"></div>");
	var msg = "Next record >>"
	if(record == null){

		var deadlink = jQuery("<span class='deadlink'></span>");
		deadlink.text(msg);
		arrowWrapper.append(deadlink);

	}
	else{

		var link = jQuery("<a></a>");
		link.text(msg);
		var id = jQuery(jQuery(record).children()[0]).text().substring("http://papyri.info".length);
		var title = jQuery(jQuery(record).children()[1]).text();
		var href = id + "?" + buildSolrQueryLinkString("next", querystring, position, total, rows);
		link.attr("title", title);
		link.attr("href", href);
		arrowWrapper.append(link);

	}
	wrapper.append(arrowWrapper);

}
/**
 * Alters the query string used by the presently-displayed page so that it is suitable for use
 * by the next or previous record in the result set.
 *
 * This will typically be by advancing or reducing the start value of the query, though
 * special considerations apply when the first or last record is reached.
 *
 */

function buildSolrQueryLinkString(direction, querystring, position, total, rows){

	var offset = jQuery(document).getUrlParam("start") / 1;
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
	querystring = querystring + "&t=" + jQuery(document).getUrlParam("t");
	querystring = querystring.replace(/[&]?q=\*:\*/, "");
	if(jQuery(document).getUrlParam("q") != null && jQuery(document).getUrlParam("q") != ""){

		querystring = querystring + "&q=" + jQuery(document).getUrlParam("q");

	}
	return querystring;

}


// TODO: Should probably hive this off into a separate file at some point

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
* @example value = jQuery(document).getUrlParam("paramName");
*
* To get the params of a html-attribut (uses src attribute)
* @example value = jQuery('#imgLink').getUrlParam("paramName");
*/
 getUrlParam: function(strParamName){
	  strParamName = escape(unescape(strParamName));

	  var returnVal = new Array();
	  var qString = null;

	  if (jQuery(this).attr("nodeName")=="#document") {
	  	//document-handler

		if (window.location.search.search(strParamName) > -1 ){

			qString = window.location.search.substr(1,window.location.search.length).split("&");
		}

	  } else if (jQuery(this).attr("src")!="undefined") {

	  	var strHref = jQuery(this).attr("src")
	  	if ( strHref.indexOf("?") > -1 ){
	    	var strQueryString = strHref.substr(strHref.indexOf("?")+1);
	  		qString = strQueryString.split("&");
	  	}
	  } else if (jQuery(this).attr("href")!="undefined") {

	  	var strHref = jQuery(this).attr("href")
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

function alignRTL() {
  //return true;
  jQuery("span.ab").each(function(i, ab) {
    var width = jQuery(ab).width() + 50;
    jQuery(ab).find("span[lang=ar]").each(function(i, elt) {
      jQuery(ab).css("width", (width + 50) + "px");
      //return true;
      var e = jQuery(elt);
      //var offset = ((width - e.width()) / e.parents("div.textpart").width()) * 100;
      var breaks = e.find("br");
      var r = document.createRange();
      var line = document.createElement("span");
      var frg;
      if (breaks.length > 0) { // we have a multiline span
        elt.removeAttribute("lang");
        elt.removeAttribute("dir");
        // deal with text before first line break
        if (breaks[0].previousSibling.textContent.trim() != "") {
          r.setStartBefore(elt.firstChild);
          r.setEndBefore(breaks[0]);
          line.appendChild(r.extractContents());
          line.setAttribute("lang", "ar");
          line.setAttribute("dir","rtl");
          var l = jQuery(elt.insertBefore(line, breaks[0]));
          if (elt.previousSibling.localName == "br" || elt.previousSibling.textContent.trim() == "") {
            var offset = width - l.width();
            l.before('<span style="display:inline-block;width:' + offset +'px;"> </span>');
          } else {
            if (l[0].parentElement.getBoundingClientRect()["right"] < width) {
              var offset = width - l.width();
            } else {
              var offset = l[0].parentElement.getBoundingClientRect()["left"] - 15;
            }
            l.before('<span style="display:inline-block;width:' + offset +'px;"> </span>');
          }
          console.log("before; offset: " + offset + "; width: " + width );
        }
        //deal with text after line breaks
        for (var i=0; i < breaks.length; i++) {
          r = document.createRange();
          line = document.createElement("span");
          r.setStartAfter(breaks[i]);
          if (i < breaks.length - 1) {
            r.setEndBefore(breaks[i + 1]);
          } else {
            r.setEndAfter(elt.lastChild);
          }
          line.appendChild(r.extractContents());
          line.setAttribute("lang", "ar");
          line.setAttribute("dir","rtl");
          jQuery(line).insertAfter(breaks[i]);
          var l = jQuery(line);
          var offset = width - l.width();
          l.before('<span style="display:inline-block;width:' + offset +'px;"> </span>');
          //l.find(".linenumber").css("margin-left", "-" + (32 + (width - l.width())) + "px");
          console.log("after; offset: " + offset + "; width: " + width );
        }
      } else {
        var offset = width - e.width();
        if (e[0].previousSibling.textContent.trim() == "" && (e[0].previousElementSibling.localName == "br" || e[0].previousElementSibling.localName == "a")) {
          e.before('<span style="display:inline-block;width:' + offset +'px;"> </span>');
        }
        e.find(".linenumber").css("margin-left", "-" + (32 + (width - e.width())) + "px");
        console.log("no lines; offset: " + offset + "; width: " + width );
      }
    });
  });

}

function getCampaign() {
	if (canShowCampaign()) {
		getMessage("/docs/campaign")
			.then(message => {
				if (message) {
					let popup = document.createElement("div");
					popup.setAttribute("id", "campaign");
					popup.innerHTML = '<div class="campaignheader"><a title="close" class="closer" href="#" onclick="return hideCampaign(1)">×</a></div>';
					message.querySelectorAll("a").forEach(a => a.setAttribute("onclick", "return hideCampaign(14)"));
					popup.appendChild(document.adoptNode(message));
					document.body.appendChild(popup);
				}
			});
	}
}
	
function hideCampaign(duration) {
	const day = 86400000;
	window.localStorage.setItem("Hide-papyriCampaign", (Date.now() + (duration * day)).toString());
	let campaign = document.querySelector("#campaign");
	campaign.parentElement.removeChild(campaign);
}

function canShowCampaign() {
	//return false; // comment out to launch; re-comment to suspend
	let time = window.localStorage.getItem("Hide-papyriCampaign");
	if (time) {
		time = Number.parseInt(time);
		if (Date.now() < time) {
			return false;
		}
	}
	return true;
}
		
function getAlert() {
	getMessage("/docs/alert")
		.then(message => {
			if (message) {
				let alert = document.createElement("div");
				alert.setAttribute("id", "alert");
				alert.innerHTML = '<a title="close" class="closer" href="#" onclick="return hideAlert()">×</a>';
				alert.appendChild(document.adoptNode(message));
				if (canShowAlert(alert)) {
					document.body.appendChild(alert);
				}
			}
		});
}

function hideAlert() {
	let alert = document.querySelector("#alert");
	window.localStorage.setItem("papyri.info-lastAlert", alert.outerHTML);
	alert.parentElement.removeChild(alert);
	return false;
}
	
function canShowAlert(alert) {
	let oldAlert = window.localStorage.getItem("papyri.info-lastAlert");
	if (oldAlert) {
		return oldAlert != alert.outerHTML;
	} else {
		return true;
	}
}
	
async function getMessage(url) {
	const parser = new DOMParser();
	let response = await fetch(url);
	let message = null;
	if (!response.ok) {
		return false;
	}
	let body = await response.text();
	let doc = parser.parseFromString(body, "text/html");
	return doc.querySelector("div.markdown");
}
	
