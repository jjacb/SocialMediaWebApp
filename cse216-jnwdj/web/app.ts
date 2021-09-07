// Prevent compiler errors when using jQuery.  "$" will be given a type of 
// "any", so that we can use it anywhere, and assume it has any fields or
// methods, without the compiler producing an error.
//var $: any;

// The 'this' keyword does not behave in JavaScript/TypeScript like it does in
// Java.  Since there is only one NewEntryForm, we will save it to a global, so
// that we can reference it from methods of the NewEntryForm in situations where
// 'this' won't work correctly.
var newEntryForm: NewEntryForm;

/**
 * NewEntryForm encapsulates all of the code for the form for adding an entry
 */
class NewEntryForm {
    /**
     * To initialize the object, we say what method of NewEntryForm should be
     * run in response to each of the form's buttons being clicked.
     */
    constructor() {
        $("#addCancel").click(this.clearForm);
        $("#addButton").click(this.submitForm);
        $("#commentButton").click(this.submitCommentForm);
        $("#cancelComment").click(this.clearForm);
        $("#profileButton").click(this.goToProfile);
    }
    /**
     * Code for "add comment" button to run on click
     */
    submitCommentForm() {
        let cmt = "" + $("#newComment").val();
        let ses = window.sessionStorage.getItem("session");
        let uid = window.sessionStorage.getItem("uid");
        let id = window.sessionStorage.getItem("CommentMid");
        $.ajax({
            type: "POST",
            url: "/comments",
            dataType: "json",
            data: JSON.stringify({ cComment: cmt , cMid: id, session: ses, cUid: uid }),
            success: newEntryForm.onSubmitResponse
        });  
    }
    /**
     * Code for when *specifically* the profile button in the top right is clicked, different method for when uids are clicked
     */
    goToProfile(){
        let ses = "" + window.sessionStorage.getItem("uid");
        window.sessionStorage.setItem("ClickedUid", ses);
        window.location.replace("/profile.html");
    }
    /**
     * Clear the form's input fields
     */
    clearForm() {
        $("#newMessage").val("");
        $("#cancelComment").val("");
        $("#newComment").val("");
        // reset the UI
        $("#addElement").hide();
        $("#addComment").hide();
        $("#editElement").hide();
        $("#showElements").show();
    }

    /**
     * Check if the input fields are valid, and if so, do an AJAX call.
     */
    submitForm() {
        // get the values of the fields, force them to be strings, and check 
        // that not empty
        let uid = window.sessionStorage.getItem("uid");
        let msg = "" + $("#newMessage").val();

        if (msg === "") {
            window.alert("Error: title or message is not valid");
            return;
        }
        let ses = window.sessionStorage.getItem("session");
       
        var input = $('#mfile').prop('files')[0];
        

        $.ajax({
            type: "GET",
            url: "https://api1.webpurify.com/services/rest/?method=webpurify.live.check&api_key=21a1f014fa91661f4eeca831f2ed1457&format=json&text=" + msg,
            dataType: "jsonp",
            success: function(data: any){
                if(data.err !== undefined){
                    console.log("Outdated API key");
                    console.log("error: " + data.err);
                }
                else if(data.rsp.found === "1"){
                    window.alert("Profanity Detected. Message not posted.");
                    return;
                }

                else{
                    var reader  = new FileReader();
                    reader.onload = function(){
                        var encodes = reader.result;
                        var b64 = (<String>encodes).split(",")[1];
                        var fileName = input.name;
                        $.ajax({
                            type: "POST",
                            url: "/messages",
                            dataType: "json",
                            data: JSON.stringify({ mMessage: msg, mUid: uid, session: ses, dFile: b64, dFileName: fileName }),
                            success: newEntryForm.onSubmitResponse
                        });
                    };

                    if (input) {
                        reader.readAsDataURL(input);
                    }

        // set up an AJAX post.  When the server replies, the result will go to
        // onSubmitResponse
                    else{
                        $.ajax({
                            type: "POST",
                            url: "/messages",
                            dataType: "json",
                            data: JSON.stringify({ mMessage: msg, mUid: uid, session: ses, dFile: "", dFileName: "" }),
                            success: newEntryForm.onSubmitResponse
                        });
                    }
                }
            }
        });
        
        
    }

    /**
     * onSubmitResponse runs when the AJAX call in submitForm() returns a 
     * result.
     * 
     * @param data The object returned by the server
     */
    private onSubmitResponse(data: any) {
        // If we get an "ok" message, clear the form
        if (data.mStatus === "ok") {
            newEntryForm.clearForm();
        }
        // Handle explicit errors with a detailed popup message
        else if (data.mStatus === "error") {
            window.alert("The server replied with an error:\n" + data.mMessage);
        }
        // Handle other errors with a less-detailed popup message
        else {
            window.alert("Unspecified error");
        }
    }
} // end class NewEntryForm


