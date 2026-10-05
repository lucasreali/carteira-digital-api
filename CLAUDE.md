# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

REST API for a wallet (balances, transactions) built for the PUCPR Cloud course, to be deployed to the cloud. Currently a bare Spring Initializr skeleton: Spring Boot 4.1.1, Java 25 (Gradle toolchain), Gradle 9.7.1 with Kotlin DSL.

## Environment

- No JDK is installed in the WSL shell (`java` is not on PATH) and the foojay toolchain resolver is not configured, so `./gradlew` will fail here. The user builds and runs tests from IntelliJ. Don't try to run Gradle; ask the user to run it, or say what to run, when verification is needed.

## Conventions

- Code, identifiers, and comments in English.
- Commits follow Conventional Commits (`feat:`, `fix:`, `chore:`, ...).
- Commit messages and PR descriptions must not mention Claude: no `Co-Authored-By: Claude`, no "Generated with Claude Code" line.
