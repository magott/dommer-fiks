package no.magott.fiks.user

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor

import scala.util.Properties

/**
 *
 */
object Encryption {

  val cipher = new StandardPBEStringEncryptor();
  cipher.setProvider(new BouncyCastleProvider());
  cipher.setPassword(Properties.envOrElse("ENCRYPTION_PWD", "foo"))
  cipher.setAlgorithm("PBEWITHSHA256AND256BITAES-CBC-BC")

}
