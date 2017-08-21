<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ include file="header.jsp" %>

<div id="bodyColumn">
    <c:if test="${not empty error}">
	    <div class="errorblock">
	
		    <spring:message code="general.error"/>
		    <br />
		    <spring:message code="general.info"/>
		    ${error}
		
	    </div>
    </c:if>
   
    <div class="feed">
        <div class="PackageDetail">
            <h2>${user.name}</h2>
            <div class="breadcrumb">
                <a href="<c:url value='users' />"><spring:message code="user.title"/></a> 
                &gt; ${user.name}
           </div>
           <p class="description">
               <spring:message code="general.overview"/>
           </p>
           <table class="author_info">
               <tr>
                   <td><spring:message code="general.lastloggedin.on"/></td>
                   <td>${lastloggedin.getDate()}</td>
               </tr>
               <tr>
                   <td><spring:message code="general.created.on"/></td>
                   <td>${created.getDate()}</td>
               </tr>
               <tr>
                   <td><spring:message code="table.header.email"/></td>
                   <td>${user.email}</td>
               </tr>
               <tr>
                   <td><spring:message code="table.header.username"/></td>
                   <td>${user.login}</td>
               </tr>
               <tr>
                   <td><spring:message code="table.header.role"/></td>
                   <td>${user.role.name}</td>
               </tr>
               <tr>
                   <td><spring:message code="table.header.active"/></td>
                   <td>
                        <c:choose>
                           <c:when test="${user.isActive()}">
                              <i class="glyphicon glyphicon-check"></i>
                           </c:when>
                           <c:otherwise>
                                <i class="glyphicon glyphicon-unchecked"></i>
                           </c:otherwise>
                       </c:choose>
                   </td>
               </tr>
           </table>
           <p class="description">
               <spring:message code="general.feed"/> 
           </p>
           <c:forEach items="${events.keySet()}" var="date" >
               <div class="day">
		           <h3>${date}</h3>
		           <c:forEach items="${events.get(date)}" var="event" >
		               <div class="event">
                           <i class='event-${event.getChangedVariable()}'></i>
                           <span class="time">${event.getTime()}</span>
                           <c:choose>
                               <c:when test="${event.getValueBefore() == ''}">
                                  <span class="action">${event.getChangedVariable()} ${event.getValueAfter()} </span>
                               </c:when>
                               <c:otherwise>
                                  <span class="action">${event.getChangedVariable()} <spring:message code="general.changed.from"/> "${event.getValueBefore()}" <spring:message code="general.changed.to"/> "${event.getValueAfter()}" </span>
                               </c:otherwise>
                           </c:choose>
			                - <a class="user" href="<c:url value='/manager/users' />/${event.getChangedBy().getLogin()}">${event.getChangedBy().getName()}</a><br>
			           </div>
		           </c:forEach>
       		   </div>
		    </c:forEach>   
        </div>
    </div>		
</div>
<script type="text/javascript">
    
    var events = { "created": "asterisk", "added": "plus", "submitted": "upload", "removed": "trash" };
    
    $(document).ready(function()
    {
        $("[class^=event-]").each(function()
        {
            var event = events[$(this).attr("class").split("-")[1]];
            if(event == undefined)
            {
                $(this).addClass("glyphicon glyphicon-edit");
            }
            else
            {
                $(this).addClass("glyphicon glyphicon-" + event);
            }
        });
        $("span.action").each(function()
        {
            $(this).html(capitaliseFirstLetter($(this).html()));
        });
    });
    
    function capitaliseFirstLetter(string)
    {
        return string.charAt(0).toUpperCase() + string.slice(1);
    }
</script>
<%@ include file="footer.jsp" %>
