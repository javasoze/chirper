var clouds = setInterval(function() {
  var div = $("#chirper-search-app .header").css("background-position-x");
  var x_position = div.substring(0, div.length - 2);
  $("#chirper-search-app .header").css("background-position-x", (parseInt(x_position, 10) + 100) + "px");
}, 10000);
