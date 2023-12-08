
// wait for window to load
function handleOnLoad(resourceId) {
	var form = document.getElementById("myHelperForm");
	form.addEventListener("submit", function(event) {
		handleFormSubmit(event, resourceId);
	});
	getFields(resourceId);
}

function handleFormSubmit(event, resourceId) {
	 event.preventDefault();
	 var form = document.getElementById("myHelperForm");
	 var formData = new FormData(form);
	 var SET_FIELDS = {
			 type: 'setTaskViewFields',
			 resourceId: resourceId,
			 fields: []
	 }
	 for (var pair of formData.entries()) {
		 SET_FIELDS.fields.push({
			name: pair[0],
			value: pair[1]
		});
     }
     sendMessage(SET_FIELDS);
     window.close();
}

// handle messages from parent window
function handleMessage(event)
{
  var message = event.data;
  if (message && message.type == 'taskViewFieldValues' && message.fields) {
	  for (var i=0; i<message.fields.length; i++) {
		  var field = message.fields[i];
		  console.log("field.name " + field.name);
		  var input = document.getElementById(field.name);
		  if (input) {
		  	input.value = field.value;
		  }
	  }
  }
}
window.addEventListener("message", handleMessage, false);

function getFields(resourceId) {
	var GET_FIELDS = {
			type: 'getTaskViewFields',
			resourceId: resourceId
	}
	sendMessage(GET_FIELDS)
}

function sendMessage(message) {
	window.opener.postMessage(message, "*");
}


