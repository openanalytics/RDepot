var MAX_LABEL_LENGTH = 35,
    TOKEN = $("meta[name='_csrf']").attr("content"),
    HEADER = $("meta[name='_csrf_header']").attr("content"),
    CREATE_MAINTAINER_REQUEST_RESPONSE = {},
    MAINTAINERS = [];

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

function cutDescriptions(maxLength) {
    var fields = document.getElementsByClassName("package-description");
    for(var i = 0; i < fields.length; i++) {
        if(fields[i].innerHTML.length > maxLength)
            fields[i].innerHTML = fields[i].innerHTML.slice(0, maxLength) + '...';
    }
}

function deletePackage(id)
{
    var url = '/manager/packages/' + String(id) + '/delete';
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
                $("#package-" + id).closest('tr').fadeOut(300, function(){ $(this).remove()});
            }
        },
        error: function(result) {
            showErrorDialog("You cannot remove this package.");
        }
    });
}

function openDeletePackageDialog(id) {
	var html = '',
    dialog = document.getElementsByClassName('mdl-dialog')[0],
    name = $('#package-' + id).closest('tr').find('.package-name').html();
	
	html += '<h4 class="mdl-dialog__title">Delete package</h4>';
	html += '<div class="mdl-dialog__content">';
	html += 'Are you sure want to delete ' + name + '?';
	html += '</div>';
	html += '<div class="mdl-dialog__actions">';
    html += '<button type="button" form="create-package-form" class="mdl-button mdl-button--primary confirm">Confirm</button>';
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
    	deletePackage(id);
    	dialog.close();
    });
    
    dialog.showModal();
}

