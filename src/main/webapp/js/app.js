$(function(){
  /**
   * Tweet Model
   * It's a JSON object coming from the scalatra servlet response, it inherits from Backbone.Model.
   */
  window.Tweet = Backbone.Model.extend({
  });

  /**
   * TweetList
   * It's a collection object with some properties and methods that are inherited from Backbone.Collection.
   */
  var TweetList = Backbone.Collection.extend({

    model: Tweet,   // The model we are storing inside the collection, defined previously.

    url: "/search", // JSON endpoint that returns the list of results.

    // We want to redefine the parse function from Backbone because we are storing custom fields like the time it took to search, and the number of documents.
    parse: function(resp) {
      this.total = resp.totaldocs;
      this.numhits = resp.numhits;
      this.totaltime = resp.totaltime;
      this.query = resp.parsedquery;
      this.models = resp.hits;
      return resp.hits;
    }
  });
  window.Tweets = new TweetList();

  /**
   * TweetView
   * A view that represents a tweet model.
   */
  window.TweetView = Backbone.View.extend({

    // A tweet is represented as a <li> element.
    tag: "li",

    initialize: function() {
      this.model.bind('change', this.render); // If we update the collection of tweets, we should re-render the view automatically.
      this.model.view = this; // Just in case we want to reference the view in the model.
    },

    render: function() {
      $(this.el).html(Mustache.to_html($('#tweet-view').html(), this.model.toJSON())); // Takes the Mustache template from "#tweet-view", and renders the Model.
      return this; // We always return this on render so we can chain calls.
    }
  });

  /**
   * AppView
   * A view that represents the Application.
   */
  window.AppView = Backbone.View.extend({

    // We define the DOM element that contains the application
    el: $("#chirper-search-app"),

    // We move the clouds every 10 seconds, we want to hold a reference for the interval on the view.
    cloudsInterval: undefined,

    // Setup event listeners such as when the user types something on the search box, we want ot trigger a search to the server.
    events: {
      "keyup #q":  "search",
      "click button": "search"
    },

    initialize: function() {
      // Bindings for updating the view when the collection changes, etc.
      _.bindAll(this, 'addAll', 'render');
      Tweets.bind('refresh', this.addAll);
      Tweets.bind('all',     this.render);

      // On initalize, let's fetch the inital list of tweets with an empty search.
      Tweets.fetch();

      // We are not really going to fire a search for each keystroke, so we need to wait until the user stops typing (300ms) and then fire the search.
      this.timeout = undefined;
      this.delay = 300;
      this.isLoading = false;

      // Clouds movement
      this.cloudsInterval = setInterval(this.refreshUIElements, 10000);
      this.totalInterval = setInterval(this.refreshTotalCount, 4000);
      this.refreshUIElements();
    },

    refreshUIElements: function() {
      // Refresh clouds
      var div = $("#chirper-search-app .header").css("background-position-x");
      var x_position = div.substring(0, div.length - 2);
      $("#chirper-search-app .header").css("background-position-x", (parseInt(x_position, 10) + 50) + "px");
      // Refresh dates x seconds ago, etc. after rendering.
      $(".ts").easydate({ live: false });
    },

    refreshTotalCount: function() {
      // Refresh total hit count periodically
      $.get('/search?offset=0&count=0', function(data) { // we setup offset=0 and count=0 beacuase it makes the search on the backend more efficient. We just want the absolute totals.
        $('#total-tweets').html("Indexed "+ data.totaldocs + " tweets");
      });
    },

    render: function() {
      $("#search-form").submit(function() { return false; }); // Don't submit the form, use live JS search.
    },

    addAll: function() {
      this.$("#tweets").html("");
      Tweets.each(this.addTweet); // For all the tweets in the collection, render them on the list.
      $(".ts").easydate({ live: false }); // Live update timestamps x seconds ago, etc.
    },

    addTweet: function(tweet) {
      var view = new TweetView({ model: tweet }); // Get the mustache template for the tweet and render it accordingly.
      this.$("#tweets").append(view.render().el);
    },

    // This function is kinda tricky at first sight. We are actually not running a search everytime the user types a letter, we wait until the user stops typing (300ms) and then after that fire the search event.
    search: function() {
      var that = this;
      if(!that.isLoading) { // If we are not loading a search already
        that.timeout = setTimeout(function() {
          that.isLoading = true;
          Tweets.url = "/search?q="+ this.$("#q").val(); // Let's update the collection url with the extra params we want to send (the search term)
          Tweets.fetch({
            success: function() {
              that.isLoading = false; // After we fetched the results, we should allow new searches.
              $('#found-tweets').html("Found "+ Tweets.numhits +" tweets in ("+ Tweets.totaltime +" ms )")
            }
          });
        }, that.delay); // Wait before performing the search, the user might want to type more.
      } else {
        // "Already there's a search in progress..just wait
      }
    }
  });

  $(document).ready(function() { // Only run after everything loads on the page.
    window.App = new AppView; // Let's run it!
  });
});
