if (jQuery.browser.msie) {
	if (parseFloat(jQuery.browser.version) < 8) {
		jQuery(document).ready(function() {
			jQuery(document.body).addClass("ie-compat");
		});
	}
}

if (PrimeFaces.widget.BaseTree) {
	var proto = PrimeFaces.widget.BaseTree.prototype;
	proto._nodeClick = PrimeFaces.widget.BaseTree.prototype.nodeClick;
	proto.nodeClick = function(event, nodeContent) {
		if (document.selection) {
			document.selection.empty();
		}

		if (event.button != 0) {
			event.stopPropagation();
		} else {
			this._nodeClick(event, nodeContent);
		}
	};

	jQuery(document).on("dblclick", "#repository-form .ui-treenode-content",
			function(e) {
				var openButton = PF("openButton");
				if (openButton.jq.attr("disabled") != "disabled") {
					openButton.jq.click();
				}
			});
}

function initNavigatorDroppables() {
	jQuery("#target-tree-pane").droppable({
		over : function(e) {
			jQuery("#source-tree-pane .ui-droppable").droppable("disable");
		},
		out : function(e) {
			jQuery("#source-tree-pane .ui-droppable").droppable("enable");
		}
	});
}

function initializeTabs(tabs) {
	var tabView = jQuery("#tab-panel").tabs({
		heightStyleType : "fill",
		activate : onTabSelected
	});

	var activeId = getActiveViewId();
	var activeIndex = -1;

	var index = 0;
	for ( var id in tabs) {
		if (activeId == id) {
			activeIndex = index;
		}

		createTab(tabs[id]);
		index++;
	}

	tabView.tabs("refresh");

	if (activeIndex > -1) {
		tabView.tabs("option", "active", activeIndex);
	}
}

function attachClosingViewParam() {
	var id = getViewToClose();

	if (id) {
		PrimeFaces.addSubmitParam("close-form", {
			viewId : id
		});
	}
}

function getViewToClose() {
	return jQuery("#tab-panel").data("viewToClose");
}

function setViewToClose(id) {
	jQuery("#tab-panel").data("viewToClose", id);
}

function getActiveViewId() {
	return jQuery("#repository-form\\:view-id").val();
}

function setActiveViewId(id) {
	jQuery("#repository-form\\:view-id").val(id);
}

function getActiveViewIndex() {
	var tabView = jQuery("#tab-panel");
	var tabs = tabView.find("li");
	var activeTab = getActiveTab();

	return tabs.index(activeTab);
}

function getActiveTab() {
	var tabView = jQuery("#tab-panel");
	var activeTab = tabView.find("li.ui-tabs-active:first");

	if (activeTab.size() == 0) {
		activeTab = undefined;
	}

	return activeTab;
}

function getActiveWindow() {
	var tabView = jQuery("#tab-panel");
	var selector = tabView.find("li.ui-tabs-active:first a").attr("href");

	var query = jQuery(selector).find("iframe");
	if (query.size() == 0) {
		return;
	}

	return query.get(0).contentWindow;
}

function addTab(tab) {
	createTab(tab);

	var tabView = jQuery("#tab-panel").tabs("refresh");

	var index = Math.max(0, tabView.find("li").size() - 1);

	tabView.tabs("option", "active", index);

	setActiveViewId(tab.id);
}

function createTab(tab) {
	var name = tab.name;

	if (!tab.path) {
		name = "*" + tab.name;
	}

	var url;

	if (tab.initialized) {
		url = "view.xhtml?";
	} else {
		url = "catalog.xhtml?";
	}

	url += settings.viewParameterName + "=" + tab.id;

	var tabView = jQuery("#tab-panel");

	var iframe = jQuery(document.createElement("iframe"));
	iframe.attr("frameborder", "0").attr("src", url);

	var link = jQuery(document.createElement("a"));
	link.attr("href", "#view-" + tab.id);
	link.text(name);

	var button = jQuery(document.createElement("span")).addClass("ui-icon")
			.addClass("ui-icon-close").on("click", onTabClose);

	var header = jQuery(document.createElement("li"));
	header.append(link);
	header.append(button);
	header.data("id", tab.id);
	header.data("name", tab.name);

	if (tab.path) {
		header.data("path", tab.path);
	}

	if (tab.dirty) {
		header.data("dirty", tab.dirty);
		header.addClass("dirty");
	}

	var panel = jQuery(document.createElement("div"));
	panel.attr("id", "view-" + tab.id);
	panel.append(iframe);

	tabView.find("ul").append(header);
	tabView.append(panel);
}

function closeActiveTab() {
	closeTab(getActiveViewIndex());
}

function closeTab(index) {
	var tabView = jQuery("#tab-panel");

	tabView.find("ul.ui-tabs-nav li:eq(" + index + ")").remove();
	tabView.find("div.ui-tabs-panel:eq(" + index + ")").remove();

	tabView.tabs("refresh");

	setViewToClose(null);
}