function changeActive(id) {
    var action = "",
        url = "";
    if(document.getElementById("checkbox-" + id).checked) {
        action = "deactivate";
    } else {
        action = "activate"
    }
    url = '/manager/packages/' + String(id) + '/' + action;
    $.ajax({
        url: url,
        type: 'PUT',
        dataType: 'json',
        beforeSend: function(xhr) {
            xhr.setRequestHeader(HEADER, TOKEN);
        },
        success: function(result) {
            if(result.error != null) {
                showErrorDialog(result.error);
            }
        },
        error: function(result) {

        }
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

function showDescriptionDialog(packageId) {
    var name = document.getElementById('package-'+ packageId).parentNode.getElementsByClassName('package-name')[0].innerHTML,
        version = document.getElementById('package-'+ packageId).parentNode.getElementsByClassName('package-version')[0].innerHTML,
        description = document.getElementById('description-' + packageId).innerHTML,
        html = "",
        dialog = document.getElementsByClassName('mdl-dialog')[0];
    html += '<h4 class="mdl-dialog__title">'+ name +'</h4>';
    html += '<div class="mdl-dialog__content">';
    html += '<p>' + version + '</p>';
    html += '<p>' + description + '</p>';
    html += '</div>';
    html += '<div class="mdl-dialog__actions">';
    html += '<button type="button" class="mdl-button close">Close</button>';
    html += '</div>';
    dialog.innerHTML = html;
    if(!dialog.showModal) {
        dialogPolyfill.registerDialog(dialog);
    }

    dialog.showModal();
    document.querySelector('.close').addEventListener('click', function(){
        dialog.close();
    });
}

function updatePackageSelect() {
    var repositorySelect = document.getElementById("maintainer-repository-select"),
        selectedRepository = repositorySelect.options[repositorySelect.selectedIndex].value,
        packageSelect = document.getElementById("maintainer-package-select");

    packageSelect.innerHTML = updatePackageList(selectedRepository);
}

function updatePackageList(selectedRepository) {
    var repositories = CREATE_MAINTAINER_REQUEST_RESPONSE.repositories,
        usedNames = [],
        html = '';
	
	if(repositories !== null) {
		for(var i = 0; i < repositories.length; i++) {
            var repository = repositories[i];
			if(repositories[i].id == selectedRepository && repository.packages !== null) {
				var packages = repository.packages;
				
				for(var k = 0; k < packages.length; k++) {
					if(!usedNames.includes(packages[k].name)) {
						html += '<option value="' + packages[k].name + '">' + packages[k].name + '</option>';
						usedNames.push(packages[k].name);
					}
                }
                
                return html;
			}
		}
    }
    				
    return html;
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

    if(users !== null) {
        for(var i = 0; i < users.length; i++) {
            html += '<option value="' + users[i].id + '">' + users[i].name + '</option>';
        }
    }

    html += '</select>';
    html += '</p><p>Repository:<br/>';
    html += '<select id="maintainer-repository-select" form="add-maintainer-form" onchange="updatePackageSelect()">';

    if(repositories !== null) {
        for(var i = 0; i < repositories.length; i++) {
            html += '<option value="' + repositories[i].id + '"';
            if(i == 0)
            	html += ' selected="selected"';
            html += '>' + repositories[i].name + '</option>';
        }
    }

    html += '</select>';
    html += '</p><p>Package:<br/>';
    html += '<select id="maintainer-package-select" form="add-maintainer-form">';
    if(repositories !== null) {
        if(repositories.length != 0)
            html += updatePackageList(repositories[0].id);
    }
    
    
    html += '</select>';
    html += '</p>';
    html += '<p id="dialog-error-message">';
    html += '</p>';
    html += '</form>';
    html += '</div>';
    html += '<div class="mdl-dialog__actions">';
    html += '<button type="button" form="add-maintainer-form" class="mdl-button mdl-button--primary send">Create</button>';
    html += '<button type="button" class="mdl-button close">Cancel</button>';
    html += '</div>';
    dialog.innerHTML = html;
    dialog.style.width = "300px";
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
            packageSelect = document.getElementById("maintainer-package-select"),
            repositoryId = '',
            packageName = '';

        if(repositorySelect.options.length === 0 || packageSelect.options.length === 0) {
            document.getElementById("dialog-error-message").innerHTML = 'The maintainer must have a package and repository!';
            return;
        }

        repositoryId = repositorySelect.options[repositorySelect.selectedIndex].value,
        packageName = packageSelect.options[packageSelect.selectedIndex].value;

            
        if(packageName == "") {
            var html = '<p class="error" style="color:red;">First fill in the blanks!</p>';
            if(document.getElementsByClassName("mdl-dialog__content")[0].getElementsByClassName("error").length == 0) {
                $("#add-maintainer-form").after(html);
                return;
            }

        }

        var data = {
            userId: userId,
            repositoryId: repositoryId,
            packageName: packageName
        };
        request.onreadystatechange = function() {
            if(this.readyState == 4) {
                dialog.close();
                var responseObject = JSON.parse(this.responseText);
                if(this.status == 200) {
                    if(responseObject['error'] != null) {
                        showErrorDialog(responseObject['error']);
                    } else {
                        location.reload();
                    }
                } else {
                    showErrorDialog(this.status);
                }
            }
        };

        request.open("POST", '/manager/packages/maintainers/create');
        request.setRequestHeader("Accept", "application/json");
        request.setRequestHeader("Content-Type", "application/json");
        request.setRequestHeader(HEADER, TOKEN);
        request.send(JSON.stringify(data));

    });
    dialog.showModal();
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
    html += '<select id="maintainer-repository-select" form="edit-maintainer-form" onchange="updatePackageSelect()">';

    if(repositories !== null) {
        for(var i = 0; i < repositories.length; i++) {
            html += '<option value="' + repositories[i].id + '">' + repositories[i].name + '</option>';
        }
    }

    html += '</select>';


    html += '</p><p>Package:<br/>';
    html += '<select id="maintainer-package-select" form="add-maintainer-form">';
    if(repositories != null) {
        if(repositories.length != 0)
            html += updatePackageList(repositories[0].id);
    }
    
    
    html += '</select>';
    html += '</p>';
    html += '<p id="dialog-error-message">';
    html += '</p>';
    html += '</form>';
    html += '</div>';
    html += '<div class="mdl-dialog__actions">';
    html += '<button type="button" form="edit-maintainer-form" class="mdl-button send">Save</button>';
    html += '<button type="button" class="mdl-button close">Cancel</button>';
    html += '</div>';
    dialog.innerHTML = html;
    dialog.style.width = "300px";
    if(!dialog.showModal) {
        dialogPolyfill.registerDialog(dialog);
    }
    document.querySelector('.close').addEventListener('click', function(){
        dialog.close();
    });
    document.querySelector('.send').addEventListener('click', function(){
        var request = new XMLHttpRequest(),
            repositorySelect = document.getElementById("maintainer-repository-select"),
            packageSelect = document.getElementById("maintainer-package-select"),
            repositoryId = '',
            packageName = '';

        if(repositorySelect.options.length === 0 || packageSelect.options.length === 0) {
            document.getElementById("dialog-error-message").innerHTML = 'The maintainer must have a package and repository!';
            return;
        }

        repositoryId = repositorySelect.options[repositorySelect.selectedIndex].value,
        packageName = packageSelect.options[packageSelect.selectedIndex].value;
            data = {
                repositoryId: repositoryId,
                packageName: packageName
            };

        request.onreadystatechange = function() {
            if(this.readyState == 4) {
                dialog.close();
                var responseObject = JSON.parse(this.responseText);
                if(this.status == 200) {
                    if(responseObject['success'] != null) {
                        location.reload();
                    } else if(responseObject['error'] != null){
                        showErrorDialog(responseObject['error']);
                    }
                } else if(this.status == 403) {
                    showErrorDialog("You do not have permissions to edit repository maintainer.");
                } else {
                    showErrorDialog("Error " + this.status + ": " + this.responseText);
                }
            }
        };

        request.addEventListener("error", function(event) {
            showErrorDialog("error!");
        });
        request.open("POST", '/manager/packages/maintainers/' + id + '/edit');
        request.setRequestHeader("Accept", "application/json");
        request.setRequestHeader("Content-Type", "application/json");
        request.setRequestHeader(HEADER, TOKEN);
        request.send(JSON.stringify(data));
    });

    dialog.showModal();
}

function openMaintainersPage() {
    window.location.href = '/manager/packages/maintainers';
}

function getPackageMaintainers() {
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

function getCreateMaintainerDialogContent() {
    var request = new XMLHttpRequest(),
        url = "/manager/packages/maintainers/create";


    request.onreadystatechange = function() {
        if(this.readyState == 4 && this.status == 200) {
            CREATE_MAINTAINER_REQUEST_RESPONSE = JSON.parse(this.responseText);
        }
    };
    request.open("GET", url, true);
    request.setRequestHeader("Accept", "application/json");
    request.send();
}

function deletePackageMaintainer(id) {
    var url = '/manager/packages/maintainers/' + id + '/delete';
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
                $("#packagemaintainer-" + id).closest('tr').fadeOut(300, function(){ $(this).remove()});
            }
        },
        error: function(result) {
           showErrorDialog("You cannot remove this package maintainer.");
        }
    });
}

function openDeletePackageMaintainerDialog(id) {
	var html = '',
    dialog = document.getElementsByClassName('mdl-dialog')[0],
    name = $('#packagemaintainer-' + id).closest('tr').find('.packagemaintainer-name').html();
	
	html += '<h4 class="mdl-dialog__title">Delete package maintainer</h4>';
	html += '<div class="mdl-dialog__content">';
	html += 'Are you sure want to delete ' + name + '?';
	html += '</div>';
	html += '<div class="mdl-dialog__actions">';
    html += '<button type="button" form="create-packagemaintainer-form" class="mdl-button mdl-button--primary confirm">Confirm</button>';
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
    	deletePackageMaintainer(id);
    	dialog.close();
    });
    
    dialog.showModal();
}

function displayMaintainersButton() {
    var currentUrl = window.location.href.split('/');
    var module = currentUrl[currentUrl.length - 1];
    var button = document.getElementById('maintainers-button');
    if(button != null) {
        if(module != 'packages') {
            button.style.display = "none";
        }
    }
}

$(document).ready(function(){
    cutDescriptions(MAX_LABEL_LENGTH);
    preventBubbling();
    getPackageMaintainers();
    getCreateMaintainerDialogContent();
    displayMaintainersButton();
});