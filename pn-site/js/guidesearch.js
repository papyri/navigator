$(document).ready(

  /**
  * Various UI-related functions
  *
  */

  function(){

    // TomSelect combox/typeahead element initialiation. See
    // https://tom-select.js.org/ for docs.

    // Helper function to fix aria-labelledby attribute on TomSelect initialization.
    const fixAriaLabel = function() {
      const elementId = this.input.id;
      if (elementId) {
        this.input.setAttribute('aria-labelledby', elementId + '-ts-label');
      }
    };

    new TomSelect("#id-collection",{
      sortField: {
        field: "text",
        direction: "asc"
      },
      onInitialize: fixAriaLabel
    });
    new TomSelect("#id-series",{
      sortField: {
        field: "text",
        direction: "asc"
      },
      onInitialize: fixAriaLabel
    });

    new TomSelect("#id-author", {
      onInitialize: fixAriaLabel
    });
    new TomSelect("#id-work", {
      onInitialize: fixAriaLabel
    });
    new TomSelect("#id-place", {
      onInitialize: fixAriaLabel
    });
    new TomSelect("#id-nome", {
      onInitialize: fixAriaLabel
    });
    new TomSelect("#id-transc", {
      onInitialize: fixAriaLabel
    });

    // Shared configuration object for date selectors
    const dateSelectConfig = {
      maxItems: 1,
      create: true,
      createOnBlur: true,
      persist: false,
      onInitialize: fixAriaLabel,
      createFilter: (input) => {
        // Allow freeform input of either:
        // 1. A zero/positive integer (just digits) optionally followed by a space and BCE/CE
        // 2. A negative integer (dash followed by digits) without BCE/CE suffix
        return input.match(/^\d+\s*(BCE|CE)?$/i) || input.match(/^\-\d+$/);
      },

      onOptionAdd(value, data) {

        // If value does not end in "BCE" or "CE", and isn't unknown, assume CE
        if (!value.endsWith("BCE") && !value.endsWith("CE") && value.toLowerCase() !== "unknown") {
          fixedValue = value + " CE";
          data.text = fixedValue;
          this.updateOption(value, data);
        }

        // If value ends in "BCE" we need to change its value to a negative number without the BCE suffix
        if (value.endsWith("BCE")) {
          const numericPart = value.replace(/BCE$/i, "").trim();
          fixedValue = "-" + numericPart;
          data.value = fixedValue;
        }

        // If value is a negative number, append " BCE" to its text
        if (value.match(/^\-\d+$/)) {
          const numericPart = value.replace(/^\-/, "").trim();
          fixedValue = numericPart + " BCE";
          data.text = fixedValue;
        }
      }
    };

    // Apply the shared config to both date selectors
    new TomSelect("#id-date-start", dateSelectConfig);
    new TomSelect("#id-date-end", dateSelectConfig);

    new TomSelect("#id-lang", {
      onInitialize: fixAriaLabel
    });
    new TomSelect("#id-transl", {
      onInitialize: fixAriaLabel
    });

    // Create TomSelect widgets for Volume and ID input fields ONLY IF they have
    // autocomplete lists available, otherwise they are just free text input fields
    const createTomSelectFromAutocomplete = (autocompleteId, selectId) => {
      const autocompleteElement = document.querySelector(autocompleteId);
      if (autocompleteElement &&
        autocompleteElement.getAttribute("data-list") &&
        autocompleteElement.getAttribute("data-list").trim() !== "") {

        new TomSelect(selectId, {
          maxItems: 1,
          create: true,
          createOnBlur: true,
          persist: false,
          allowEmptyOption: true,
          onInitialize: fixAriaLabel,
          options: (function() {
            const values = autocompleteElement.getAttribute("data-list").split(" ");
            return values.map(value => ({ value: value, text: value }));
          })()
        });
      }
    };
    createTomSelectFromAutocomplete("#volume-autocomplete", "#id-volume");
    createTomSelectFromAutocomplete("#idno-autocomplete", "#id-idno");

    // a little namespacing

    if(typeof info == 'undefined') info = {};
    if(typeof info.papyri == 'undefined') info.papyri = {};
    if(typeof info.papyri.thill == 'undefined') info.papyri.thill = {};
    if(typeof info.papyri.thill.guidesearch == 'undefined') info.papyri.thill.guidesearch = {};

    // alias to save typing
    var hic = info.papyri.thill.guidesearch;
    hic.HIDE_REVEAL_COOKIE = "togglestate";    // for persising show/hide search panel
    hic.BETA_COOKIE = "betacode";        // for persisting beta-as-you-type settings
    hic.SEARCH_STACK = "searchstack";      // back-button behaviour for string-search
    hic.reqd_on = {};
    hic.reqd_off = {};
    hic.selectedRadios = [];

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
    * including those with a null or default value, leading to very long and illegible querystrings.
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
          } else {
            params["COLLECTION"] = "editions";
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

          // the hidden collection field should be overridden by settings in the
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

        Array.from(document.querySelectorAll("select.form-select"))
          .map(elt => {return elt})
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
        const datefield = date_wrapper.querySelector("select");
        let selected_date = datefield.value;

        if(selected_date == "") return;
        selected_date = selected_date.replace(/\s*\(\d+\)\s*/g, "");  // trim count
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
            var proxunit = $(stringcontrols[i]).find(".prxunit").val() == "words" ? "words" : "chars";  // default to 'chars'
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


    // Helper function to set (hidden) era radio button based on date value
    const setEraFromDateValue = (dateValue, eraRadioName) => {
      const cleanValue = dateValue.replace(/\s*\(\d+\)\s*/g, ""); // trim count
      const eraRadios = document.getElementsByName(eraRadioName);

      let targetEra = null;
      if (cleanValue.match(/^\-\d+$/)) {
        targetEra = "BCE"; // Negative integer
      } else if (cleanValue.match(/^\d+/)) {
        targetEra = "CE"; // Positive integer
      }

      if (targetEra) {
        for (const radio of eraRadios) {
          if (radio.value === targetEra) {
            radio.checked = true;
            break;
          }
        }
      }
    };

    // If the selected value for select#id-date-start or select#id-date-end
    // has changed, submit the form
    $("select#id-date-start, select#id-date-end").on("change", (event) => {
      const selectElement = event.target;
      const isStartDate = selectElement.id === "id-date-start";
      const eraRadioName = isStartDate ? "after-era" : "before-era";

      setEraFromDateValue(selectElement.value, eraRadioName);

      $("form[name='facets']").trigger("submit");
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

    // changing date mode causes tidy and submit

    // turning betacode on/off selects text input
    $("#beta-on").on("change", (event) => {

      $(".stringsearch-top-controls:last .keyword").trigger('focus');
      var beta = $(event.target).is(":checked") ? "beta-on" : "beta-off";
      $.cookie(hic.BETA_COOKIE, beta);

    });
    // entry into string search triggers text monitoring
    $("#text-search-widget").on("focus", ".stringsearch-top-controls:last .keyword", hic.monitorTextInput);

    // submit triggers tidy ...
    $("form[name='facets']").on("submit", hic.tidyQueryString);

    // ... unless checks need to be in place first
    $("form select").not("select[name='DATE_START']").not("select[name='DATE_START_ERA']").not("select[name='DATE_END']").not("select[name='DATE_END_ERA']").not("select[name='prxunit']").on("change", function(){
      hic.tidyQueryString();
    });

    // sets cookie on click to record to allow reversion to current search results
    $("td.identifier a").on("click", (e) => {  hic.setCookie("lbpersist", window.location.search, 12); return true; });
    $("a.reset-all").on("click", (e) => {

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

    // Changing either date field causes form submit if either field has a value
    $("input:radio[name=DATE_MODE]").on("change", (evt) => {
      // if either date field has a value, submit the form
      const startDateVal = $("select#id-date-start").val();
      const endDateVal = $("select#id-date-end").val();
      if ((startDateVal && startDateVal !== "default" && startDateVal !== "Unknown") ||
        (endDateVal && endDateVal !== "default" && endDateVal !== "Unknown")) {
        $("form[name='facets']").trigger("submit");
      }

    });

    // Search results pagination: insert ellipses wherever
    // extraneous page links were hidden by CSS
    $("#results-pagination li.page-item").each(function(){
      if ($(this).css("display") === "list-item") {
        var nextItem = $(this).next("li.page-item");
        if (nextItem.css("display") === "none") {
          $(this).after("<li class='page-item disabled' style='display: list-item !important'><a class='page-link disabled' disabled>...</a></li>");
        }
      }
    });

    $(document).ready(function () {


      // Handle sidebar toggle
      $('#sidebarContent').on('show.bs.collapse', function () {
        $('#toggleIcon').css('transform', 'rotate(0deg)');
        $('#sidebar').removeClass('sidebar-collapsed').addClass('col-lg-4').removeClass('col-lg-auto');
        $('#toggleNavLabel').text('Hide Filters');
        $('#toggleNavButton').attr('aria-label', 'Hide Filters');
      });

      $('#sidebarContent').on('hide.bs.collapse', function () {
        $('#toggleIcon').css('transform', 'rotate(180deg)');
        $('#sidebar').addClass('sidebar-collapsed').removeClass('col-lg-4').addClass('col-lg-auto');
        $('#toggleNavLabel').text('Show Filters');
        $('#toggleNavButton').attr('aria-label', 'Show Filters');
      });

      // Jump to page functionality
      const handlePageJump = () => {
        const input = document.getElementById('page-jump');
        const pageNum = parseInt(input.value);
        const pageMax = parseInt(input.getAttribute('max'));

        if (!/^\d+$/.test(pageNum) || pageNum < 1 || pageNum > pageMax) {
          input.setCustomValidity("Please enter a whole number between 1 and " + pageMax + ".");
          input.reportValidity();
        } else {
          input.setCustomValidity("");
          var url = new URL(window.location.href);
          url.searchParams.set('page', pageNum);
          window.location.href = url.toString();
        }
      };

      $('input#page-jump').on('keypress', function (e) {
        if (e.which === 13) { // Enter key
          e.preventDefault();
          handlePageJump();
        }
      });

      $('button#page-jump-go').on('click', function (e) {
        e.preventDefault();
        handlePageJump();
      });

    });

  }

);
