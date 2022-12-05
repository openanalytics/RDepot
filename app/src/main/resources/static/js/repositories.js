var TOKEN = $("meta[name='_csrf']").attr("content"),
    HEADER = $("meta[name='_csrf_header']").attr("content"),
    CREATE_MAINTAINER_REQUEST_RESPONSE = {},
    MAINTAINERS = []

function deleteRepository(id) {
    var url = '/manager/repositories/' + id + '/delete'; 
	$.ajax({
        url: url,
        type: 'DELETE',
        dataType: 'json',
        beforeSend: function(xhr) {
            xhr.setRequestHeader(HEADER, TOKEN);
        },
        success: function(result) {
            if(result.error != null) {
            	
                showErrorDialog(result.error);
            } else {
                $("#repository-" + id).closest('tr').fadeOut(300, function(){ $(this).remove()});
            }
        },
        error: function(result) {
           showErrorDialog("You cannot remove this repository.");
        }
    });
	
}


function downloadPackage(packageId, packageName, packageVersion) {
    window.location.href = "/manager/packages/" + packageId + "/download/" + packageName + "_" + packageVersion + ".tar.gz";
}

function openDeleteRepositoryDialog(id) {
	var html = '',
    dialog = document.getElementsByClassName('mdl-dialog')[0],
    name = $('#repository-' + id).closest('tr').find('.repository-name').html();
	
	html += '<h4 class="mdl-dialog__title">Delete repository</h4>';
	html += '<div class="mdl-dialog__content">';
	html += 'Are you sure want to delete ' + name + '?';
	html += '</div>';
	html += '<div class="mdl-dialog__actions">';
    html += '<button type="button" form="create-repository-form" class="mdl-button mdl-button--primary confirm">Confirm</button>';
    html += '<button type="button" class="mdl-button close">Cancel</button>';
    html += '</div>';
    dialog.innerHTML = html;
    if(!dialog.showModal) {
        dialogPolyfill.registerDialog(dialog);
    }
    document.querySelector('.close').addEventListener('click', function(){
        dialog.close();
    });
    
    document.querySelector('.confirm').addEventListener('click', function(){
    	deleteRepository(id);
    	dialog.close();
    });
    
    dialog.showModal();
}

function deleteRepositoryMaintainer(id) {
    var url = '/manager/repositories/maintainers/' + String(id) + '/delete';
    $.ajax({
        url: url,
        type: 'DELETE',
        dataType: 'json',
        beforeSend: function(xhr) {
            xhr.setRequestHeader(HEADER, TOKEN);
        },
        success: function(result) {
            if(result.error != null) {
                showErrorDialog(result.error);
            } else {
                $("#repositorymaintainer-" + id).closest('tr').fadeOut(300, function(){ $(this).remove()});
            }
        },
        error: function(result) {
            if(result.error != null) {
                showErrorDialog(result.error);
            } else {
                showErrorDialog("You cannot remove this repository maintainer.");
            }
        }
    });
}

function openDeleteRepositoryMaintainerDialog(id) {
	var html = '',
    dialog = document.getElementsByClassName('mdl-dialog')[0],
    name = $('#repositorymaintainer-' + id).closest('tr').find('.repositorymaintainer-name').html();
	
	html += '<h4 class="mdl-dialog__title">Delete repository maintainer</h4>';
	html += '<div class="mdl-dialog__content">';
	html += 'Are you sure want to delete ' + name + '?';
	html += '</div>';
	html += '<div class="mdl-dialog__actions">';
    html += '<button type="button" form="create-repositorymaintainer-form" class="mdl-button mdl-button--primary confirm">Confirm</button>';
    html += '<button type="button" class="mdl-button close">Cancel</button>';
    html += '</div>';
    dialog.innerHTML = html;
    if(!dialog.showModal) {
        dialogPolyfill.registerDialog(dialog);
    }
    document.querySelector('.close').addEventListener('click', function(){
        dialog.close();
    });
    
    document.querySelector('.confirm').addEventListener('click', function(){
    	deleteRepositoryMaintainer(id);
    	dialog.close();
    });
    
    dialog.showModal();
}

