<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ include file="header.jsp" %>

<div id="bodyColumn">
<div class="center center-login">
	<h2><spring:message code="login.info"/></h2>

    <c:if test="${not empty error}">	
	<div class="alert alert-block alert-danger fade in">
        <button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
        <h4><spring:message code="login.error"/></h4>
        <p><spring:message code="general.info"/> ${sessionScope["SPRING_SECURITY_LAST_EXCEPTION"].message}</p>
    </div>
    </c:if>
    <c:if test="${not empty success}">	
	<div class="alert alert-block alert-success fade in">
        <button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
        <h4><spring:message code="logout.success"/></h4>
    </div>
    </c:if>
	<form action="login" method='POST'>
		<table class="login">
			<tr>
				<td>
					<input class="form-control" size="40" name='username' placeholder="<spring:message code='form.placeholder.username'/> " required >
				</td>
			</tr>
			<tr>
				<td>
					<input type='password' size="40" class="form-control" name='password' placeholder="<spring:message code='form.placeholder.password'/> " required />
				</td>
			</tr>
			<tr>
				<td style="text-align: center;">
					<input id="btn_submit" class="btn btn-info" name="submit" type="submit" value="<spring:message code="login.button.submit"/>" />
				</td>
			</tr>
			<tr>
				<td style="text-align: center;">
				    <input class="btn btn-danger" name="reset" type="reset" value="<spring:message code="form.button.reset"/>" />
				</td>
			</tr>
		</table>
		<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
	</form>
</div>
</div>
	
<script type="text/javascript">
	
	$(document).ready(function() {
		doFocus();
	});
	
	function doFocus()
	{
		if(!$.trim($('#username').val()).length)
		{
			$('#username').focus();
		}
		else if(!$.trim($('#password').val()).length)
		{
			$('#username').focus();
		}
		else
		{
			$('#btn_submit').focus();
		}
	}

</script>
    
<%@ include file="footer.jsp" %>
