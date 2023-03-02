var TOKEN = $("meta[name='_csrf']").attr("content"),
    HEADER = $("meta[name='_csrf_header']").attr("content");

function showMessageDialog(header, content) {
    var html = '',
        dialog = document.getElementsByClassName('mdl-dialog')[0];
    html += '<h4 class="mdl-dialog__title">' + header + '</h4>';
    html += '<div class="mdl-dialog__content">';
    html += 'Message: ' + content;
    html += '</div>';
    html += '<div class="mdl-dialog__actions">';
    html += '<button type="button" class="mdl-button close">OK</button>';
    html += '</div>';
    dialog.innerHTML = html;
    if(!dialog.showModal) {
        dialogPolyfill.registerDialog(dialog);
    }
    document.querySelector('.close').addEventListener('click', function(){
        dialog.close();
        window.location.reload();
    });
    dialog.showModal();
}

function acceptSubmission(id) {
    var url = "/manager/submissions/" + id + "/accept";
	document.getElementById("accept-submission-button-" + id).setAttribute('disabled', true);
	document.getElementById("cancel-submission-button-" + id).setAttribute('disabled', true);
    $.ajax({
        type: "PATCH",
        dataType: "json",
        url: url,
        beforeSend: function(request) {
            request.setRequestHeader(HEADER, TOKEN);
        },
        success: function(data) {
            $("#accept-submission-button-" + id).fadeOut(300, function(){ $(this).remove()});
            $("#cancel-submission-button-" + id).fadeOut(300, function(){ $(this).remove()});
            document.getElementById("checkbox-" + id).parentElement.classList.add("is-checked");
        },
        error: function(data) {
            if(data.error != null) {
                showMessageDialog('Error', data.error);

            } else {
                showMessageDialog('Error', "Unknown error");
            }
            document.getElementById("accept-submission-button-" + id).setAttribute('disabled', false);
            document.getElementById("cancel-submission-button-" + id).setAttribute('disabled', false);
        }
    });
}

function cancelSubmission(id) {
    var url = "/manager/submissions/" + id + "/cancel";
    document.getElementById("cancel-submission-button-" + id).setAttribute('disabled', true);
    document.getElementById("accept-submission-button-" + id).setAttribute('disabled', true);
    $.ajax({
        type: "DELETE",
        dataType: 'json',
        url: url,
        beforeSend: function(request) {
            request.setRequestHeader(HEADER, TOKEN);
        },
        success: function(data) {
            $("#submission-" + id).closest('tr').fadeOut(300, function(){ $(this).remove()});

            if(data.warning != null) {
                showMessageDialog("Warning", data.warning);
            }
        },
        error: function(data) {
            if(data.error != null){
                showMessageDialog("Error", data.error);
            } else {
                showMessageDialog("Error", "Unknown error");
            }
            document.getElementById("accept-submission-button-" + id).setAttribute('disabled', false);
            document.getElementById("cancel-submission-button-" + id).setAttribute('disabled', false);
        }
    });
}

function goToUrl(url) {
    window.location.href = url;
}

$(document).ready(function(){
    var container = document.getElementById("submissions-buttons");
    var html = '<button type="button" class="mdl-button" onclick="';
    var currentUrl = window.location.href.split('/');
    var module = currentUrl[currentUrl.length - 1];
    if (module == "all") {
        html += 'goToUrl(\'\/manager\/submissions\');">View only my submissions';
    } else {
        html += 'goToUrl(\'\/manager\/submissions\/all\');">View all submissions';
    }
    html += "</button>"
    container.innerHTML = html;
});
