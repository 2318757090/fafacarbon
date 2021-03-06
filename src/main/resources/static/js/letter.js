$(function(){
	$("#sendBtn").click(send_letter);
	$(".close").click(delete_msg);
});

function send_letter() {
	console.log(1);
	$("#sendModal").modal("hide");
	var recipientName = $("#recipient-name").val();
	var messageText = $("#message-text").val();
	$.post(
		CONTEXT_PATH+ "/letter/send",
		{"recipientName":recipientName,"messageText":messageText},
		function (data) {
			data = $.parseJSON(data);
			if(data.code==0){
				$("#hintBody").text("发送成功！");
			}else{
				$("#hintBody").text(data.msg );
			}
			$("#hintModal").modal("show");
			setTimeout(function(){
				$("#hintModal").modal("hide");
				location.reload();
			}, 2000);

		});
}

function delete_msg() {
	// TODO 删除数据
	$(this).parents(".media").remove();
}
function send_message(){

}