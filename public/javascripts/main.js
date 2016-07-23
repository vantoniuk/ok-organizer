$(document).ready(function(){
  $("#editor-new").on("click", function(e){
    $("#editor-wrapper form input").each(function(i, el){$(el).val("")});
    $("#editor-wrapper form input#id").val(-1);
    $("#editor-wrapper #order-wrapper input").val($("#elements li").length + 1);
    $("#editor-wrapper #order-wrapper input").attr("disabled", "disabled");
    $("#editor-wrapper").fadeIn();
    $("#editor-submit").addClass("new");
  });

  $(".editor-edit").on("click", function(e){
    $("#editor-wrapper #order-wrapper input").removeAttr("disabled");
    $(this).parent(".editor-block").find(".editable-original").each(function(i, el){
      var id = $(el).attr("class").replace("editable-original ", "");
      $("#" + id).val($(el).text());
    });
    $("#editor-wrapper").fadeIn();
    $("#editor-submit").attr("class", "edit");
  });

  $(".editor-delete").on("click", function(e){
    var id =  $(this).parent(".editor-block").find(".editable-original.id").text();
    $(this).parent(".editor-block").fadeOut();
    $.ajax({
          url: "/edit/menu/" + id,
          type: 'DELETE',
          success: function(r) {
          }
        });
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