function changePublished(id) {
    var url = "",
        request = new XMLHttpRequest(),
        checked = document.getElementById("checkbox-" + id).checked;
    if(checked) {
        url = "/manager/repositories/" + id + "/unpublish";
    } else {
        url = "/manager/repositories/" + id + "/publish";
    }

    request.open("PATCH", url);
    request.onreadystatechange = function(reponse) {
        if(this.readyState == 4) {
            if(this.status == 200) {
                var responseObject = JSON.parse(this.responseText);
                
            } else if(this.status == 403) {
                showErrorDialog("You are not allowed to perform this operation.");
            } else {
                if(responseObject['error'] != null) {
                    showErrorDialog(responseObject.error);
                    document.getElementById("checkbox-" + id).checked = checked;
                    if(document.getElementById("checkbox-" + id).parentElement.classList.contains("is-checked")) {
                        document.getElementById("checkbox-" + id).parentElement.classList.remove("is-checked");
                    } else {
                        document.getElementById("checkbox-" + id).parentElement.classList.add("is-checked");
                    }

                } else {
                    showErrorDialog(this.response.status);
                }
            }
        }
    };
    request.setRequestHeader(HEADER, TOKEN);
    request.setRequestHeader("Accept", "application/json");
    request.setRequestHeader("Content-Type", "application/json");
    request.send();
}

function getCreateMaintainerDialogContent() {
    var request = new XMLHttpRequest(),
        url = "/manager/repositories/maintainers/create";


    request.onreadystatechange = function() {
        if(this.readyState == 4 && this.status == 200) {
            CREATE_MAINTAINER_REQUEST_RESPONSE = JSON.parse(this.responseText);
        }
    };
    request.open("GET", url, true);
    request.setRequestHeader("Accept", "application/json");
    request.send();
}

function getRepositoryMaintainers() {
    var request = new XMLHttpRequest();
    request.onreadystatechange = function(data) {
        if(this.readyState == 4) {
            MAINTAINERS = JSON.parse(this.responseText);
        }
    };
    request.open("GET", '/manager/repositories/maintainers/list');
    request.setRequestHeader(HEADER, TOKEN);
    request.setRequestHeader("Accept", "application/json");
    request.send();
}

function openEditMaintainerDialog(id, name) {
    var html = '',
        dialog = document.getElementsByClassName('mdl-dialog')[0],
        repositories = CREATE_MAINTAINER_REQUEST_RESPONSE.repositories;

    html += '<h4 class="mdl-dialog__title">Edit maintainer</h4>';
    html += '<div class="mdl-dialog__content">';
    html += '<form id="edit-maintainer-form">';
    html += '<p>User:<br/>';
    html += '<select id="maintainer-user-select" form="edit-maintainer-form" disabled>';
    html += '<option selected value="' + id + '">' + name + '</option>';
    html += '</select>';
    html += '</p><p>Repository:<br/>';
    html += '<select id="maintainer-repository-select" name="repository" form="edit-maintainer-form">';

    if(repositories != null) {
        for(var i = 0; i < repositories.length; i++) {
            html += '<option value="' + repositories[i].id + '">' + repositories[i].name + '</option>';
        }
    }

    html += '</select>';
    html += '</p>';
    html += '</form>';
    html += '</div>';
    html += '<div class="mdl-dialog__actions">';
    html += '<button type="button" form="edit-maintainer-form" class="mdl-button send">Save</button>';
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
            repositorySelect = document.getElementById("maintainer-repository-select"),
            repositoryId = repositorySelect.options[repositorySelect.selectedIndex].value;

        var data = {
            repositoryId: repositoryId
        };
        request.onreadystatechange = function() {
            if(this.readyState == 4) {
                dialog.close();
                var responseObject = JSON.parse(this.responseText);
                if(this.status == 200) {
                    location.reload();
                } else if(this.status == 403) {
                    showErrorDialog("You do not have permissions to edit repository maintainer.");
                } else {
                    if(responseObject['error'] != null){
                        showErrorDialog(responseObject['error']);
                    } else {
                        showErrorDialog("Error " + this.status + ": " + this.responseText);
                    }
                }
            }
        };

        request.addEventListener("error", function(event) {
            showErrorDialog("error!");
        });
        request.open("POST", '/manager/repositories/maintainers/' + id + '/edit');
        request.setRequestHeader("Accept", "application/json");
        request.setRequestHeader("Content-Type", "application/json");
        request.setRequestHeader(HEADER, TOKEN);
        request.send(JSON.stringify(data));
    });

    dialog.showModal();
}

