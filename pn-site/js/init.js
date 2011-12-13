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
         jQuery("#login").html(data.user.name + " | <a href=\"/editor/user/signout\">sign out</a>");
        }
      },
      error: function (data, status, xhr) {
        jQuery("#login").html("Editor not available.");
      },
      timeout: 10000
    });
    jQuery.getJSON("/mulgara/sparql/?query="
        + encodeURIComponent("prefix dc: <http://purl.org/dc/terms/> "
        + "select ?subject "
        + "from <rmi://localhost/papyri.info#pi> "
        + "where { ?subject dc:references <http://papyri.info" + getPath().replace(/\/$/, "") + "/source>}")
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
