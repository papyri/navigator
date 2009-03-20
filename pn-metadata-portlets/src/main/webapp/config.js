PN_EXHIBIT_CONFIG = {
		lensSelector : function(itemID, exhibit){
			window.alert('itemID=' + itemID);
			if (itemID.startsWith('apis')){
				return document.getElementById('apis-template');
			};
            return document.getElementById('default-template');
			}
};
