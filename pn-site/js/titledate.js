$(document).ready(function () {

  $("#controls input").on('click', (e) => {
    const target = e.currentTarget;
    if (target.checked) {
      $("." + target.name).show();
      if (target.name == "transcription") {
        $(".image").css('width', '50%');
        $(".translation").css('width', '50%');
      }
    } else {
      $("." + target.name).hide();
      if (target.name == "transcription") {
        $(".image").css('width', '100%');
        $(".translation").css('width', '100%');
      }
    }
  }
  );

  var showEditHistory = function () {

    hideAllHistory();
    $("#edit-history-list").show("blind", 250);
    $("#edit-history").off("click");
    $("#edit-history").on("click", hideEditHistory);

  }

  var hideEditHistory = function () {

    if (!($("#edit-history-list").css("display") == "none")) { $("#edit-history-list").hide("blind", 250); }
    $("#edit-history").off("click");
    $("#edit-history").on("click", showEditHistory);
  }

  var showAllHistory = function () {

    hideEditHistory();
    $("#all-history-list").show("blind", 250);
    $("#all-history").off("click");
    $("#all-history").on("click", hideAllHistory);

  }

  var hideAllHistory = function () {

    if (!($("#all-history-list").css("display") == "none")) { $("#all-history-list").hide("blind", 250); }
    $("#all-history").off("click");
    $("#all-history").on("click", showAllHistory);

  }

  $("#edit-history").on("click", showEditHistory);
  $("#all-history").on("click", showAllHistory);

});
