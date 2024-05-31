# Simplifier Key-Value Store Plugin

## Introduction

The Key-Value-Store-Plugin is an extension to [Simplifier](http://simplifier.io), adding the capability
of creating/deleting/accessing data, key-value based stored in a real database.

See Simplifier Community documentation for more information: [https://community.simplifier.io](https://community.simplifier.io/doc/current-release/extend/plugins/list-of-plugins/keyvaluestore-json-store-plugin/)


## Documentation

See here for a short [Plugin documentation markdown](./documentation/manual.md).
or for or a short [Plugin documentation PDF](./documentation/manual.pdf).


## Deployment

You can run the plugin locally. Your Simplifier AppServer has to be running and has to be accessible locally.

There is only one additional step to be done before building and testing:


### Additional Step: Adding the Oracle JDBC Driver
- in the source tree: create a lib directory
- then copy the Oracle JDBC driver version 23.3 to this directory
- the sourcetree then looks like this:
```
├── build
│   └── artifacts
│       ├── assets
│       │   ├── keyValueStorePlugin.arg
│       │   └── keyValueStorePlugin.conf
│       ├── build.sh
│       ├── Dockerfile
│       └── setup.sh
├── build.sbt
├── lib
│   └── ojdbc8.jar
├── LICENSE
├── project
├── README.md
├── src
│   ├── main   ...
│   └── test   ...
└── version.sh

```



## Local Deployment

The build runs the process with SBT locally, Simplifier's plugin registration port and the plugin communication port must be accessible locally.


### Prerequisites

- A plugin secret has to be available. It can be created in the Plugins section of Simplifier,
  please look [here for an instruction](https://community.simplifier.io/doc/current-release/extend/plugins/plugin-secrets/).
- replace the default secret: <b>XXXXXX</b> in the [PluginRegistrationSecret](./src/main/scala/byDeployment/PluginRegistrationSecret.scala)
  class with the actual secret.
- Simplifier must be running and the <b>plugin.registration</b> in the settings must be configured accordingly.


### Preparation

#### Simplifier Configuration Modification

Copy the file [settings.conf.dist](./src/main/resources/settings.conf.dist) as <b>settings.conf</b> to your installation path and edit the values as needed.
When launching the jar, the config file must be given as a commandline argument.


- Provide the correct ```database``` data according to your local setup.
- Provide the correct ```filename``` path according to your local setup.

__Please note__: Only MySQL and Oracle are supported!


### Build and Run

At the commandline, run
```bash
sbt compile
```

and then

```bash
sbt run
```