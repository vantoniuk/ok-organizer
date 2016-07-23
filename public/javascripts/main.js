$(document).ready(function(){
  $("#editor-new").on("click", function(e){
    $("#editor-wrapper form input").each(function(i, el){$(el).val("")});
    $("#editor-wrapper #order-wrapper input").val($("#elements li").length + 1);
    $("#editor-wrapper #order-wrapper input").attr("disabled", "disabled");
    $("#editor-wrapper").fadeIn();
    $("#editor-submit").addClass("new");
  });
  $("#editor-edit").on("click", function(e){
//    $("#editor-wrapper form input").each(function(i, el){$(el).val("")});
    $("#editor-wrapper").fadeIn();
    $("#editor-submit").removeClass("new");
  });

  $("#editor-submit").on("click", function(e){
    var $inputList = $("#editor-wrapper form input");
    var actionUrl = $(this).is(".new") ? "new" : "update";
    var dataObj = {};

    $inputList.each(function(i, el){
      var $el = $(el);
      dataObj[$el.attr("id")] = $el.val();
    });

    $.post(
      "/edit/menu/"+actionUrl,
      dataObj,
      function(r) {
        $("#editor-wrapper").fadeOut();
        console.log("server post response", dataObj);
      }
    );

  });
});

function PostR(url, data, onSuccess) {
  $.ajax({
    url:url,
    type:"POST",
    data:data,
    contentType:"application/json; charset=utf-8",
    dataType:"json",
    success: onSuccess
  })
}