function init() {
    
		// fix highlight bug in title (for searches)
    if (document.title.includes('<mark')) {
        document.title = document.title.replace(/<mark[^>]*>/g, '').replace(/<\/mark>/g, '');
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
		initLineNumberVisibility();
		initApparatusDetailsToggle();
		initBootstrapScrollSpy();
		initSidebar();
		initBackToTop();
    setCollection();

		// Initialize transformation for /current/ and /editions/ pages
		if (window.location.pathname.includes('/current/') || window.location.pathname.includes('/editions/')) {

					// Add resize listener to recalculate apparatus max-height
					/*
					let resizeTimer;
					window.addEventListener('resize', () => {
							clearTimeout(resizeTimer);
							resizeTimer = setTimeout(() => {
									setApparatusMaxHeight();
							}, 250);
					});
					*/

					// Fade in the #edition element
					const editions = document.querySelectorAll('#edition');
					if (editions) {
							editions.forEach(edition => edition.classList.add('ready'));
					}
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

	const position = jQuery(document).getUrlParam("p");
	const total = jQuery(document).getUrlParam("t");
  const urlParams = new URLSearchParams(decodeURIComponent(window.location.search));
	const rows = urlParams.get("rows")
	let qs = buildSolrQueryString();
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

	var querystring = decodeURIComponent(window.location.search);
	if(!querystring.match(/fq=/)) return "";
	// get rid of values not used by Solr: t, d, and q (which is used only for highlighting)
	querystring = querystring.replace(/[&?]t=\d+/, "");
	querystring = querystring.replace(/[&?]p=\d+/, "");
	const urlParams = new URLSearchParams(decodeURIComponent(window.location.search));
	const highlightstring = urlParams.get("q");
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

	// Hide pagination if both previous and next are disabled
	if(prevRecord == null && nextRecord == null){
		htmlWrapper.addClass("d-none");
	}

	// Wrap pagination and canonical-uri in a wrapper div
	var utilityWrapper = jQuery("<div class=\"utility-content-wrapper\"></div>");
	var canonicalUri = jQuery("#canonical-uri");
	canonicalUri.before(utilityWrapper);
	utilityWrapper.append(pageNavWrapper);
	utilityWrapper.append(canonicalUri);

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

function setCollection() {
  const params = new URLSearchParams(window.location.search);
  const collection = params.get("COLLECTION");
  if (collection && document.querySelector("#target-collection-current")) {
    switch (collection) {
      case "current":
        document.querySelector("#target-collection-current").checked = true;
        break;
      case "editions":
        document.querySelector("#target-collection-historical").checked = true;
        break;
      default:
        document.querySelector("#target-collection-all").checked = true;
    }
  } else if (document.querySelector("#target-collection-current")) {
    document.querySelector("#target-collection-current").checked = true;
  }
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

function initBootstrapScrollSpy() {
	console.log("Initializing Bootstrap ScrollSpy");
	// Initialize Bootstrap ScrollSpy for the record page navigation
	const scrollSpy = new bootstrap.ScrollSpy(document.body, {
		target: '#controls',
		smoothScroll: false, // Let CSS scroll-margin-top handle smooth scrolling and offset
		rootMargin: '0px 0px -25%',
		// Bootstrap bug fix, see:
		// https://github.com/twbs/bootstrap/pull/41016
		threshold: [0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1]
	});
}


/**
 * Allows for line number display on hover of text lines
 */

function initLineNumberVisibility() {
	// Select all line number spans with initially-hidden class inside #edition
	const lineNumberSpans = document.querySelectorAll('#edition span.linenumber.initially-hidden');

	lineNumberSpans.forEach(span => {
		// Get the parent text-line div
		const textLineDiv = span.closest('.text-line');
		if (!textLineDiv) return;

		// Mouse events on the text-line div
		textLineDiv.addEventListener('mouseenter', () => {
			span.classList.remove('initially-hidden');
		});

		textLineDiv.addEventListener('mouseleave', () => {
			span.classList.add('initially-hidden');
		});

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

// Wire up breadcrumb copy buttons
document.addEventListener('DOMContentLoaded', function() {
    document.querySelectorAll('.breadcrumb-copy-btn').forEach(function(btn) {
        const originalTitle = btn.getAttribute('title') || 'Copy citation';
        let tooltip = bootstrap.Tooltip.getOrCreateInstance(btn);

				btn.addEventListener('click', function () {
					const ol = btn.closest('nav.breadcrumb')?.querySelector('ol.breadcrumb');
					if (!ol) return;
					const clone = ol.cloneNode(true);
					clone.querySelectorAll('.visually-hidden, .breadcrumb-copy').forEach(function (n) { n.remove(); });
					const text = clone.textContent.replace(/\s+/g, ' ').trim();

					navigator.clipboard.writeText(text).then(function () {
						const existingTipId = btn.getAttribute('aria-describedby');
						if (existingTipId) document.getElementById(existingTipId)?.remove();
						tooltip.dispose();

						const icon = btn.querySelector('i');
						if (icon) icon.classList.replace('bi-copy', 'bi-check-lg');

						btn.setAttribute('title', 'Copied!');

						setTimeout(function () {
							tooltip = new bootstrap.Tooltip(btn);
							tooltip.show();

							setTimeout(function () {
								tooltip.hide();
								setTimeout(function () {
									tooltip.dispose();
									btn.setAttribute('title', originalTitle);
									tooltip = new bootstrap.Tooltip(btn);
									if (icon) icon.classList.replace('bi-check-lg', 'bi-copy');
								}, 200);
							}, 1500);
						}, 50);
					});
				});

    });
});

// Listen for hash changes and update highlighting
window.addEventListener('hashchange', function() {
    highlightHash();
});

// Initialize sticky navigation observer so we can add "stuck"
// class when nav is stuck to top of viewport
document.addEventListener("DOMContentLoaded", () => {
  const stickyElements = document.querySelectorAll(".sticky-top");

  stickyElements.forEach((el) => {
    const sentinel = document.createElement("div");
    sentinel.className = "sticky-sentinel";
    el.parentNode.insertBefore(sentinel, el);

    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.boundingClientRect.top < 0) {
          el.classList.add("stuck");
        } else {
          el.classList.remove("stuck");
        }
      },
      {
        //threshold: [0],
        //rootMargin: `0px 0px 0px 0px`
      }
    );

    observer.observe(sentinel);
  });
});

/******************************************/
/** Apparatus / Transcription UI scripts **/
/******************************************/


/**
 * Initialize the details toggle for apparatus
 * Shows/hides detailed apparatus explanations with fade effect
 * Uses event delegation to work regardless of where apparatus content is moved
 */
function initApparatusDetailsToggle() {
    // Remove any existing event listener to prevent duplicates
    document.removeEventListener('change', handleDetailsToggle);

    // Add event listener to document with delegation
    document.addEventListener('change', handleDetailsToggle);
}

function handleDetailsToggle(event) {
    // Check if the changed element is our details toggle
    if (event.target.id === 'detailsToggle') {
        const toggle = event.target;
        const details = document.querySelectorAll('.apparatus-detail');

        if (toggle.checked) {
            // Toggle ON: Show details with fade in
            details.forEach(detail => {
                detail.classList.remove('visually-hidden');
                // Use jQuery for fade animation
                jQuery(detail).hide().fadeIn(300);
            });
        } else {
            // Toggle OFF: Hide details with fade out
            details.forEach(detail => {
                jQuery(detail).fadeOut(300, function() {
                    detail.classList.add('visually-hidden');
                });
            });
        }
    }
}


function initSidebar() {
	const sidebarSelect = document.getElementById('sidebar-content-select');
	const sidebar = document.getElementById('sidebar');
	const apparatus = document.getElementById('apparatus');
	const apparatusUnder = document.getElementById('apparatus-under');
	const translationUnder = document.getElementById('translations');
	// const commentaryUnder = document.getElementById('TBD');

	if (!sidebarSelect || !sidebar) {
		return; // Required elements don't exist
	}

	// Store original apparatus content for moving between locations
	let originalApparatusContent = apparatus ? apparatus.innerHTML : '';

	function updateSidebarContent(selectedValue) {
		if (!sidebar) return;

		// Show sidebar by default (will be hidden for 'no-sidebar')
		sidebar.style.display = '';

		switch (selectedValue) {
			
			case 'no-sidebar':
				// Hide sidebar and move apparatus content under
				sidebar.style.display = 'none';
				if (apparatus && apparatusUnder) {
					apparatusUnder.innerHTML = originalApparatusContent;
					apparatusUnder.style.display = '';
				}

				// Show commentary under
				// if (commentaryUnder) {
				// 	commentaryUnder.innerHTML = originalCommentaryContent;
				// 	commentaryUnder.style.display = '';
				// }
				
				// show translation under
				if (translationUnder) {
					translationUnder.style.display = '';
				}
				break;

			case 'apparatus':
				// Show apparatus in sidebar and hide it under
				if (apparatus && apparatusUnder) {
					apparatus.innerHTML = originalApparatusContent;
					apparatusUnder.innerHTML = '';
					apparatusUnder.style.display = 'none';
				}

				// Show commentary under
				// if (commentaryUnder) {
				// 	commentaryUnder.innerHTML = originalCommentaryContent;
				// 	commentaryUnder.style.display = '';
				// }
				
				// show translation under
				if (translationUnder) {
					translationUnder.style.display = '';
				}
				sidebar.innerHTML = apparatus ? apparatus.outerHTML : '';
				break;

			case 'commentary':
				// Show commentary in sidebar
				const commentaryElement = document.getElementById('commentary');
				if (commentaryElement) {
					sidebar.innerHTML = commentaryElement.outerHTML;
					
					// Show apparatus under
					if (apparatusUnder) {
						apparatusUnder.innerHTML = originalApparatusContent;
						apparatusUnder.style.display = '';
					}

					// hide commentary under
					// if (commentaryUnder) {
					// 	commentaryUnder.style.display = 'none';
					// }

					// show translation under
					if (translationUnder) {
						translationUnder.style.display = '';
					}
				}
				break;

			default:
				// Show translation content in sidebar (e.g., "11853-2")
				const translationElement = document.getElementById(`translation-${selectedValue}`);
				if (translationElement) {
					sidebar.innerHTML = translationElement.outerHTML;
					// Show apparatus under
					if (apparatusUnder) {
						apparatusUnder.innerHTML = originalApparatusContent;
						apparatusUnder.style.display = '';
					}

					// Show commentary under
					// if (commentaryUnder) {
					// 	commentaryUnder.innerHTML = originalCommentaryContent;
					// 	commentaryUnder.style.display = '';
					// }

					// hide translation under
					if (translationUnder) {
						translationUnder.style.display = 'none';
					}
				}
				break;
		}
	}

	// Handle select change
	sidebarSelect.addEventListener('change', function() {
		updateSidebarContent(this.value);

		// Scroll to the #text element
		const textElement = document.getElementById('text');
		if (textElement) {
			textElement.scrollIntoView({ behavior: 'smooth' });
		}
	});

	// Initialize with current selection
	updateSidebarContent(sidebarSelect.value);
}


/**
 * Initialize the back-to-top button functionality
 * Shows/hides buttons based on scroll position and handles smooth scroll to top
 * Works with any element that has the .back-to-top class
 */
function initBackToTop() {
    const backToTopButtons = document.querySelectorAll('.btn-back-to-top');

    if (backToTopButtons.length === 0) {
        return; // No back-to-top buttons exist on this page
    }

    // Show/hide buttons based on scroll position
    function toggleBackToTopButtons() {
        const shouldShow = window.pageYOffset > 300; // Show after scrolling 300px

        backToTopButtons.forEach(button => {
            // Skip buttons with 'in-controls-nav' class - they should always be visible
            if (button.classList.contains('in-controls-nav')) {
                return;
            }

            if (shouldShow) {
                button.classList.add('show');
            } else {
                button.classList.remove('show');
            }
        });
    }

    // Scroll to top when button is clicked
    function scrollToTop() {
        // Hide any visible Bootstrap tooltips before scrolling
        document.querySelectorAll('[data-bs-toggle="tooltip"]').forEach(trigger => {
            const tooltip = bootstrap.Tooltip.getInstance(trigger);
            if (tooltip) tooltip.hide();
        });

        window.scrollTo({
            top: 0,
            behavior: 'smooth'
        });
    }

    // Add event listeners to all back-to-top buttons
    backToTopButtons.forEach(button => {
        button.addEventListener('click', scrollToTop);
    });

    // Add scroll listener
    window.addEventListener('scroll', toggleBackToTopButtons);

    // Initial check in case page is already scrolled
    toggleBackToTopButtons();
}
