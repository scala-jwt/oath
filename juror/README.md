[![Build status](https://img.shields.io/github/workflow/status/scala-jwt/oath/Continuous%20Integration.svg)](https://github.com/scala-jwt/oath/actions)
[![Coverage status](https://img.shields.io/codecov/c/github/scala-jwt/oath/master.svg)](https://codecov.io/github/scala-jwt/oath)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.scala-jwt/jwt-core_2.13.svg)](https://central.sonatype.dev/artifact/io.github.scala-jwt/jwt-core_2.13/0.0.6)

# Juror

---

## SBT Dependencies

* [juror-core](https://mvnrepository.com/artifact/io.github.scala-jwt/juror-core)

```scala
libraryDependencies += "io.github.scala-jwt" %% "juror-core" % "0.0.0"
```

---

## Introduction

Juror is an extension library on top of [JWT](../jwt/README.md). It's suggested to read first
the [JWT](../jwt/README.md) library documentation.
Juror will allow you to create custom tokens from scala ADT `Enum` associated with different properties and hide the boilerplate
in configuration files. Juror depends on [Enumeratum](https://github.com/lloydmeta/enumeratum) in order to collect 
the information needed for the custom `Enum`.

### Juror Overview

#### TokenEnumEntry && TokenEnum

Those traits are helpers traits that extend the functionality of [Enumeratum](https://github.com/lloydmeta/enumeratum) to retrieve the 
names for each `Enum` custom value on compile time using macros.

```scala
trait TokenEnumEntry extends EnumEntry

trait TokenEnum[A <: TokenEnumEntry] extends Enum[A]
```

### JurorToken Example

The `Enum` token names will be converted from `UPPER_CAMEL => LOWER_HYPHEN` which is
going to be the name that the library is going to search in your local config file.

```scala
sealed trait JurorExampleToken extends TokenEnumEntry

object JurorExampleToken extends TokenEnum[JurorExampleToken] {
  case object AccessToken extends JurorExampleToken // name in config access-token
  case object RefreshToken extends JurorExampleToken // refresh-token
  case object ActivationEmailToken extends JurorExampleToken // activation-email-token
  case object ForgotPasswordToken extends JurorExampleToken // forgot-password-token

  override def values: IndexedSeq[JurorExampleToken] = findValues

  val jurorManager: JurorManager[JurorExampleToken] = JurorManager.createOrFail(JurorExampleToken)

  val AccessTokenManager: JwtManager[AccessToken.type] = jurorManager.as(AccessToken)
  val RefreshTokenManager: JwtManager[RefreshToken.type] = jurorManager.as(RefreshToken)
  val ActivationEmailTokenManager: JwtManager[ActivationEmailToken.type] = jurorManager.as(ActivationEmailToken)
  val ForgotPasswordTokenManager: JwtManager[ForgotPasswordToken.type] = jurorManager.as(ForgotPasswordToken)
}
```

### Configuration

```hocon
juror {
  access-token {
    algorithm {
      name = "HS256"
      secret-key = "secret"
    }
    issuer {
      registered {
        issuer-claim = "access-token"
        subject-claim = "subject"
        audience-claims = ["aud1", "aud2"]
        include-issued-at-claim = true
        include-jwt-id-claim = true
        expires-at-offset = 15 minutes
        not-before-offset = 0 minute
      }
    }
    verifier {
      provided-with {
        issuer-claim = ${juror.access-token.issuer.registered.issuer-claim}
        subject-claim = ${juror.access-token.issuer.registered.subject-claim}
        audience-claims = ${juror.access-token.issuer.registered.audience-claims}
      }
      leeway-window {
        leeway = 1 minute
        issued-at = 1 minute
        expires-at = 1 minute
        not-before = 1 minute
      }
    }
  }

  refresh-token = ${juror.access-token}
  refresh-token {
    issuer {
      registered {
        issuer-claim = "refresh-token"
        expires-at-offset = 6 hours
      }
    }
    verifier {
      provided-with {
        issuer-claim = ${juror.refresh-token.issuer.registered.issuer-claim}
      }
    }
  }
  activation-email-token = ${juror.access-token}
  activation-email-token {
    issuer {
      registered {
        issuer-claim = "activation-email-token"
        expires-at-offset = 1 day
        audience-claims = []
      }
    }
    verifier {
      provided-with {
        issuer-claim = ${juror.activation-email-token.issuer.registered.issuer-claim}
        audience-claims = []
      }
    }
  }

  forgot-password-token = ${juror.access-token}
  forgot-password-token {
    issuer {
      registered {
        issuer-claim = "forgot-password-token"
        expires-at-offset = 2 hours
        audience-claims = []
      }
    }
    verifier {
      provided-with {
        issuer-claim = ${juror.forgot-password-token.issuer.registered.issuer-claim}
        audience-claims = []
      }
    }
  }
}
```
