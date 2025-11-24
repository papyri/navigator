function init() {
    //mute: initjQueryMigrate();
    jQuery("div#hd h1").on('click', () => { window.location = "/" });
    jQuery("li.dialog").each(function(i) {
        jQuery(this).after("<li><a href=\"#\" onclick=\"javascript:jQuery('#" + this.id + "c').dialog({height:100,modal:true})\">" + this.title + "</a></li>");
        jQuery(this).hide();
    });
    jQuery("ul.nav li").not(".dialog").not(jQuery("#footer ul.nav li")).not(".current").has("a").on({
      mouseenter: function() {
          jQuery(this).css('background-color', '#F8F6F4');
      },
      mouseleave: function() {
          jQuery(this).css('background-color', 'initial'); //set it back to its initial state
      }
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

    jQuery.ajax({
      type: "GET",
      url: "/editor/user/info",
      dataType: "json",
      success: function(data, status, xhr) {
        if (data.user) {
         jQuery("#login").html("<a href=\"/editor/user/user_dashboard\" class=\"btn btn-link btn-sm text-decoration-none\">home</a><a href=\"/editor/users/edit\" class=\"btn btn-link btn-sm text-decoration-none\">" + data.user.name + "</a><a href=\"/editor/user/signout\" class=\"btn btn-link btn-sm text-decoration-none\">sign out</a>");
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
		initBootstrapTooltips();
		initMetadataTextSliders();

		// Initialize apparatus link transformation for /current/ and /editions/ pages
		if (window.location.pathname.includes('/current/') || window.location.pathname.includes('/editions/')) {
		    transformTextPartNumbers();
		    reorderTextSections();
		    transformTranslationHeadings();
		    hideLineNumbersFromScreenReaders();
		    transformApparatusLinks();
		    transformApparatusContent().then(() => {
		        addLineNumberHoverEffect();
		        handleApparatusHashOnLoad();

		        // Add resize listener to recalculate apparatus max-height
		        let resizeTimer;
		        window.addEventListener('resize', () => {
		            clearTimeout(resizeTimer);
		            resizeTimer = setTimeout(() => {
		                setApparatusMaxHeight();
		            }, 250);
		        });

		        // Fade in the #edition element now that all processing is complete
		        const editions = document.querySelectorAll('#edition');
		        if (editions) {
		            editions.forEach(edition => edition.classList.add('ready'));
		        }
		    });
		}
}

function initjQueryMigrate() {
  jQuery.migrateMute = true;
  jQuery.migrateTrace = false;
  jQuery.migrateWarnings.push = (message) => {
    const stacktrace = new Error().stack;
    const cleanedStacktrace = stacktrace.replace(/^.*at jQuery\.migrateWarnings\.push.*$/gm, '');
    _paq.push(['trackEvent', 'warning.jqmigrate', message + '\n' + cleanedStacktrace]);
  };
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

function querySolrServer(query, position, total, rows, querystring) {

	var that = this;
	var serverUrl = "/pn-search/select/";
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

	var pageNavWrapper = jQuery("<nav aria-label=\"Page navigation\"></nav>");
	var htmlWrapper = jQuery("<ul class=\"pagination pagination-sm\"></ul>");
	pageNavWrapper.append(htmlWrapper);

	addPrevRecordHTML(htmlWrapper, prevRecord, position, total, rows, querystring);
	addBackToFacetBrowse(htmlWrapper)
	addNextRecordHTML(htmlWrapper, nextRecord, position, total, rows, querystring);

	jQuery("#controls").before(pageNavWrapper);

}

function addPrevRecordHTML(wrapper, record, position, total, rows, querystring){

	var arrowWrapper = jQuery("<li class=\"page-item\"></li>");
	var msg = "« Previous record"; // using raw unicode here for compatibility with link.text()

	var link = jQuery("<a class=\"page-link\"></a>");
	link.text(msg);
	var id = jQuery(jQuery(record).children()[0]).text().substring("https://papyri.info".length);
	var title = jQuery(jQuery(record).children()[1]).text();
	var href = id + "?" + buildSolrQueryLinkString("prev", querystring, position, total, rows);
	link.attr("title", title);
	link.attr("href", href);

	if(record == null){
		arrowWrapper.addClass("disabled");
		link.addClass("disabled");
		link.attr("href", "#");
	}

	arrowWrapper.append(link);
	wrapper.append(arrowWrapper);
}

function addBackToFacetBrowse(wrapper){

	// note that unlike the 'previous' and 'next' HTML controls, persistence here is
	// achieved not by passing a query string around, but through
	// a cookie ("lbpersist")

	var searchstring = getCookie("lbpersist");
	if(searchstring == null) return;
	var prevpageURL = window.location.protocol + "//" + window.location.host + "/search" + searchstring;

	var linkWrapper = jQuery("<li class='page-item'></li>");
	var link = jQuery("<a class='page-link' href='" + prevpageURL + "' title='Back to search page'>Back to search results</a>");
	linkWrapper.append(link);
	wrapper.append(linkWrapper);
}

function addNextRecordHTML(wrapper, record, position, total, rows, querystring){

	var arrowWrapper = jQuery("<li class=\"page-item\"></li>");
	var msg = "Next record »"; // using raw unicode here for compatibility with link.text()

	var link = jQuery("<a class=\"page-link\"></a>");
	link.text(msg);
	var id = jQuery(jQuery(record).children()[0]).text().substring("https://papyri.info".length);
	var title = jQuery(jQuery(record).children()[1]).text();
	var href = id + "?" + buildSolrQueryLinkString("next", querystring, position, total, rows);
	link.attr("title", title);
	link.attr("href", href);

	if(record == null){
		arrowWrapper.addClass("disabled");
		link.addClass("disabled");
		link.attr("href", "#");
	}

	arrowWrapper.append(link);
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

/**
 *  Pad
 */
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
        if ((e[0].previousSibling.textContent.trim() == "" && (e[0].previousElementSibling.localName == "br" || e[0].previousElementSibling.localName == "a")) || e[0].previousSibling.localName == 'span' && e[0].previousSibling.classList.contains('linenumber')) {
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
	return false; // comment out to launch; re-comment to suspend
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

function initBootstrapTooltips() {
	// Initialize tooltips for both standard elements and span elements within transcriptions
	const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"], .transcription span[title]');
	const tooltipList = [...tooltipTriggerList].map(tooltipTriggerEl => new bootstrap.Tooltip(tooltipTriggerEl));

	// Add tooltips for translation terms with glosses
	jQuery(".translation span.term").each( function (i, elt) {
		const htmlContent = $(elt).find("span.gloss").html();
		if (htmlContent) {
			$(elt).attr('title', htmlContent);
			$(elt).tooltip({html: true});
		}
  });
}

function initMetadataTextSliders() {
	$("#controls input").on("click", (e) => {
    const target = e.currentTarget;
    if (target.checked) {
      $("." + target.name).show();
    } else {
      $("." + target.name).hide();
    }
  });
}

function highlightHash() {
    // Only work within .transcription elements if they exist
    const transcriptionContainer = document.querySelector('.transcription');
    if (!transcriptionContainer) {
        return;
    }

    // Remove existing highlights only within transcription
    transcriptionContainer.querySelectorAll('.active').forEach(element => {
        element.classList.remove('active');
    });

    const hash = window.location.hash;

    // If there's a hash, find the element within transcription and highlight it
    if (hash && hash.length > 1) {
        const elementId = hash.substring(1);
        const targetElement = transcriptionContainer.querySelector('#' + elementId);

        if (targetElement) {
            targetElement.classList.add('active');
        }

        // Also highlight the corresponding to/from element
        let correspondingId = null;
        if (elementId.startsWith('to-app-')) {
            correspondingId = elementId.replace('to-app-', 'from-app-');
        } else if (elementId.startsWith('from-app-')) {
            correspondingId = elementId.replace('from-app-', 'to-app-');
        }

        if (correspondingId) {
            const correspondingElement = transcriptionContainer.querySelector('#' + correspondingId);
            if (correspondingElement) {
                correspondingElement.classList.add('active');
            }
        }
    }
}

// Initialize hash highlighting on page load
document.addEventListener('DOMContentLoaded', function() {
    highlightHash();
});

// Listen for hash changes and update highlighting
window.addEventListener('hashchange', function() {
    highlightHash();
});


/***********************/
/** APPARATUS SCRIPTS **/
/***********************/

// Transform textpartnumber spans into H3 elements and wrap content
function transformTextPartNumbers() {
	const edition = jQuery('#edition');
	if (!edition.length) return;

	// Remove text nodes that appear between textpartnumber spans and ab spans
	edition.find('span.textpartnumber').each(function() {
		let nextNode = this.nextSibling;
		// Keep removing text nodes until we hit an element node (or end of siblings)
		while (nextNode && nextNode.nodeType === 3) {
			const nodeToRemove = nextNode;
			nextNode = nextNode.nextSibling;
			nodeToRemove.remove();
		}
	});

	// Collect all direct children of #edition (textpartnumber spans and ab spans)
	const children = edition.children('h3.textpartnumber, span.ab').toArray();

	// If there are no children, or all children are empty, remove the #edition element
	if (children.length === 0) {
		edition.remove();
		return;
	}

	// Check if all children are empty (have no text content)
	const allEmpty = children.every(child => jQuery(child).text().trim() === '');
	if (allEmpty) {
		edition.remove();
		return;
	}

	// Create wrapper div
	const wrapper = jQuery('<div></div>').addClass('edition-content');

	// Process all children and add to wrapper
	children.forEach(function(element) {
		const $element = jQuery(element);
			// Detach and append ab span
			wrapper.append($element.detach());
	});

	// Insert wrapper at the beginning of #edition
	edition.prepend(wrapper);
}

// Reorder text sections: move #history and related elements to be direct children of .text.row
function reorderTextSections() {
	const textRow = jQuery('.text.row');
	if (!textRow.length) return;

	// Find sections
	const history = jQuery('#history');
	const translations = jQuery('.translations');
	const ld = jQuery('#ld');

	// Move #history, its h2, and the copyright paragraph together
	if (history.length) {
		// Find the h2 that precedes #history
		const historyH2 = history.prev('h2');

		// Find the copyright paragraph that follows #history
		const copyrightP = history.next('p').filter(':has(a[rel="license"])');

		// Create a wrapper for all history-related content
		const historyWrapper = jQuery('<div></div>').attr('id', 'history-section');

		// Move elements into wrapper
		if (historyH2.length) {
			historyWrapper.append(historyH2.detach());
		}
		historyWrapper.append(history.detach());
		if (copyrightP.length) {
			historyWrapper.append(copyrightP.detach());
		}

		// Insert the wrapper in the correct position
		// Order will be: transcription, translations, history-section, ld
		if (translations.length) {
			historyWrapper.insertAfter(translations);
		} else {
			// If no translations, insert before #ld or append to textRow
			if (ld.length) {
				historyWrapper.insertBefore(ld);
			} else {
				textRow.append(historyWrapper);
			}
		}
	}
}

// Transform h2 headings to h3 in translation sections
function transformTranslationHeadings() {
	jQuery('.translation.data div h2').each(function() {
		const $h2 = jQuery(this);
		const text = $h2.text().trim().toLowerCase();

		// Only transform h2s that contain "translation" or "bibliography"
        if (text.includes('translation') || text.includes('bibliography')) {
			const h3 = jQuery('<h3></h3>').html($h2.html());

			// Copy any attributes
			if ($h2.attr('id')) {
				h3.attr('id', $h2.attr('id'));
			}
			if ($h2.attr('class')) {
				h3.attr('class', $h2.attr('class'));
			}

			$h2.replaceWith(h3);
		}
	});
}

// hide hard-coded line numbers from screen readers
function hideLineNumbersFromScreenReaders() {
	jQuery('span.linenumber').attr('aria-hidden', 'true');
}

// replace (*) with *
function transformApparatusLinks() {
    jQuery('#edition span.ab a[href^="#to-app-"]').addClass('apparatus-link').html('<span aria-hidden="true">*</span>').attr('aria-label', 'Apparatus note');

    // control apparatus link behavior
    jQuery('.apparatus-link').on('click', function(e) {
        e.preventDefault();
        const href = jQuery(this).attr('href');
        const targetId = href.substring(1); // Remove the #

        jQuery('.apparatus-link.active').removeClass('active');
        jQuery(this).addClass('active');
        jQuery('.apparatus-entry.active').removeClass('active');

        // Find and highlight the corresponding apparatus entry
        const apparatusEntry = document.getElementById(targetId);
        if (apparatusEntry) {
            apparatusEntry.classList.add('active');

            // Scroll apparatus entry into view within its container
            apparatusEntry.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
        }
    });
}

// apparatus content
// - Wrap each entry in a div instead of using <br> separators
// - Move anchor IDs from <a> tags to wrapper divs
// - Remove the "^" link text
// - Make line numbers clickable to highlight words in transcription
function transformApparatusContent() {
    return new Promise((resolve) => {
        const apparatus = document.querySelector('#apparatus');
        if (!apparatus) {
            resolve();
            return;
        }

    // Set max-height based on viewport and ab span height
    setApparatusMaxHeight();

    // Add click handlers to line numbers to highlight corresponding words
    const lineNumbers = apparatus.querySelectorAll('.apparatus-line-number');

    lineNumbers.forEach(lineNum => {
        lineNum.addEventListener('click', function(e) {
            e.preventDefault();
            const href = this.getAttribute('href');
            const targetId = href.substring(1); // Remove the #

            // Update URL with to-app- version of the link (change from-app- to to-app-)
            const apparatusEntryId = this.closest('.apparatus-entry').id;
            if (apparatusEntryId) {
                history.replaceState(null, '', '#' + apparatusEntryId);
            }

            // Remove active class from all apparatus entries
            document.querySelectorAll('.apparatus-entry.active').forEach(el => {
                el.classList.remove('active');
            });

            // Add active class to the clicked entry's parent
            this.closest('.apparatus-entry').classList.add('active');

            // Remove active class from all apparatus links in transcription
            document.querySelectorAll('.apparatus-link.active').forEach(el => {
                el.classList.remove('active');
            });

            // Find the target element in the transcription and add active class
            const targetElement = document.getElementById(targetId);
            if (targetElement) {
                targetElement.classList.add('active');

                // Remove previous highlights
                document.querySelectorAll('.apparatus-highlight').forEach(el => {
                    el.classList.remove('apparatus-highlight');
                });

                // Highlight the target word
                targetElement.classList.add('apparatus-highlight');

                // Scroll to it
                targetElement.scrollIntoView({ behavior: 'smooth', block: 'center' });

                // Set focus to the target element for keyboard navigation
                // If it's not naturally focusable, make it focusable
                if (!targetElement.hasAttribute('tabindex')) {
                    targetElement.setAttribute('tabindex', '-1');
                }
                targetElement.focus();
            }
        });
    });

        // Set apparatus section max-height to match the transcription height
        // const edition = document.querySelector('#edition');
        // const transcription = edition.querySelector('span.ab');
        // if (transcription) {
        //     const transcriptionHeight = transcription.offsetHeight;
        //     apparatus.style.maxHeight = transcriptionHeight + 'px';
        // }

        // Resolve the promise after DOM updates
        resolve();
    });
}

// Wrap each line in a span with line number class and add hover effect
function addLineNumberHoverEffect() {

    const edition = document.querySelector('#edition');
    if (!edition) {
        return;
    }

    // Handle multiple span.ab sections
    const transcriptions = edition.querySelectorAll('span.ab');
    if (!transcriptions || transcriptions.length === 0) {
        return;
    }

    transcriptions.forEach(transcription => {
        // Get the HTML content
        let html = transcription.innerHTML;

        // Get all line breaks with id="alN" or id="aN-lN" format
        const lineBreakPattern = /<br\s+id="a([^"]+)"[^>]*>/g;
        const lineBreaks = [];
        let match;

        while ((match = lineBreakPattern.exec(html)) !== null) {
            const idPart = match[1];
            // Extract line number from formats like "l1", "i-l1", "ii-l1", "v-l35", etc.
            const lineNumMatch = idPart.match(/l(\d+)$/);
            const lineNumber = lineNumMatch ? parseInt(lineNumMatch[1]) : 1;

            lineBreaks.push({
                fullMatch: match[0],
                lineNumber: lineNumber,
                index: match.index
            });
        }

        // If there are no line breaks, skip this transcription (leave it as-is)
        if (lineBreaks.length === 0) {
            return;
        }

        // Helper function to check if line should skip line numbering
        const shouldSkipLineNumber = (content) => {
            // Skip if line contains the dash separator pattern
            if (content.includes('-- -- -- -- -- -- -- -- -- --')) {
                return true;
            }
            // Skip if line number contains 'bis' (e.g., "20bis")
            if (content.includes('bis')) {
                return true;
            }
            // Skip if line contains "lines missing" or "line missing" pattern (e.g., "[ca.26 lines missing]" or "[2 lines missing]")
            if (content.includes('lines missing') || content.includes('line missing')) {
                return true;
            }
            return false;
        };

        // Helper function to track and close/reopen open tags across line breaks
        const getOpenTags = (htmlContent) => {
            const tags = [];
            const tagPattern = /<(\/?)([\w-]+)([^>]*)>/g;
            let tagMatch;

            while ((tagMatch = tagPattern.exec(htmlContent)) !== null) {
                const isClosing = tagMatch[1] === '/';
                const tagName = tagMatch[2];
                const attributes = tagMatch[3];

                if (isClosing) {
                    // Remove the last occurrence of this tag from the stack
                    for (let i = tags.length - 1; i >= 0; i--) {
                        if (tags[i].name === tagName) {
                            tags.splice(i, 1);
                            break;
                        }
                    }
                } else if (tagName !== 'br' && tagName !== 'a') {
                    // Add opening tag to stack (skip br and a tags)
                    tags.push({ name: tagName, attributes: attributes });
                }
            }

            return tags;
        };

        const closeOpenTags = (openTags) => {
            return openTags.map(tag => `</${tag.name}>`).reverse().join('');
        };

        const reopenTags = (openTags) => {
            return openTags.map(tag => `<${tag.name}${tag.attributes}>`).join('');
        };

        // Build new HTML with wrapped lines
        let newHtml = '';
        let currentPos = 0;
        let currentLineNumber = 1;

        // Wrap line 1
        const firstBreakPos = lineBreaks[0].index;
        const line1Content = html.substring(0, firstBreakPos);
        const noDashClass = shouldSkipLineNumber(line1Content) ? ' no-line-number' : '';
        const multipleOf5Class = (currentLineNumber % 5 === 0) ? ' multiple-of-5' : '';

        // Track open tags at end of line 1
        const openTags1 = getOpenTags(line1Content);
        const closingTags1 = closeOpenTags(openTags1);

        newHtml += `<span class="text-line line-${currentLineNumber}${noDashClass}${multipleOf5Class}" data-line="${currentLineNumber}" aria-label="Line ${currentLineNumber}">${line1Content}${closingTags1}</span>`;

        // Skip the <br> tag itself
        currentPos = lineBreaks[0].index + lineBreaks[0].fullMatch.length;
        currentLineNumber = lineBreaks[0].lineNumber;

        // Wrap remaining lines
        for (let i = 0; i < lineBreaks.length; i++) {
            const nextBreak = lineBreaks[i + 1];
            const lineContent = nextBreak
                ? html.substring(currentPos, nextBreak.index)
                : html.substring(currentPos);

            if (lineContent.trim()) {
                // Reopen any tags that were open at the end of the previous line
                const openTagsAtLineStart = (i === 0) ? openTags1 : getOpenTags(html.substring(0, currentPos));
                const reopenedTags = reopenTags(openTagsAtLineStart);

                // Track open tags at end of current line
                const openTagsAtLineEnd = getOpenTags(html.substring(0, nextBreak ? nextBreak.index : html.length));
                const closingTags = closeOpenTags(openTagsAtLineEnd);

                // Check if line should skip line numbering
                const noDashClass = shouldSkipLineNumber(lineContent) ? ' no-line-number' : '';
                const multipleOf5Class = (currentLineNumber % 5 === 0) ? ' multiple-of-5' : '';

                newHtml += `<span class="text-line line-${currentLineNumber}${noDashClass}${multipleOf5Class}" data-line="${currentLineNumber}" aria-label="Line ${currentLineNumber}">${reopenedTags}${lineContent}${closingTags}</span>`;
            }

            if (nextBreak) {
                // Skip the <br> tag itself
                currentPos = nextBreak.index + nextBreak.fullMatch.length;
                currentLineNumber = nextBreak.lineNumber;
            }
        }

        // Update the transcription HTML
        transcription.innerHTML = newHtml;
    });
}

// Set apparatus max-height based on viewport and ab span height
function setApparatusMaxHeight() {
    const apparatus = document.querySelector('#apparatus');
    if (!apparatus) return;

    const edition = document.querySelector('#edition');
    if (!edition) return;

    // Get all ab spans and find the tallest one
    const abSpans = edition.querySelectorAll('span.ab');
    if (!abSpans || abSpans.length === 0) return;

    // Find the maximum height among all ab spans
    let maxAbHeight = 0;
    abSpans.forEach(abSpan => {
        const height = abSpan.offsetHeight;
        if (height > maxAbHeight) {
            maxAbHeight = height;
        }
    });

    // Get viewport height minus some padding (accounting for sticky top offset)
    const viewportHeight = window.innerHeight;
    const apparatusTopOffset = apparatus.getBoundingClientRect().top;
    const maxViewportHeight = viewportHeight - apparatusTopOffset - 32; // 32px for bottom padding

    // Use the smaller of the two (viewport height or tallest ab span)
    const maxHeight = Math.min(maxViewportHeight, maxAbHeight);

    // Set the max-height
    apparatus.style.maxHeight = `${maxHeight}px`;
}

// apparatus links
function handleApparatusHashOnLoad() {
    const hash = window.location.hash;

    // Only proceed if there's a hash that starts with #to-app-
    if (!hash || !hash.startsWith('#to-app-')) {
        return;
    }

    const targetId = hash.substring(1); // Remove the #

    // Find the apparatus entry with this ID (should already have active class)
    const apparatusEntry = document.getElementById(targetId);

    if (apparatusEntry) {
        // Scroll apparatus entry into view
        apparatusEntry.scrollIntoView({ behavior: 'smooth', block: 'nearest' });

        // Find the backlink (from-app-xxx) to activate in transcription
        const backLink = apparatusEntry.getAttribute('data-back-link');
        if (backLink) {
            const transcriptionElement = document.querySelector(backLink);
            if (transcriptionElement) {
                transcriptionElement.classList.add('active');

                // Scroll the transcription element into view as well
                transcriptionElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
            }
        }
    } 

    // Find the corresponding link in the transcription
    // The link will have href="#to-app-xxx"
    const correspondingLink = document.querySelector(`a.apparatus-link[href="${hash}"]`);
    if (correspondingLink) {
        // Add active class to the link
        correspondingLink.classList.add('active');
    }
}
