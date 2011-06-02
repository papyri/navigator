function init() {
    jQuery("li.dialog").each(function(i) {
        jQuery(this).after("<li><a href=\"#\" onclick=\"javascript:jQuery('#" + this.id + "c').dialog({height:100,modal:true})\">" + this.title + "</a></li>");
        jQuery(this).hide();
    });
    jQuery("ul.nav a").not($("#footer ul.nav li")).hover(function() {
            jQuery(this).css('background-color', '#445B88');
            jQuery(this).css('color', 'white')
        },
        function() {
            jQuery(this).css('background-color', 'transparent');
            jQuery(this).css('color', 'black')
    });
    jQuery("div.controls input").each(function() {
        if (!this.checked) {
            jQuery("." + this.name).css('display', 'none');
        }
    });
    if (jQuery(".translation").length == 0 && jQuery(".image").length == 0) {
        jQuery(".transcription").css('width', '100%');
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
    jQuery.getJSON("/mulgara/sparql/?query="
        + encodeURIComponent("prefix dc: <http://purl.org/dc/terms/> "
        + "select ?subject "
        + "from <rmi://localhost/papyri.info#pi> "
        + "where { ?subject dc:references <http://papyri.info" + window.location.pathname.replace(/\/$/, "") + "/source>}")
        + "&format=json", function(data) {
            if (data.results.bindings.length > 0) {
                jQuery("#controls").append('<div id="related" class="ui-widget-content ui-corner-all" style="margin-left:2em"><h4>related resources</h4></div>')
                jQuery.each(data.results.bindings, function(i, row) {
                    var val = row.subject.value;
                    jQuery("#related").append('<a href="'+ val + '" style="margin-left:1em" target="_blank">GLRT</a>');
                })
            }
    });
}