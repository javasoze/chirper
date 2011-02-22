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
    }
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
    cloudsInterval: undefined,

    events: {
      "keyup #q":  "search",
      "click button": "search"
    },

    initialize: function() {
      _.bindAll(this, 'addOne', 'addAll', 'render');

      Tweets.bind('refresh', this.addAll);
      Tweets.bind('all',     this.render);

      Tweets.fetch();

      // Instant search properties
      this.timeout = undefined;
      this.delay = 300;
      this.isLoading = false;

      // Clouds movement
      this.cloudsInterval = setInterval(this.moveClouds, 10000);
      this.moveClouds();
    },

    moveClouds: function() {
      var div = $("#chirper-search-app .header").css("background-position-x");
      var x_position = div.substring(0, div.length - 2);
      $("#chirper-search-app .header").css("background-position-x", (parseInt(x_position, 10) + 50) + "px");
      // Refresh dates
      $(".ts").easydate({ live: false }); // Live update timestamps
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
      var that = this;
      if(!that.isLoading) {
        that.timeout = setTimeout(function() {
          that.isLoading = true;
          Tweets.url = "/search?q="+ this.$("#q").val();
          Tweets.fetch({
            success: function() {
              that.isLoading = false;
            }
          });
        }, that.delay);
      } else {
        // "Already there's a search in progress..just wait
      }
    }
  });

  $(document).ready(function() {
    window.App = new AppView;
  });
});

