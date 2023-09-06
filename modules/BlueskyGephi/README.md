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
  - *Bluesky Fetch default data* , will fetch network based on the current configuration on the plugin panel
  - *Fetch followers only data*, will fetch only the followers of the node
  - *Fetch follows only data*, will fetch only the follows of the node