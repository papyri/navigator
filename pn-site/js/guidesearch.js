$(document).ready(

	/**
	* Various UI-related functions
	*
	*/

	function(){

		// a little namespacing

		if(typeof info == 'undefined') info = {};
		if(typeof info.papyri == 'undefined') info.papyri = {};
		if(typeof info.papyri.thill == 'undefined') info.papyri.thill = {};
		if(typeof info.papyri.thill.guidesearch == 'undefined') info.papyri.thill.guidesearch = {};

		// alias to save typing	
		var hic = info.papyri.thill.guidesearch;
		hic.HIDE_REVEAL_COOKIE = "togglestate";		// for persising show/hide search panel
		hic.BETA_COOKIE = "betacode";				// for persisting beta-as-you-type settings
		hic.SEARCH_STACK = "searchstack";			// back-button behaviour for string-search
		hic.reqd_on = {};
		hic.reqd_off = {};
		hic.selectedRadios = [];
		hic.startDateSet = false;
		hic.endDateSet = false;

		// 'Search Type' is really a proxy for setting the fields to be searched
		// and the string transformations to use in search. Certain combinations
		// of search target and string config are thus forbidden.

		// for reqd_on and reqd_off members
		// keys are name of element clicked
		// values are list of elements that *must* be 
		// switched on or off onclick.

		hic.reqd_on["target-metadata"] = ["#caps", "#marks"];
		hic.reqd_off["target-metadata"] = ["#beta-on"];
		hic.reqd_on["target-translations"] = ["#caps", "#marks"];
		hic.reqd_off["target-translations"] = ["#beta-on"];

		/**
		 * Restricts user options so that only possible string-search configurations
		 * can be set
		 * TODO: needs to be revised in wake of poor user response!
		 */

	   	hic.configureSearchSettings = function(){

	   		var val = $(this).val();

	   		if(val == "text"){

	   			$("#beta-on, #caps, #marks").prop("disabled", false);
	   			hic.checkBetacode();

	   		} 
	   		else{

	   			$("#beta-on").prop("checked", false);
	   			$("#beta-on").prop("disabled", true);
	   			$("#caps").prop("checked", true);
	   			$("#caps").prop("disabled", true);
	   			$("#marks").prop("checked", true);
	   			$("#marks").prop("disabled", true);	   			

	   		}

	    }

	    /**
	    * Without javascript, the form automatically sends values for every form field to the server -
	    * including those wtih a null or default value, leading to very long and illegible querystrings.
	    * This method strips out all default/null submitted values before passing them on to
	    * the server.
	    */

	    hic.tidyQueryString = function(){

            var querystring = "";
	    	var filteredels = [];
	    	// mixedsearch refers to string searches of the user-defined type
	    	// i.e., not necessarily corresponding to the string-search types
	    	// defined by the interface controls
	    	var mixedsearch = false;

	    	// if a string is set for search, than the associated text, target, and option
	    	// fields must also be set.

	    	var textval = hic.buildTextSearchString();
	    	if(!textval.match(/^\s*$/)){

				if(textval.indexOf(":") != -1) mixedsearch = true;

				var textel = $("<input type=\"text\" name=\"STRING\"></input>");
				textel.val(textval);
                filteredels.push(textel);
	    		var betas = $("#betaYes:checked");
	    		if(betas.length > 0) filteredels.push(betas);

	    		var caps = $("#caps:checked");
	    		if(caps.length > 0) filteredels.push(caps);

	    		var marks = $("#marks:checked");
	    		if(marks.length > 0) filteredels.push(marks);

                if(!mixedsearch){
                 
				     filteredels.push($("input[name='target']").filter(":checked"));	

	    		}


	    	}		
	    	// image filter elements
	    	var internals = $("input:checkbox[name='INT']:checked");
	    	if(internals.length > 0) filteredels.push(internals);
	    	var externals = $("input:checkbox[name='EXT']:checked");
			if(externals.length > 0) filteredels.push(externals);
			var printpubs = $("input:checkbox[name='PRINT']:checked");
			if(printpubs.length > 0) filteredels.push(printpubs);
			if($("select[name='TRANSC'] option:selected").val() != "default" && !($("select[name='TRANSC']").attr("disabled"))) filteredels.push($("select[name='TRANSC']"));

			// date mode selector
			var datemode = $("input:radio[name='DATE_MODE']:checked");
			if(datemode.length > 0) filteredels.push(datemode);


			var vol = $("#id-volume");
			var volno = vol.val();
			if(volno != "" && volno != "n.a.") filteredels.push(vol);

			var ident = $("#id-idno");
			var identno = ident.val();
			if(identno != "" && identno != "n.a.") filteredels.push(ident);

			 hic.addDatesToFilteredEls("date-start-selector", filteredels);
			 hic.addDatesToFilteredEls("date-end-selector", filteredels);

	    	var docsperpage = $("#DOCS_PER_PAGE");
	    	var docsval = docsperpage.val();
	    	if(docsval.match(/^\d{1,3}$/) && docsval > 0) filteredels.push(docsperpage);

	    	var hiddens = document.getElementsByTagName("input");

	    	for(var j = 0; j < hiddens.length; j++){

	    		var hidden = hiddens[j];
	    		var htype = hidden.getAttribute("type");
	    		// note weirdness here - jQuery cannot retrieve attributes from hidden input fields
	    		// standard js .getAttribute is thus used
	    		if(htype == "hidden"){

	    			// the hidden collection field should be overriden by settings in the
	    			// control itself
	    			if(hidden.getAttribute("name") == "COLLECTION"){

	    				var collvalue = $("select[name='COLLECTION']").prop("value");
	    				if(collvalue != "default"){

	    					filteredels.push($("select[name='COLLECTION']"));
	    					continue;

	    				}
	    				else{

	    					filteredels.push(hidden);

	    				}

	    			}
	    			else{

	    				filteredels.push(hidden);	

	    			}

	    		}

	    	}

	    	var combos = $(".custom-combobox").prev('select');

	    	for(var m = 0; m < combos.length; m++){

	    		var combo = $(combos[m]);

	    		if(combo.attr("name") != "DATE_START" && combo.attr("name") != "DATE_END" && !combo.prop("disabled")){

					var val = $(combo).val();
					if(val != "" && val != "default"){

						filteredels.push(combo);

					}

				}

	    	}

	    	var params = {};

			for(var k = 0; k < filteredels.length; k++){

				var fel = filteredels[k];
				var name = $(fel).attr("name");
				var val = $(fel).val();

				// workaround for jQuery hidden field blindness
				if(typeof name == 'undefined' || typeof val == 'undefined'){

					name = fel.attr("name");
					val = fel.val();

				}
				val = val.replace(/\s*\(\d+\)\s*$/, "");
				if(name == "IDNO") val = val.replace(/:/g, "*");
				if(!params[name] || params[name] == val){

					params[name] = val;

				} 
				else{

					var startVal = params[name];
					if($.isArray(startVal)){

						startVal.push(val);

					}
					else{
				
						var vals = new Array();
						vals.push(startVal);
						vals.push(val);
						params[name] = vals;

					}

				}

			}

			if(mixedsearch){

				params["target"] = "user_defined";

			}
				var current = window.location;
				if(current.toString().match(/\?/)) {
				var currentbits = current.toString().split("?");
				current = currentbits[0];
			}
			hic.concatenateSearchToCookie(textval);
			var qs = $.param(params);
			qs = qs.replace(/%5B%5D/g, "");
			var hrefwquery = current + "?" + qs;
			window.location = hrefwquery;
			return false;
	    }

	    hic.addDatesToFilteredEls = function(date_wrapper_name, filteredels){

	    	var date_selector = "#" + date_wrapper_name + " input";
	    	var datefield = $(date_selector);
	    	var selected_date = datefield.val();
	    	
	    	if(selected_date == "") return;
	    	selected_date = selected_date.replace(/\s*\(\d+\)\s*/g, "");	// trim count
	    	var era_finder = new RegExp(/\s*(B?CE)$/);
	    	var era = "";

	    	if(selected_date.match(era_finder)){

	    		era = era_finder.exec(selected_date)[1];
	    		selected_date = selected_date.replace(era, "").replace(/^\s*/, "").replace(/\s*$/, "");

	    	}
	    	else if(selected_date.toLowerCase() != "unknown"){

	    		selected_date = selected_date.replace(/\D/g, "");
	    		era = $(datefield.parents(".date-facet-widget").find("input[type=radio]:checked")).val();

	    	}
	    	else if(selected_date.toLowerCase() == "unknown"){

	    		selected_date = "n.a.";

	    	}
	    	if(selected_date.match(/^\s*$/)) return;

	    	// date mode selector
			var datemode = $("input:radio[name='DATE_MODE']:checked");
			if(datemode.length > 0) filteredels.push(datemode);
	    	var date_el_name = date_wrapper_name.match("start") ? "DATE_START_TEXT" : "DATE_END_TEXT";
	    	var era_el_name = date_wrapper_name.match("start") ? "DATE_START_ERA" : "DATE_END_ERA";
	    	var date_el = $("<input type=\"text\" name=\"" + date_el_name + "\"></input>");
	    	date_el.val(selected_date);
	    	filteredels.push(date_el);
	    	if(era == "") return;
	    	var era_el = $("<input type=\"radio\" name=\"" + era_el_name + "\"></input>");
	    	era_el.val(era);
	    	filteredels.push(era_el);

	    }
	    hic.buildTextSearchString = function(){

	    	var proxRegExp = new RegExp(/\s+(THEN|NEAR)\s+/);
	    	var totalSearchString = "";
	    	var stringcontrols = $(".stringsearch-top-controls");

	    	for(var i = 0; i < stringcontrols.length; i++){

	    		var keyword = $(stringcontrols[i]).find(".keyword").val();
	    		var searchString = keyword.replace(/(\s+)/g, " ");
	    		searchString = searchString.trim();	    		
	    		if(searchString.length == 0) continue;
	    		searchString = "(" + searchString + ")";
	    		if(keyword.match(proxRegExp)){

	    			var proxcount = $(stringcontrols[i]).find(".prxcount").val().match(/^\d{1,2}$/) ? $(stringcontrols[i]).find(".prxcount").val() : "1";
	    			var proxunit = $(stringcontrols[i]).find(".prxunit").val() == "words" ? "words" : "chars";	// default to 'chars'
	    			searchString += "~" + proxcount + proxunit;

	    		}	 
	    		searchString = i == 0 ? searchString : "¤" + searchString;
	    		totalSearchString += searchString;

	    	}
			totalSearchString = totalSearchString.replace(/\)¤\(OR/g, " OR");
	    	return totalSearchString;

	    }

	    hic.concatenateSearchToCookie = function(textval){

	    	var searchstack = $.cookie(hic.SEARCH_STACK) ? $.cookie(hic.SEARCH_STACK) : "";

	    	if(textval){

	    		if(searchstack.length > 1) searchstack += "|";
	    		searchstack += textval;

	    	}

	    	$.cookie(hic.SEARCH_STACK, searchstack);

	    }

	    /**
	     * Monitors the text being entered into the search box for a variety of inputs:
	     * (i)  for continuous conversion from betacode, if required
	     * (ii) for entry of a colon character, to switch into direct string search mode
	     */

	    hic.monitorTextInput = function(){

	    	$(this).off('focus');
			$(this).off('keypress');
			$(this).off('keyup');
	    	var betaOn = $("#beta-on").is(":checked");
	    	colonFound = false;
	    	var selectedRadios = [];

	    	if(betaOn){

				$(this).on('keypress', function(event){ return convertCharToggle(this, true, event); });
	    		$(this).on('keyup', function(event){ return convertStr( this, event ); });	    	    	

	    	}
	    	else{

			$(this).on('keyup', function(event){			
				event.stopPropagation();
				var val = $(this).val();
				if(!colonFound && val.match(":")) {

					colonFound = true;
					$(".stringsearch-section input:radio").prop("disabled", true);
					$(".stringsearch-section input:checkbox").prop("disabled", false);
					selectedRadios = $(".stringsearch-section input:radio:checked");
					$(".stringsearch-section input:radio:checked").prop("checked", false);

				}
				// check to make sure user hasn't deleted a previously-entered colon char
				else if(!val.match(":") && colonFound){

		            colonFound = false;
					$(".stringsearch-section input:radio").prop("disabled", false);
				    for(var i = 0; i < selectedRadios.length; i++){

                	    selectedRadios[i].trigger('click');   

			    	} 

			}

			});

	    	}

	    }

	    /***********************************************
	     START HIDE/REVEAL
	     **********************************************/

	    /**
	     * Hides search controls in order to expand result display
	     *
	     */
	    hic.hideSearch = function(evt){

			var widthcomp = $("#search-toggle").outerWidth() + 1;
			var delay = evt.data.delay;
			var currentValsWrapperLeft = $("#vals-and-records-wrapper").position().left;
			var initialHeight = $("#facet-wrapper").height();
			var initialWidgetHeight = $("#facet-widgets-wrapper").height();
			var finalHeight = initialHeight > initialWidgetHeight ? initialHeight : initialWidgetHeight;
	    	var newValsWidth = hic.getValsAndRecordsWidth("hide-search");
	    	$("#facet-wrapper").height(initialHeight);
	    	$("#facet-widgets-wrapper").animate({ left: -($("#facet-widgets-wrapper").width() + widthcomp) }, delay);
	    	window.setTimeout(function(){
	    		$(".title-long").css("display", "block")
	    		$(".title-short").css("display", "none");
	    		}, 150);
	    	$("#vals-and-records-wrapper").css({"position":"absolute", "left": currentValsWrapperLeft });
	    	$("#vals-and-records-wrapper").animate({ left: 0, width: newValsWidth }, delay, "swing",

    			function(){

	    			$("#era-selector").css("display", "none");
	    			$("#reset-all, #search").addClass("hidden-buttons");
    				$("#facet-wrapper").height(finalHeight);
    				$("#facet-widgets-wrapper").addClass("search-closed");
    				$("form[name='facets']").addClass("search-closed");
    				$("#facet-widgets-wrapper").offset({ left:-widthcomp });
    				$("#facet-widgets-wrapper").removeClass("search-open");
    					$("#search-toggle").addClass("toggle-closed");
    				$("#search-toggle").removeClass("toggle-open");
    				$("#vals-and-records-wrapper").removeClass("vals-and-records-min");
    				$("#vals-and-records-wrapper").addClass("vals-and-records-max");
    				$("#vals-and-records-wrapper").css({"position":"relative" });
    				var height = initialWidgetHeight > $("#vals-and-records-wrapper").height() ? initialWidgetHeight : $("#vals-and-records-wrapper").height();
    				$("#search-toggle-pointer").text(">>");
    				$("#search-toggle-pointer").offset({ top: ($(window).height() / 2) - 5 });	    			
    				hic.positionTogglePointer();

    			});
    	$("#search-toggle").off('click');
    	$("#search-toggle").on("click", hic.showSearch);
    	$.cookie(hic.COOKIE, 0);

   }
    
	    /**
	     * Shrinks search results in order to display search panel.
	     *
	     * TODO: Clunky. Fix.
	     */

		hic.showSearch = function(evt){

			$("#reset-all, #search").removeClass("hidden-buttons");
			var widgetWrapperWidth = 500;
			var widthcomp = $("#search-toggle").outerWidth() + 1;
			var newWidgetWidthVal = widgetWrapperWidth;
			$("#vals-and-records-wrapper").css("position", "absolute");
			$("#facet-widgets-wrapper").removeClass("search-closed");
	    	$("form[name='facets']").removeClass("search-closed");
			$("#facet-widgets-wrapper").css("left", "-" + newWidgetWidthVal + "px");
	    	$("#facet-widgets-wrapper").addClass("search-open");
			var newValsWidth = hic.getValsAndRecordsWidth("show-search");
			window.setTimeout(function(){
	    		$(".title-long").css("display", "none");
	    		$(".title-short").css("display", "block");
	    	}, 200);
			$("#facet-widgets-wrapper").animate({ left: 0 }, 325);
				    			$("#era-selector").css("display", "block");

			$("#vals-and-records-wrapper").animate({ left: newWidgetWidthVal - widthcomp, width: newValsWidth }, 325, 

				function(){

					var widgetHeight = $("#facet-widgets-wrapper").height();
					var resultsHeight = $("#vals-and-records-wrapper").height();
					var greaterHeight = widgetHeight > resultsHeight ? widgetHeight : resultsHeight;
					$("#facet-wrapper").height(greaterHeight);
					$("#search-toggle").height($("#facet-wrapper").height());
	    			$("#search-toggle").removeClass("toggle-closed");
	    			$("#search-toggle").addClass("toggle-open");
	    			$("#vals-and-records-wrapper").addClass("vals-and-records-min");
	    			$("#vals-and-records-wrapper").removeClass("vals-and-records-max");	
	    			$("#search-toggle-pointer").text("<<");
	    			hic.positionTogglePointer();

				}

			);
			$("#search-toggle").off('click');
			$("#search-toggle").on("click", { delay:325 }, hic.hideSearch);
			$.cookie(hic.COOKIE, null);

		}

		hic.getValsAndRecordsWidth = function(direction){

		      var fullWidth = $(window).width();
		      var searchWidth = (direction == "hide-search") ? $("#search-togger").outerWidth() + 1 : 500;
		      var ownMargin = 23;
		      var widgetPadding = 25;
		      return fullWidth - searchWidth - ownMargin - widgetPadding - 1;

		}

		hic.positionTogglePointer = function(){

			$("#search-toggle-pointer").offset({ top: ($(window).height() / 2) - 5 });

		}

	    /***********************************************
	     END HIDE/REVEAL
	     **********************************************/

		/**
		 * Ensures that date era selector change events do not trigger submit unless a date
		 * value has been entered
		 *
		 */
		$("input[name='after-era'], input[name='before-era']").on("change", () => {

	    	var correlatedText = $(this).attr("name") == 'after-era' ? 'DATE_START' : 'DATE_END';
	    	var correlatedTextInput = $("input[name='" + correlatedText + "']");
	    	var correlatedValue = correlatedTextInput.val();
	    	if(correlatedValue == "" || correlatedValue == "n.a.") return false;	
	    	correlatedTextInput.val(correlatedValue.replace(/\s*\(\d+\)[\s]*$/g, "").replace(/\D/g, "").trim());
			$("form[name='facets']").trigger("submit");

		});

		/**
		 * Ensures that 'n.a.' style disappears when the user enters new
		 * values manually
		 *
		 */
		$("input#DATE_START_TEXT, input#DATE_END_TEXT").on("focus", () => {

			$(this).css("font-style", "normal");

		});

		/**
		 * Applies special styling to 'n.a.' value in DATE_START_TEXT and
		 * DATE_END_TEXT controls
		 *
		 */
		$("input#DATE_START_TEXT, input#DATE_END_TEXT").on("blur", () => {

			if($(this).val() == "n.a."){

				$(this).css("font-style", "italic");

			}

		});

		/**
		 * Autocomplete functionality for volume text input
		 */
		$("#id-volume").autocomplete({

			source: $("#volume-autocomplete").text().split(' ').sort(function(a,b){return a - b;}),
			select: function(event, ui){ 

				$("#id-volume").val(ui.item.value);
				hic.tidyQueryString();

			}
		});

		/**
		 * Autocomplete functionality for id number text input
		 */
		$("#id-idno").autocomplete({

			source: $("#idno-autocomplete").text().split(' '),
			select: function(event, ui){

				$("#id-idno").val(ui.item.value);
				hic.tidyQueryString();
			}

		});

		/**
		 * Autocomplete for provenance
		 */

		$("#id-place").autocomplete({

			//source: $("#place-autocomplete").text().split('¤'),
			source: function(req, responseFn){

				var re = $.ui.autocomplete.escapeRegex(req.term);
				var matcher = new RegExp( "^" + re, "i" );
				var a = $.grep(  $("#place-autocomplete").text().split('¤'), function(item,index){ return matcher.test(item);});
        		responseFn( a );				

			},
			select: function(event, ui){

				$("#id-place").val(ui.item.value);
				hic.tidyQueryString();
			}

		});

		hic.isSubsequentPage = function(){

			var pageno = decodeURI((RegExp('page=(\\d+)(&|$)').exec(location.search)||[,null])[1]);
			if(pageno != 'null') return true;
			return false;

		}

		hic.checkBetacode = function(){

			if($.cookie(hic.BETA_COOKIE) == "beta-on"){

				$("#beta-on").prop("checked", true);

			} else {

				$("#beta-on").prop("checked", false);

			}		

		}

		$("#text-search-widget").find("input[name='target']").on("click", hic.configureSearchSettings);
		// select substring as default
		$("#substring").trigger('click');
		$(".toggle-open").on("click", { delay: 325 }, hic.hideSearch);
		$(".toggle-closed").on("click", hic.showSearch);		
		$("#vals-and-records-wrapper").width(hic.getValsAndRecordsWidth("init"));
		hic.positionTogglePointer();
		$("#search-toggle").height($("#facet-wrapper").height());
		// changing date mode causes tidy and submit
		$("input:radio[name='DATE_MODE']").on("change", hic.tidyQueryString);
		// turning betacode on/off selects text input
		$("#beta-on").on("change", () => {

			$(".stringsearch-top-controls:last .keyword").trigger('focus');
			var beta = $(this).is(":checked") ? "beta-on" : "beta-off";
			$.cookie(hic.BETA_COOKIE, beta);

		});
		// entry into string search triggers text monitoring 
		$("#text-search-widget").on("focus", ".stringsearch-top-controls:last .keyword", hic.monitorTextInput);

		//$(".stringsearch-top-controls:last .keyword").live("blur", function(){ $("#keyword").focus(hic.monitorTextInput) });
		// submit triggers tidy ...
		$("form[name='facets']").on("submit", hic.tidyQueryString);
		// ... unless checks need to be in place first
		$("form select").not("select[name='DATE_START']").not("select[name='DATE_START_ERA']").not("select[name='DATE_END']").not("select[name='DATE_END_ERA']").not("select[name='prxunit']").on("change", hic.tidyQueryString);

		// sets cookie on click to record to allow reversion to current search results
		$("td.identifier a").on("click", (e) => {  hic.setCookie("lbpersist", window.location.search, 12); return true; });
		$("#reset-all").on("click", (e) => {

			$.cookie(hic.SEARCH_STACK, null);
			return true;

		});
		hic.checkBetacode();
		if($.cookie(hic.HIDE_REVEAL_COOKIE) == 0 && hic.isSubsequentPage()){

			var e = {};
			e.data = {};
			e.data.delay = 0;
			hic.hideSearch(e);

		}

		$("select[name='SERIES']").combobox();
		$("select[name='COLLECTION']").combobox();
		$("select[name='AUTHOR']").combobox();
		$("select[name='WORK']").combobox();
		$("select[name='PLACE']").combobox();
		$("select[name='NOME']").combobox();
		$("select[name='DATE_START']").combobox();
		$("select[name='DATE_END']").combobox();
		$("select[name='LANG']").combobox();
		$("select[name='TRANSL']").combobox();
		$(".custom-combobox").on("click", (evt) => {

      const actionTarget = evt.target;
			$(actionTarget).val("");	
			var button = $(actionTarget).next("a");
			button.css("outline-color", $(actionTarget).css("outline-color"));
			button.css("outline-width", $(actionTarget).css("outline-width"));
			button.css("outline-style", $(actionTarget).css("outline-style"));

		});
		$(".custom-combobox").on("blur", (evt) => {

      const actionTarget = evt.target;
			var button = $(actionTarget).next("a");
			button.css("outline","none");


		});
		$(".custom-combobox").on("focus", (evt) => {

      const actionTarget = evt.target;
			var button = $(actionTarget).next("a");
			button.css("outline-color", $(actionTarget).css("outline-color"));
			button.css("outline-width", $(actionTarget).css("outline-width"));
			button.css("outline-style", $(actionTarget).css("outline-style"));		

		});
		
		$("input[name=DATE_START]").on("focus", (evt) => {
			
			hic.startDateSet = true;
			$(evt.target).off("focus");
		
		});
		
		$("input[name=DATE_START]").on("click", (evt) => {
			
			$("input:radio[name=after-era]").prop("disabled", false);
		
		});

		$("input[name=DATE_END]").on("click", (evt) => {
			
			$("input:radio[name=before-era]").prop("disabled", false);

		});
		
		$("input[name=DATE_START]").on("blur", (evt) => {
			
			var val = $(evt.target).val();
			if(val == "" || val == "default") $("input:radio[name=after-era]").attr("disabled", "");

		});

		$("input[name=DATE_END]").on("blur", (evt) => {
			
			var val = $(evt.target).val();
			if(val == "" || val == "default") $("input:radio[name=before-era]").attr("disabled", "");
		
		});
		

		$("input[name=DATE_END]").on("focus", (evt) => {
			
			hic.endDateSet = true;
			$(evt.target).off("focus");
		
		});
		
		$("input:radio[name=after-era]").on("change", (evt) => {
		
			if($("input[name=DATE_START]").val() != "default" && $("input[name=DATE_START]").val() != "Unknown"){
			
				hic.startDateSet = true;
				$(evt.target).off("change");
				
			}
			
		});
		
	    $("input:radio[name=before-era]").on("change", (evt) => {
		
			if($("input[name=DATE_END]").val() != "default" && $("input[name=DATE_END]").val() != "Unknown"){
			
				hic.endDateSet = true;
				$(evt.target).off("change");
				
			}
			
		});
		
		$("input:radio[name=DATE_MODE]").on("change", (evt) => {
		
			if($("input[name=DATE_START]").val() != "default" && $("input[name=DATE_START]").val() != "Unknown"){
			
				hic.startDateSet = true;
				$(evt.target).off("change");
				
			}		
			if($("input[name=DATE_END]").val() != "default" && $("input[name=DATE_END]").val() != "Unknown"){	
			
				hic.endDateSet = true;
				$(evt.target).off("change");
				
			}		
			
		
		});

	}



);
