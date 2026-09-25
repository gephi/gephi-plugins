# Gephi AI

This plugin lets an AI assistant drive Gephi. It runs a small HTTP API inside
Gephi Desktop, so an assistant such as Claude can create projects, build and
edit graphs, run layouts and statistics, style and filter the result, and export
it, while you watch the work happen in the Gephi window.

The plugin is the Gephi half of the system. The assistant talks to it through a
separate Model Context Protocol server, which is distributed independently and
is not required for this plugin to build or install.

## Quick start

- Install the plugin in Gephi and restart.
- The API starts automatically and listens on `http://127.0.0.1:8080`.
- Open **Tools > Gephi AI Server** to see whether it is running and on which URL,
  to stop or start it, and to change the port. The port setting persists and
  takes effect the next time the server starts.
- Point an MCP client at the companion server. Setup instructions are at
  https://github.com/MattArtzAnthro/gephi-ai

Check that it is running from a terminal:

```
curl http://127.0.0.1:8080/health
```

Opening that URL in a browser returns `403` by design. See below.

## Scope and safety

The listener binds to the loopback interface only, so it is not reachable from
the network.

Two checks keep it that way. A request whose `Host` header names anything but a
loopback address is refused, which blocks DNS rebinding. A request carrying
`Origin` or `Sec-Fetch-Site` is also refused, because both are set by browsers
and cannot be forged by page JavaScript. Without that second check a page the
user merely visits could call the API with `mode: "no-cors"`, and although the
browser would hide the reply, the side effect would already have happened.
Local clients such as the companion server send neither header.

Beyond that there is no authentication, so any process running as you can drive
the API. The API is read and write: an assistant driving it can delete nodes,
clear a workspace, and overwrite files you name. Save your work before a long
session.

## Licence

Apache License 2.0. See `LICENSE`.
