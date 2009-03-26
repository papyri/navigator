
dojo.provide("dojo.validate.jp");
dojo.require("dojo.validate.common");
dojo.validate.isJapaneseCurrency = function(value) {var flags = {symbol: "\u00a5",
fractional: false};
return dojo.validate.isCurrency(value, flags);}
