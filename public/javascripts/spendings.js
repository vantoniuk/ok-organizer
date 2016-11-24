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
  $("#add-statement").on("submit", function(e){
    var $this = $(this);
    e.preventDefault();
    var cardId = $("#statement-card").val();
    if(!isNaN(cardId) && $("#statement-card").val() != "") {
      var data = {
        "card_id": Number($("#statement-card").val()),
        "available": Number($("#statement-available").val()),
        "amount_paid": Number($("#statement-amount_paid").val()),
        "timestamp": datePickerDateToMillis({"date": $("#statement-date").val(), "ignore_empty": true})
      };

      $.get(
        "/spendings/statements/save", {
          "statement": JSON.stringify(data)
        },
        function(e){
          showStatementsForQuery(queryForStatements());
          hideTheForm($this);
        }
      );
    } else {
      alert("select a card");
    }
  })

  $("#filter-statement").on("submit", function(e){
    e.preventDefault();
    showStatementsForQuery(queryForStatements());
  });

  /* date picker setup */
  $("#statement-to").datepicker();
  $("#statement-from").datepicker();
  $("#statement-date").datepicker();

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

  showStatementsForQuery(queryForStatements());
})

function queryForStatements(){
  var cardQuery = $("#statement-filter-card").val() != "" && $("#statement-filter-card").val() != null  ? {"card_id" : Number($("#statement-filter-card").val())} : {};

  return $.extend({
    "from": datePickerDateToMillis({"date": $("#statement-from").val()}),
    "to": datePickerDateToMillis({"date": $("#statement-to").val(), "end_of_the_day": true})
  }, cardQuery);
}

function datePickerDateToMillis(config) {
  var dateArray = config.date.split("/").map(x => Number(x));
  var timeArray = config.end_of_the_day ? [23,59,59] : [0, 0, 0];
  if(dateArray.length != 3) {
    if(config.ignore_empty) {
      return null;
    } else {
      var d = new Date();
      return Date.UTC(d.getFullYear(), d.getMonth(), d.getDate(), timeArray[0], timeArray[1], timeArray[2]);
    }
  } else {
    return Date.UTC(dateArray[2], dateArray[0] - 1, dateArray[1], timeArray[0], timeArray[1], timeArray[2]);
  }
}

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
    '<td>' + statement.available  + '</td>' +
    '<td>' + statement.amount_paid  + '</td>' +
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
          "selector": "#spending-statement-table tbody",
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

function hideTheForm($form) {
  $form.find("input").each(function(i, input){$(input).val("")});
  $form.find("textarea").each(function(i, input){$(input).val("")});
  $form.addClass("hidden")
}