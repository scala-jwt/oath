[![Build status](https://img.shields.io/github/workflow/status/scala-jwt/oath/Continuous%20Integration.svg)](https://github.com/scala-jwt/oath/actions)
[![Coverage status](https://img.shields.io/codecov/c/github/scala-jwt/oath/master.svg)](https://codecov.io/github/scala-jwt/oath)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.scala-jwt/jwt-core_2.13.svg)](https://central.sonatype.dev/artifact/io.github.scala-jwt/jwt-core_2.13/0.0.6)

## Scala JWT - OATH

__OATH__ provides a set of tools for WEB Applications 
1. CSRF token generation 
2. Authentication (Issuing JWT Tokens) 
3. Authorization (Verifying JWT Tokens)

### Modules - Documentation

* [CSRF Docs](./csrf/README.md) - CSRF token generator
* [JWT Docs](./jwt/README.md) - A Scala API of [java-jwt](https://github.com/auth0/java-jwt)
* [Juror Docs](./juror/README.md) - Extension of JWT to manipulate different type of tokens

### SBT Dependencies

* [csrf-core](https://mvnrepository.com/artifact/io.github.scala-jwt/csrf-core)
* [jwt-core](https://mvnrepository.com/artifact/io.github.scala-jwt/jwt-core)
* [juror-core](https://mvnrepository.com/artifact/io.github.scala-jwt/juror-core)

```scala
libraryDependencies += Seq(
  "io.github.scala-jwt" %% "csrf-core"  % "0.0.0",
  "io.github.scala-jwt" %% "jwt-core"   % "0.0.0",
  "io.github.scala-jwt" %% "juror-core" % "0.0.0"
)
```

### Json Converters

* [jwt-circe](https://mvnrepository.com/artifact/io.github.scala-jwt/jwt-circe)

```scala
libraryDependencies += "io.github.scala-jwt" %% "jwt-circe" % "0.0.0"
```

* [jwt-jsoniter-scala](https://mvnrepository.com/artifact/io.github.scala-jwt/jwt-jsoniter-scala)

```scala
libraryDependencies += "io.github.scala-jwt" %% "jwt-jsoniter-scala" % "0.0.0"
```
