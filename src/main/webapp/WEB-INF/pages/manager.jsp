<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ include file="header.jsp" %>
<div id="bodyColumn">
<div class="center center-submit">
	<c:if test="${not empty success}">	
	<div class="alert alert-block alert-success fade in">
        <button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
        <h4><spring:message code="general.success"/></h4>
        <p><spring:message code="general.info"/> <br> ${success}</p>
    </div>
    </c:if>
	<c:if test="${not empty warning}">
	<div class="alert alert-block alert-warning fade in">
        <button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
        <h4><spring:message code="general.warning"/></h4>
        <p><spring:message code="general.info"/> <br> ${warning}</p>
    </div>
	</c:if>
	<c:if test="${not empty error}">
	<div class="alert alert-block alert-danger fade in">
        <button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
        <h4><spring:message code="general.error"/></h4>
        <p><spring:message code="general.info"/>  ${error}</p>
    </div>
	</c:if>
    <br>
    <h2><spring:message code="manager.title"/></h2>
    <c:url value="/manager/packages/submit" var="submitPackages" />
    <form:form enctype="multipart/form-data" modelAttribute="multiUploads" method="POST" action="${submitPackages}?${_csrf.parameterName}=${_csrf.token}">
	    <table class="submitpackages" >
	    <tr>
	        <th><spring:message code="table.header.file"/></th>
	        <th><spring:message code="table.header.repository"/></th>
	        <th><spring:message code="table.header.actions"/></th></tr>
	    <tr id="submissionRow0">
  			<td>
  			    <span class="btn btn-success fileinput-button">
                    <i class="glyphicon glyphicon-folder-open"></i>
                    <span> <spring:message code='form.input.files.multiple'/></span>
                    <form:input path="uploadRequests[0].fileData" onchange="validateForm()" style="display: inline" type="file" multiple="true" />
                </span>
			    <span style="display:none" id="uploadRequests0.fileData_span"><spring:message code="form.invalid.extension"/></span>
  			</td>
		    <td>
    			<form:select path="uploadRequests[0].repository" class="form-control" items="${repositories}" itemValue="name" itemLabel="name" />
  			</td>
  			<td>
  			    <button type="button" id="addChangesBtn0" onclick="toggleChanges(0)" class="btn btn-success">
  			        <span class="glyphicon glyphicon-list-alt"></span> <spring:message code='submission.changes'/>
  			    </button>
  			</td>
	    </tr>
	    <tr id="changesRow0">
	        <td colspan="3" id="changes0"></td>
	    </tr>
	    <tr>
  			<td colspan="3" >
    			<button type="button" id="addRowBtn" disabled="disabled" onclick="addRow()" class="btn btn-success">
    			    <span class="glyphicon glyphicon-chevron-down"></span> <spring:message code='package.add'/>
    			</button>
  			</td>
	    </tr>
	    <tr>
  			<td colspan="3" style="text-align: center;">
    			<button data-loading-text="<spring:message code='form.button.submitting'/>" id="submitBtn" type="submit" disabled="disabled" class="btn btn-info" >
    			    <span class="glyphicon glyphicon-upload"></span> <spring:message code='form.button.submit'/>
    			</button>
    			<br/>				
  			</td>
	    </tr>
	    </table>
    </form:form>
    </div>
</div>

