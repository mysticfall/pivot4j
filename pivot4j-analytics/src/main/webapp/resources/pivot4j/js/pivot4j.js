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
			var node = nodeContent.parent();

			if (!this.isNodeSelected(node)) {
				this._nodeClick(event, nodeContent);
			}
		}
	};

	jQuery(document).on("dblclick", "#repository-form .ui-treenode-content",
			function(e) {
				if (openButton.jq.attr("disabled") != "disabled") {
					openButton.jq.click();
				}
			});
}

function initializeTabs(tabs) {
	var tabView = jQuery("#tab-panel")
			.tabs(
					{
						heightStyleType : "fill",
						tabTemplate : "<li><a href='#{href}'>#{label}</a><span class='ui-icon ui-icon-close'></span></li>"
					});

	tabView.bind("tabsshow", onTabSelected);
	tabView.on("click", "span.ui-icon-close", function() {
		var tab = jQuery(this).parent();

		var href = tab.find("a").attr("href");
		var id = href.substring(6);

		jQuery("#tab-panel").data("viewToClose", id);

		if (tab.data("dirty")) {
			confirmCloseDialog.show();
		} else {
			closeReport([ {
				name : 'viewId',
				value : id
			} ]);
		}
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
		tabView.tabs("select", activeIndex);
	}
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
	var activeTab = tabView.find("li.ui-tabs-selected:first");

	if (activeTab.size() == 0) {
		activeTab = undefined;
	}

	return activeTab;
}

function getActiveWindow() {
	var tabView = jQuery("#tab-panel");
	var selector = tabView.find("li.ui-tabs-selected:first a").attr("href");

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

	tabView.tabs("select", index);

	setActiveViewId(tab.id);
}

function createTab(tab) {
	var url = "#view-" + tab.id;

	var name = tab.name;

	if (!tab.path) {
		name = "*" + tab.name;
	}

	var tabView = jQuery("#tab-panel");
	var panel = tabView.tabs("add", url, name).find(url);

	var iframe = jQuery(document.createElement("iframe"));
	iframe.attr("frameborder", "0").attr("src",
			"view.xhtml?" + settings.viewParameterName + "=" + tab.id);

	panel.append(iframe);

	var newTab = tabView.find("li:last");
	newTab.data("id", tab.id);
	newTab.data("name", tab.name);

	if (tab.path) {
		newTab.data("path", tab.path);
	}

	if (tab.dirty) {
		newTab.data("dirty", tab.dirty);
		newTab.addClass("dirty");
	}
}

function closeTab() {
	var tabView = jQuery("#tab-panel");
	var index = getTabIndex(getViewToClose());

	tabView.tabs("remove", index);
}

function closeActiveTab() {
	var index = getActiveViewIndex();

	jQuery("#tab-panel").tabs("remove", index);
}

function getViewToClose() {
	return jQuery("#tab-panel").data("viewToClose");
}

function getTabIndex(id) {
	var tabs = jQuery("#tab-panel div.ui-tabs-panel");
	var activeTab = jQuery("#view-" + id);

	return tabs.index(activeTab);
}

function enableSave(enable) {
	var tab = getActiveTab();

	if (enable) {
		if (typeof onReportChanged == "function") {
			onReportChanged();
		}
	} else {
		tab.removeClass("dirty");
	}

	tab.data("dirty", enable);
}

function checkAndSaveReport() {
	var tab = getActiveTab();
	if (!tab) {
		return;
	}

	if (tab.data("path")) {
		saveReport();
	} else {
		showSaveDialog();
	}
}

function onReportSaved(args) {
	newReportDialog.hide();

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

function onTabSelected(event, ui) {
	var iframe = jQuery(ui.panel).find("iframe");

	if (iframe.size() == 0) {
		return;
	}

	var contentWin = iframe.get(0).contentWindow;

	if (!contentWin || !contentWin.workbench) {
		return;
	}

	// Fix layout abnormalities due to a hidden initialization.
	if (!contentWin.workbench.layout) {
		contentWin.workbench.createLayout();

		if (contentWin.mdxEditor
				&& !contentWin.mdxEditor.getCodeMirrorInstance()) {
			contentWin.mdxEditor.initialize();
		}

		if (contentWin.cubeList) {
			contentWin.cubeList.initWidths();
		}
	}

	var href = getActiveTab().find("a:first").attr("href");
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
	jQuery(".ui-tabs-panel iframe").each(function(index, elem) {
		var pf = elem.contentWindow.PrimeFaces;
		if (pf) {
			pf.changeTheme(themeSwitcher.value);
		}
	});
}

function applyThemeToCMEditor(selector) {
	if (!selector) {
		selector = ".properties-config .CodeMirror";
	}

	jQuery(selector).addClass(
			"ui-state-default ui-inputfield ui-widget ui-corner-all");
}