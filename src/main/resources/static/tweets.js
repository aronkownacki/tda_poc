// based on http://ctrlq.org/code/19933-embed-tweet-with-javascript

function embedTweets() {
    var ttweets = jQuery(".tweet");

    jQuery(ttweets).each( function( t, ttweet ) {

    var id = jQuery(this).attr('id');

    twttr.widgets.createTweet(
      id, ttweet,
      {
        conversation : 'none',    // or all
        cards        : 'hidden',  // or visible
        linkColor    : '#cc0000', // default is blue
        theme        : 'light'    // or dark
      });

    });
}