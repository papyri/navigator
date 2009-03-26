
dojo.provide("dojo.widget.Wizard");
dojo.require("dojo.widget.*");
dojo.require("dojo.widget.LayoutContainer");
dojo.require("dojo.widget.ContentPane");
dojo.require("dojo.event.*");
dojo.require("dojo.html.style");
dojo.widget.defineWidget(
"dojo.widget.WizardContainer",
dojo.widget.LayoutContainer,
{templatePath: dojo.uri.dojoUri("src/widget/templates/Wizard.html"),
templateCssPath: dojo.uri.dojoUri("src/widget/templates/Wizard.css"),
selected: null,
nextButtonLabel: "next",
previousButtonLabel: "previous",
cancelButtonLabel: "cancel",
doneButtonLabel: "done",
cancelFunction: "",
hideDisabledButtons: false,
fillInTemplate: function(args, frag){dojo.event.connect(this.nextButton, "onclick", this, "_onNextButtonClick");
dojo.event.connect(this.previousButton, "onclick", this, "_onPreviousButtonClick");
if (this.cancelFunction){dojo.event.connect(this.cancelButton, "onclick", this.cancelFunction);}else{this.cancelButton.style.display = "none";}
dojo.event.connect(this.doneButton, "onclick", this, "done");
this.nextButton.value = this.nextButtonLabel;
this.previousButton.value = this.previousButtonLabel;
this.cancelButton.value = this.cancelButtonLabel;
this.doneButton.value = this.doneButtonLabel;},
_checkButtons: function(){var lastStep = !this.hasNextPanel();
this.nextButton.disabled = lastStep;
this._setButtonClass(this.nextButton);
if(this.selected.doneFunction){this.doneButton.style.display = "";
if(lastStep){this.nextButton.style.display = "none";}}else{this.doneButton.style.display = "none";}
this.previousButton.disabled = ((!this.hasPreviousPanel()) || (!this.selected.canGoBack));
this._setButtonClass(this.previousButton);},
_setButtonClass: function(button){if(!this.hideDisabledButtons){button.style.display = "";
dojo.html.setClass(button, button.disabled ? "WizardButtonDisabled" : "WizardButton");}else{button.style.display = button.disabled ? "none" : "";}},
registerChild: function(panel, insertionIndex){dojo.widget.WizardContainer.superclass.registerChild.call(this, panel, insertionIndex);
this.wizardPanelContainerNode.appendChild(panel.domNode);
panel.hide();
if(!this.selected){this.onSelected(panel);}
this._checkButtons();},
onSelected: function( panel){if(this.selected ){if (this.selected._checkPass()) {this.selected.hide();} else {return;}}
panel.show();
this.selected = panel;},
getPanels: function() {return this.getChildrenOfType("WizardPane", false);},
selectedIndex: function() {if (this.selected) {return dojo.lang.indexOf(this.getPanels(), this.selected);}
return -1;},
_onNextButtonClick: function() {var selectedIndex = this.selectedIndex();
if ( selectedIndex > -1 ) {var childPanels = this.getPanels();
if (childPanels[selectedIndex + 1]) {this.onSelected(childPanels[selectedIndex + 1]);}}
this._checkButtons();},
_onPreviousButtonClick: function() {var selectedIndex = this.selectedIndex();
if ( selectedIndex > -1 ) {var childPanels = this.getPanels();
if (childPanels[selectedIndex - 1]) {this.onSelected(childPanels[selectedIndex - 1]);}}
this._checkButtons();},
hasNextPanel: function() {var selectedIndex = this.selectedIndex();
return (selectedIndex < (this.getPanels().length - 1));},
hasPreviousPanel: function() {var selectedIndex = this.selectedIndex();
return (selectedIndex > 0);},
done: function() {this.selected.done();}});
dojo.widget.defineWidget(
"dojo.widget.WizardPane",
dojo.widget.ContentPane,
{canGoBack: true,
passFunction: "",
doneFunction: "",
postMixInProperties: function(args, frag) {if (this.passFunction) {this.passFunction = dj_global[this.passFunction];}
if (this.doneFunction) {this.doneFunction = dj_global[this.doneFunction];}
dojo.widget.WizardPane.superclass.postMixInProperties.apply(this, arguments);},
_checkPass: function() {if (this.passFunction && dojo.lang.isFunction(this.passFunction)) {var failMessage = this.passFunction();
if (failMessage) {alert(failMessage);
return false;}}
return true;},
done: function() {if (this.doneFunction && dojo.lang.isFunction(this.doneFunction)) {this.doneFunction();}}});
