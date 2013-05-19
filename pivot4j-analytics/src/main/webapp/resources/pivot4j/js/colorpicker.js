PrimeFaces.widget.AjaxColorPicker = PrimeFaces.widget.ColorPicker.extend({
	bindCallbacks_ : PrimeFaces.widget.ColorPicker.prototype.bindCallbacks,

	bindCallbacks : function() {
		var _self = this;

		this.bindCallbacks_.apply(this);

		this.cfg.onChange_ = this.cfg.onChange;
		this.cfg.onChange = function(hsb, hex, rgb) {
			_self.cfg.onChange_.call(_self, hsb, hex, rgb);
			_self.fireChangeEvent(hex);
		};
	},

	fireChangeEvent : function(hex) {
		if (this.cfg.behaviors) {
			var behavior = this.cfg.behaviors["change"];

			if (behavior) {
				behavior.call(this);
			}
		}
	}
});