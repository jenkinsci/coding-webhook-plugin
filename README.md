# Jenkins Coding Webhook Plugin

This plugin integrates [Coding][1] with Jenkins. It handles [WebHook][2] request and triggers
builds for pushes and merge/pull requests.

## Installation

This plugin has been published to the official [Plugins Index][4], follow the [Managing Plugins][5]
document to install it.

## Usage

See <https://open.coding.net/ci/jenkins> (Chinese)

## Development

Run `./gradlew server` to start the development server.

You may want to disable debug options to get faster page load time: 

```
./gradlew -Dstapler.jelly.noCache=false -Dstapler.trace=false -Ddebug.YUI=false server
```

## Acknowledgements

This project is started and based on the [gitlab-plugin][3], thanks for the great project.
The coding-api module follows [github-api][6], and coding-auth module follows [github-oauth-plugin][7], thanks for them.

  [1]: https://coding.net
  [2]: https://open.coding.net/webhooks
  [3]: https://github.com/jenkinsci/gitlab-plugin
  [4]: https://plugins.jenkins.io
  [5]: https://jenkins.io/doc/book/managing/plugins
  [6]: https://github.com/kohsuke/github-api
  [7]: https://github.com/jenkinsci/github-oauth-plugin
