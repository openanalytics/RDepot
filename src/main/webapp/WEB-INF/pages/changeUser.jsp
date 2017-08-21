<%@ include file="header.jsp" %>
     	<% if (session.getAttribute("currentUser") == null) { %>
     		<jsp:forward page="logIn.jsp" /> <% } %>
     	<%
     		if (((eu.openanalytics.rdepot.model.User)session.getAttribute("currentUser")).getRole().getValue() < 2) {
     	%>
     		<jsp:forward page="../index.jsp" /> <%
 	}
 %>
     	<center>
        <h2>
          Change user "<%=((eu.openanalytics.rdepot.model.User)request.getAttribute("changeUser")).getLogin()%>"
        </h2>
        <%
        	if (request.getAttribute("customMessage") != null) {
        %>
     			<%
     				if (request.getAttribute("customMessage") != "") {
     			%>
     				<%=request.getAttribute("customMessage")%> <%
 	} }
 %>
		<form name="changeUserForm" onsubmit="return validateForm()" method="post" action="/rwebapp/action.do">
        <input type="hidden" name="action" value="changeuser" />
        <input type="hidden" id="isactive" name="isactive" value="1" />
        <input type="hidden" id="rolearray" name="rolearray" value="" />
        <input type="hidden" id="userid" name="userid" value="<%=((eu.openanalytics.rdepot.model.User)request.getAttribute("changeUser")).getId()%>" />
          <table>
            <tr>
              <td>
                Name:
              </td>
              <td id="nRC">
                <input id="name" name="name" type="text" size="40" value="<%=((eu.openanalytics.rdepot.model.User)request.getAttribute("changeUser")).getName()%>">
              </td>
            </tr>
            <tr>
              <td>
                E-mail:
              </td>
              <td id="eRC">
                <input id="email" name="email" type="text" size="40" value="<%=((eu.openanalytics.rdepot.model.User)request.getAttribute("changeUser")).getEmail()%>">
                <%
                	if (request.getAttribute("emailError") != null) {
                %>
     			<%
     				if (request.getAttribute("emailError") != "") {
     			%>
     				<br /><%=request.getAttribute("emailError")%> <%
 	} }
 %>
              </td>
            </tr>
            <tr>
              <td>
                Login:
              </td>
              <td id="lRC">
                <input id="login" name="login" type="text" size="40" value="<%=((eu.openanalytics.rdepot.model.User)request.getAttribute("changeUser")).getLogin()%>">
                <%
                	if (request.getAttribute("loginError") != null) {
                %>
     			<%
     				if (request.getAttribute("loginError") != "") {
     			%>
     				<br /><%=request.getAttribute("loginError")%> <%
 	} }
 %>
              </td>
            </tr>
            <tr>
              <td>
                New password (optional):
              </td>
              <td id="p1RC">
                <input id="password1" name="password1" type="password" size="40" value="">
              </td>
            </tr>
            <tr>
              <td>
                Confirm new password:
              </td>
              <td id="p2RC">
                <input id="password2" name="password2" type="password" size="40">
              </td>
            </tr>
            <tr>
              <td>
                Role:
              </td>
              <td>
                <select onchange="checkRoleSelector()" id="roleSelector" name="role">
        			<%
        				if (request.getAttribute("allRoles") != null) {
        			%>
     					<%
     						if (request.getAttribute("allRoles") != "") {
     					%>
     						<%=request.getAttribute("allRoles")%> <%
 	} }
 %>
      			</select>
              </td>
            </tr>
            <tr id="repositoryRow">
            	<td>Repository:</td>
            	<td>
                		<select id="repositorySelector" onchange="changeRepository()" name="repository">
        				<%
        					if (session.getAttribute("userRepositories") != null) {
        				%>
     					<%
     						if (session.getAttribute("userRepositories") != "") {
     					%>
     					<%=session.getAttribute("userRepositories")%> <%
 	} }
 %>
      					</select>
              	</td>
            </tr>
            <tr id="packageRow">
            	<td>Packages:</td>
            	<td>
        				<%
        					if (request.getAttribute("userPackages") != null) {
        				%>
     					<%
     						if (request.getAttribute("userPackages") != "") {
     					%>
     					<%=request.getAttribute("userPackages")%> <%
 	} }
 %>
              	</td>
            </tr>
            <tr id="repositoryMaintainerRow">
            	<td>Repositories:</td>
            	<td>
        				<%
        					if (request.getAttribute("maintainerRepositories") != null) {
        				%>
     					<%
     						if (request.getAttribute("maintainerRepositories") != "") {
     					%>
     					<%=request.getAttribute("maintainerRepositories")%> <%
 	} }
 %>
              	</td>
            </tr>
            <tr>
              <td>
                Active:
              </td>
              <td>
                <input id="checkActive" name="active" type="checkbox" checked="checked" size="40" onchange="changeActive()">
              </td>
            </tr>
            <tr>
              <td id="submitRC" colspan="2" style="text-align: center;">
                <input name="submitted" type="submit" value="Submit"><br/>				
              </td>
            </tr>
          </table>
        </form>
        </center>
        <script type="text/javascript">
        var roleArray = [];
        
        function validateForm()
		{
			var x = document.forms["changeUserForm"]["name"].value;
			if ((x==null) || (x==""))
  			{
  				document.getElementById("nRC").innerHTML="<input id=\"name\" name=\"name\" type=\"text\" size=\"40\"><br />Please fill out a name.";
  				return false;
  			}
  			else
  			{
  				document.getElementById("nRC").innerHTML="<input id=\"name\" name=\"name\" type=\"text\" size=\"40\" value=\""+x+"\">";
  			}
  			x = document.forms["changeUserForm"]["email"].value;
			if ((x==null) || (x.indexOf("@") < 2) || (x.indexOf(".") < 0))
  			{
  				document.getElementById("eRC").innerHTML="<input id=\"email\" name=\"email\" type=\"text\" size=\"40\"><br />Please fill out a valid e-mail address.";
  				return false;
  			}
  			else
  			{
  				document.getElementById("eRC").innerHTML="<input id=\"email\" name=\"email\" type=\"text\" size=\"40\" value=\""+x+"\">";
  			}
  			x = document.forms["changeUserForm"]["login"].value;
			if ((x==null) || (x==""))
  			{
  				document.getElementById("lRC").innerHTML="<input id=\"login\" name=\"login\" type=\"text\" size=\"40\"><br />Please fill out a login.";
  				return false;
  			}
  			else
  			{
  				document.getElementById("lRC").innerHTML="<input id=\"login\" name=\"login\" type=\"text\" size=\"40\" value=\""+x+"\">";
  			}
  			x = document.forms["changeUserForm"]["password1"].value;
  			var y = document.forms["changeUserForm"]["password2"].value;
			if (x!="")
  			{
  				document.getElementById("p1RC").innerHTML="<input id=\"password1\" name=\"password1\" type=\"password\" size=\"40\" value=\""+x+"\">";
  				
  				if ((y==null) || (y==""))
  				{
  					document.getElementById("p2RC").innerHTML="<input id=\"password2\" name=\"password2\" type=\"password\" size=\"40\"><br />Please confirm the password.";
  					return false;
  				}
  				else
  				{
  					if(x != y)
  					{
  						document.getElementById("p1RC").innerHTML="<input id=\"password1\" name=\"password1\" type=\"password\" size=\"40\"><br />Passwords do not match.";
  						document.getElementById("p2RC").innerHTML="<input id=\"password2\" name=\"password2\" type=\"password\" size=\"40\"><br />Passwords do not match.";
  						return false;
  					}
  					document.getElementById("p2RC").innerHTML="<input id=\"password2\" name=\"password2\" type=\"password\" size=\"40\" value=\""+y+"\">";
  					document.getElementById("p1RC").innerHTML="<input id=\"password1\" name=\"password1\" type=\"password\" size=\"40\" value=\""+x+"\">";
  				}
  			
  			}
  			if (y!="")
  			{
  				document.getElementById("p2RC").innerHTML="<input id=\"password2\" name=\"password2\" type=\"password\" size=\"40\" value=\""+y+"\">";
  				
  				if ((x==null) || (x==""))
  				{
  					document.getElementById("p1RC").innerHTML="<input id=\"password1\" name=\"password1\" type=\"password\" size=\"40\"><br />Please fill out a password.";
  					return false;
  				}
  				else
  				{
  					if(x != y)
  					{
  						document.getElementById("p1RC").innerHTML="<input id=\"password1\" name=\"password1\" type=\"password\" size=\"40\"><br />Passwords do not match.";
  						document.getElementById("p2RC").innerHTML="<input id=\"password2\" name=\"password2\" type=\"password\" size=\"40\"><br />Passwords do not match.";
  						return false;
  					}
  					document.getElementById("p2RC").innerHTML="<input id=\"password2\" name=\"password2\" type=\"password\" size=\"40\" value=\""+y+"\">";
  					document.getElementById("p1RC").innerHTML="<input id=\"password1\" name=\"password1\" type=\"password\" size=\"40\" value=\""+x+"\">";
  				}
  			}
  			
  			return setRoleArray();
		}
		
		function changeActive()
		{
		
			var x = $('#isactive').val();
			if(x=="0")
			{
				$('#isactive').val("1");
			}
			else
			{
				$('#isactive').val("0");
			}
			
		
		}
		
		function addToRoleArray(id)
		{
			
			roleArray.push(id);
					
		}
		
		function removeFromRoleArray(id)
		{
			roleArray = jQuery.grep(roleArray, function(value) {return value != id;});
		}
		
		function changePackage(id)
		{
			if($("#package"+id).is(":checked"))
			{
				addToRoleArray(id);
			}
			else
			{
				removeFromRoleArray(id);
			}
		}
		
		function changeRepos(id)
		{
			if($("#repository"+id).is(":checked"))
			{
				addToRoleArray(id);
			}
			else
			{
				removeFromRoleArray(id);
			}
		}
		
		function setRoleArray()
		{
			var role = $("#roleSelector option:selected").val();
			if(role == "1")
			{
				if(roleArray.length != 0)
				{
					$("#rolearray").val(roleArray.toString());
					$("#submitRC").html("<input name=\"submitted\" type=\"submit\" value=\"Submit\"><br/>");
				}
				else
				{
					//$("#submitRC").html("Please make sure there are packages selected to maintain.<br /><input name=\"submitted\" type=\"submit\" value=\"Submit\"><br/>");
					//return false;
				}
			}
			else if(role == "2")
			{
				if(roleArray.length != 0)
				{
					$("#rolearray").val(roleArray.toString());
					$("#submitRC").html("<input name=\"submitted\" type=\"submit\" value=\"Submit\"><br/>");
				}
				else
				{
					//$("#submitRC").html("Please make sure there are repositories selected to maintain.<br /><input name=\"submitted\" type=\"submit\" value=\"Submit\"><br/>");
					//return false;
				}
			}
			return true;
		}
		
		function checkRoleSelector()
		{
			var v = $("#roleSelector").val();
			if(v=="0")
			{
				$('#repositoryRow').hide();
   				$('#packageRow').hide();
   				$('#repositoryMaintainerRow').hide();
			}
			else if(v=="1")
			{
				$('#repositoryRow').show();
   				$('#packageRow').show();
   				$('#repositoryMaintainerRow').hide();
			}
			else if(v=="2")
			{
				$('#repositoryRow').hide();
   				$('#packageRow').hide();
   				$('#repositoryMaintainerRow').show();
			}
			else
			{
				$('#repositoryRow').hide();
   				$('#packageRow').hide();
   				$('#repositoryMaintainerRow').hide();
			}
			changeRepository();
			roleArray = [];
		
		}
		
		function changeRepository()
		{
		
			var v = $("#repositorySelector").val();
			
			$('tr[class*="packageFromRepository"]').hide();
			$('.packageFromRepository'+v).show();
		
		}
		
		function setActive()
		{
		
			var activeB = <%=((boolean)((eu.openanalytics.rdepot.model.User)request.getAttribute("changeUser")).isActive())%>;
			if(activeB)
			{
				$("#isactive").val("1");
			}
			else
			{
				$("#isactive").val("0");
			
			}
			$("#checkActive").prop("checked", activeB);
		
		}
		
		$(document).ready(function()
		{
			setActive();
			checkRoleSelector();
			var currentUserRole = "<%=((eu.openanalytics.rdepot.model.User)session.getAttribute("currentUser")).getRole().getValue()%>";
			if(currentUserRole == "2")
			{
				$("#name").prop("disabled", true);
				$("#login").prop("disabled", true);
				$("#email").prop("disabled", true);
				$("#password1").prop("disabled", true);
				$("#password2").prop("disabled", true);
				$("#checkActive").prop("disabled", true);
			}
			var changeUserRole = $("#roleSelector :selected").val();
			if(changeUserRole == "1")
			{
				$('input:checked[id*="package"]').each(function() {addToRoleArray(this.id.split("package")[1]);});
			}
			else if(changeUserRole == "2")
			{
				$('input:checked[id*="repository"]').each(function() {addToRoleArray(this.id.split("repository")[1]);});
			}
			
		});
        </script>
   </div>
   <div id="sidebar">
   <h5>Actions:</h5>
   <ul>
    	<li><a href="/rwebapp/action.do?action=useractions">Go back</li>
    	<li><a href="/rwebapp/action.do?action=logout">Log out</a></li>
   </ul>
  </div>
  </body>
</html>