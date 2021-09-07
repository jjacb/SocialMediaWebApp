var message: Message;

/**
 * Prints a table with comment info for whatever message you clicked to get here
 */
class Message {
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
        let link = window.sessionStorage.getItem("clickedLink");
        let lid = window.sessionStorage.getItem("lid");
        let type = window.sessionStorage.getItem("type");
        if(type == 'm'){
            $.ajax({
                type: "POST",
                url: "/getmessages/" + lid,
                dataType: "json",
                data: JSON.stringify({ session: ses }),
                success: this.printInfo
            });
        }
        else if(type == 'c'){
            $.ajax({
                type: "POST",
                url: "/getcomments/" + lid,
                dataType: "json",
                data: JSON.stringify({ session: ses }),
                success: this.printInfo
            });
        }
    }
    private printInfo(data: any){
        let link = window.sessionStorage.getItem("clickedLink");
        $("h3").append("" + link);
        let type = window.sessionStorage.getItem("type");
        if(type == 'm'){
            if(data.mData)
                $("#lmessage").append(data.mData.mMessage);
            else{
                window.alert("Invalid Link");
                window.location.replace("/index.html");
            }
        }
        else if(type == 'c'){
            if(data.mData)
                $("#lmessage").append(data.mData.cComment);
            else{
                window.alert("Invalid Link");
                window.location.replace("/index.html");
            }
        }
        
    }
        
}

$(document).ready(function () {
    message = new Message();

    $("#homeButton").click(function () {
        window.location.replace("/index.html");
    });
});