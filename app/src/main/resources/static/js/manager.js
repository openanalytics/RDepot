var STORED_FILES = [];

function validateForm() {
    var permissions = [];
    var button = document.getElementById('upload-button');
    button.setAttribute('disabled', true);
    for(var i = 0; i < STORED_FILES.length; i++) {
        var allowed = false;
        var packageName = STORED_FILES[i].name;
        var dotArray = packageName.split('.');
        var gz = dotArray.pop();
        if(gz == "gz") {
            var tar = dotArray.pop();
            if(tar == "tar") {
                allowed = true;
            }
        }
        if(!allowed) {
            var card = document.getElementById("card_" + i);
            var messageField = card.getElementsByClassName('mdl-card__title-text')[0];
            var background = card.getElementsByClassName('mdl-card__title')[0];
            messageField.innerHTML = 'Incorrect file';
            messageField.display = 'inline-block';
            background.style.backgroundColor = "rgb(208, 69, 56)";
        }
        permissions.push(allowed);
    }
    if(STORED_FILES.length != 0) {
        button.removeAttribute('disabled');
    }
    for(var i = 0; i < permissions.length; i++) {
        if(permissions[i] == false) {
            button.setAttribute('disabled', true);
        }
    }
}

function hideButtons() {
	$('#upload-button').fadeOut();
    $('.add-package-fab').fadeOut();
}

function submitFiles() {
    var submitInput = document.getElementById("submitInput");
    var submitButton = document.getElementById('upload-button');
    var addPackageButton = document.getElementsByClassName('add-package-fab')[0];

    if(!submitButton.hasAttribute('disabled')) {
        submitInput.click();
    }
    
}

function reloadPage() {
	window.location.href = "/manager";
}

function openFileDialog() {
    var input = document.getElementsByClassName("filesToUpload")[0];
    input.click();
}

function createFileCard(filename, fileNumber) {
    var entry = document.createElement('div');
    var title = "";
    if(filename.indexOf('_') > -1) {
        title = filename.split('_')[0];
    }
    var html = '<div id="card_' + fileNumber + '" class="package-card-square mdl-card mdl-shadow--2dp">';
    html += '<div class="mdl-card__title mdl-card--expand">';
    html += '<h2 class="mdl-card__title-text">' + title + '</h2></div>';
    html += '<div class="mdl-card__supporting-text">';
    html += '<span class="package-filename">' + filename + '</span></div>';
    html += '<div class="progress-container"></div>';
    html += '<div class="mdl-card__actions mdl-card--border">';
    html += '<a class="remove-package-button mdl-button mdl-js-button mdl-js-ripple-effect" data-file="'+ filename +'" onclick="removeFile(this)">Remove</a>';
    html += '</div></div>';
    entry.className = "package-cell mdl-cell";
    entry.innerHTML = html;
    return entry;
}

function handleFileSelect(e) {
    var filesArr = document.getElementsByClassName("filesToUpload")[0].files;
    addFiles(filesArr);
}

function increaseHeight(element, value) {
    var height = element.offsetHeight;
    var newHeight = height + value;
    element.style.height = newHeight + 'px';
}

function showProgress(fileNumber, repositoryNumber, repositoryName) {
    $.getScript("material.js", function(){});

    var progressBar = document.createElement('div');
    progressBar.classList.add("mdl-progress");
    progressBar.classList.add("mdl-js-progress");
    progressBar.id = 'p' + fileNumber + '_' + repositoryNumber;

    var progressText = document.createElement('div');
    progressText.classList.add("progress-text");
    progressText.id = 'text' + fileNumber + '_' + repositoryNumber;
    progressText.innerHTML = 'Uploading to <b>' + repositoryName + '</b> <span class="progress-value">[0%]</span>';

    var cancelButton = document.createElement('button');
    cancelButton.classList.add('mdl-button');
    cancelButton.classList.add('mdl-js-button');
    cancelButton.classList.add('mdl-button--icon');
    cancelButton.classList.add('mdl-button--colored');
    cancelButton.id = 'cancel-button_' + fileNumber + '_' + repositoryNumber;
    cancelButton.innerHTML = '<i class="material-icons">cancel</i>';

    var card = document.getElementById('card_' + fileNumber);
    var progressContainer = card.getElementsByClassName('progress-container')[0];
    var titleField = card.getElementsByClassName('mdl-card__title')[0];
    progressContainer.appendChild(progressText);
    progressContainer.appendChild(progressBar);
    progressContainer.appendChild(cancelButton);
    if(titleField.offsetHeight < 65)
        increaseHeight(card, 52);

    if(repositoryNumber == 0) {
        card.getElementsByClassName('remove-package-button')[0].remove();
    }
    if(repositoryNumber > 1) {
        var currentHeight = parseInt(card.style.height, 10);
        card.style.height = (currentHeight + 44) + 'px';
    }
    
    componentHandler.upgradeDom();
}

