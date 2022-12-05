var TOKEN = $("meta[name='_csrf']").attr("content"),
    HEADER = $("meta[name='_csrf_header']").attr("content"),
    ROLES = [];

function showUserDialog(login) {
    var request = new XMLHttpRequest(),
        url = "/manager/users/" + login,
        userDetails = {},
        html = '',
        dialog = document.getElementsByClassName('mdl-dialog')[0];

    request.onreadystatechange = function() {
        if(this.readyState == 4 && this.status == 200) {
            userDetails = JSON.parse(this.responseText);
            var name = (userDetails.user.name == null ? '' : userDetails.user.name),
                lastloginDate = (userDetails.lastloggedin == null ? '': formatDateTimeFromISO(userDetails.lastloggedin)),
                createdDate = (userDetails.created == null ? '' : formatDateTimeFromISO(userDetails.created)),
                email = (userDetails.user == null ? '' : userDetails.user.email),
                username = (userDetails.user == null ? '' : userDetails.user.login),
                role = (userDetails.user == null ? '' : (userDetails.user.role == null ? '' : userDetails.user.role.description)),
                active = (userDetails.user == null ? '' : userDetails.user.active);
            html += '<h4 class="mdl-dialog__title">' + name + '</h4>';
            html += '<div class="mdl-dialog__content">';
            html += '<table><tbody>';
            html += '<tr>';
            html += '<th>Last log in on</th>';
            html += '<td>' + lastloginDate + '</td>'
            html += '</tr>';
            html += '<th>Created on</th>';
            html += '<td>' + createdDate + '</td>'
            html += '</tr>';
            html += '<th>Email</th>';
            html += '<td>' + email + '</td>'
            html += '</tr>';
            html += '<th>User name</th>';
            html += '<td>' + username + '</td>'
            html += '</tr>';
            html += '<th>Role</th>';
            html += '<td>' + role + '</td>'
            html += '</tr>';
            html += '<th>Active</th>';
            html += '<td>' + active + '</td>'
            html += '</tr>';
            html += '</tbody></table>';
            html += '</div>';
            html += '<div class="mdl-dialog__actions">';
            html += '<button type="button" class="mdl-button close">Close</button>';
            html += '</div>';
            dialog.innerHTML = html;
            dialog.style.width = "500px";
            if(!dialog.showModal) {
                dialogPolyfill.registerDialog(dialog);
            }
            document.querySelector('.close').addEventListener('click', function(){
                dialog.close();
            });

            dialog.showModal();
        }
    };

    request.open("GET", url, true);
    request.setRequestHeader("Accept", "application/json");
    request.send();
}

function formatDateTimeFromISO(date){
	return date.substr(0,16).replace("T", " ")
}

function changeActive(id) {
    var action = "",
        url = "";
    if(document.getElementById("checkbox-" + id).checked) {
        action = "deactivate";
    } else {
        action = "activate"
    }
    url = '/manager/users/' + String(id) + '/' + action;
    $.ajax({
        url: url,
        type: 'PATCH',
        dataType: 'json',
        beforeSend: function(xhr) {
            xhr.setRequestHeader(HEADER, TOKEN);
        },
        error: function(result) {
            if(result.error != null) {
                showErrorDialog(result.error);
            } else {
                showErrorDialog("Unknown error");
            }
        }
    });
}

function getRoles() {
    var request = new XMLHttpRequest(),
        url = "/manager/users/roles";


    request.onreadystatechange = function() {
        if(this.readyState == 4 && this.status == 200) {
            ROLES = JSON.parse(this.responseText);
        }
    };

    request.open("GET", url, true);
    request.setRequestHeader("Accept", "application/json");
    request.send();
}

function openEditUserDialog(id, login, currentRole, name, email, isActive) {
    var html = '',
        dialog = document.getElementsByClassName('mdl-dialog')[0];

    html += '<h4 class="mdl-dialog__title">Change ' + login + '&apos;s role</h4>';
    html += '<div class="mdl-dialog__content">';
    html += '<form id="change-user-role-form" method="post" enctype="multipart/form-data">';
    html += '<select id="change-user-role-select" class="mdl-textfield__input" name="role" form="change-user-role-form">';
    for(var i = 0; i < ROLES.length; i++) {
        var selected = '';
        if(ROLES[i].description == currentRole) {
            selected = 'selected="selected"';
        }
        html += '<option ' + selected + ' value="' + ROLES[i].description + '">' + ROLES[i].description + '</option>';
    }
    html += '</select>';
    html += '<input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}" />';
    html += '</form>';
    html += '</div>';
    html += '<div class="mdl-dialog__actions">';
    html += '<button type="button" form="change-user-role-form" class="mdl-button mdl-button--primary send">Update</button>';
    html += '<button type="button" class="mdl-button close">Cancel</button>';
    html += '</div>';
    dialog.innerHTML = html;
    dialog.style.width = "250px";
    if(!dialog.showModal) {
        dialogPolyfill.registerDialog(dialog);
    }
    document.querySelector('.close').addEventListener('click', function(){
        dialog.close();
    });
    document.querySelector('.send').addEventListener('click', function(){
        var request = new XMLHttpRequest(),
            formData = new FormData(),
            select = document.getElementById("change-user-role-select"),
            role = select.options[select.selectedIndex].value;

        formData.set("name", name);
        formData.set("email", email);
        formData.set("login", login);
        formData.set("role", role);
        formData.set("active", isActive);


        request.onreadystatechange = function() {
            if(this.readyState == 4) {
                dialog.close();
                var responseObject = JSON.parse(this.responseText);
                if(this.status == 200) {
                    location.reload();
                } else {
                    if(responseObject['error'] != null) {
                        showErrorDialog(responseObject['error']);
                    } else {
                        showErrorDialog("Error " + this.status);
                    }
                }
            }
        }
        request.open("POST", '/manager/users/' + id + '/edit');
        request.setRequestHeader(HEADER, TOKEN);
        request.setRequestHeader("Accept", "application/json");
        request.send(formData);

    });

    dialog.showModal();
}

function showErrorDialog(message) {
    var html = "";
    html += '<h4 class="mdl-dialog__title">Error</h4>';
    html += '<div class="mdl-dialog__content">';
    html += '<p>' + message + '</p>';
    html += '</div>';
    html += '<div class="mdl-dialog__actions">';
    html += '<button type="button" class="mdl-button close">Close</button>';
    html += '</div>';

    var dialog = document.getElementsByClassName('mdl-dialog')[0];
    dialog.innerHTML = html;
    if(!dialog.showModal) {
        dialogPolyfill.registerDialog(dialog);
    }

    dialog.showModal();
    document.querySelector('.close').addEventListener('click', function(){
        dialog.close();
    });
}

function preventBubbling() {
    var checkboxes = document.getElementsByClassName('mdl-checkbox__input');
    for(var i = 0; i < checkboxes.length; i++) {
        checkboxes[i].addEventListener('click', function(e){
            e.stopPropagation();
        });
    }
}

$(document).ready(function() {
    getRoles();
    preventBubbling();
});