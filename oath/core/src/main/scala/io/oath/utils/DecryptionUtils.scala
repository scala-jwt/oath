package io.oath.utils

import io.oath.JwtVerifyError
import io.oath.JwtVerifyError.DecryptionError

import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import scala.util.control.Exception.allCatch

private[oath] object DecryptionUtils {

  private def hexStringToByte(hexString: String): Array[Byte] = {
    // https://stackoverflow.com/questions/140131/convert-a-string-representation-of-a-hex-dump-to-a-byte-array-using-java
    val len  = hexString.length
    val data = new Array[Byte](len / 2)
    var i    = 0
    while i < len do
      data(i / 2) = ((Character.digit(hexString.charAt(i), 16) << 4) +
        Character.digit(hexString.charAt(i + 1), 16)).toByte
      i += 2
    data
  }

  def decryptAES(message: String, secret: String): Either[DecryptionError, String] =
    allCatch.withTry {
      val raw           = java.util.Arrays.copyOf(secret.getBytes(UTF8), 16)
      val secretKeySpec = new SecretKeySpec(raw, AES)
      val cipher        = Cipher.getInstance(AES)
      cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
      new String(cipher.doFinal(hexStringToByte(message)))
    }.toEither.left.map(e => JwtVerifyError.DecryptionError(e.getMessage))
}
