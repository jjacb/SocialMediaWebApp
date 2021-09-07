var loginForm: LoginForm;

/**
 * LoginForm encapsulates all of the code for the form for adding an entry
 */
class LoginForm {
    /**
     * To initialize the object, we say what method of LoginForm should be
     * run in response to each of the form's buttons being clicked.
     */
    // constructor() {
    //     $("#cancelButton").click(this.clearForm);
    //     $("#submitButton").click(this.submitForm);
    // }

    // /**
    //  * Clear the form's input fields
    //  */
    // clearForm() {
    //     $("#username").val("");
    //     $("#password").val("");
    // }
    /**
     * Check if the input fields are both valid, and if so, do an AJAX call.
     */
    // submitForm() {
    //     // get the values of the two fields, force them to be strings, and check 
    //     // that neither is empty
    //     let username = "" + $("#username").val();
    //     let password = "" + $("#password").val();
    //     if (username === "" || password === "") {
    //         window.alert("Error: username or password not valid");
    //         return;
    //     }
    //     window.sessionStorage.setItem("username", username);
    //     $.ajax({
    //         type: "POST",
    //         url: "/userLogin",
    //         dataType: "json",
    //         data: JSON.stringify({ uUsername: username, uPassword: password }),
    //         success: loginForm.checkLogin
    //     });
    // }
    // /**
    //  * Runs on successful ajax validation to set up our "environment variables" and bring us to index.html
    //  */
    // private checkLogin(data: any){
    //         if(data.mStatus === "ok"){
    //             window.sessionStorage.setItem("uid", data.mMessage);
    //             window.sessionStorage.setItem("session", data.mData);
    //             window.location.replace("/index.html");
    //         }
    //         else{
    //             window.alert("Error message is: " + data.mMessage);
    //         }
    // }
} // end class LoginForm

$(document).ready(function () {
    // Create the object that controls the login form
    loginForm = new LoginForm();
});