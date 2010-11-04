function init() {
    jQuery("li.dialog").each(function(i) {
        jQuery(this).after("<li><a href=\"#\" onclick=\"javascript:jQuery('#" + this.id + "c').dialog({height:100,modal:true})\">" + this.title + "</a></li>");
        jQuery(this).hide();
    });
    jQuery("ul.nav li").hover(function() {
        jQuery(this).css('background-color', '#445B88');
        jQuery(this).find("a").css('color', 'white')
    },
    function() {
        jQuery(this).css('background-color', 'transparent');
        jQuery(this).find("a").css('color', 'black')
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
    jQuery("[xml:id]").each( function (i, elt) {
       elt.id = jQuery(elt).attr("xml:id"); 
    }); 
}