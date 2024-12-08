package io.oath

import io.oath.macros.OathEnum

enum OathToken derives OathEnum {
  case AccessToken, RefreshToken, ActivationEmailToken, ForgotPasswordToken
}
