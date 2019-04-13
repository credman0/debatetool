# Debate Tool
This project is an attempt to build a cohesive piece of software that helps ease the process of researching and presenting evidence in highschool and college debate.

![Screenshot](https://raw.githubusercontent.com/credman0/unnamed-debate-tool/master/wiki/running_application.png)

## Compilation
Clone the repository with ```git clone --recursive```. The recursive setting is important because a few of the packages are broken out into submodules. If you clone without that, you will need to run ```git submodule update --init```.

To compile you will need to have gradle installed, as well as of course a JDK (>=JDK 9). Then you should be able to run the code using ```gradle run``` or build a distribution package by running ```gradle build jar -xtest```.

## Usage
The easiest way to try out the software is to just run it using the local filesystem setting (its right on the login dialog, should be set that way by default), so you don't need to set up a database. That will at least let you see the basics of the how the program works without having to set up mongo. The most important thing about actually using the program is the "edit mode" switch in the top left (also modified by a hotkey you can see in the "View" dropdown menu). That is what switches between for example cutting cards and adding text to cards.

Most of the other stuff is done by right clicking in the tree (on the left side) and using the context menu, then drag dropping stuff into speeches/blocks. Cards are created by just adding the text to the card editor (open by default, and also in the file menu) and then when you save them they will be placed in the selected directory.

This isn't really ready for actual use yet. It has bugs I know about, and probably a lot more that I don't. Some of the features are either missing or not quite right. Consider yourselves warned.

## More misc. notes
* Searching is not implemented for the filesystem storage back end. No particular reason for this, I just didn't do it.
* The admin login dialog is presently wierd (its the same as the login dialog, but shouldn't be). Just don't mess with the other settings on that screen.
* Some super bizzare bug came up in my thesis defense that I can't reproduce, but know that if some very strange graphical gliches occur when you try to view particular speeches, you are not alone.
* Please forgive the lack of documentation and sometimes very strange looking code. 

## Deploying a database
In the future, I hope to create a comprehensive guide to deploying a functional database. In the mean time, the gist is this:
* Install MongoDB and start a server with auth enabled
* From a local connection, set up an administrator account
* Enter the database IP (and port, if changed from the default) in the software. The setup should happen on its own.
