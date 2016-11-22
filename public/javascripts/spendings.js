$(document).ready(function(){
  $(".spending-tracking-form").on("click", "a.add-new", function(e){
    var $a = $(this);
    var $form = $a.closest(".spending-tracking-form").find("form");
    e.preventDefault();
    if($form.is(".hidden")) {
      $form.removeClass("hidden");
    } else {
      $form.addClass("hidden");
    }
  });
});

$(document).ready(function(){
  $("#add-category").on("submit", function(e){
    e.preventDefault();
    var data = {
      "name": $("#category-name").val(),
      "description": $("#category-description").val(),
      "limit": Number($("#category-limit").val())
    };

    $.get(
      "/spendings/categories/save", {
        "cat": JSON.stringify(data)
      },
      function(e){}
    );
  })

  showCategories();
})

$(document).ready(function(){
  $("#add-category").on("submit", function(e){
    e.preventDefault();
    var data = {
      "name": $("#category-name").val(),
      "description": $("#category-description").val(),
      "limit": Number($("#category-limit").val())
    };

    $.get(
      "/spendings/categories/save", {
        "cat": JSON.stringify(data)
      },
      function(e){}
    );
  })

  showCategories();
})

function categoryToHtml(cat) {
  return '<tr>' +
    '<td>' + cat.name + '</td>' +
    '<td>' + cat.description + '</td>' +
    '<td>' + cat.limit + '</td>' +
  '</tr>';
}

function showCategories() {
  $.get("/spendings/categories", function(data){
    $("#spending-category tbody").html("");
    data.categories.forEach(function(cat){
      $("#spending-category tbody").append(categoryToHtml(cat));
    });
    console.log("got categories", data);
  });
}

function creditCardToHtml(card) {
  return '<tr>' +
    '<td>' + card.vendor + '</td>' +
    '<td>' + card.name + '</td>' +
    '<td>' + card.description + '</td>' +
    '<td>' + card.available + '</td>' +
    '<td>' + card.limit + '</td>' +
  '</tr>';
}

function showCreditCards() {
  $.get("/spendings/cards", function(data){
    data.cards.forEach(function(cat){
      $("#spending-card tbody").append(categoryToHtml(cat));
    });
    console.log("got cards", data);
  });
}

function statementToHtml(card) {
  return '<tr>' +
    '<td>' + card.vendor + '</td>' +
    '<td>' + card.name + '</td>' +
    '<td>' + card.description + '</td>' +
    '<td>' + card.available + '</td>' +
    '<td>' + card.limit + '</td>' +
  '</tr>';
}

function showStatements() {
  $.get("/spendings/statements", function(data){
    data.statements.forEach(function(cat){
      $("#spending-card tbody").append(categoryToHtml(cat));
    });
    console.log("got cards", data);
  });
}