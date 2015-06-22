
${nanoGong} = {
	statusUpdate : function(status) {
		if(status == 1) this.started = true;
		if(status == 7 && this.started) {
			this.started = false;
			var response = document.getElementById('${nanoGongAppletId}').sendGongRequest(
					'PostToForm', '${postUrl}', 'file', document.cookie, 'filename.wav');
			if(response == "true") wicketAjaxGet('${wicketAjaxUrl}');
		}
	}
}
