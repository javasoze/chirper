$(function(){
  window.Tweet = Backbone.Model.extend({
  });

  var TweetList = Backbone.Collection.extend({
    model: Tweet,

    url: "/search",

    parse: function(resp) {
      this.total = resp.totaldocs;
      this.query = resp.parsedquery;
      this.models = resp.hits;
      return resp.hits;
    },
  });

  window.Tweets = new TweetList();

  window.TweetView = Backbone.View.extend({

    tag: "li",

    initialize: function() {
      // _.bindAll(this, 'render', 'close');
      this.model.bind('change', this.render);
      this.model.view = this;
    },

    render: function() {
      $(this.el).html(Mustache.to_html($('#tweet-view').html(), this.model.toJSON()));
      return this;
    }
  });

  window.AppView = Backbone.View.extend({
    el: $("#chirper-search-app"),

    events: {
      "keyup #q":  "search",
      "click button": "search"
    },

    initialize: function() {
      _.bindAll(this, 'addOne', 'addAll', 'render');

      Tweets.bind('refresh', this.addAll);
      Tweets.bind('all',     this.render);

      Tweets.fetch();
    },

    render: function() {
      $("#search-form").submit(function() { return false; }); // Don't submit form, use live search
    },

    addAll: function() {
      this.$("#tweets").html("");
      Tweets.each(this.addTweet);
      $(".ts").easydate({ live: false }); // Live update timestamps
    },

    addTweet: function(tweet) {
      var view = new TweetView({ model: tweet });
      this.$("#tweets").append(view.render().el);
    },

    search: function() {
      Tweets.url = "/search?q="+ this.$("#q").val();
      Tweets.fetch();
    }
  });

  window.App = new AppView;
});

var Chirper = {

  cloudsInterval: undefined,

  init: function() {
    // this.addSearchListeners();
    this.cloudsInterval = setInterval(this.moveClouds, 10000);
    this.moveClouds();
  },

  moveClouds: function() {
    var div = $("#chirper-search-app .header").css("background-position-x");
    var x_position = div.substring(0, div.length - 2);
    $("#chirper-search-app .header").css("background-position-x", (parseInt(x_position, 10) + 50) + "px");
    // Refresh dates
    $(".ts").easydate({ live: false }); // Live update timestamps
  }
  // ,
  // 
  // addSearchListeners: function() {
  //   var that = this;
  //   $("#search-form").submit(function() {
  //     that.search($("#q").val());
  //     return false;
  //   });
  // },
  // 
  // search: function(query) {
  //   $.ajax
  // }
};

$(document).ready(function() {
  Chirper.init();
});
