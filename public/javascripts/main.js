$(document).ready(function(){
  $("#editor-new").on("click", function(e){
    $("#editor-wrapper form .input:not(.predef)").each(function(i, el){$(el).val("")});
    $("#editor-wrapper form .input#id").val(-1);
    $("#editor-wrapper #order-wrapper input").val($("#elements li").length + 1);
    $("#editor-wrapper #order-wrapper input").attr("disabled", "disabled");
    $("#editor-wrapper").fadeIn();
    $("#editor-submit").addClass("new");
  });

  $(".input-helper").on("change", function(){
    var $this = $(this);
    var id = $this.attr("id").replace("for-", "#");
    $(id).val($this.val());
  })

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
    var cls = $(this).attr("class").split(" ")[0];
    var id =  $(this).parent(".editor-block").find(".editable-original.id").text();
    $(this).parent(".editor-block").fadeOut();
    $.ajax({
          url: "/edit/" + cls + "/" + id,
          type: 'DELETE',
          success: function(r) {
          }
        });
  });

  $("#editor-submit").on("click", function(e){
    var cls = $(this).parent().attr("class").split(" ")[0];
    var $inputList = $("#editor-wrapper form .input");
    var actionUrl = $(this).is(".new") ? "new" : "update";
    var dataObj = {};

    $inputList.each(function(i, el){
      var $el = $(el);
      var d = new Date();
      var value = $el.is('.date') && $el.val() == "" ? d.getTime() + d.getTimezoneOffset() * 60 * 1000 : $el.val();
      if(value) dataObj[$el.attr("id")] = value;
    });


    $.post(
      "/edit/" + cls + "/" + actionUrl,
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