// a global for the main ElementList of the program.  See newEntryForm for 
// explanation
    var mainList: ElementList;

/**
 * ElementList provides a way of seeing all of the data stored on the server.
 */
class ElementList {
    /**
     * refresh is the public method for updating messageList
     */
    refresh() {
        // Issue a GET, and then pass the result to update()
        let ses = window.sessionStorage.getItem("session");
        if (ses == "" || ses == null) {
            window.alert("Error: Please login first");
            return;
        }
        $.ajax({
            type: "POST",
            url: "/getmessages",
            data: JSON.stringify({ session: ses }),
            dataType: "json",
            success: mainList.update
        });
    }

    /**
     * update is the private method used by refresh() to update messageList
     */
    private update(data: any) {
        let ses = window.sessionStorage.getItem("session");
        $("#messageList").html("<table>");
        for (let i = 0; i < data.mData.length; ++i) {
            $.ajax({
                type: "POST",
                async: false,//DB: there has to be a better way than this line, this is why the table takes long to load
                //if you remove it you'll see why it's here: ajax is inherintly async and doesnt really work in a loop like this
                url: "/getmessages/" + data.mData[i].mMid + "/votes",
                data: JSON.stringify({ session: ses }),
                dataType: "json",
                success: mainList.saveVotes
            });

            var link = data.mData[i].mMessage.split('@');

            
            if(data.mData[i].dFile == ""){
                $("#messageList").append("<tr><td>" + data.mData[i].mMessage + "<pre></pre><a class='profileLink' data-value='"
                    + data.mData[i].mUid + "'>Posted By User: " + data.mData[i].mUid + "</a> With Votes: " + window.sessionStorage.getItem("numVotes")
                    + "<pre></pre>Links found: " + "<div id = 'links" + i + "'></div>" + "</td>" + mainList.buttons(data.mData[i].mMid, data.mData[i].mUid) + "</tr>");
            }
            else{
                var fileType = data.mData[i].dFileName.substr(data.mData[i].dFileName.length - 3);
                if(fileType == 'png' || fileType == 'jpg'){
                    $("#messageList").append("<tr><td>" + data.mData[i].mMessage + "<pre></pre><a class='profileLink' data-value='"
                        + data.mData[i].mUid + "'>Posted By User: " + data.mData[i].mUid + "</a> With Votes: " + window.sessionStorage.getItem("numVotes")
                        + "<pre></pre> <img src='data:image/" + fileType + ";base64, " + data.mData[i].dFile + "' />"
                        + "<pre></pre>Links found: " + "<div id = 'links" + i + "'></div>" + "</td>" + mainList.buttons(data.mData[i].mMid, data.mData[i].mUid) + "</tr>");
                }

                else if(fileType == 'pdf'){
                    $("#messageList").append("<tr><td>" + data.mData[i].mMessage + "<pre></pre><a class='profileLink' data-value='"
                        + data.mData[i].mUid + "'>Posted By User: " + data.mData[i].mUid + "</a> With Votes: " + window.sessionStorage.getItem("numVotes")
                        + "<pre></pre> <iframe src='data:application/" + fileType + ";base64, " + data.mData[i].dFile + "' height='650px' width = '500px'/>"
                        + "<pre></pre>Links found: " + "<div id = 'links" + i + "'></div>" + "</td>" + mainList.buttons(data.mData[i].mMid, data.mData[i].mUid) + "</tr>");
                }
            }
            
            for(var k = 1; k < link.length; k++){
                var newLink = "";
                if(link[k].charAt(0) == 'c' || link[k].charAt(0) == 'm'){
                    newLink = newLink + link[k].charAt(0);
                    if(link[k].charAt(1) >= '0' && link[k].charAt(1) <= '9'){
                        var s = 1;
                        while(link[k].charAt(s) >= '0' && link[k].charAt(s) <= '9'){
                            newLink = newLink + link[k].charAt(s);
                            s = s + 1;
                        }
                    }
                }
                $("#links" + i).append("<a class = 'mLink' data-value='" + newLink + "'>" + newLink + " </a>");
            }
        }
        $("#messageList").append("</table>");
        // Find all of the Delete buttons, and set their behavior
        $(".delbtn").click(mainList.clickDelete);
        // Find all of the Edit buttons, and set their behavior
        $(".editbtn").click(mainList.clickEdit);
        $(".upvote").click(mainList.clickUpvote);
        $(".downvote").click(mainList.clickDownvote);
        $(".profileLink").click(mainList.goToOtherProfile);
        $(".viewComment").click(mainList.clickViewComment);
        $(".addCmt").click(mainList.clickAddComment);
        $(".mLink").click(mainList.clickLink);
        $(".flagged").click(mainList.flagMessage);
    }