function getTabIndex(id) {
	var tabs = jQuery("#tab-panel div.ui-tabs-panel");
	var activeTab = jQuery("#view-" + id);

	return tabs.index(activeTab);
}

function enableSave(enable) {
	var tab = getActiveTab();

	if (enable && typeof onReportChanged == "function") {
		onReportChanged();
	}

	if (tab) {
		tab.data("dirty", enable);

		if (enable) {
			tab.addClass("dirty");
		} else {
			tab.removeClass("dirty");
		}
	}
}

function onReportSaved(args) {
	PF("newReportDialog").hide();

	var tab = getActiveTab();

	tab.find("a:first").text(args.name);
	tab.data("name", args.name);
	tab.data("path", args.path);

	enableSave(false);
}

function selectTab(id) {
	var index = getTabIndex(id);

	var tabView = jQuery("#tab-panel");
	tabView.tabs("select", index);
}

function onTabClose(event) {
	var tab = jQuery(this).parent();

	var href = tab.find("a").attr("href");
	var id = href.substring(6);

	setViewToClose(id);

	if (tab.data("dirty")) {
		PF('confirmCloseDialog').show();
	} else {
		closeReport(id);
	}
}

function onTabSelected(event, ui) {
	var iframe = jQuery(ui.newPanel).find("iframe");

	if (iframe.size() == 0) {
		return;
	}

	var contentWin = iframe.get(0).contentWindow;

	if (contentWin && typeof contentWin.initLayout == "function") {
		contentWin.initLayout();
	}

	var href = jQuery(ui.newTab).find("a:first").attr("href");
	var id = href.substring(6);

	if (getActiveViewId() != id) {
		setActiveViewId(id);

		onReportSelected([ {
			name : "viewId",
			value : id
		} ]);
	}
}

function onViewChanged() {
	if (parent && parent.enableSave) {
		parent.enableSave(true);
	}
}

function onThemeChanged() {
	jQuery("#tab-panel .ui-tabs-panel iframe").each(function(index, elem) {
		var pf = elem.contentWindow.PrimeFaces;
		if (pf) {
			pf.changeTheme(PF('themeSwitcher').value);
		}
	});
}

function onViewResize() {
	for ( var name in PrimeFaces.widgets) {
		var widget = PF(name);
		if (widget.plot) {
			widget.plot.replot();
		}
	}
}

function applyThemeToCMEditor(selector) {
	if (!selector) {
		selector = ".properties-config .CodeMirror";
	}

	jQuery(selector).addClass(
			"ui-state-default ui-inputfield ui-widget ui-corner-all");
}

function showWaitDialog() {
	var waitDialog = PF("waitDialog");

	if (waitDialog) {
		waitDialog.block();
	}
}

function hideWaitDialog() {
	var waitDialog = PF("waitDialog");

	if (waitDialog) {
		waitDialog.unblock();
	}
}

function completeMdx(editor) {
	editor.suggestions = null;
	editor.token = null;

	var cursor = editor.instance.getCursor();
	var token = editor.instance.getTokenAt(cursor);

	var isIdentifier = function(string) {
		return /^\[[\w$_ ]*\]$/.test(string);
	};

	var isPartialIdentifier = function(string) {
		return /^\[[\w$_ ]*\]?$/.test(string);
	};

	var completeIdentifier = function(token) {
		// If it is a property, find out what it is a property of.
		while (true) {
			tokenProperty = editor.instance.getTokenAt({
				line : cursor.line,
				ch : tokenProperty.start
			});

			if (tokenProperty.string != ".") {
				break;
			}

			tokenProperty = editor.instance.getTokenAt({
				line : cursor.line,
				ch : tokenProperty.start
			});

			if (!isIdentifier(tokenProperty.string)) {
				break;
			}

			if (!context) {
				var context = [];
			}

			context.splice(0, 0, tokenProperty);
		}

		var contextString = null;
		if (context) {
			contextString = "";

			for (var i = 0; i < context.length; i++) {
				var currentContext = context[i];

				if (i > 0) {
					contextString = contextString + ".";
				}

				contextString = contextString + currentContext.string;
			}
		}

		editor.token = token;
		editor.search(token.string.substring(1), contextString, cursor.line,
				cursor.ch);
	};

	var tokenProperty = token;
	var keyword = token.string;

	token.string = token.string.substring(0, (cursor.ch - token.start));

	if (isPartialIdentifier(token.string)) {
		if (!isIdentifier(keyword)) {
			token.end = token.start + token.string.length;
		}

		completeIdentifier(token);
	} else if (keyword == ".") {
		var offset = cursor.ch;

		cursor.ch--;

		var context = [];

		while (cursor.ch > 0) {
			var previous = editor.instance.getTokenAt(cursor);

			if (isIdentifier(previous.string)) {
				context.splice(0, 0, previous.string);
				cursor.ch = previous.start;
			} else {
				break;
			}
		}

		if (context.length > 0) {
			token.start = offset;
			token.end = offset;
			token.string = "";

			editor.token = token;
			editor.search("", context.join("."), cursor.line, offset);
		}
	} else {
		editor.complete();
	}
}