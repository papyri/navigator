$(document).ready(

	/**
	* Functions related to UI text-search controls
	*
	* Note that the main issue is which buttons should be (en|dis)abled depending
	* on the text in the input box. Accordingly, the general logic of the functions
	* grouped here is:
	* (1) define a number of filter functions which return booleans
	* (2) create a data structure (hic.activationRules) associating the appropriate set of
	*     filter functions with the name of each button
	* (3) Each time the text in the input box changes, iterate through the buttons,
	*     testing each against the required filter functions, and (en|dis)able each
	*     accordingly
	*
	* Note that this is not the *entire* functionality of the functions grouped here; 
	* just the bits where the flow is hard to follow. Specific points are made ad loc.
	*/

	function(){
					
		// namespacing
		
		if(typeof info == 'undefined') info = {};
		if(typeof info.papyri == 'undefined') info.papyri = {};
		if(typeof info.papyri.thill == 'undefined') info.papyri.thill = {};
		if(typeof info.papyri.thill.stringsearch == 'undefined') info.papyri.thill.stringsearch = {};
		var hic = info.papyri.thill.stringsearch;
		
		// first, we make a copy of the entire text-search control block
		// for easy repetition when users want to add controls
		// (see hic.addNewClause function)
		
		var topSelectorID = "stringsearch-top-controls";
		var topSelector = "." + topSelectorID;
		var lastTopSelector = topSelector + ":last";
		var w = $(topSelector);
		var searchHTML = $(w.clone());

		// next, we define a bunch of regexes for use in the various
		// filtration functions
		
		var whitespaceOnlyRegExp = new RegExp(/^\s*$/);
		var lastWordIsKeywordRegExp = new RegExp(/(AND|OR|NOT|(LEX(:?))|THEN|NEAR)\s*$/);
		var lastWordIsLexNotAndNearOrRegExp = new RegExp(/(NOT|AND|NEAR|LEX|OR)\s*$/);
		var lastWordIsKeywordNotNotRegExp = new RegExp(/(AND|OR|LEX|THEN|NEAR)\s*$/);
		var alreadyContainsProximityRegExp = new RegExp(/(THEN|NEXT)/);
		var notNotPrecededRegExp = new RegExp(/^\s*NOT\s*$/);
		var proxCountRegExp = new RegExp(/^\d{1,2}$/);
		var proxRegExp = new RegExp(/(THEN|NEAR)\s+\S+/);
		
		/*************************
		 * filtration functions  *
		**************************/
		
		hic.textBoxIsEmpty = function(boxval, controls){
		
			if(boxval.match(whitespaceOnlyRegExp)) return true;
			return false;
		
		}
		
		hic.lastWordIsKeyword = function(boxval, controls){
		
			if(boxval.match(lastWordIsKeywordRegExp)) return true;
			return false;
		
		}
		
		hic.lastWordIsNotOrLexOrAndOrNear = function(boxval, controls){
		
			if(boxval.match(lastWordIsLexNotAndNearOrRegExp)) return true;
			return false;		
		
		}
		
		hic.lastWordIsKeywordOtherThanNot = function(boxval, controls){
		
			if(boxval.match(lastWordIsKeywordNotNotRegExp)) return true;
			return false;		
		
		}
		
		hic.alreadyContainsProximity = function(boxval, controls){
		
			if(boxval.match(alreadyContainsProximityRegExp)) return true;
			return false;
		
		}
		
		hic.notAloneNotPreceded = function(boxval, controls){
		
			if(boxval.match(notNotPrecededRegExp)) return true;
			return false;
		
		}
		
		hic.isAwaitingProximityInput = function(boxval, controls){
		
			if(boxval.match(proxRegExp)){
			
				var prxcount = $(controls).find(".prxcount").val();
				if(!prxcount.match(proxCountRegExp)) return true;
				var prxunit = $(controls).find(".prxunit").val();
				if(prxunit == 'none') return true;
			
			}
			return false;
		
		}
		
		hic.onlyOneTextInput = function(boxval, controls){
		
			return $(topSelector).length == 1;
		
		}
		
		/****************************
		 * end filtration functions *
		*****************************/
		
		// build filtration data structure
		
		hic.activationRules = {};
		hic.activationRules["AND"] = [hic.textBoxIsEmpty, hic.lastWordIsKeyword, hic.isAwaitingProximityInput];
		hic.activationRules["OR"] = [hic.textBoxIsEmpty, hic.lastWordIsKeyword, hic.isAwaitingProximityInput];
		hic.activationRules["NOT"] = [hic.lastWordIsNotOrLexOrAndOrNear, hic.isAwaitingProximityInput];
		hic.activationRules["LEX"] = [hic.lastWordIsKeyword, hic.isAwaitingProximityInput];
		hic.activationRules["THEN"] = [hic.textBoxIsEmpty, hic.alreadyContainsProximity, hic.lastWordIsKeyword, hic.isAwaitingProximityInput ];
		hic.activationRules["NEAR"] = [hic.textBoxIsEmpty, hic.alreadyContainsProximity, hic.lastWordIsKeywordOtherThanNot, hic.notAloneNotPreceded, hic.isAwaitingProximityInput];
		hic.activationRules["CLEAR"] = [hic.textBoxIsEmpty];
		hic.activationRules["ADD"] = [hic.textBoxIsEmpty, hic.lastWordIsKeyword, hic.isAwaitingProximityInput];
		hic.activationRules["REMOVE"] = [hic.onlyOneTextInput];	
		
		// (en|dis)ables proximity controls appropriately
		
		hic.doProxControlsActivationCheck = function(boxval, controls){
		
			hic.checkProxCountActivate(boxval, controls);
			hic.checkProxUnitActivate($(controls).find(".prxcount").val(), controls);
		
		}
		
		// (en|dis)ables proximity number input box
		
		hic.checkProxCountActivate = function(boxval, controls){
		
			if(boxval.match(proxRegExp)){
			
				$(controls).find(".prxcount").removeAttr("disabled");
			
			}
			else{
			
				var prxcount = $(controls).find(".prxcount");
				$(prxcount).val("");
				$(prxcount).attr("disabled", "disabled");
			
			}
		
		}
		
		// (en|dis)ables proximity unit (words|characters) select control
		// appropriately
		
		hic.checkProxUnitActivate = function(boxval, controls){
		
			if(boxval.match(proxCountRegExp)){
			
				$(controls).find(".prxunit").removeAttr("disabled");
			
			}
			else{
			
				var prxunit = $(controls).find(".prxunit");
				$(prxunit).val("none");
				$(prxunit).attr("disabled", "disabled");
			
			}
		
		}
		
		// iterates through all buttons and their appropriate filtration
		// functions to (en|dis)able appropriately
		
		hic.doButtonActivationCheck = function(textBoxValue, controls){
		
			var buttons = $(".syntax");
			
			OUTER: for(var i = 0; i < buttons.length; i++){
			
				var button = $(buttons[i]);
				var name = button.val().toUpperCase();
				if(name == "+") name = "ADD";
				if(name == "-") name = "REMOVE";
				
				var failCriteria = hic.activationRules[name] || [];
						
				INNER: for(var j = 0; j < failCriteria.length; j++){
				
						var failFunction = failCriteria[j];
						var failed = failFunction(textBoxValue, controls);
						if(failed){

							hic.disableButton(button);
							continue OUTER;
						}
				
				}
				
				hic.activateButton(button);
			
			}
		
		}
		
		hic.activateButton = function(button){
		
			$(button).removeAttr("disabled");
			$(button).removeClass("ui-state-disabled");
			$(button).addClass("ui-state-default");
		
		}
		
		hic.disableButton = function(button){
		
			$(button).attr("disabled", "disabled");
			$(button).removeClass("ui-state-default");
			$(button).addClass("ui-state-disabled");
		
		}
				
		hic.addKeyWord = function(){

			var keyword = $(this).val().toUpperCase();
			var parent = $(this).parents(topSelector);
			var input = $(parent).find(".keyword");
			var val = input.val();
			var newVal = (val.length > 0 ? val + " " : val) + keyword;
			newVal += " ";
			input.val(newVal);
			input.focus();
			hic.doButtonActivationCheck(input.val(), parent);
			hic.doProxControlsActivationCheck(input.val(), parent);
			
		}
		
		hic.addNewClause = function(){
		
			var val = $(this).val();
			if(val == 'none') return false;
			var mtr = $(this).parents(topSelector);
			mtr.find("#str-search-controls").remove();
			mtr.find("input.keyword").attr("disabled", "disabled");
			mtr.find("input.prxcount").attr("disabled", "disabled");
			mtr.find(".prx select").attr("disabled", "disabled");
			mtr.after(searchHTML.clone());
			$(lastTopSelector + " .str-operator").text("and");
			hic.doButtonActivationCheck("", $(lastTopSelector));
			hic.doProxControlsActivationCheck("", $(lastTopSelector));
			
		}
		
		hic.clearValues = function(){
		
			var textbox = $(this).parents(topSelector).find(".keyword");
			$(textbox).val("");
			$(this).parents(topSelector).find(".str-operator").text("");	
			hic.clearProxValues(this);
			hic.doButtonActivationCheck("", $(topSelector));
			$(textbox).focus();
			
		}
		
		hic.clearProxValues = function(that){
		
			$(that).parents(topSelector).find(".prxcount").val("");
			$(that).parents(topSelector).find(".prxunit option[value='none']").attr("selected", "selected");			
		
		}
		
		hic.analyzeTextInput = function(evt){
		
			var controls = $(this).parents(topSelector);
			var val = $(this).val();
			val += evt.type.indexOf("key") == -1 ? "" : evt.which;
			hic.doButtonActivationCheck(val, controls);
			hic.doProxControlsActivationCheck(val, controls);
			
		}
	
		/* TODO: live() now deprecated; replace with on() syntax */
		$(".keyword").live("focus", hic.analyzeTextInput);
		$(".keyword").live("keypress", hic.analyzeTextInput);
		$(".keyword").live("keyup", hic.analyzeTextInput);
		$(".prxcount").live("click keypress keyup", function(){ hic.checkProxUnitActivate($(this).val(), $(this).parents(".prx")); });
		$(".prxunit").live("change", function(evt){ 

				hic.doButtonActivationCheck($(lastTopSelector).find(".keyword").val(), $(lastTopSelector));
				hic.doProxControlsActivationCheck($(lastTopSelector).find(".keyword").val(), $(lastTopSelector));
				
		});
		$(".syntax-add").live("click", hic.addNewClause);
		$(".syntax:not(.syntax-clear, .syntax-add, .syntax-remove)").live("click", hic.addKeyWord);
		$(".syntax-clear").live("click", hic.clearValues);
		$(".syntax-remove").live("click", function(){
		
			var buttonBar = $(this).parent().clone();
			$(this).parent().parent().remove();
			$(lastTopSelector).append(buttonBar);
			$(lastTopSelector).find(".keyword").removeAttr("disabled");
			$(lastTopSelector).find(".keyword").focus();
			var prxcount = $(lastTopSelector + " input.prxcount");
			if(prxcount.val().length > 0){
			
				$(lastTopSelector + " .prx *").removeAttr("disabled");
			
			}
			hic.doButtonActivationCheck($(lastTopSelector).find(".keyword").val(), $(lastTopSelector));
		
		});

		hic.doButtonActivationCheck("", $(topSelector));
		
});