    /**
    * buttons() adds all the buttons to the HTML for each
    * row
    */
    private buttons(id: string, uid: string): string {
        let logUid = window.sessionStorage.getItem("uid");
        let stringUid = uid.toString();
        
        if(logUid !== stringUid){
            return "<td><button class='viewComment btn btn-light' data-value='" + id
                + "'>View Comments</button></td>" 
                + "<td><button class='addCmt btn btn-light' data-value='" + id
                + "'>Add Comment</button></td>"
                + "<td><button class='upvote btn btn-light' data-value='" + id
                + "'><img src=up.jpg height='15' /></button></td>"
                + "<td><button class='downvote btn btn-light' data-value='" + id
                + "'><img src=down.jpg height='15' /></button></td>"
                + "<td><button class='flagged btn btn-light' data-value ='" + id
                + "'>Flag</button></td>";
        }
        else{
            return "<td><button class='editbtn btn btn-light' data-value='" + id
                + "'>Edit</button></td>"
                + "<td><button class='delbtn btn btn-light' data-value='" + id
                + "'>Delete</button></td>" 
                + "<td><button class='viewComment btn btn-light' data-value='" + id
                + "'>View Comments</button></td>" 
                + "<td><button class='addCmt btn btn-light' data-value='" + id
                + "'>Add Comment</button></td>"
                + "<td><button class='upvote btn btn-light' data-value='" + id
                + "'><img src=up.jpg height='15' /></button></td>"
                + "<td><button class='downvote btn btn-light' data-value='" + id
                + "'><img src=down.jpg height='15' /></button></td>"
                + "<td><button class='flagged btn btn-light' data-value ='" + id
                + "'>Flag</button></td>";
        }
    }
    private flagMessage(){
        window.alert("Message has been flagged");
        let id = $(this).data("value");
        let ses = window.sessionStorage.getItem("session");
        
        $.ajax({
            type: "POST",
            url: "/messages/" + id + "/flag",
            dataType: "json",
            data: JSON.stringify({ session: ses }),
            success: editEntryForm.init
        });
    }
    private saveVotes(data: any){
        let foo = 0;
        window.sessionStorage.setItem("numVotes", foo + data.mData);
    }
    private clickViewComment(){
        window.sessionStorage.setItem("CommentMid", "" + $(this).data("value"));
        window.location.replace("/comment.html");
    }
    private clickAddComment(){
        $("#addComment").show();
        $("#showElements").hide(); 
        window.sessionStorage.setItem("CommentMid", "" + $(this).data("value"));
    }
    private goToOtherProfile(){
        let ses = "" + $(this).data("value");
        window.sessionStorage.setItem("ClickedUid", ses);
        window.location.replace("/profile.html");
    }
    private clickLink(){
        let ses = "" + $(this).data("value");
        let id = ses.substr(1);
        let type = ses.charAt(0);
        window.sessionStorage.setItem("clickedLink", ses);
        window.sessionStorage.setItem("lid", id);
        window.sessionStorage.setItem("type", type);
        window.location.replace("/message.html");
        
    }
    /**
    * clickDelete is the code we run in response to a click of a delete button
    */
    private clickDelete() {
        let id = $(this).data("value");
        let ses = window.sessionStorage.getItem("session");
        $.ajax({
            type: "DELETE",
            url: "/messages/" + id,
            dataType: "json",
            data: JSON.stringify({ session: ses }),
            // TODO: we should really have a function that looks at the return
            //       value and possibly prints an error message.
            success: mainList.refresh
        })
    }
    /**
    * clickEdit is the code we run in response to a click of a edit button
    */
    private clickEdit() {
        // as in clickDelete, we need the ID of the row
        let id = $(this).data("value");
        let ses = window.sessionStorage.getItem("session");
        $.ajax({
            type: "POST",
            url: "/getmessages/" + id,
            dataType: "json",
            data: JSON.stringify({ session: ses }),
            success: editEntryForm.init
        });
    }
    /**
    * clickUpvote is the code we run in response to a click of an upvote button
    */
    private clickUpvote() {
        // as in clickDelete, we need the ID of the row
        let id = $(this).data("value");
        let ses = window.sessionStorage.getItem("session");
        let uid = window.sessionStorage.getItem("uid");
        $.ajax({
            type: "POST",
            url: "/votes",
            dataType: "json",
            data: JSON.stringify({ votes: 1, vMid: id, session: ses, vUid: uid }),
            success: mainList.refresh
        });
    }
    /**
    * clickDownvote is the code we run in response to a click of an downvote button
    */
    private clickDownvote() {
        // as in clickDelete, we need the ID of the row
        let id = $(this).data("value");
        let ses = window.sessionStorage.getItem("session");
        let uid = window.sessionStorage.getItem("uid");
        $.ajax({
            type: "POST",
            url: "/votes",
            dataType: "json",
            data: JSON.stringify({ votes: -1, vMid: id, session: ses, vUid: uid }),
            success: mainList.refresh
        });
    }
} // end class ElementList

