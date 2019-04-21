# Anton's IntelliJ Helper plugin

![Anton Logo](src/main/resources/icons/antonIcon.png)

Available at: [plugins.jetbrains.com](https://plugins.jetbrains.com/plugin/12251-anton-s-intellij-helper)

### Current features:

* Automatically initialize missing git submodules recursively for projects with a *.gitmodules* file
* Auto switch detached git submodules to the branch listed in *.gitmodules* if file *.anton* exists

### Working on:

* Auto add a git managed gitconfig to *.git/config*
* Auto add a git managed pre-commit hook to *.git/hooks/pre-commit*
* Warn about outdated `go version` when a *go.mod* file is present

Additional requests will be considered, file an [feature request](https://github.com/phntom/anton/issues)
