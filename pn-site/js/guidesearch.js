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
	    	var params = {};
	    	// mixedsearch refers to string searches of the user-defined type
	    	// i.e., not necessarily corresponding to the string-search types
	    	// defined by the interface controls
	    	var mixedsearch = false;

	    	// if a string is set for search, than the associated text, target, and option
	    	// fields must also be set.

	    	var textval = hic.buildTextSearchString();
	    	if(!textval.match(/^\s*$/)){

				  if(textval.indexOf(":") != -1) mixedsearch = true;

				  params["STRING"] = textval;
	    		const betas = document.querySelector("#betaYes");
	    		if(betas && betas.checked) params["BETA"] = true;

	    		const caps = document.querySelector("#caps");
	    		if(caps && caps.checked) params["CAPS"] = true;

	    		const marks = document.querySelectorAll("#marks:checked");
	    		if(marks.length > 0) params["MARKS"] = true;

          if(!mixedsearch){

				     params["target"] = Array.from(document.querySelectorAll("input[name='target']:checked")).map(el => el.value);

	    		}
          if (document.querySelector("#target-collection").checked) {
            params["COLLECTION"] = "current";
          }
	    	}
	    	// image filter elements
        const internals = document.querySelector("input[name='INT']");
        if(internals && internals.checked) params["INT"] = true;
        const externals = document.querySelector("input[name='EXT']");
        if(externals && externals.checked) params["EXT"] = true;
        const printpubs = document.querySelector("input[name='PRINT']");
        if(printpubs && printpubs.checked) params["PRINT"] = true;

        // has transcription
        const transc = document.querySelector("select[name='TRANSC']");
        if(transc && transc.value != "default" && !transc.disabled) params["TRANSC"] = transc.value;

        const vol = document.querySelector("#id-volume");
        if(vol.value != "" && vol.value != "n.a.") params["VOLUME"] = vol.value;

        const ident = document.querySelector("#id-idno");
        if(ident.value != "" && ident.value != "n.a.") params["IDNO"] = ident.value;

        hic.addDatesToFilteredEls("date-start-selector", params);
        hic.addDatesToFilteredEls("date-end-selector", params);

	    	var docsperpage = document.getElementById("DOCS_PER_PAGE");
	    	var docsval = docsperpage.value;
	    	if(docsval.match(/^\d{1,3}$/) && docsval > 0) params[docsperpage.name] = docsval;

	    	const hiddens = document.querySelectorAll("input[type='hidden']");
        hiddens.forEach(hidden => {

          // the hidden collection field should be overriden by settings in the
          // control itself
          if(hidden.getAttribute("name") == "COLLECTION"){

            const coll = document.querySelector("select[name='COLLECTION']");
            if(coll.value != "default" && coll.value != "current"){

              params["COLLECTION"] = coll.value;

            } else{

              params[hidden.getAttribute("name")] = hidden.getAttribute("value");

            }

          } else {
            if (hidden.getAttribute("value") != "default" && hidden.getAttribute("value") != ""){
              params[hidden.getAttribute("name")] = hidden.getAttribute("value");
            }
          }

        });

        Array.from(document.querySelectorAll("select+*.custom-combobox"))
          .map(elt => {return elt.previousElementSibling})
          .forEach(combo => {

	    		  if(combo.getAttribute("name") != "DATE_START" && combo.getAttribute("name") != "DATE_END" && !combo.disabled){

              if(combo.value != "" && combo.value != "default"){

                params[combo.getAttribute("name")] = combo.value;

              }

				    }

        });

				if (params["IDNO"]) {
          params["IDNO"] = params["IDNO"].replace(/:/g, "*");
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
      const qs = new URLSearchParams(params);
			var hrefwquery = current + "?" + qs.toString();
			window.location = hrefwquery;
			return false;
    }
    

	    hic.addDatesToFilteredEls = function(date_wrapper_name, params){

        const date_wrapper = document.getElementById(date_wrapper_name);
	    	const datefield = date_wrapper.querySelector("input");
	    	let selected_date = datefield.value;
	    	
	    	if(selected_date == "") return;
	    	selected_date = selected_date.replace(/\s*\(\d+\)\s*/g, "");	// trim count
	    	const era_finder = new RegExp(/\s*(B?CE)$/);
	    	let era = "";

	    	if(selected_date.match(era_finder)){

	    		era = era_finder.exec(selected_date)[1];
	    		selected_date = selected_date.replace(era, "").replace(/^\s*/, "").replace(/\s*$/, "");

	    	}
	    	else if(selected_date.toLowerCase() != "unknown"){

	    		selected_date = selected_date.replace(/\D/g, "");
	    		era = date_wrapper.querySelector("input[type=radio]:checked").value;

	    	}
	    	else if(selected_date.toLowerCase() == "unknown"){

	    		selected_date = "n.a.";

	    	}
	    	if(selected_date.match(/^\s*$/)) return;

	    	// date mode selector
        const datemode = document.querySelector("input[name='DATE_MODE']:checked").value;
        params["DATE_MODE"] = datemode;
	    	const date_el_name = date_wrapper_name.match("start") ? "DATE_START_TEXT" : "DATE_END_TEXT";
	    	const era_el_name = date_wrapper_name.match("start") ? "DATE_START_ERA" : "DATE_END_ERA";
        params[date_el_name] = selected_date;
        params[era_el_name] = era;

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
		$("#beta-on").on("change", (event) => {

			$(".stringsearch-top-controls:last .keyword").trigger('focus');
			var beta = $(event.target).is(":checked") ? "beta-on" : "beta-off";
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

    const params = new URLSearchParams(window.location.search);
    if (params.has("COLLECTION")) {
      if (params.get("COLLECTION") === "current") {
        document.querySelector("#target-collection").checked = true;
      }
    } else {
      document.querySelector("#target-collection").checked = true;
    }

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
