function showApiTokenDialog(prefix, id) {
    var request = new XMLHttpRequest(),
    	url = prefix.concat("/users/", id, "/token");

    request.onreadystatechange = function() {
        if(this.readyState == 4 && this.status == 200) {
            showDialog("API Token", JSON.parse(this.responseText).token);
        }
    };
    request.open("GET", url, true);
    request.setRequestHeader("Accept", "application/json");
    request.send();
}

function showDialog(title, message) {
    var html = "";
    html += '<h4 class="mdl-dialog__title">' + title + '</h4>';
    html += '<div class="mdl-dialog__content" style>';
    html += '<textarea style="overflow:auto;resize:none;width:100%" id="ta" rows="5" cols="46" readonly>' + message + '</textarea>';
    html += '</div>';
    html += '<div class="mdl-dialog__actions">';
    html += '<button type="button" class="mdl-button close">Close</button>';
    html += '<button type="button" class="mdl-button copy">Copy</button>';
    html += '</div>';

    var dialog = document.getElementsByClassName('mdl-dialog')[0];
    dialog.style = "width: 400px;";
    dialog.innerHTML = html;
    if(!dialog.showModal) {
        dialogPolyfill.registerDialog(dialog);
    }

    dialog.showModal();
    document.querySelector('.close').addEventListener('click', function(){
        dialog.close();
    });
    document.querySelector('.copy').addEventListener('click', function(){
    	var copyText = document.getElementById("ta");
    	copyText.select();
    	copyText.setSelectionRange(0, 99999);

    	document.execCommand("copy");
    	alert("Api token copied");
    });
}

function goToManager(url) {
	window.location.href = url;
}