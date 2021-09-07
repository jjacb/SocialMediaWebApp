var profile: Profile;

/**
 * Profile prints all the relevant user info the the page, there are no individual profiles per se, just this one with varying info depending on how you got here
 */
class Profile {
    constructor() {
        $("#addPassword").click(this.addPassword);
        this.getInfo();
        this.viewMessage();
        this.viewComments();
    }
    /**
     * getInfo is called right when you get to profile.html
     */
    private getInfo() {
        let ses = window.sessionStorage.getItem("session");
        if(ses == ""){
            window.alert("Please login before continuing")
        }
        let uid = window.sessionStorage.getItem("ClickedUid");
        $.ajax({
            type: "POST",
            url: "/getusers/" + uid,
            dataType: "json",
            data: JSON.stringify({ session: ses }),
            success: this.printInfo
        });
    }
    private printInfo(data: any){
        $("#userInfo").html("<p>");
        $("h3").append(data.mData.uUsername);
        $("#userInfo").append("Hello " + data.mData.uUsername + ", thank you for using our site!");
        $("#userInfo").append("</br>Your user ID is: " + data.mData.uUid);
        $("#userInfo").append("</br>Your locaton is: " + data.mData.uLocation);
        $("#userInfo").append("</br>Your email is: " + data.mData.uEmail);
        $("#userInfo").append("</br></br>If you'd like to change YOUR password (not necesarilly the current profile), you can do so below");
        $("#userInfo").append("</p>");
    }

    /**
    * list message in profile page for current user
    */
    private viewMessage() {
        let ses = window.sessionStorage.getItem("session");
        let uid = window.sessionStorage.getItem("uid");
        console.log(uid);
        if(ses == ""){
            window.alert("Please login before continuing")
        }
        let id = window.sessionStorage.getItem("ClickedUid");
        console.log(id);
        $.ajax({
            type: "POST",
            url: "/getusers/" + id + "/messages",
            dataType: "json",
            data: JSON.stringify({ session: ses }),
            success: function(data) {
                $("#message").append("<br>Messages: " + data.mData);
                console.log(data.mData)}
        });
    }

    /*list comment in profile page*/
    private viewComments() {
        let ses = window.sessionStorage.getItem("session");
        let uid = window.sessionStorage.getItem("uid");
        console.log(uid);
        if(ses == ""){
            window.alert("Please login before continuing")
        }
        let id = window.sessionStorage.getItem("ClickedUid");
        console.log(id);
        $.ajax({
            type: "POST",
            url: "/getusers/" + id + "/comments",
            dataType: "json",
            data: JSON.stringify({ session: ses }),
            success: function(data) {
                $("#comment").append("<br>Comments: " + data.mData);
                console.log(data.mData)}
        });
    }

    /**
     * This is our code for updating user passwords
     */
    addPassword(){
        let password = "" + $("#newPassword").val();
        if (password === "") {
            window.alert("Error: password not valid");
            return;
        }
        let uid = window.sessionStorage.getItem("uid");
        let ses = window.sessionStorage.getItem("session");
        $.ajax({
            type: "PUT",
            url: "/users/" + uid + "/password",
            dataType: "json",
            data: JSON.stringify({ uPassword: password, session: ses }),
            success: profile.clearForm
        });
    }
    private clearForm(data: any) {
        $("#newPassword").val("");
    }
}

$(document).ready(function () {
    profile = new Profile();

    $("#homeButton").click(function () {
        window.location.replace("/index.html");
    });
});