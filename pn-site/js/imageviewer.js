var OUlayer;       // layer for an open layers map
var map;           // open layers map for an image
var metadataURL;   // URL to the file that supplies djatoka with parameters for an image.  
var djatokaserverURL = "http://libdc3-03.oit.duke.edu";  // djatoka server URL

function initImage() {
  var images = jQuery("#image ul a").detach();
  jQuery("#image ul").replaceWith('<div id="olimage"></div>');
  if (images.length > 1) {
    jQuery("#olimage").after('<form><select id="imglist" onchange="loadImage(this[this.selectedIndex].value)"></select></form>');
    images.each(function(i) {
      var imgname = jQuery(this).attr("href").substr(jQuery(this).attr("href").lastIndexOf("/") + 1);
      jQuery("#imglist").append('<option value="' + jQuery(this).attr("href") + '">' + imgname + '</option>');
    });
  }
  loadImage(images.attr("href"));
}

function loadImage(imageURL) {

    if (map) {
        map.destroy();
    }

    /* Initialize URLs */
    metadataURL = djatokaserverURL + "/adore-djatoka/resolver?url_ver=Z39.88-2004&svc_id=info:lanl-repo/svc/getMetadata&rft_id=" + imageURL;        

    /*
       Create open layers layer calling the OpenURL class. This class
       calculates the parameters needed to create a new open layers
       map. 
     */ 
    OUlayer = new OpenLayers.Layer.OpenURL( "OpenURL",
      djatokaserverURL, {layername: 'basic', format:'image/jpeg', 
      rft_id:imageURL, metadataUrl:metadataURL}); 

    /* set the rest of the djatoka server URL after the host name */       
    OpenLayers.Layer.OpenURL.djatokaURL = '/adore-djatoka/resolver';


    /* Get the parameters needed to create a new open layers map. */ 
    var metadata = OUlayer.getImageMetadata();
    var resolutions = OUlayer.getResolutions();
    var maxExtent = new OpenLayers.Bounds(0, 0, metadata.width, metadata.height);
    var tileSize = OUlayer.getTileSize();

    var options = { resolutions: resolutions,
                    maxExtent: maxExtent,
                    tileSize: tileSize};
    map = new OpenLayers.Map('olimage', options);
    map.addLayer(OUlayer);

    var lon = metadata.width / 2;
    var lat = metadata.height / 2;

    /* set the center of the map to the center of the height and width */      
    map.setCenter(new OpenLayers.LonLat(lon, lat), 0);
    map.zoomTo(OUlayer.getOptimalViewerLevel());

}
