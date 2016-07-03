# Jenkins Coding Builder Plugin

This plugin integrates [Coding][1] with Jenkins. It handles [WebHook][2] request and triggers
builds for pushes and merge/pull requests.


## Development

Run `./gradlew server` to start the development server.

You may want to disable debug options to get faster page load time: 

```
./gradlew -Dstapler.jelly.noCache=false -Dstapler.trace=false -Ddebug.YUI=false server
```

## Acknowledgements

This project is started and based on the [gitlab-plugin][3], thanks for the great project.

  [1]: https://coding.net
  [2]: https://coding.net/help/doc/git/webhook.html
  [3]: https://github.com/jenkinsci/gitlab-plugin
