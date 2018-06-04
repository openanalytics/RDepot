<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 5//EN">
<html>
  <head>
    <c:url value="/static" var="staticUrl"/>
    <link rel="shortcut icon" href="${staticUrl}/favicon.ico" type="image/x-icon">
    <link rel="stylesheet" type="text/css" href="${staticUrl}/vendor/Bootstrap/css/bootstrap.css">
		<link rel="stylesheet" type="text/css" href="${staticUrl}/vendor/Chosen/chosen.css" />
    <link rel="stylesheet" type="text/css" href="${staticUrl}/css/rrm.css">
    <title>R Depot</title>
    <meta name="_csrf" content="${_csrf.token}"/>
		<meta name="_csrf_header" content="${_csrf.headerName}"/>
    <script type="text/javascript" src="${staticUrl}/vendor/jQuery/jquery-2.1.1.min.js"></script>
    <script type="text/javascript" src="${staticUrl}/vendor/Bootstrap/js/bootstrap.js"></script>
    <script type="text/javascript" src="${staticUrl}/vendor/Chosen/chosen.jquery.js"></script>
    <script>
        $(document).ready(function()
        {
            $("img").tooltip();
            $("#logoutButton").tooltip();
            $('ul.nav > li').click(function (e)
            {
                $('ul.nav > li').removeClass('active');
                $(this).addClass('active');                
            });  
            var token = $("meta[name='_csrf']").attr("content"); 
						var header = $("meta[name='_csrf_header']").attr("content");
            $(document).ajaxSend(function(e, xhr, options) {
		      		xhr.setRequestHeader(header, token);
						});
        });
    </script>
  </head>
  <body>
    <c:choose> 
    <c:when test="${empty role}">
    <div id="banner">
     <div id="bannerLeft">R Depot</div>
     <div class="clear">
      <hr/>
     </div>
    </div>
    </c:when>
    <c:otherwise>
    <div class="navbar navbar-default">
        <div class="navbar-header">
          <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </button>
          <a class="navbar-brand" href="<c:url value='/manager' />"><img data-placement="bottom" data-toggle="tooltip" data-original-title="<spring:message code='application.name'/>" class="menu-logo" src="${staticUrl}/img/logo.png" > R Depot </a>
        </div>       
        <div class="navbar-collapse collapse">
          <ul class="nav navbar-nav">
            <li><a href="<c:url value='/manager' />"><spring:message code="menu.submit"/></a></li>
            <c:if test="${role > 2}">
                <li><a href="<c:url value='/manager/users' />"><spring:message code="menu.users"/></a></li>                        
            </c:if>
            <li class="dropdown">
                <a href="#" class="dropdown-toggle" data-toggle="dropdown" ><spring:message code="menu.repositories"/> <b class="caret"></b></a>
                <ul class="dropdown-menu">
                    <li><a href="<c:url value='/manager/repositories' />"><spring:message code="menu.general.overview"/></a></li>
                    <c:if test="${role > 2}">
                    	<li><a href="<c:url value='/manager/repositories/create' />"><spring:message code="menu.general.create"/></a></li>
	                      <li class="divider"></li>
	                      <li class="dropdown-header"><spring:message code="menu.general.maintainers"/></li>
	                      <li><a href="<c:url value='/manager/repositories/maintainers' />"><spring:message code="menu.general.overview"/></a></li>
	                      <li><a href="<c:url value='/manager/repositories/maintainers/create' />"><spring:message code="menu.general.create"/></a></li>
	                    </c:if>
                </ul>        
            </li>
            <li class="dropdown">
                <a href="#" class="dropdown-toggle" data-toggle="dropdown" ><spring:message code="menu.packages"/> <b class="caret"></b></a>
                <ul class="dropdown-menu">
                    <li><a href="<c:url value='/manager/packages' />"><spring:message code="menu.general.overview"/></a></li>
                    <c:if test="${role > 1}">
                        <li class="divider"></li>
                        <li class="dropdown-header"><spring:message code="menu.general.maintainers"/></li>
                        <li><a href="<c:url value='/manager/packages/maintainers' />"><spring:message code="menu.general.overview"/></a></li>
                        <li><a href="<c:url value='/manager/packages/maintainers/create' />"><spring:message code="menu.general.create"/></a></li>    
                    </c:if>          
                </ul>        
            </li>
            <li class="dropdown">
                <a href="#" class="dropdown-toggle" data-toggle="dropdown" ><spring:message code="menu.submissions"/> <b class="caret"></b></a>
                <ul class="dropdown-menu">
                    <li><a href="<c:url value='/manager/submissions' />"><spring:message code="menu.submissions.mine"/></a></li>
                    <c:if test="${role > 0}">
                        <li class="divider"></li>
                        <li><a href="<c:url value='/manager/submissions/all' />"><spring:message code="menu.submissions.all"/></a></li>
                    </c:if>          
                    <li class="divider"></li>
                    <li><a href="<c:url value='/manager' />"><spring:message code="menu.general.create"/></a></li>
                </ul>        
            </li>
        </ul>
        <ul class="nav navbar-nav navbar-right">
            <li><a class="menu-icon" href="<c:url value='/manager/settings' />"><img data-placement="bottom" data-toggle="tooltip" data-original-title="<spring:message code='menu.settings'/>" src="${staticUrl}/img/settings.png" ></a></li>
            <li><c:url var="logoutUrl" value="/logout"/>
    <form:form id="logoutForm" action="${logoutUrl}"
            method="post">
    <input id="logoutButton" type="image" src="${staticUrl}/img/logout.png" class="menu-icon" data-placement="bottom" data-toggle="tooltip" data-original-title="<spring:message code='menu.logout'/>"
               value="Log out" />
    <input type="hidden"
                name="${_csrf.parameterName}"
                value="${_csrf.token}"/>
    </form:form></li>
        </ul>
        </div><!--/.nav-collapse -->
        </div>
        </c:otherwise>
    </c:choose>
