package com.github.ldaniels528.scalascript.social.facebook

import com.github.ldaniels528.scalascript.util.ScalaJsHelper._
import org.scalajs.dom.console

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g}
import scala.scalajs.js.annotation.JSExport

/**
  * Facebook Injector
  * @author lawrence.daniels@gmail.com
  */
case class FacebookInjector(facebook: Facebook, apiVersion: String = "v2.5", facebookAppID: String => String, onInit: (Facebook, FacebookAppConfig) => Unit = (f, c) => ()) {
  private lazy val FB = Facebook.SDK

  // capture the application ID and API version
  val appID = facebookAppID(g.location.hostname.as[String])
  facebook.appID = appID
  facebook.version = apiVersion

  /**
    * Initializes the Facebook SDK
    */
  g.fbAsyncInit = (() => {
    console.log(s"Initializing Facebook SDK (App ID $appID)...")
    val config = FacebookAppConfig(appId = appID, status = true, xfbml = true, version = apiVersion)
    FB.init(config)

    onInit(facebook, config)
    ()
  }): js.Function0[Unit]

  /**
    * Inject the Facebook SDK
    */
  @JSExport
  def init: js.Function0[Unit] = () => inject(g.document)

  /**
    * Injects the Facebook SDK
    * @param fbroot the Facebook root element
    */
  private def inject(fbroot: js.Dynamic) {
    // is the element our script?
    val id = "facebook-jssdk"
    if (!isDefined(fbroot.getElementById(id))) {
      // dynamically create the script
      val fbScript = fbroot.createElement("script")
      fbScript.id = id
      fbScript.async = true
      fbScript.src = "http://connect.facebook.net/en_US/all.js"

      // get the script and insert our dynamic script
      val ref = fbroot.getElementsByTagName("script").asArray[js.Dynamic](0)
      ref.parentNode.insertBefore(fbScript, ref)
    }
    ()
  }

}