function openAddMaintainerDialog() {
    var html = '',
    dialog = document.getElementsByClassName('mdl-dialog')[0];
    var users = CREATE_MAINTAINER_REQUEST_RESPONSE.users;
    var repositories = CREATE_MAINTAINER_REQUEST_RESPONSE.repositories;

    html += '<h4 class="mdl-dialog__title">Create maintainer</h4>';
    html += '<div class="mdl-dialog__content">';
    html += '<form id="add-maintainer-form">';
    html += '<p>User:<br/>';
    html += '<select id="maintainer-user-select" form="add-maintainer-form">';

    if(users != null) {
        for(var i = 0; i < users.length; i++) {
            html += '<option value="' + users[i].id + '">' + users[i].name + '</option>';
        }
    }

    html += '</select>';
    html += '</p><p>Repository:<br/>';
    html += '<select id="maintainer-repository-select" form="add-maintainer-form">';

    if(repositories != null) {
        for(var i = 0; i < repositories.length; i++) {
            html += '<option value="' + repositories[i].id + '">' + repositories[i].name + '</option>';
        }
    }

    html += '</select>';
    html += '</p>';
    html += '</form>';
    html += '</div>';
    html += '<div class="mdl-dialog__actions">';
    html += '<button type="button" form="add-maintainer-form" class="mdl-button mdl-button--primary send">Create</button>';
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
            userSelect = document.getElementById("maintainer-user-select"),
            userId = userSelect.options[userSelect.selectedIndex].value,
            repositorySelect = document.getElementById("maintainer-repository-select"),
            repositoryId = repositorySelect.options[repositorySelect.selectedIndex].value,
            data = {
                userId: userId,
                repositoryId: repositoryId
            };

        request.onreadystatechange = function() {
            if(this.readyState == 4) {
                dialog.close();
                var responseObject = JSON.parse(this.responseText);
                if(this.status == 200) {
                    location.reload();
                } else if(this.status == 403) {
                    showErrorDialog("You do not have permissions to create repository maintainer.");
                } else {
                    if(responseObject['error'] != null){
                        showErrorDialog(responseObject['error']);
                    } else {
                        showErrorDialog("Error " + this.status);
                    }
                }
            }
        };

        request.open("POST", '/manager/repositories/maintainers/create');
        request.setRequestHeader(HEADER, TOKEN);
        request.setRequestHeader("Accept", "application/json");
        request.setRequestHeader("Content-Type", "application/json");
        request.send(JSON.stringify(data));

    });

    dialog.showModal();
}

