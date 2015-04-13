package no.magott.fiks.data

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor
import org.scalatest.FunSuite

import scala.util.Properties

/**
 *
 */
class EncryptionTest extends FunSuite{

  test("foo") {
    val cipher = new StandardPBEStringEncryptor();
    cipher.setProvider(new BouncyCastleProvider());
    cipher.setPassword(Properties.envOrElse("ENCRYPTION_PWD", "foo"))
    cipher.setAlgorithm("PBEWITHSHA256AND256BITAES-CBC-BC")
    cipher.encrypt("foooodasdsadasdsado")
  }

}
