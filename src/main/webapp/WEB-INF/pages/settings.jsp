<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ include file="header.jsp" %>
<div id="bodyColumn">
    <h4><spring:message code="settings.title"/></h4>
    <div class="center">
	<c:if test="${not empty success}">	
	<div class="alert alert-block alert-success fade in">
        <button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
        <h4><spring:message code="general.success"/></h4>
        <p><spring:message code="general.info"/> ${success}</p>
    </div>
    </c:if>
	<c:if test="${not empty warning}">
	<div class="alert alert-block alert-warning fade in">
        <button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
        <h4><spring:message code="general.warning"/></h4>
        <p><spring:message code="general.info"/> ${warning}</p>
    </div>
	</c:if>
	<c:if test="${not empty error}">
	<div class="alert alert-block alert-danger fade in">
        <button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
        <h4><spring:message code="general.error"/></h4>
        <p><spring:message code="general.info"/> ${error}</p>
    </div>
	</c:if>

    <h3><spring:message code='form.action.changelanguage'/></h3>
    
    <div class="btn-group" data-toggle="buttons">
        <label class="btn btn-default">
            <input type="radio" id="en"> English
        </label>
        <label class="btn btn-default">
            <input type="radio" id="nl"> Nederlands
        </label>
        <label class="btn btn-default">
            <input type="radio" id="fr"> fran&ccedil;ais
        </label>
        <label class="btn btn-default">
            <input type="radio" id="de"> Deutsch
        </label>
    </div>
</div>
<script type="text/javascript">
    
    $(document).ready(function()
    { 
        $("input:radio").button(); 
        $("#${locale}").parent().removeClass().addClass("btn btn-info");
        
    });
    
    $("input:radio").change(function(){  window.location="<c:url value='/manager/settings' />/?lang="+this.id; });

</script>
<%@ include file="footer.jsp" %>
