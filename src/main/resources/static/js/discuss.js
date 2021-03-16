function like(btn,entityType,entityId,entityUserId,discussPostId){
    $.post(
        CONTEXT_PATH+"/like",
        {"entityType":entityType,"entityId":entityId,"entityUserId":entityUserId,"discussPostId":discussPostId},
        function (data) {
            data = $.parseJSON(data);
            if(data.code==0){
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus==1?"已赞":"赞 ");
            }else{
                alert(data.msg);
            }
    });
}
$(function () {
    $("#topBtn").click(setTop);
    $("#wonderfulBtn").click(setWonderful);
    $("#deleteBtn").click(setDelete);
})
function setTop(){
    $.post(
        CONTEXT_PATH+"/discuss/top",
        {"discussPostId":$("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if(data.code==0){
                $("#topBtn").attr("disabled","disabled");
            }else{
                alert(data.msg);
            }
        });
}
function setWonderful(){
    $.post(
        CONTEXT_PATH+"/discuss/wonderful",
        {"discussPostId":$("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if(data.code==0){
                $("#wonderfulBtn").attr("disabled","disabled");
            }else{
                alert(data.msg);
            }
        });

}
function setDelete(){
    $.post(
        CONTEXT_PATH+"/discuss/delete",
        {"discussPostId":$("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if(data.code==0){
                location.href=CONTEXT_PATH+"/index";
            }else{
                alert(data.msg);
            }
        });
}