var commentList: CommentList;

/**
 * Prints a table with comment info for whatever message you clicked to get here
 */
class CommentList {
    constructor() {
        this.getComments();
    }
    /**
     * update is the private method used by refresh() to update commentList
     */
    private getComments() {
        let ses = window.sessionStorage.getItem("session");
        if(ses == ""){
            window.alert("Please login before continuing")
        }
        let mid = window.sessionStorage.getItem("CommentMid");
        $.ajax({
            type: "POST",
            url: "/getmessages/" + mid + "/comments",
            dataType: "json",
            data: JSON.stringify({ session: ses }),
            success: this.printInfo
        });

    }
    private printInfo(data: any){
        $("h3").append("" + window.sessionStorage.getItem("CommentMid"));
        
        $("#comments").html("<table>");
        for (let i = 0; i < data.mData.length; ++i) {
            $("#comments").append("<tr><td>" + data.mData[i].cComment + "<pre></pre><a class='profileLink' data-value='"
                + data.mData[i].cUid + "'>Posted By User: " + data.mData[i].cUid + "</a></td></tr>");
        }
        $("#comments").append("</table>");
        $(".profileLink").click(commentList.goToOtherProfile);
    }
    private goToOtherProfile(){
        let ses = "" + $(this).data("value");
        window.sessionStorage.setItem("ClickedUid", ses);
        window.location.replace("/profile.html");
    }
}

$(document).ready(function () {
    commentList = new CommentList();

    $("#homeButton").click(function () {
        window.location.replace("/index.html");
    });
});