// a global for the EditEntryForm of the program.  See newEntryForm for 
// explanation
var editEntryForm: EditEntryForm;

/**
 * EditEntryForm encapsulates all of the code for the form for editing an entry
 */
class EditEntryForm {
    /**
     * To initialize the object, we say what method of EditEntryForm should be
     * run in response to each of the form's buttons being clicked.
     */
    constructor() {
        $("#editCancel").click(this.clearForm);
        $("#editButton").click(this.submitForm);
    }

    /**
     * init() is called from an AJAX GET, and should populate the form if and 
     * only if the GET did not have an error
     */
    init(data: any) {
        if (data.mStatus === "ok") {
            $("#editMessage").val(data.mData.mMessage);
            $("#editId").val(data.mData.mMid);
            //$("#editCreated").text(data.mData.mCreated);
            // show the edit form
            $("#addElement").hide();
            $("#addComment").hide();
            $("#editElement").show();
            $("#showElements").hide();
        }
        else if (data.mStatus === "error") {
            window.alert("Error: " + data.mMessage);
        }
        else {
            window.alert("An unspecified error occurred");
        }
    }

    /**
     * Clear the form's input fields
     */
    clearForm() {
        $("#editMessage").val("");
        $("#editId").val("");
        // reset the UI
        $("#addElement").hide();
        $("#addComment").hide();
        $("#editElement").hide();
        $("#showElements").show();
    }

    /**
     * Check if the input fields are both valid, and if so, do an AJAX call.
     */
    submitForm() {
        // get the values of the two fields, force them to be strings, and check 
        // that neither is empty

        let msg = "" + $("#editMessage").val();
        // NB: we assume that the user didn't modify the value of #editId
        let id = "" + $("#editId").val();
        if (msg === "") {
            window.alert("Error: message is not valid");
            return;
        }
        let ses = window.sessionStorage.getItem("session");
        // set up an AJAX post.  When the server replies, the result will go to
        // onSubmitResponse
        $.ajax({
            type: "PUT",
            url: "/messages/" + id,
            dataType: "json",
            data: JSON.stringify({ mMessage: msg, session: ses }),
            success: editEntryForm.onSubmitResponse
        });
    }

    /**
     * onSubmitResponse runs when the AJAX call in submitForm() returns a 
     * result.
     * 
     * @param data The object returned by the server
     */
    private onSubmitResponse(data: any) {
        // If we get an "ok" message, clear the form and refresh the main 
        // listing of messages
        if (data.mStatus === "ok") {
            editEntryForm.clearForm();
            mainList.refresh();
        }
        // Handle explicit errors with a detailed popup message
        else if (data.mStatus === "error") {
            window.alert("The server replied with an error:\n" + data.mMessage);
        }
        // Handle other errors with a less-detailed popup message
        else {
            window.alert("Unspecified error");
        }
    }
} // end class EditEntryForm

// Run some configuration code when the web page loads
$(document).ready(function () {
    // Create the object that controls the "New Entry" form
    newEntryForm = new NewEntryForm();
    // Create the object that controls the "Edit Entry" form
    editEntryForm = new EditEntryForm();
    // Create the object for the main data list, and populate it with data from
    // the server
    mainList = new ElementList();
    if(window.sessionStorage.getItem("session") == "" || window.sessionStorage.getItem("session") == null){
        window.location.replace("/login.html");
    }
    // set up initial UI state
    $("#editElement").hide();
    $("#addElement").hide();
    $("#addComment").hide();
    $("#showElements").show();
    // set up the "Add Message" button
    $("#showFormButton").click(function () {
        $("#addElement").show();
        $("#showElements").hide();
    });
    mainList.refresh();
});