
dojo.provide("dojo.widget.RemoteTabController");
dojo.require("dojo.widget.*");
dojo.require("dojo.widget.TabContainer");
dojo.require("dojo.event.*");
dojo.deprecated("dojo.widget.RemoteTabController is slated for removal in 0.5; use PageController or TabController instead.", "0.5");
dojo.widget.defineWidget(
"dojo.widget.RemoteTabController",
dojo.widget.TabController,
{templateCssPath: dojo.uri.dojoUri("src/widget/templates/RemoteTabControl.css"),
templateString: '<div dojoAttachPoint="domNode" wairole="tablist"></div>',
"class": "dojoRemoteTabController",
tabContainer: "",
postMixInProperties: function(){this.containerId = this.tabContainer;
dojo.widget.RemoteTabController.superclass.postMixInProperties.apply(this, arguments);},
fillInTemplate: function() {dojo.html.addClass(this.domNode, this["class"]);
if (this.tabContainer) {dojo.addOnLoad(dojo.lang.hitch(this, "setupTabs"));}
dojo.widget.RemoteTabController.superclass.fillInTemplate.apply(this, arguments);}}
);
