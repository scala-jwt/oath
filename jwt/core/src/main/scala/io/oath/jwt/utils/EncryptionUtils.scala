package io.oath.jwt.utils

import io.oath.jwt.model.JwtIssueError

import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import scala.util.control.Exception.allCatch

object EncryptionUtils {

  private lazy val HexArray = "0123456789ABCDEF".toCharArray

  private def toHexString(bytes: Array[Byte]): String = {
    // from https://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
    val hexChars = new Array[Char](bytes.length * 2)
    var j        = 0
    while (j < bytes.length) {
      val v = bytes(j) & 0xff
      hexChars(j * 2)     = HexArray(v >>> 4)
      hexChars(j * 2 + 1) = HexArray(v & 0x0f)
      j += 1
    }
    new String(hexChars)
  }

  private[oath] def encryptAES(message: String, secret: String): Either[JwtIssueError.EncryptionError, String] =
    allCatch.withTry {
      val raw           = java.util.Arrays.copyOf(secret.getBytes(UTF8), 16)
      val secretKeySpec = new SecretKeySpec(raw, AES)
      val cipher        = Cipher.getInstance(AES)
      cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)
      toHexString(cipher.doFinal(message.getBytes(UTF8)))
    }.toEither.left.map(e => JwtIssueError.EncryptionError(e.getMessage))
}
