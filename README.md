[![Build status](https://img.shields.io/github/workflow/status/scala-jwt/oath/Continuous%20Integration.svg)](https://github.com/scala-jwt/oath/actions)
[![CI Status](https://github.com/scala-jwt/oath/actions/workflows/ci.yml/badge.svg)](https://github.com/scala-jwt/oath/actions/workflows/ci.yml)
[![Coverage status](https://img.shields.io/codecov/c/github/scala-jwt/oath/master.svg)](https://codecov.io/github/scala-jwt/oath)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.scala-jwt/jwt-core_2.13.svg)](https://central.sonatype.dev/artifact/io.github.scala-jwt/jwt-core_2.13/0.0.6)

## OATH - Scala JWT

__OATH__ provides an easy way for Rest API Applications to manipulate JWTs securely in complex systems.

1. Customize registered claims via configuration.
1. Create a variety of JWT tokens with different configuration for each use case
1. Token encryption.

## SBT Dependencies

* [oath-core](https://mvnrepository.com/artifact/io.github.scala-jwt/oath-core)

```scala
libraryDependencies += "io.github.scala-jwt" %% "oath-core" % "0.0.0"
```

### Json Converters

* [oath-circe](https://mvnrepository.com/artifact/io.github.scala-jwt/oath-circe)

```scala
libraryDependencies += "io.github.scala-jwt" %% "oath-circe" % "0.0.0"
```

* [oath-jsoniter-scala](https://mvnrepository.com/artifact/io.github.scala-jwt/oath-jsoniter-scala)

```scala
libraryDependencies += "io.github.scala-jwt" %% "oath-jsoniter-scala" % "0.0.0"
```

---

### Supported Algorithms

|  JWS  | Algorithm | Description                        |
|:-----:|:---------:|:-----------------------------------|
| HS256 |  HMAC256  | HMAC with SHA-256                  |
| HS384 |  HMAC384  | HMAC with SHA-384                  |
| HS512 |  HMAC512  | HMAC with SHA-512                  |
| RS256 |  RSA256   | RSASSA-PKCS1-v1_5 with SHA-256     |
| RS384 |  RSA384   | RSASSA-PKCS1-v1_5 with SHA-384     |
| RS512 |  RSA512   | RSASSA-PKCS1-v1_5 with SHA-512     |
| ES256 | ECDSA256  | ECDSA with curve P-256 and SHA-256 |
| ES384 | ECDSA384  | ECDSA with curve P-384 and SHA-384 |
| ES512 | ECDSA512  | ECDSA with curve P-521 and SHA-512 |

## Introduction

Oath is an extension on top of [JWT](./docs/JWT.md). It's suggested to read that first to gain
a basic understanding of JWTs, the internals and the configuration settings available for this library.
Oath will allow you to create custom tokens from scala ADT `Enum` associated with different properties and hide the
boilerplate
in configuration files. Oath depends on [Enumeratum](https://github.com/lloydmeta/enumeratum) in order to collect
the information needed for the custom `Enum`.

### Oath Overview

#### TokenEnumEntry && TokenEnum

Those traits are helpers that extend the functionality of [Enumeratum](https://github.com/lloydmeta/enumeratum) to
retrieve the
names for each `Enum` custom value on compile time using macros.

```scala
trait TokenEnumEntry extends EnumEntry

trait TokenEnum[A <: TokenEnumEntry] extends Enum[A]
```

### OathToken Example

The `Enum` token names will be converted from `UPPER_CAMEL => LOWER_HYPHEN` which is
going to be the name that the library is going to search in your local config file.

```scala
sealed trait OathExampleToken extends TokenEnumEntry

object OathExampleToken extends TokenEnum[OathExampleToken] {
  case object AccessToken extends OathExampleToken // name in config access-token

  case object RefreshToken extends OathExampleToken // refresh-token

  case object ActivationEmailToken extends OathExampleToken // activation-email-token

  case object ForgotPasswordToken extends OathExampleToken // forgot-password-token

  override def values: IndexedSeq[OathExampleToken] = findValues

  val oathManager: OathManager[OathExampleToken] = OathManager.createOrFail(OathExampleToken)

  val AccessTokenManager: JwtManager[AccessToken.type] = oathManager.as(AccessToken)
  val RefreshTokenManager: JwtManager[RefreshToken.type] = oathManager.as(RefreshToken)
  val ActivationEmailTokenManager: JwtManager[ActivationEmailToken.type] = oathManager.as(ActivationEmailToken)
  val ForgotPasswordTokenManager: JwtManager[ForgotPasswordToken.type] = oathManager.as(ForgotPasswordToken)
}
```

### Configuration

For all available configuration settings see [JWT Configuration](./docs/JWT.md#configuration).

```hocon
oath {
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
        issuer-claim = ${oath.access-token.issuer.registered.issuer-claim}
        subject-claim = ${oath.access-token.issuer.registered.subject-claim}
        audience-claims = ${oath.access-token.issuer.registered.audience-claims}
      }
      leeway-window {
        leeway = 1 minute
        issued-at = 1 minute
        expires-at = 1 minute
        not-before = 1 minute
      }
    }
  }

  refresh-token = ${oath.access-token}
  refresh-token {
    issuer {
      registered {
        issuer-claim = "refresh-token"
        expires-at-offset = 6 hours
      }
    }
    verifier {
      provided-with {
        issuer-claim = ${oath.refresh-token.issuer.registered.issuer-claim}
      }
    }
  }
  activation-email-token = ${oath.access-token}
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
        issuer-claim = ${oath.activation-email-token.issuer.registered.issuer-claim}
        audience-claims = []
      }
    }
  }

  forgot-password-token = ${oath.access-token}
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
        issuer-claim = ${oath.forgot-password-token.issuer.registered.issuer-claim}
        audience-claims = []
      }
    }
  }
}
```

### Known Issues

* Audience single empty string in the list might lead to unexpected behaviours raised
  in [java-jwt#662](https://github.com/auth0/java-jwt/issues/662) 