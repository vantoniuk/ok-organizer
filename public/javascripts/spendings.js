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

/*********************** STATEMENTS *****************/
$(document).ready(function(){
  var d = new Date();
  var from = Date.UTC(d.getFullYear()-1, d.getMonth(), d.getDay(), d.getHours(), d.getMinutes(), d.getSeconds(), d.getMilliseconds());
  var to = Date.UTC(d.getFullYear() + 1, d.getMonth(), d.getDay(), d.getHours(), d.getMinutes(), d.getSeconds(), d.getMilliseconds());

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
        showStatements(from, to);
      }
    );
  })

  $("#filter-statement").on("submit", function(e){
    e.preventDefault();
    var cardQuery = $("#statement-filter-card").val() != "" ? {"card_id" : Number($("#statement-filter-card").val())} : {};

    var query = $.extend({"from": from, "to": to}, cardQuery);
    showStatementsForQuery(query);
  });

  showCreditCardsForTransformers([
    {
      "selector": "#statement-filter-card",
       "toHtml": function(card){
         return dataToOption({"id": card.card_id, "name": card.vendor + " " + card.name});
       }
    },
    {
      "selector": "#statement-card",
       "toHtml": function(card){
         return dataToOption({"id": card.card_id, "name": card.vendor + " " + card.name});
       }
    }
  ],'<option value=""> ... </option>');
  showStatements(from, to);
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
  console.log(statement)
  return '<tr id="statement-' + statement.id + '">' +
    '<td>' + statement.card_vendor + '</td>' +
    '<td>' + statement.card_name + '</td>' +
    '<td>' + statement.amount  + '</td>' +
    '<td>' + formatDate(statement.timestamp) + '</td>' +
  '</tr>';
}

function spendingToHtml(spending) {
  return '<tr id="spending-' + spending.id + '">' +
    '<td>' + spending.card_vendor + '</td>' +
    '<td>' + spending.card_name + '</td>' +
    '<td>' + spending.category + '</td>' +
    '<td>' + spending.amount  + '</td>' +
    '<td>' + formatDate(spending.timestamp) + '</td>' +
  '</tr>';
}

function formatDate(dateString) {
  return new Date(dateString).toLocaleDateString();
}

function dataToOption(data) {
  return '<option value="' + data.id + '">' + data.name + '</option>';
}

function genericShow(config) {
  var emptyElement = config.empty_element ? config.empty_element : "";
  $.get(config.url, config.send_data, function(data){
    config.transformers.forEach(function(transformer){
      $(transformer.selector).html(emptyElement + data.items.map(transformer.toHtml).join("\n"));
    });
    console.log(config.success_message, data);
  });
}

function showCreditCardsForTransformers(transformers, emptyElement) {
   genericShow({
     "url": "/spendings/cards",
     "success_message": "got cards",
     "transformers": transformers,
     "empty_element": emptyElement
    });

}

function showCreditCards() {
  showCreditCardsForTransformers([{
    "selector": "#spending-card tbody",
     "toHtml": creditCardToHtml
  },
  {
    "selector": "#spending-actions-card",
     "toHtml": function(card){
       return dataToOption({"id": card.card_id, "name": card.vendor + " " + card.name});
     }
  }]);
}

function showCategories() {
  genericShow({
    "url": "/spendings/categories",
    "success_message": "got categories",
    "transformers": [
      {
        "selector": "#spending-category tbody",
        "toHtml": categoryToHtml
      },
      {
        "selector": "#statement-category",
         "toHtml": function(statement){
           return dataToOption(statement);
         }
      }
    ]
  });
}

function showStatementsForQuery(query) {
  genericShow({
      "url": "/spendings/statements",
      "success_message": "got statements",
      "transformers": [
        {
          "selector": "#spending-statement tbody",
          "toHtml": statementToHtml
        }
      ],
      "send_data": query
    });
}

function showStatements(from, to) {
  showStatementsForQuery({
    "from": from,
    "to": to
  });
}