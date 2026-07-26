# Overview

Kreator is a Kotlin library for generating new classes.
It is implemented using the KSP tool and is compatible with the Kotlin Multiplatform framework.
The primary purpose of the library is to generate new classes from existing classes using annotations.
The library is mainly used for generating DTO classes, both in server and client applications.
However, it can be used to generate any classes from existing code.

## Glossary

Domain class
: The user-defined application class from which the library generates new (DTO) classes.

DTO (Data Transfer Object)
: A simple class used for encapsulation and data transfer.

Mapping method
: A method or function that is used to convert one class to another (from DTO to DOMAIN; from DOMAIN to DTO)

KMP (Kotlin Multiplatform)
: Kotlin framework for developing multi-platform applications.

KSP (Kotlin Symbol Processor)
: A tool for developing processors that go through annotations during compilation.
