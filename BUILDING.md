# Building HotBaaaar (multi-version / multi-loader)

This repository uses a **branch matrix**. Each Minecraft version + mod loader combination lives on
its own self-contained, buildable branch. The `master` branch is a **hub**: it holds the CI/release
workflows and this document, but no mod project.

## Branch convention

```
mc/<mc-version>-<loader>
```

Examples: `mc/1.19.2-forge`, `mc/1.21.11-neoforge`, `mc/26.2-fabric`.

Each target branch is a complete project (its own `build.gradle`, sources, and `mods.toml` /
`fabric.mod.json`) and carries:

- `.github/workflows/*` — inherited from `master`, so push-CI runs on the branch.
- `.github/ci/java-version` — the JDK used to **run Gradle** on that branch (e.g. `17` for the
  Forge 1.19.2 branch, which uses ForgeGradle 6 + Gradle 8.1.1). The JDK used to **compile** the mod
  is provisioned automatically by Gradle's toolchain where configured. The 1.21.x targets use Java
  21; the 26.x targets use Java 25.

## CI (`.github/workflows/ci.yml`)

On every push to `mc/**` and on PRs into `mc/**` or `master`, the branch is built with
`./gradlew build` and the resulting jars are uploaded as a workflow artifact.

## Release (`.github/workflows/release.yml`)

Push a tag matching `v*` (e.g. `v1.0.0`) on `master`. The workflow:

1. **discover** — lists every `mc/*` branch via the GitHub API.
2. **build** — a matrix job (`fail-fast: false`) checks out and builds each branch.
3. **release** — collects all jars and publishes a single GitHub Release for the tag.

Each jar is named from its own branch's `gradle.properties` (`mod_version`).

## Adding a new target

1. Branch from the closest existing target, e.g. `git switch -c mc/1.20.1-forge mc/1.19.2-forge`.
2. Port the code (rendering, mixin targets, loader entry point, metadata) for the new version/loader.
3. Update `.github/ci/java-version` if the new build needs a different JDK to run Gradle.
4. `git push -u origin mc/1.20.1-forge` → CI builds it automatically.
5. Next `v*` tag on `master` includes it in the release automatically.

> Note: each branch carries its own copy of the workflows. If you change `ci.yml`, merge the change
> into the target branches. `release.yml` only needs to be current on the tagged `master` commit.
