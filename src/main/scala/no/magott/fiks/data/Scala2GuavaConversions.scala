package no.magott.fiks.data
import com.google.common.base.{Function => GuavaFunction}
import java.util.concurrent.Callable

object Scala2GuavaConversions {

  implicit def scalaFunction2GuavaFunction[K, V](fn: (K) => V) =
    new GuavaFunction[K, V] {
      def apply(key: K) = fn(key)
    }

  implicit def fun2Call[R](f: () => R) = new Callable[R] { def call : R = f() }
}
