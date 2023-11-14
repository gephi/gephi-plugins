# Gephi Plugin: Publish to the web

This is a work initiated during the Gephi Code Retreat (Aug 29 to Sept 2, 2022).
The plugin allows users to publish their network to the web, directly from the Gephi desktop app.

## Function of the plugin

- the plugin's interface can be accessed via a new item added to the File -> Export menu of Gephi.
- the user needs to follow a series of steps to authenticate
- then, the user can publish the network currently open in Gephi. The network gets exported as a gexf file hosted as a Github's gist.
- the users get a url to the Retina web app, that allows them to explore this gexf directly from their browser

## Author and contact

- Detailed design of the roadmap: Alexis Jacomy, see: https://github.com/gephi/gephi-plugins/issues/262#issuecomment-1231627948
- Plugin development: Clement Levallois (admin@clementlevallois.net or https://twitter.com/seinecle)
- Code review: [Mathieu Bastian](https://www.linkedin.com/in/mathieubastian/)
- Retina: https://ouestware.gitlab.io/retina/beta