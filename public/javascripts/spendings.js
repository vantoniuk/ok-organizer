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
      function(e){
        showCategories();
      }
    );
  })

  showCategories();
})

$(document).ready(function(){
  $("#add-card").on("submit", function(e){
    e.preventDefault();
    var data = {
      "vendor": $("#card-vendor").val(),
      "name": $("#card-name").val(),
      "description": $("#card-description").val(),
      "available": Number($("#card-available").val()),
      "total": Number($("#card-total").val())
    };

    $.get(
      "/spendings/cards/save", {
        "card": JSON.stringify(data)
      },
      function(e){
        showCreditCards();
      }
    );
  })

  showCreditCards();
})

$(document).ready(function(){
  $("#add-statement").on("submit", function(e){
    e.preventDefault();
    var data = {
      "card_id": Number($("#statement-card").val()),
      "category_id": Number($("#statement-category").val()),
      "available": Number($("#statement-amount").val())
    };

    $.get(
      "/spendings/statements/save", {
        "statement": JSON.stringify(data)
      },
      function(e){
        showStatements();
      }
    );
  })

  showStatements();
})

function categoryToHtml(cat) {
  return '<tr>' +
    '<td>' + cat.name + '</td>' +
    '<td>' + cat.description + '</td>' +
    '<td>' + cat.limit + '</td>' +
  '</tr>';
}

function creditCardToHtml(card) {
  return '<tr>' +
    '<td>' + card.vendor + '</td>' +
    '<td>' + card.name + '</td>' +
    '<td>' + card.description + '</td>' +
    '<td>' + card.available + '</td>' +
    '<td>' + card.total  + '</td>' +
  '</tr>';
}

function statementToHtml(statement) {
  return '<tr>' +
    '<td>' + statement.card_vendor + '</td>' +
    '<td>' + statement.card_name + '</td>' +
    '<td>' + statement.category + '</td>' +
    '<td>' + statement.amount  + '</td>' +
    '<td>' + formatDate(statement.timestamp) + '</td>' +
  '</tr>';
}

function formatDate(dateString) {
  return new Date(dateString).toLocaleDateString();
}

function dataToOption(data) {
  return '<option value="' + data.id + '">' + data.name + '</option>';
}

function genericShow(url, successMessage, transformers, sendData) {
  $.get(url, sendData, function(data){
    transformers.forEach(function(transformer){
      $(transformer.selector).html(data.items.map(transformer.toHtml).join("\n"));
    });
    console.log(successMessage, data);
  });
}

function showCreditCards() {
  genericShow("/spendings/cards", "got cards", [{
    "selector": "#spending-card tbody",
     "toHtml": creditCardToHtml
  },
  {
    "selector": "#statement-card",
     "toHtml": function(card){
       return dataToOption({"id": card.card_id, "name": card.vendor + " " + card.name});
     }
  }]);
}

function showCategories() {
  genericShow("/spendings/categories", "got categories", [{
    "selector": "#spending-category tbody",
    "toHtml": categoryToHtml
  },
  {
    "selector": "#statement-category",
     "toHtml": function(statement){
       return dataToOption(statement);
     }
  }]);
}

function showStatements() {
  var d = new Date();
  var from = Date.UTC(d.getFullYear()-1, d.getMonth(), d.getDay(), d.getHours(), d.getMinutes(), d.getSeconds(), d.getMilliseconds());
  var to = Date.UTC(d.getFullYear() + 1, d.getMonth(), d.getDay(), d.getHours(), d.getMinutes(), d.getSeconds(), d.getMilliseconds());
  genericShow("/spendings/statements", "got statements", [{
    "selector": "#spending-statement tbody",
    "toHtml": statementToHtml
  }],
  {
    "from": from,
    "to": to
  });
}