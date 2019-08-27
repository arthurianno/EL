WHAT IS THIS.
============
This project represents new-project-skeleton based on clean architecture  
and Single-Activity-Application(SAP) approach.
Presentation layer built above [RxPM](https://github.com/dmdevgo/RxPM).  
About SAP you can read [here](https://habr.com/company/redmadrobot/blog/426617/), [example](https://gitlab.com/terrakok/gitlab-client), [navigation](https://github.com/terrakok/Cicerone)

HOW TO START.
============
* Copy this project to new folder 
* Change ```origin``` 
* Change ```application.id``` in [configuration.properties](./configuration.properties)
* Change ```archive.name``` in [configuration.properties](./configuration.properties)
* Set your initial version in [version.properties](./version.properties)
* Create release key store and credentials-release.properties in [keystore.](./keystore)
(As example of credentials look to [Debug credentials](./keystore/credentials-debug.properties)

That's all!

Important things:
----------------
* [Code Style](http://172.16.100.20/developers-android/android-code-style) 
* [Version gradle](./version.gradle) contains tasks to easy bump version and package archives
* [Dependencies gradle](./dependencies.gradle) contains number of common dependencies
* [Detekt](https://github.com/arturbosch/detekt) for static code analysis.

Detekt
------
Before making pull request check code calling _./gradlew detektCheck_ in Terminal.
Report will be printed to Terminal. HTML version of report you can find at _..detekt-report_

Versions
--------
1. Bump version:
     - _./gradlew bumpPatch_ if build contains bug fixes or improvements of existing functional
     - _./gradlew bumpMinor_ if build contains some new features
     - _./gradlew bumpMajor_ if build contains changes that not compatible with previous versions
     - _./gradlew bumpLocalBuildNumber_ if build contains fixes for open release
     - _./gradlew bumpBuildNumber_ 
2. Push branch to remote
3. Open _Terminal_ and execute commands:
    - _./gradlew clean_
    - _./gradlew build_
    - _./gradlew zipArtifacts_
4. Open _../artifacts_ and send archive with highest version for test

For easier work
===============

1. Create features using plugin [PackageTemplates](http://ceh9.github.io/PackageTemplates/)  
Template with feature you can find in [templates](./templates).
2. Use file and live templates.  
Jar file with templates you can find in [templates](./templates).  
3. Create pull request using shell script: _mr.sh_.  
    
Live Templates
--------------
1. For logging: 
    - ```timd```
    - ```time```
    - ```timi``` 
2. For di:
    - ```bindDataSource```
    - ```bindFactory```
    - ```bindFragment```
    - ```bindFragmentWithoutModule```
    - ```bindPm```
    - ```bindMapper```
    - ```bindRepository```
    - ```provideAdapter```
3. For action, state and command:
    - ```pact```
    - ```pstt```
    - ```pcmd```
    - ```act```
    - ```stt```
    - ```cmd```
    - ```createAction```
4. For ListItem:
    - ```addpay```
    - ```bindItem```
    - ```pay```
    
Pull requests
-------------
- Add file _mr.sh_ to root dir with content:  
```bash
#!/usr/bin/env bash
python3 scripts/merge_request_creator.py --token=TOKEN --project=PROJECT
```
- Create git alias:  
``` bash
git config alias.mr '!sh mr.sh'
```
- Run with _git mr_  

_TOKEN_ - create at [GitLab](http://172.16.100.20/profile/personal_access_tokens)  
_PROJECT_ - path to project. For example: _nullgravity%2FTele2%2Ftele2-self-service-android_

Build app in different environments
-------------
Project contains 2 predefined config file: [test](./configuration-build-test.properties) and [stage](./configuration-build-stage.properties).   
If you need to build app in other environment you need to create your own config file and set environment variable.  

List of supported configs:
- ```environment``` - represents name of environment
- ```server.url``` - represents base url to be used to make requests
- ```deep.link.host``` - represents host to be used to handle deep links
- ```app.id.suffix``` - represents suffix of application id. Can be used to create different application id for different environments.
- ```app.name.suffix``` - represents suffix of application name. Can be used to create different application name for different environments.
- ```log.enabled``` - represents boolean flag to enable or disable logs in application.

NOTE: currently ```app.id.suffix``` unused to keep integration with social networks in working state at iOS and Android.  

How to set environment variable:  
Name of variable builds in next way: ```"{applicationId}.config.file".toUpperCase().replace(".", "_")```  
```applicationId``` you can get at [base config](./configuration.properties).  
Set variable, in Terminal type: ```export VARIABLE_NAME=path/to/file```  
To set variable for GitLab CI add ```variables``` section to job:  
```
beta:
  stage: beta
  variables:
    VARIABLE_NAME: 'path/to/file'
```