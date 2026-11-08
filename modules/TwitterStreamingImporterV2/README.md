## Twitter Streaming Importer V2

V2 of the Twitter Streaming Importer, mainly due to Twitter API V2 change.

https://developer.twitter.com/en/docs/tutorials/stream-tweets-in-real-time


This plugin connects to the Twitter Streaming API and represents tweets as graph.
It includes two possible ways to represent a graph:

- Full Smart Network: A full representation with nodes for Users, Tweets, Hashtags, Urls, Media and Symbols
- User Network: A weighted User to User network with parallel edges for RT and Mentions
- Hashtag Network : A weighted Hashtag to Hashtag network