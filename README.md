# Fruitarian
A clone of the herbivore anonymous communication protocol

## Setup

We use the LTS version of the JDK which at this point is Java 11.

### IntelliJ IDEA
Make sure to download the [scala plugin](https://www.jetbrains.com/help/idea/discover-intellij-idea-for-scala.html).
You might get a popup for this if you open the project without having it installed.

Once opened in IntelliJ you can open the sbt tool window (`view > tool windows > sbt` on MacOS) and import the project.
This should download any dependencies and set up project in IntelliJ itself.

You can now mark the `src/main` folder as sources root and the `src/test` folder as test sources root.
This will help resolve any issues in IntelliJ itself.

You should now be able to run the project from the main file.

### Terminal
To run the project in the terminal cd to the root of the project and call `sbt`.
Once inside the `sbt` terminal you can run the command `run` to run the main project and `test` to run the tests.
