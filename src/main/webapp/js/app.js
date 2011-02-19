var Chirper = {

  cloudsInterval: undefined,

  init: function() {
    this.cloudsInterval = setInterval(this.moveClouds, 10000);
    this.moveClouds();
  },

  moveClouds: function() {
    var div = $("#chirper-search-app .header").css("background-position-x");
    var x_position = div.substring(0, div.length - 2);
    $("#chirper-search-app .header").css("background-position-x", (parseInt(x_position, 10) + 50) + "px");
  }
};

$(document).ready(function() {
  Chirper.init();
});
