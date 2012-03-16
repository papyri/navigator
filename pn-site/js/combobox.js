/*
 * jQuery UI Combobox 1.0.0
 *
 * Copyright 2011, Eric Mann and Jumping Duck Media (http://jumping-duck.com)
 * Dual licensed under the MIT or GPL Version 2 (or later) licenses.
 *
 * Depends:
 *	jquery.ui.core.js
 *	jquery.ui.widget.js
 *	jquery.ui.position.js
 *  jquery.ui.autocomplete.js
 *
 * Button positioning, modifications to regex matching , and copying of attributes added thill 2012.03.06
 *
 */
(function ($) {
	$.widget("ui.combobox", {
		_create: function () {
			var self = this,
					select = this.element.hide(),
					selected = select.children(":selected"),
					value = selected.val() ? selected.text() : "";
			var input = this.input = $("<input class=\"combobox\">")
					.attr('id', select.attr('id') + '_input')
					.attr('tabIndex', select.attr('tabIndex') + 1)
					.attr('name', select.attr('name'))
					.attr('disabled', select.attr('disabled'))
					.insertAfter(select)
					.val(value)
					.autocomplete({
						delay: 0,
						minLength: 0,
						source: function (request, response) {
							var matcher = new RegExp($.ui.autocomplete.escapeRegex(request.term), "i");
							response(select.children("option").map(function () {
								var text = $(this).text();
								if (this.value && (!request.term || matcher.test(text.replace(/\(\d+\)\s*$/, ""))))
									return {
										label: text.replace(
											new RegExp(
												"(?![^&;]+;)(?!<[^<>]*)(" +
												$.ui.autocomplete.escapeRegex(request.term) +
												")(?![^<>]*>)(?![^&;]+;)", "gi"
											), "<strong>$1</strong>"),
										value: text,
										option: this
									};
							}));
						},
						select: function (event, ui) {
							ui.item.option.selected = true;
							self._trigger("selected", event, {
								item: ui.item.option
							});
							window.setTimeout(info.papyri.thill.guidesearch.tidyQueryString, 200);
						},
						change: function (event, ui) {
							if (!ui.item) {
								var matcher = new RegExp("^" + $.ui.autocomplete.escapeRegex($(this).val()) + "$", "i"),
									valid = false;
								select.children("option").each(function () {
									if ($(this).text().match(matcher)) {
										this.selected = valid = true;
										return false;
									}
								});
							}
						}
					})
					.addClass("ui-widget ui-widget-content ui-corner-left");

			input.data("autocomplete")._renderItem = function (ul, item) {
				return $("<li></li>")
						.data("item.autocomplete", item)
						.append("<a>" + item.label + "</a>")
						.appendTo(ul);
			};
			var surround_height = input.parent().height();
			this.button = $("<button type='button'>&nbsp;</button>")
					.attr("tabIndex", -1)
					.attr("title", "Show All Items")
					.insertAfter(input)
					.button({
						icons: {
							primary: "ui-icon-triangle-1-s"
						},
						text: false
					})
					.removeClass("ui-corner-all")
					.addClass("ui-corner-right ui-button-icon")
					.click(function () {
						// close if already visible
						if (input.autocomplete("widget").is(":visible")) {
							input.autocomplete("close");
							return;
						}

						// work around a bug (likely same cause as #5265)
						$(this).blur();

						// pass empty string as value to search for, displaying all results
						input.autocomplete("search", "");
						input.focus();
					});
			// need to play with these values to get them working x-browser
			var ht = input.outerHeight() - 2;
			var wd = this.button.width();
			var leftness = input.position().left + input.outerWidth() - wd - 3;
			var topness = input.position().top + 1;
			var stylestring = "height:" + ht + "px; position:absolute; top:" + topness + "px; left:" + leftness + "px";
			this.button.attr("style", stylestring);
			if(input.attr("disabled")) this.button.attr("disabled", "disabled");
			
		},

		destroy: function () {
			this.input.remove();
			this.button.remove();
			this.element.show();
			$.Widget.prototype.destroy.call(this);
		}
	});
})(jQuery);