function openAddRepositoryDialog() {
    var html = '',
        dialog = document.getElementsByClassName('mdl-dialog')[0];
    html += '<h4 class="mdl-dialog__title">Create repository</h4>';
    html += '<div class="mdl-dialog__content">';
    html += '<form id="create-repository-form">';
    html += 'Name: <input class="mdl-textfield__input" type="text" id="name-input" placeholder="Repository name" required> <label class="mdl-textfield__label" for="name-input"></label><br/>';
    html += 'Publication URI: <input class="mdl-textfield__input" type="text" id="publicationuri-input" placeholder="http://localhost/repo/{name}" brequired> <label class="mdl-textfield__label" for="publicationuri-input"></label><br/>';
    html += 'Server address: <input class="mdl-textfield__input" type="text" id="serveraddress-input" placeholder="http://oa-rdepot-repo:8080/{name}" required> <label class="mdl-textfield__label" for="serveraddress-input"></label>';
    html += '</form>'
    html += '</div>';
    html += '<div class="mdl-dialog__actions">';
    html += '<button type="button" form="create-repository-form" class="mdl-button mdl-button--primary send">Create</button>';
    html += '<button type="button" class="mdl-button close">Cancel</button>';
    html += '</div>';
    dialog.innerHTML = html;
    if(!dialog.showModal) {
        dialogPolyfill.registerDialog(dialog);
    }
    document.querySelector('.close').addEventListener('click', function(){
        dialog.close();
    });
    document.querySelector('.send').addEventListener('click', function(){
        var request = new XMLHttpRequest();
        var publicationUri = document.getElementById("publicationuri-input").value;
        var name = document.getElementById("name-input").value;
        var serverAddress = document.getElementById("serveraddress-input").value;

        if(name == "" || publicationUri == "" || serverAddress == "") {
            var html = '<p class="error" style="color:red;">First fill in the blanks!</p>';
            if(document.getElementsByClassName("mdl-dialog__content")[0].getElementsByClassName("error").length == 0)
                $("#create-repository-form").after(html);
        } else {
            var formData = new FormData();
            var data = {
                name: name,
                publicationUri: publicationUri,
                serverAddress: serverAddress
            };
            request.onreadystatechange = function() {
                if(this.readyState == 4) {
                    dialog.close();
                    var responseObject = JSON.parse(this.responseText);
                    if(this.status == 200) {
                        location.reload();
                    } else if(this.status == 403) {
                        showErrorDialog("You do not have permissions to create repository.");
                    } else {
                        if(responseObject['error'] != null){
                            showErrorDialog(responseObject['error']);
                        } else {
                            showErrorDialog("Error " + this.status + ": " + this.responseText);                        
                        }
                    }
                }
            }
            request.open("POST", '/manager/repositories/create');
            request.setRequestHeader(HEADER, TOKEN);
            request.setRequestHeader("Accept", "application/json");
            request.setRequestHeader("Content-Type", "application/json");
            request.send(JSON.stringify(data));
        }


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

function openEditRepositoryDialog(id) {
    var html = '',
        dialog = document.getElementsByClassName('mdl-dialog')[0],
        name = $('#repository-' + id).closest('tr').find('.repository-name').html(),
        publicationUri = $('#repository-' + id).closest('tr').find('.repository-publication-uri').html(),
        serverAddress = $('#repository-' + id).closest('tr').find('.repository-server-address').html();
		version = $('#repository-' + id).closest('tr').find('.repository-version').html();

    html += '<h4 class="mdl-dialog__title">Edit repository</h4>';
    html += '<div class="mdl-dialog__content">';
    html += '<form id="edit-repository-form" method="post" enctype="multipart/form-data">';
    html += 'Name: <input class="mdl-textfield__input" type="text" id="name-input" name="name" value="' + name +'" required> <label class="mdl-textfield__label" for="name-input"></label><br/>';
    html += 'Publication URI: <input class="mdl-textfield__input" type="text" id="publicationuri-input" name="publicationuri" value="' + publicationUri +'" required> <label class="mdl-textfield__label" for="publicationuri-input"></label><br/>';
    html += 'Server address: <input class="mdl-textfield__input" type="text" id="serveraddress-input" name="serveraddress" value="' + serverAddress +'" required> <label class="mdl-textfield__label" for="serveraddress-input"></label>';
    html += '<input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}" />';
    html += '</form>'
    html += '</div>';
    html += '<div class="mdl-dialog__actions">';
    html += '<button type="button" form="create-repository-form" class="mdl-button mdl-button--primary send">Update</button>';
    html += '<button type="button" class="mdl-button close">Cancel</button>';
    html += '</div>';
    dialog.innerHTML = html;
    if(!dialog.showModal) {
        dialogPolyfill.registerDialog(dialog);
    }
    document.querySelector('.close').addEventListener('click', function(){
        dialog.close();
    });
    document.querySelector('.send').addEventListener('click', function(){
        var request = new XMLHttpRequest();
        publicationUri = document.getElementById("publicationuri-input").value;
        name = document.getElementById("name-input").value;
        serverAddress = document.getElementById("serveraddress-input").value;

        if(name == "" || publicationUri == "" || serverAddress == "") {
            var html = '<p style="color:red;">First fill in the blanks!</p>';
            $("form").after(html);
        } else {
            var formData = new FormData();
            formData.set("name", name);
            formData.set("publicationUri", publicationUri);
            formData.set("serverAddress", serverAddress);
            formData.set("version", version);

            request.onreadystatechange = function() {
                if(this.readyState == 4) {
                    dialog.close();
                    var responseObject = JSON.parse(this.responseText);
                    if(this.status == 200) {
                        location.reload();
                    } else if(this.status == 403) {
                        showErrorDialog("You do not have permissions to edit this repository.");
                    } else {
                        if(responseObject['error'] != null){
                            showErrorDialog(responseObject['error']);
                        } else {
                            showErrorDialog("Error " + this.status);
                        }
                    }
                }
            };
            request.open("POST", '/manager/repositories/' + id + '/edit');
            request.setRequestHeader(HEADER, TOKEN);
            request.setRequestHeader("Accept", "application/json");
            request.send(formData);
        }
    });
    dialog.showModal();
}

function openMaintainersPage() {
    window.location.href = '/manager/repositories/maintainers';
}

function openRepositoryPage(name) {
    window.location.href = "/manager/repositories/" + name;
}

function openPackagePage(repository, packageName, packageVersion) {
    window.location.href = "/manager/repositories/" + repository + "/packages/" + packageName;
//    without packageVersion!!!
}

function openPackagePageWithVersion(repository, packageName, packageVersion) {
    window.location.href = "/manager/repositories/" + repository + "/packages/" + packageName + "/" + packageVersion;
}

function preventBubbling() {
    var checkboxes = document.getElementsByClassName('mdl-checkbox__input');
    for(var i = 0; i < checkboxes.length; i++) {
        checkboxes[i].addEventListener('click', function(e){
            //e.preventDefault();
            e.stopPropagation();
        });
    }
}

function displayMaintainersButton() {
    var currentUrl = window.location.href.split('/');
    var module = currentUrl[currentUrl.length - 1];
    var button = document.getElementById('maintainers-button');
    if(button != null) {
        if(module != 'repositories') {
            button.style.display = "none";
        }
    }
}

function synchronizeMirrors(id) {
	var url = '/manager/repositories/' + id + '/synchronize-mirrors'; 
	const synchronizationField = document.getElementById('repository-' + id + '-row')
		.getElementsByClassName('synchronization-field')[0];
	const previousFieldContent = synchronizationField.innerHTML;
	synchronizationField.innerHTML = 
			'<div class="mdl-spinner mdl-spinner--single-color mdl-js-spinner is-active"></div>';
	componentHandler.upgradeDom();
	$.ajax({
        url: url,
        type: 'PATCH',
        dataType: 'json',
        beforeSend: function(xhr) {
            xhr.setRequestHeader(HEADER, TOKEN);
        },
        success: function(result) {
            if(result.error != null) {
                showErrorDialog(result.error);
				synchronizationField.innerHTML = previousFieldContent;
            }
            checkSynchronization();
        },
        error: function(result) {
            showErrorDialog("You cannot synchronize this repository.");
		    synchronizationField.innerHTML = previousFieldContent;
        }
    });
}

function checkSynchronization() {
    const request = new XMLHttpRequest();
    const url = '/manager/repositories/synchronization/status';

    request.onreadystatechange = function() {
        if(this.readyState == 4 && this.status == 200) {
            var response = JSON.parse(this.responseText);
            updateSynchronization(response);
        }
    }
    request.open("GET", url);
    request.setRequestHeader(HEADER, TOKEN);
    request.setRequestHeader("Accept", "application/json");
    request.setRequestHeader("Content-Type", "application/json");
    request.send();
}

function updateSynchronization(synchronizingRepositories) {
    const role = document.getElementsByTagName("body")[0].dataset.role;
    var rows = document.getElementsByClassName('repository-row');
    
    for(var i = 0; i < rows.length; i++) {
        var row = rows[i];
        var repositoryId = row.id.split('-')[1];
        const synchronizationField = row.getElementsByClassName('synchronization-field')[0];

        var matchingRepositories = synchronizingRepositories.filter(r => r.repositoryId === repositoryId);
        var html = '';

        if(matchingRepositories.length === 1 && matchingRepositories[0].pending === "true") {
                html = '<div class="mdl-spinner mdl-spinner--single-color mdl-js-spinner is-active"></div>';
        } else if(parseInt(role) > 2) {
            if(matchingRepositories.length === 1 && matchingRepositories[0].error !== "null") {
                html = '<button class="mdl-button mdl-js-button mdl-button--icon synchronization-error-button" onclick="synchronizeMirrors('+ repositoryId + ')" >';
            } else {
                html = '<button class="mdl-button mdl-js-button mdl-button--icon mdl-button--colored-alternative" onclick="synchronizeMirrors('+ repositoryId + ')" >';
            }

            html += '<i class="material-icons">update</i>';
            html += '</button>';
        }

        synchronizationField.innerHTML = html;
        componentHandler.upgradeDom();

    }

    // for(var i = 0; i < synchronizingRepositories.length; i++) {
    //     var id = synchronizingRepositories[i].repositoryId;

    //     const synchronizationField = document.getElementById('repository-' + id + '-row')
    //             .getElementsByClassName('synchronization-field')[0];

    //     if(synchronizingRepositories[i].pending === "true") {
    //         if(synchronizationField.getElementsByClassName('mdl-spinner').length == 0) {
    //             synchronizationField.innerHTML = 
    //                 '<div class="mdl-spinner mdl-spinner--single-color mdl-js-spinner is-active"></div>';
    //         }

    //     } else {
    //         var html = '';
    //         if(parseInt(role) > 2) {
    //             if(synchronizingRepositories[i].error === "null") {
    //                 html = '<button class="mdl-button mdl-js-button mdl-button--icon mdl-button--colored-alternative" onclick="synchronizeMirrors('+ id + ')" >';

    //             } else {
    //                 html = '<button class="mdl-button mdl-js-button mdl-button--icon mdl-button--colored-alternative synchronization-error-button" onclick="synchronizeMirrors('+ id + ')" >';
                    
    //             }

    //             html += '<i class="material-icons">update</i>';
    //             html += '</button>';
    //         }
            
    //         synchronizationField.innerHTML = html;
    //     }

    //     componentHandler.upgradeDom();
    // }
}

function scheduleSynchronization() {
    checkSynchronization();
    var intervalId = window.setInterval(checkSynchronization, 10000);
}

$(document).ready(function() {
    preventBubbling();
    getCreateMaintainerDialogContent();
    getRepositoryMaintainers();
    displayMaintainersButton();
});