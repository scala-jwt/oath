[![Build status](https://img.shields.io/github/workflow/status/scala-jwt/oath/Continuous%20Integration.svg)](https://github.com/scala-jwt/oath/actions)
[![Coverage status](https://img.shields.io/codecov/c/github/scala-jwt/oath/master.svg)](https://codecov.io/github/scala-jwt/oath)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.scala-jwt/jwt-core_2.13.svg)](https://central.sonatype.dev/artifact/io.github.scala-jwt/jwt-core_2.13/0.0.6)

# JWT (JSON Web Token)

The JWT layer depends on [oath0/java-jwt](https://github.com/auth0/java-jwt) library. Is inspired
by [akka-http-session](https://github.com/softwaremill/akka-http-session)
& [jwt-scala](https://github.com/jwt-scala/jwt-scala)
if you have already used those libraries you would probably find your self familiar with this API.

---

## SBT Dependencies

* [jwt-core](https://mvnrepository.com/artifact/io.github.scala-jwt/jwt-core)

```scala
libraryDependencies += "io.github.scala-jwt" %% "jwt-core" % "0.0.0"
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

---

## Introduction

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

### Model overview

All registered claims documented in [RFC-7519](https://www.rfc-editor.org/rfc/rfc7519.html) are provided with optional
values, therefore the library doesn't enforce you to use them.

```scala
final case class RegisteredClaims(
                                   iss: Option[NonEmptyString] = None,
                                   sub: Option[NonEmptyString] = None,
                                   aud: Seq[NonEmptyString] = Seq.empty,
                                   exp: Option[Instant] = None,
                                   nbf: Option[Instant] = None,
                                   iat: Option[Instant] = None,
                                   jti: Option[NonEmptyString] = None
                                 )
```

Claims is more than Registered Claims though. Therefore, if the business requirements requires extra claims to be able
to authenticate & authorize the clients,
the library provides an ADT to describe each use case and the location for additional claims.

```scala
sealed trait JwtClaims

object JwtClaims {

  final case class Claims(registered: RegisteredClaims) extends JwtClaims

  final case class ClaimsH[+H](header: H, registered: RegisteredClaims) extends JwtClaims

  final case class ClaimsP[+P](payload: P, registered: RegisteredClaims) extends JwtClaims

  final case class ClaimsHP[+H, +P](header: H, payload: P, registered: RegisteredClaims) extends JwtClaims
}
```

The JWT (JSON Web Token) is described as a whole with the `claims` & `token` in
the below data structure. The `token` is in this form `base64(header).base64(payload).signature`.

```scala
final case class Jwt[+C <: JwtClaims](claims: C, token: NonEmptyString)
```

### API Overview

In a microservice architecture you could have more than on service issuing or verifying tokens.
The library is being design to follow this principle by splitting the requirements to different APIs.

#### JWT Issuer

Use only for issuing JWT Tokens. For asymmetric algorithms only private-key is required,
see [configuration](#configuration).

```scala
import io.circe.generic.auto._
import io.oath.jwt.syntax._
import io.oath.jwt.circe._

final case class Foo(name: String, age: Int)

val config = IssuerConfig.loadOrThrow("token") // HMAC256 with "secret" as secret
val issuer = new JwtIssuer(config)
val foo = Foo("foo", 10)

val maybeJwt: Either[IssueJwtError, Jwt[JwtClaims.ClaimsP[Foo]]] = issuer.issueJwt(foo.toClaimsP)

// Right(Jwt(ClaimsP(Foo(foo,10),RegisteredClaims(None,None,List(),None,None,None,None)),eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJuYW1lIjoiZm9vIiwiYWdlIjoxMH0.oeU3zySKPA-fowGQkl0WPDwyBhXJUEtobSjGQsDBXcs))
```

#### Verifier Overview

Use only for verifying JWT Tokens. For asymmetric algorithms only public-key is required,
see [configuration](#configuration).
In order for the verifier API to determine the location of the data in the token, the `verifyJwt` function takes
a `JwtToken`.
There is extension methods already created if you `import io.oath.jwt.syntax._` then you should be able to convert any
string to a `JwtToken`.

```scala
sealed trait JwtToken {
  def token: String
}

object JwtToken {

  final case class Token(token: String) extends JwtToken // From registered claims

  final case class TokenH(token: String) extends JwtToken // From registered claims + header

  final case class TokenP(token: String) extends JwtToken // From registered claims + payload

  final case class TokenHP(token: String) extends JwtToken // From registered claims + header + payload
}

```

```scala
import io.circe.generic.auto._
import io.oath.jwt.syntax._
import io.oath.jwt.circe._

final case class Foo(name: String, age: Int)

val config = VerifierConfig.loadOrThrow("token") // HMAC256 with "secret" as secret
val verifier = new JwtVerifier(config)
val token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJuYW1lIjoiZm9vIiwiYWdlIjoxMH0.oeU3zySKPA-fowGQkl0WPDwyBhXJUEtobSjGQsDBXcs"

val claims: Either[JwtVerifyError, JwtClaims.ClaimsP[Foo]] = verifier.verifyJwt[Foo](token.toTokenP)

// Right(ClaimsP(Foo(foo,10),RegisteredClaims(None,None,List(),None,None,None,None)))
```

#### Manager Overview

Used for verifying and issuing JWT Tokens, see [configuration](#configuration).

```scala
import io.circe.generic.auto._
import eu.timepit.refined.auto._
import io.oath.jwt.syntax._
import io.oath.jwt.circe._

final case class Foo(name: String, age: Int)

val config = ManagerConfig.loadOrThrow("token")
val manager = new JwtManager(config)
val foo = Foo("foo", 10)

val jwt: Jwt[JwtClaims.ClaimsP[Foo]] = manager.issueJwt(foo.toClaimsP).toOption.get
val claims: JwtClaims.ClaimsP[Foo] = manager.verifyJwt[Foo](jwt.token.toTokenP).toOption.get

```

### Advanced Encryption Standard (AES)

Sensitive data in JWT Tokens might lead to an exposure of unwanted information (User data, Internal technologies, etc.).
It's recommended to encrypt the data when send over networks to prevent data leaks and been exposed to attacks.
To enable encryption you must provide a `secret` key to the [configuration](#configuration) file.

```hocon
  encrypt {
  secret = "password"
}
```

### Ad-hoc Registered Claims

The library also provides ad-hoc claims manipulation with priority to the claims that have been provided by the code.

```hocon
token {
  algorithm {
    name = "HMAC256"
    secret = "secret"
  }
  issuer {
    registered {
      issuer-claim = "issuer"
      subject-claim = "subject"
    }
  }
}
```

```scala
import io.circe.generic.auto._
import io.oath.jwt.model._
import io.oath.syntax._
import io.oath.circe._

final case class Foo(name: String, age: Int)

val config = IssuerConfig.loadOrThrow("token")
val issuer = new JwtIssuer(config)
val foo = Foo("foo", 10)
val adHocClaimsP = JwtClaims.ClaimsP(foo, RegisteredClaims.empty.copy(iss = Some("foo")))

val maybeJwt: Either[IssueJwtError, Jwt[JwtClaims.ClaimsP[Foo]]] = issuer.issueJwt(adHocClaimsP)

// Right(Jwt(ClaimsP(Foo(foo,10),RegisteredClaims(Some(foo),Some(subject),List(),None,None,None,None)),eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJuYW1lIjoiZm9vIiwiaXNzIjoiaXNzdWVyIiwiYWdlIjoxMH0.Dlow6pYmJ-5STSuEzL3WYnjpCrGYMKzadIwlOK_WBBc))
```

### Configuration

**algorithm:**

| Key                              | Type   | Description                                | Required                                              |
|----------------------------------|--------|--------------------------------------------|-------------------------------------------------------|
| `algorithm.name`                 | String | The Algorithm name (HMAC256, RSA256, etc.) | ✅                                                     |
| `algorithm.private-key-pem-path` | String | Private key pem file path                  | ✅ Only for asymmetric algorithms and issuing tokens   |
| `algorithm.public-key-pem-path`  | String | Public key pem file path                   | ✅ Only for asymmetric algorithms and verifying tokens |
| `algorithm.secret`               | String | Secret signing key                         | ✅ Only for symmetric algorithms                       |

**encrypt:**

| Key              | Type   | Description           | Required |
|------------------|--------|-----------------------|----------|
| `encrypt.secret` | String | Secret encryption key | ❎        |

**issuer:**

| Key                                         | Type         | Description                                  | Required | Default | 
|---------------------------------------------|--------------|----------------------------------------------|----------|---------|
| `issuer.registered.issuer-claim`            | String       | `iss` claim value                            | ❎        | Null    | 
| `issuer.registered.subject-claim`           | String       | `sub` claim value                            | ❎        | Null    |
| `issuer.registered.audience-claims`         | List[String] | `aud` claim values                           | ❎        | Null    |
| `issuer.registered.include-issued-at-claim` | Boolean      | `iat` claim auto-generated value             | ❎        | false   |
| `issuer.registered.include-jwt-id-claim`    | Boolean      | `jti` claim auto-generated value             | ❎        | false   |
| `issuer.registered.expires-at-offset`       | Duration     | `exp` claim adjust time with offset provided | ❎        | Null    |
| `issuer.registered.not-before-offset`       | Duration     | `nbf` claim adjust time with offset provided | ❎        | Null    |

**verifier:**

| Key                                      | Type         | Description                                                             | Required | Default | 
|------------------------------------------|--------------|-------------------------------------------------------------------------|----------|---------|
| `verifier.provided-with.issuer-claim`    | String       | Verify `iss` claim contains the exact value                             | ❎        | Null    | 
| `verifier.provided-with.subject-claim`   | String       | Verify `sub` claim contains the exact value                             | ❎        | Null    |
| `verifier.provided-with.audience-claims` | List[String] | Verify `aud` claim contains the exact values                            | ❎        | Null    |
| `verifier.leeway-window.leeway`          | Duration     | Leeway window allow late JWTs with offset, checks [`exp`, `nbf`, `iat`] | ❎        | Null    |
| `verifier.leeway-window.issued-at`       | Duration     | Leeway window allow late JWTs with offset, checks [`iat`]               | ❎        | Null    |
| `verifier.leeway-window.expires-at`      | Duration     | Leeway window allow late JWTs with offset, checks [`exp`]               | ❎        | Null    |
| `verifier.leeway-window.not-before`      | Duration     | Leeway window allow late JWTs with offset, checks [`nbf`]               | ❎        | Null    |

#### Issuer Sample

```hocon
token {
  algorithm {
    name = "RS256"
    private-key-pem-path = "src/test/secrets/rsa-private.pem"
  }
  //  algorithm { 
  //    name = "HMAC256"
  //    secret = "secret" When using HMAC single secret is required for both verifier and issuer
  //  }
  encrypt {
    secret = "password"
  }
  issuer {
    registered {
      issuer-claim = "issuer"
      subject-claim = "subject"
      audience-claims = ["aud1", "aud2"]
      include-issued-at-claim = true
      include-jwt-id-claim = false
      expires-at-offset = 1 day
      not-before-offset = 1 minute
    }
  }
}
```

#### Verifier Sample

```hocon
token {
  algorithm {
    name = "RS256"
    public-key-pem-path = "src/test/secrets/rsa-public.pem"
  }
  //  algorithm { 
  //    name = "HMAC256"
  //    secret = "secret" When using HMAC single secret is required for both verifier and issuer
  //  }
  encrypt {
    secret = "password"
  }
  verifier {
    provided-with {
      issuer-claim = "issuer"
      subject-claim = "subject"
      audience-claims = []
    }
    leeway-window {
      issued-at = 4 minutes
      expires-at = 3 minutes
      not-before = 2 minutes
    }
  }
}
```

#### Manager Sample

```hocon
token {
  algorithm {
    name = "RS256"
    private-key-pem-path = "src/test/secrets/rsa-private.pem"
    public-key-pem-path = "src/test/secrets/rsa-public.pem"
  }
  //  algorithm { 
  //    name = "HMAC256"
  //    secret = "secret" When using HMAC single secret is required for both verifier and issuer
  //  }
  encrypt {
    secret = "password"
  }
  issuer {
    registered {
      issuer-claim = "issuer"
      subject-claim = "subject"
      audience-claims = ["aud1", "aud2"]
      include-issued-at-claim = true
      include-jwt-id-claim = false
      expires-at-offset = 1 day
      not-before-offset = 1 minute
    }
  }
  verifier {
    provided-with {
      issuer-claim = ${token.issuer.registered.issuer-claim}
      subject-claim = ${token.issuer.registered.subject-claim}
      audience-claims = ${token.issuer.registered.audience-claims}
    }
    leeway-window {
      issued-at = 4 minutes
      expires-at = 3 minutes
      not-before = 2 minutes
    }
  }
}
```