<script type="text/javascript">
    var numberOfFileRows = 1;
    
    $(document).ready(function()
    {
        if(getSize() <= 1)
        {
            $("#addRowBtn").prop("disabled", true);
        }
        else
        {
            $("#addRowBtn").prop("disabled", false);
        }
        $("#changesRow0").hide();
        $("#addChangesBtn0").prop("disabled", true);

        $("#submitBtn").button();
        
        $('select#uploadRequests0\\.repository').chosen(
        {
            no_results_text: "Nothing found with",
            search_contains: true
        });
    });
    
    function toggleChanges(id)
    {
        
        $("#changesRow"+String(id)).toggle();
    }
    
    function getSize()
    {
        return $('select#uploadRequests0\\.repository option').length;
    }
    

    
    function addRow()
    {               
        var newRowData = '<tr id="submissionRow'+numberOfFileRows+'">';
        newRowData += '<td>';
        newRowData += '<span class="btn btn-success fileinput-button">';
        newRowData += '<i class="glyphicon glyphicon-folder-open"></i>';
        newRowData += '<span> <spring:message code="form.input.files.multiple"/></span>';
        newRowData += '<input id="uploadRequests'+numberOfFileRows+'.fileData" name="uploadRequests['+numberOfFileRows+'].fileData" onchange="validateForm()" type="file" multiple >';
        newRowData += '</span>';
        newRowData += '<span style="display:none" id="uploadRequests'+numberOfFileRows+'.fileData_span">';
        newRowData += '<spring:message code="form.invalid.extension"/>';
        newRowData += '</span>';
        newRowData += '</td>';
        newRowData += '<td id="D'+numberOfFileRows+'">';
        newRowData += '</td>';
        newRowData += '<td>';
        newRowData += '<button type="button" id="addChangesBtn'+numberOfFileRows+'" onclick="toggleChanges('+numberOfFileRows+')" class="btn btn-success">';
        newRowData += '<span class="glyphicon glyphicon-list-alt"></span> <spring:message code='submission.changes'/>';
        newRowData += '</button>';
        newRowData += '<button type="button" id="remRowBtn'+numberOfFileRows+'" class="btn btn-danger" onclick="removeRow()">';
        newRowData += '<span class="glyphicon glyphicon-remove"></span>';
        newRowData += '</button>';
        newRowData += '</td>';
        newRowData += '</tr>';
        newRowData += '<tr style="display: none;" id="changesRow'+numberOfFileRows+'">';
	    newRowData += '<td colspan="3" id="changes'+numberOfFileRows+'"></td>';
	    newRowData += '</tr>';
        var newRow = $(newRowData);
        newRow.insertAfter($("#changesRow"+(numberOfFileRows-1)));
        $('select#uploadRequests0\\.repository').clone().attr('id', 'uploadRequests'+numberOfFileRows+'.repository').attr('name', 'uploadRequests['+numberOfFileRows+'].repository').css("display", "inline").appendTo('#D'+numberOfFileRows);
        for(var count = 1; count < numberOfFileRows; count++)
        {
            $("#remRowBtn"+count).prop("disabled", true);
        }
        var c = numberOfFileRows;
        $('select#uploadRequests'+c+'\\.repository').chosen(
        {
            no_results_text: "Nothing found with",
            search_contains: true
        });
        numberOfFileRows++;
        if(numberOfFileRows >= getSize())
        {
            $("#addRowBtn").prop("disabled", true);
        }
        else
        {
            $("#addRowBtn").prop("disabled", false);
        }
        validateForm();
    }
    
    function validateForm()
	{
	    var submitcheck = false;
	    $("input[id$='fileData']").each(function(index)
	    {
	        $("#changes"+index).html("");
	        var files = this.files;
            if(files.length > 0)
            {
                var spancheck = false;
                for(var i = 0; i < files.length; i++)
                {   
                    var newField = '<div class="form-group">';
                    newField += '<label for="uploadRequests'+index+'.changes">'+ files[i].name +':</label>';
                    newField += '<input id="uploadRequests'+index+'.changes" name="uploadRequests['+index+'].changes" class="form-control" placeholder="<spring:message code="form.placeholder.changes"/>" size="40" >';
                    newField += '</div>';
                    $("#changes"+index).html($("#changes"+index).html() + newField);
                    var dotArray = files[i].name.split('.');
                    var gz = dotArray.pop();
                    if(gz == "gz")
                    {
                        var tar = dotArray.pop();
                        if(tar != "tar")
                        {
                            spancheck = true;
                            submitcheck = true;
                        }
                    }
                    else
                    {
                        spancheck = true;
                        submitcheck = true;
                    }
                }
                if(spancheck)
                {
                    $("#changes"+index).html("");
                    $('#'+this.id.split('.')[0]+'\\.fileData_span').show();
                }
                else
                {
                    $('#'+this.id.split('.')[0]+'\\.fileData_span').hide();
                }
                $("#addChangesBtn"+index).prop("disabled", spancheck);
            }
            else
            {
                submitcheck = true;
                $("#addChangesBtn"+index).prop("disabled", submitcheck);
            }
	    });
	    $("#submitBtn").prop("disabled", submitcheck);
		
	}
	
  	function removeRow()
	{
	    numberOfFileRows--;
		$("#submissionRow"+numberOfFileRows).remove();
		$("#changesRow"+numberOfFileRows).remove();
        $("#remRowBtn"+(numberOfFileRows-1)).prop("disabled", false);
        if(numberOfFileRows >= getSize())
        {
            $("#addRowBtn").prop("disabled", true);
        }
        else
        {
            $("#addRowBtn").prop("disabled", false);
        }
        validateForm();
	}
</script>
<%@ include file="footer.jsp" %>
