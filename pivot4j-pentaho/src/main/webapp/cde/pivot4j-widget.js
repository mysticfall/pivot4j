var Pivot4JComponent = BaseComponent.extend({

	update : function() {
		if (!this.htmlObject) {
			return;
		}

		var widget = $("#" + this.htmlObject);
		var iframe = $(document.createElement("iframe"));

		if (this.width) {
			iframe.width(this.width);
		}

		if (this.width) {
			iframe.height(this.height);
		}

		var path = encodeURIComponent(this.filePath);

		iframe.attr("frameBorder", "none").attr(
				"src",
				webAppPath + "/plugin/pivot4j/faces/open.xhtml?embeded=true&path="
						+ path);

		widget.append(iframe);
	}
});
