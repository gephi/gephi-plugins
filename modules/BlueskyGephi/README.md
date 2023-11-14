# Bluesky Gephi

This plugin allow you to visualize and explore the network of users in bluesky via the atprotocol.

# Quick start
- Get a bluesky account
- Generate a password https://bsky.app/settings/app-passwords
- Install the plugin in Gephi
- Open Gephi
- Put handle and password information
- Search for yourself
- Graph of your connection should appears.

# Docs

Keep in mind current atproto access point from bluesky is quite permissive and might change in the future.

## Fetch from user
You can fetch network from user from multiple way :
- Put one or multiple (separated by line return) handles or dids inside the plugin textarea and click on "Go!" 
- You can right click on a node and select contextual menu item related to the plugin 
  - **Bluesky Fetch default data** , will fetch network based on the current configuration on the plugin panel
  - **Fetch followers only data**, will fetch only the followers of the node
  - **Fetch follows only data**, will fetch only the follows of the node

## Deep Search
By activating **Fetch also n+1**, the plugin will fetch the selected handles network **and also** the network of the handles found.

/!\ Keep in mind that this can be very long as some users has a long list of followers or follows. /!\

## Crawl Limit
To have the list of the followers and follows of a user, the atproto api is build in a way that the application need to loop over multiple
"pages". It means that for hub user, that have a high number of followers and follows (10k, 100k,1M) it might take an important amount of time 
to retrive information for this kind of user.

Therefore, there is a possibility to limit this by only retriving a fraction of the followers and follows in order to speedup the exploration.

It's ok to do that if analysing theses hub isn't your main goal, as if theses hub are highly connected, they will automatically appears on the relationship
of other users.