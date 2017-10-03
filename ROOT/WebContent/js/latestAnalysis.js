function getLatestAnalysis(callback){
	var userID = readCookie('user_id');
	$.ajax({
		type: 'POST',
		url: 'VC/rest/getAnalysis',
		contentType: 'text/plain',
        //Supply the JWT auth token
        headers: {"Authorization": localStorage.getItem('auth_token')}, 
		data: userID,
		success: function(result){
					callback(result);
				},
		error: function(result){
					alert('An error occurred fetching data.');
					//callback(result);
				}
		});	
}

function readCookie(name) {
	var nameEQ = name + "=";
	var ca = document.cookie.split(';');
	for(var i=0;i < ca.length;i++) {
		var c = ca[i];
		while (c.charAt(0)==' ') c = c.substring(1,c.length);
			if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
	}
			
	return null;
}
