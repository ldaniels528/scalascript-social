package com.github.ldaniels528.scalascript.social.linkedin

import com.github.ldaniels528.scalascript._
import com.github.ldaniels528.scalascript.core.HttpError

import scala.concurrent.{Future, Promise}
import scala.language.implicitConversions
import scala.scalajs.js
import scala.scalajs.js.UndefOr

/**
  * LinkedIn Promise
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait LinkedInPromise[T <: js.Any] extends js.Object {

  def error(callback: js.Function): LinkedInPromise[T] = js.native

  def error(callback: js.Function1[js.Any, Unit]): LinkedInPromise[T] = js.native

  def error(callback: js.Function2[js.Any, Int, Unit]): LinkedInPromise[T] = js.native

  def error(callback: js.Function3[js.Any, js.Any, Int, Unit]): LinkedInPromise[T] = js.native

  def error(callback: js.Function4[js.Any, Int, js.Any, js.Any, Unit]): LinkedInPromise[T] = js.native

  def error(callback: js.Function5[js.Any, Int, js.Any, js.Any, UndefOr[String], Unit]): LinkedInPromise[T] = js.native

  def result(callback: js.Function): LinkedInPromise[T] = js.native

  def result(callback: js.Function1[T, Unit]): LinkedInPromise[T] = js.native

  def result(callback: js.Function2[T, Int, Unit]): LinkedInPromise[T] = js.native

  def result(callback: js.Function3[T, js.Any, Int, Unit]): LinkedInPromise[T] = js.native

  def result(callback: js.Function4[T, Int, js.Any, js.Any, Unit]): LinkedInPromise[T] = js.native

  def result(callback: js.Function5[T, Int, js.Any, js.Any, js.Any, Unit]): LinkedInPromise[T] = js.native

  def `then`: js.Function3[js.Function, js.Function, js.Function, LinkedInPromise[T]] = js.native

}

/**
  * LinkedIn Promise Companion Object
  * @author lawrence.daniels@gmail.com
  */
object LinkedInPromise {

  /**
    * Implicit conversion to transform a LinkedIn Promise into a Scala Future
    * @param inPromise the given [[LinkedInPromise LinkedIn Promise]]
    * @return the wrapped [[Future Future]]
    */
  implicit def promise2future[T <: js.Any](inPromise: LinkedInPromise[T]): Future[T] = {
    val promise = Promise[T]()

    def onSuccess(data: T): Unit = promise.success(data)

    def onError(data: js.Any, status: Int, config: js.Any, headers: js.Any, statusText: UndefOr[String]): Unit = {
      promise failure HttpError(status, statusText getOrElse s"Failed to process request: '${angular.toJson(data)}'")
    }

    inPromise.result(onSuccess _).error(onError _)
    promise.future
  }

}
