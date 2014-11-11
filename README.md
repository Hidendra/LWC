LWC ![](https://api.travis-ci.org/Hidendra/LWC.png)
======

*The* chest (and other blocks) protection plugin.

This branch is for the rewrite and will tend to be volatile until more stable alpha builds are available.

Compilation
-----------

We use maven to handle our dependencies.

* Install [Maven 3](http://maven.apache.org/download.html)
* Check out this repo and: `mvn clean install`

Distribution files will be located in their respective target folders, e.g. for Bukkit look to `mods/bukkit/target/LWC-Bukkit-XX.jar`

Coding and Pull Request Conventions
-----------

* No tabs; use 4 spaces instead.
* No trailing whitespaces.
* No CRLF line endings, LF only, put your gits 'core.autocrlf' on 'true'.
* No 80 column limit or 'weird' midstatement newlines.
* The number of commits in a pull request should be kept to a minimum (squish them into one most of the time - use common sense!).
* No merges should be included in pull requests unless the pull request's purpose is a merge.
* Pull requests should be tested (does it compile? AND does it work?) before submission.
* Any major additions should have documentation ready and provided if applicable (this is usually the case). This is normally in the form of modified or added wiki files.=
* Try to follow test driven development where applicable.