function updateProgressMessage(message, fileNumber, repositoryNumber) {
    return function(e) {
        var container = document.getElementById("text" + fileNumber + "_" + repositoryNumber);
        container.innerHTML = message;
    }   
}

function updateProgressBar(fileNumber, repositoryNumber, progress) {
    $.getScript("material.js", function(){});
    document.querySelector('#p' + fileNumber + '_' + repositoryNumber).MaterialProgress.setProgress(progress);
    document.querySelector('#text' + fileNumber + '_' + repositoryNumber).getElementsByClassName('progress-value')[0].innerHTML = '[' + progress + '%]';
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

function handleForm(e) {
    e.preventDefault();

    var selectedRepositories = $("#select-repos").val();
    if(selectedRepositories.length < 1) {
        showErrorDialog("Select one or more repositories!");
        return;
   }

    var  data = {};

    data = {
        fileData: STORED_FILES,
        repositories: selectedRepositories
    };

    var url = "/manager/packages/submit";
    
    hideButtons();
    
    sendRequests(selectedRepositories, url);
}

function handleFinalMessage(fileNumber, repositoryNumber, request){
    return function(e) {       
        var button = document.getElementById('cancel-button_' + fileNumber + '_' + repositoryNumber);
        var icon = button.getElementsByTagName('i')[0];
        var progressBar = document.getElementById('card_' + fileNumber).getElementsByClassName('progressbar')[repositoryNumber];
        var successColor = "rgb(79, 210, 49)";
        var failureColor = "rgb(208, 69, 56)";
        var warningColor = "rgb(255, 223, 0)";
        var message = '';
        //if(request.status == 200) {
        var responseObject = JSON.parse(request.responseText);
        if(responseObject['success'] != null) {
            message = '<span style="color:green;">Success</span>';
            icon.style.color = successColor;
            icon.innerHTML = "check_circle";
            progressBar.style.backgroundColor = successColor;
        } else if(responseObject['warning'] != null) {
            if(responseObject.warning.second == "email.send.exception") {
                message = '<span style="color:rgb(255,223,0);">Warning: Your submission needs to be accepted by administrator</span>';
            } else {
                message = '<span style="color:rgb(255,223,0);">Warning: ' + responseObject.warning.second + '</span>';
            }
            
            icon.style.color = warningColor;
            icon.innerHTML = "check_circle";
            progressBar.style.backgroundColor = warningColor;
        } else if(responseObject['error'] != null){
            message = '<span style="color:red;">Error: ' + responseObject.error.second + '</span>';
            progressBar.style.backgroundColor = failureColor;
        } else {
            message = '<span style="color:red;">Unknown error</span>';
            progressBar.style.backgroundColor = failureColor;
        }
        /*} else if(request.status != 0) {
            message = '<span style="color:red;">Error ' + request.status + '</span>';
        }*/

        var field = document.getElementById('card_' + fileNumber).getElementsByClassName('progress-text')[repositoryNumber];
        field.innerHTML = message;
    };
    
}

function handleProgress(fileNumber, reporitoryNumber){
    return function(e) {
        if(e.lengthComputable) {
            var percentComplete = Math.round((e.loaded / e.total) * 100);
            updateProgressBar(fileNumber,reporitoryNumber,percentComplete);
        }
    };
    
}

function handleCancellation(message, fileNumber, repositoryNumber){
    return function(e) {
        var container = document.getElementById("text" + fileNumber + "_" + repositoryNumber);
        container.innerHTML = message;
    }
}

function sendRequests(repositories, url) {
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");

    for(var k = 0; k < repositories.length; k++) {
        for(var i = 0; i < STORED_FILES.length; i++) {
            (function() {
                    
                var repositoryName = repositories[k],
                    formData = new FormData();
                formData.set("file", STORED_FILES[i]);
                formData.append("repository", repositories[k]);

                var request = new XMLHttpRequest(),
                    cancelButton = {};
                showProgress(i, k, repositoryName);
                cancelButton = document.getElementById('cancel-button_' + i + '_' + k);
                cancelButton.addEventListener('click', function(e){
                    e.preventDefault();
                    request.abort();
                });
                
                request.upload.addEventListener("progress", (handleProgress)(i, k), true);
                request.open("POST", url, true);
                request.setRequestHeader("Accept", "application/json");
                request.addEventListener("load", (handleFinalMessage)(i, k, request), true);
                request.addEventListener("error", (handleFinalMessage)(i, k, request), true)
                request.addEventListener("abort", (handleCancellation)("Canceled", i, k), true);
                request.setRequestHeader(header, token);
                request.send(formData);
            }());
        }
    }
 
   	$('#reload-button').fadeIn();
}

function shiftCardsIds(start) {
    var cards = document.getElementsByClassName('package-card-square');
    for(var i = 0; i < cards.length; i++) {
        if(cards[i].id.split('_')[1] > start) {
            cards[i].id = "card_" + (cards[i].id.split('_')[1] - 1);
        }
    }
}

function removeFile(element) {
    var file = $(element).data("file");
    for(var i = 0; i < STORED_FILES.length; i++) {
        if(STORED_FILES[i].name == file) {
            STORED_FILES.splice(i, 1);
            shiftCardsIds(i);
            break;
        }
    }
    $(element).closest('.package-cell').fadeOut(300, function(){ $(this).remove()});
    validateForm();
}

function dragLeave(e) {
    e.stopPropagation();
    e.preventDefault();
    var filedrag = document.getElementsByTagName('main')[0];
    filedrag.style.backgroundColor = "white";
}

function dragOver(e) {
    e.stopPropagation();
    e.preventDefault();
    var filedrag = document.getElementsByTagName('main')[0];
    filedrag.style.backgroundColor = "grey";
}


function handleDrop(e) {
    e.stopPropagation();
    e.preventDefault();
    addFiles(e.dataTransfer.files);
    var filedrag = document.getElementsByTagName('main')[0];
    filedrag.style.backgroundColor = "white";
}

function addFiles(files) {
    var grid = document.getElementById("files-grid");
    for(var i = 0; i < files.length; i++) {
        var file = files[i];     
        var reader = new FileReader();
        STORED_FILES.push(file);             
        grid.appendChild(createFileCard(file.name, STORED_FILES.length - 1));
        $('#card_' + i).fadeIn(300);
        reader.readAsDataURL(file);
    }
    validateForm();
}

function setUpDragAndDrop() {
    var filedrag = document.getElementsByTagName('main')[0];
    filedrag.addEventListener("dragover", dragOver);
    filedrag.addEventListener("dragleave", dragLeave);
    filedrag.addEventListener("drop", handleDrop);
}

document.addEventListener("DOMContentLoaded", function(event) {
    var $select = null;
    $select = $('#select-repos').selectize({});
    document.getElementsByClassName("filesToUpload")[0].addEventListener('change', handleFileSelect, false);
    document.getElementById("files-form").addEventListener('submit', handleForm);
    var token = $("meta[name='_csrf']").attr("content"),
        header = $("meta[name='_csrf_header']").attr("content");
    $.ajaxSetup({
        beforeSend: function(xhr) {
            xhr.setRequestHeader(header, token);
        }
    });
    setUpDragAndDrop();
});