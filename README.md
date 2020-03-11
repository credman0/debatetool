# Debate Tool
This project is an attempt to build a cohesive piece of software that helps ease the process of researching and presenting evidence in highschool and college debate.

![Screenshot](https://raw.githubusercontent.com/credman0/unnamed-debate-tool/master/wiki/running_application.png)

## State
After taking a long hiatus from this project, I'm back on the grind for now. I realized at some point that in order to go forward any with this project, I need to pretty much completely rework the backend (starting with a shift to a REST api which will allow me to run the server somewhat intelligently). On the other hand, the frontend is also a concurrency nightmare that might be better torn up than fixed, but I'm also a little destruction happy so I want to at least fix the backend and then see where the inspiration takes me with the front.

For some god-forsaken reason, I decided before I would do any of that I would also move the whole project to jdk11, which has also broken everything. Unclear what my reasons were, but I'm committed. At the very least, the old preferences system (which was backed by PreferencesFX) seems pretty much unrecoverable, and many of my components that were provided by JFoenix are also broken (that one I am more optimistic about?).

In any case, I think that most of this is not as bad as it looks, but still needs doing. Maybe as I go around this time I'll actually take the time to provide javadoc comments.
