package no.magott.fiks.data
import com.google.common.base.{Function => GuavaFunction}

object Guava2ScalaConversions {

  implicit def scalaFunction2GuavaFunction[K, V](fn: (K) => V) =
    new GuavaFunction[K, V] {
      def apply(key: K) = fn(key)
    }
}
