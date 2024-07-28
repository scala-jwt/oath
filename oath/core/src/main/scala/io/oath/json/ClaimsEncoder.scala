package io.oath.json

trait ClaimsEncoder[P]:
  def encode(data: P): String
