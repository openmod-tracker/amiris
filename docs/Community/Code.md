# Code Contributions to AMIRIS Repositories

Please read the file `Contributing.md` in the repository you want to contribute to and follow the given instructions.
Before we can merge your code into AMIRIS, we need you to fill out the [Contributor License Agreement](./CLA.pdf) and send it to [amiris@dlr.de](mailto:amiris@dlr.de).

## Definition of Done

A feature can be **closed**, once it has been

- implemented
- tested
- documented in the code, e.g., JavaDoc or DocString
- documented in the wiki
- reviewed by another team member
- merged to `dev`

## Project Environment

We recommend to use [Eclipse](https://www.eclipse.org/) for AMIRIS development.
An Eclipse project is included in the repository.

## Model Architecture

to be added

## Branch Structure

Each repository has two protected branches `main` and `dev`.
*New features* should be developed on a separate branch and are to be merged into `dev`.
*Bug fixes* should be developed in a separate branch and may be merged either into `dev` or `main` if deemed urgent.
Any change to the model code or model data in the `main` branch advances the version (see below) and must be accompanied by a release.
Changes that do not affect the behaviour of the model, e.g., related to documentation, continuous integration, or licence headers, may be released without advancing the release.

## Commit Messages

Inspired by [conventional commits](https://www.conventionalcommits.org/en/v1.0.0) and the [Git Emoji Guide](https://gitmoji.dev/) commit messages should follow this structure:

```
:type-emoji:(scope) description

body
```

where
* `type-emoji` MUST declare the type of commit (see table below),
* `scope` is a MANDATORY noun describing a section of the affected codebase, surrounded by parenthesis,
* `description` is a MANDATORY short summary of the code changes that immediately follows the colon and space after the type/scope prefix,
* `body` MAY be provided after the description providing additional contextual information. The body MUST begin one blank line after the description.

### Emoji-Table

| Commit Type | Emoji              | EmojiCode        | Description                                                        |
|-------------|--------------------|------------------|--------------------------------------------------------------------|
| build       | :wrench:           | `wrench`           | update the build system or external dependencies                   |
| ci          | :rocket:           | `rocket`           | change CI configurations                                           |
| style       | :lipstick:         | `lipstick`         | change style or formatting, not affecting code meaning             |
| feat        | :sparkles:         | `sparkles`         | implement a feature                                                |
| fix         | :bug:              | `bug`              | fix a bug                                                          |
| docs        | :memo:             | `memo`             | change the documentation, e.g., README, license headers, changelog |
| test        | :white_check_mark: | `white_check_mark` | add, update or correct tests                                       |
| perf        | :zap:              | `zap`              | improve performance                                                |
| refactor    | :recycle:          | `recycle`          | code changes that neither fix a bug nor add a feature                   |
| release     | :bookmark:         | `bookmark`         | release a version                                                  |

## Versioning & Release Naming

We follow the [semantic versioning 2.0.0 scheme](https://semver.org/) for all AMIRIS Repositories.

In addition, tags of AMIRIS have a name.
The overall theme of naming tags in the AMIRIS group repository is [**Physical Geography**](https://en.wikipedia.org/wiki/Physical_geography).
The following schemes apply for the different projects within the group:

| project       | tag name scheme                                                                               |
|---------------|-----------------------------------------------------------------------------------------------|
| amiris        | [Volcanoes](https://www.vulkane.net/blogmobil/welcher-vulkan-eruptiert-aktuell/vulkan-liste/) |
| examples      | n/a                                                                                           |
| amiris-py     | [Rivers](https://en.wikipedia.org/wiki/List_of_alternative_names_for_European_rivers)                       |
| investment    | n/a                                                                                           |
| scengen       | [Landscapes](https://en.wikipedia.org/wiki/Glossary_of_landforms#Landforms,_alphabetic)       |
| priceforecast | [Soil](https://en.wikipedia.org/wiki/Category:Types_of_soil)                                  |

Tags names are to be chosen in alphabetical order (i.e. starting with "A").
For each new major version number (**x**.a.b) the first letter of the tag name is increased (to "B", "C", etc.).
For each new minor revision number (a.**x**.c) the first letter stays the same, but a new scheme-specific name is chosen, increasing in alphabetical order.
Bug fix revisions (a.b.**x**) do not receive an individual name.
