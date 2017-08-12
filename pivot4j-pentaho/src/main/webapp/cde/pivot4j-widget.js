var Pivot4JComponent = BaseComponent.extend({

	update : function() {
		var myself = this;

		if (!myself.htmlObject) {
			return;
		}

		var widget = $("#" + myself.htmlObject);
		var iframe = $(document.createElement("iframe"));

		if (myself.width) {
			iframe.width(myself.width);
		}

		if (myself.width) {
			iframe.height(myself.height);
		}

		var query = "";

		if (myself.parameters) {
			var p = new Array(myself.parameters ? myself.parameters.length : 0);

			for(var i= 0, len = p.length; i < len; i++) {
				var key = myself.parameters[i][0];
				var value = myself.parameters[i][1] == "" || myself.parameters[i][1] == "NIL" ? myself.parameters[i][2] : myself.parameters[i][1];

				if (window.hasOwnProperty(value)) {
					value = window[value];
				} else {
					value = Dashboards.getParameterValue(value);
				}
				query = query + "&" + encodeURIComponent(key) + "=" + encodeURIComponent(value);
			}
		}

		var path = encodeURIComponent(myself.filePath) + query;

		$("iframe.p4j-frame", widget).remove();

		iframe
			.addClass("p4j-frame")
			.attr("frameBorder", "none")
			.attr("src", webAppPath + "/plugin/pivot4j/faces/open.xhtml?embeded=true&path=" + path);

		widget.append(iframe);
	}
});
