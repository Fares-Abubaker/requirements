## 2022.4 (October, 30, 2022)

* Fix: Must not start write action from within read action in the other thread - deadlock is coming
* Fix: Inspection has no description
* Fix error message for `Install all packages` action
* Fix DuplicatedInspection for packages with extras
+ New Inspection: merge package extras

## 2022.3.3 (October, 23, 2022)

* Fix NullPointerException
* Fix PluginException for INFORMATION level problem in batch mode
* Fix PluginException `already taken by action 'requirements.txt (Create a new requirements.txt file)'`
* Fix RunCanceledByUserException
* Fix deadlock is coming
* Fix Fail when Cannot find remote credentials
* Fix Fail when Cannot start process, the working directory does not exist

## 2022.3.2 (October, 15, 2022)

* Fix NullPointerException

## 2022.3.1 (October, 9, 2022)

* Fix JsonDecodingException

## 2022.3 (January, 30, 2022)

* Improve inspections
* Improve parser
* Improve formatter
* Fix regression

## 2022.2 (January, 23, 2022)

+ Support pre-release packages

## 2022.1 (January, 15, 2022)

+ Support multiple projects

## 2021.4.1 (May, 11, 2021)

* Fix checking python version

## 2021.4 (May, 3, 2021)

+ Support additional pypi repositories
* Fix Installed requirements in container not recognized

## 2021.3 (February, 27, 2021)

+ Create a new blank Requirements file
+ Create a new Requirements file with installed packages (pip freeze)

## 2021.2 (February, 23, 2021)

+ Versions completion (beta)
+ Options completion (beta)

* Improved version comparison
* Update file icon

## 2021.1.1 (February, 7, 2021)

* Fix: Consider python version when checking package versions
* Fix: null cannot be cast to non-null type ru.meanmail.psi.Versionspec

## 2021.1 (January, 24, 2021)

+ Consider python version when checking package versions
+ Uninstall package inspection

## 2020.6 (December, 27, 2020)

* Fix: element.containingFile.containingDirectory must not be null
* Fix: Assertion failed: Notification should have title and/or content; groupId: pipenv
* Fix: DisposalException: Already disposed
* Support prefer-binary, pre, use-feature syntax
* Improve constrain option

## 2020.5.2 (December, 20, 2020)

* Fix: Inspection has no description

## 2020.5.1 (December, 16, 2020)

* Fix: prerelease versions of packages are recommended

## 2020.5 (December, 13, 2020)

* Fix: java.lang.NoClassDefFoundError
* Fix: Package manager is not available

+ Support error reporting

* Support all relations

+ Check if package exists
+ Check if package version exists

* Improved performance

## 2020.4.1 (December, 5, 2020)

* Fix environment marker comparison
* Improve files detection
* Fix renaming

## 2020.4 (November, 15, 2020)

* Fix Local file path requirement breaks parsing
* Fix environment markers for path requirements

+ Add support --hash statement

## 2020.3 (October, 12, 2020)

* Fix exception: java.lang.Throwable

## 2020.2 (August, 30, 2020)

+ Add action `Install all packages` to a context menu

## 2020.1 (February, 03, 2020)

+ Support environment markers

## 2019.7.1 (January, 25, 2020)

* Fix: Spaces not recognized after version comparator

Read more https://meanmail.dev/post/4

## 2019.7 (December, 22, 2019)

* A line ending in an unescaped \ is treated as a line continuation and the newline following it is effectively ignored.
* Options are supported

Read more https://meanmail.dev/post/3

## 2019.6.3 (December, 18, 2019)

* Fix bugs

## 2019.6.2 (December, 13, 2019)

* Fix: Incorrect language specified in <fileType>

## 2019.6.1 (December, 12, 2019)

* Fix bugs
* Updated description

## 2019.6.0.2 (December, 04, 2019)

* Version for Intellij 2019.3.*
* jvmTarget 11

## 2019.6.0.1 (Nov, 21, 2019)

* Version for Intellij 2019.1.*

## 2019.6 (Nov, 2, 2019)

+ Check for duplicate definitions
+ Simple reformat file

## 2019.5 (Oct, 13, 2019)

* Improved lexer and parser
* Fix: an underscored package name is not supported

## 2019.4.1 (July, 28, 2019)

* Update IDE build

## 2019.4 (May, 26, 2019)

* Fix: installing without required version is failed

+ Add Open in browser for package
+ Add Open in browser for version
+ Improve inspections
+ Checks the latest version for packages

## 2019.3 (Apr, 6, 2019)

* Fix: after installing a package reloaded from disk

+ Show a error notification when a package is not installed
+ Improved detection of not installed package
+ Syntax highlight work without python plugin
+ Checking for the existence of “subrequirements” files

## 2019.2.1 (Mar 29, 2019)

Update IDE build

## 2019.2 (Mar 18, 2019)

+ Package installation. Not installed packages are highlighted

+ Support Reference to subrequirements files (Ctrl + click)

## 2019.1 (Mar 01, 2019)

Update IDE version

## 0.2-beta (Jun 25, 2018)

Fix: eof after package is syntax error

## 0.1-alpha (Jun 17, 2018)

First release
