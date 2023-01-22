[![Build status](https://img.shields.io/github/workflow/status/scala-jwt/oath/Continuous%20Integration.svg)](https://github.com/scala-jwt/oath/actions)
[![Coverage status](https://img.shields.io/codecov/c/github/scala-jwt/oath/master.svg)](https://codecov.io/github/scala-jwt/oath)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.scala-jwt/jwt-core_2.13.svg)](https://central.sonatype.dev/artifact/io.github.scala-jwt/jwt-core_2.13/0.0.6)

# CSRF (Cross-Site Request Forgery) Token 

---

## SBT Dependencies

* [csrf-core](https://mvnrepository.com/artifact/io.github.scala-jwt/csrf-core)
```scala
libraryDependencies += "io.github.scala-jwt" %% "csrf-core" % "0.0.0"
```

---

## Introduction

### Supported Algorithm

| Algorithm | Description |
| :-------------: | :----- |
| HMAC256 | HMAC with SHA-256 |

### Model overview

CSRF Token is generated using `System.currentTimeMillis()` which will issue a unique token with signature every time.

```scala
final case class CsrfToken(token: NonEmptyString)
```

### Manager Overview

```scala
import io.oath.csrf.config.CsrfManagerConfig
import io.oath.csrf.CsrfManager

val config: CsrfManagerConfig = CsrfManagerConfig.loadOrThrow("csrf-token")
val csrfManager: CsrfManager = new CsrfManager(csrfConfig)

val csrfToken: Option[CsrfToken] = csrfManager.issueCSRF()
// CsrfToken(1673808897799-lB5T6M8NUkDB7rvoxhh0dfVn6kzeoUfIFJcolYA_8v4=)
// CsrfToken(currentTimeMillis-signedMessage)

csrfToken.exists(csrfManager.verifyCSRF(_)) // => true
```

### Configuration

```hocon
token {
  csrf {
    secret = "secret"
  }
}
```
