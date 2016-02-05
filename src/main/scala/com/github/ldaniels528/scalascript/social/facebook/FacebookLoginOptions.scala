package com.github.ldaniels528.scalascript.social.facebook

import com.github.ldaniels528.scalascript.util.ScalaJsHelper._

import scala.scalajs.js

@js.native
trait FacebookLoginOptions extends js.Object {
  var scope: js.UndefOr[String]
  var auth_type: js.UndefOr[String]
  var return_scopes: js.UndefOr[Boolean]
  var enable_profile_selector: js.UndefOr[Boolean]
  var profile_selector_ids: js.UndefOr[String]

}

object FacebookLoginOptions {

  def apply(scope: js.UndefOr[String] = js.undefined,
            authType: js.UndefOr[String] = js.undefined,
            returnScopes: js.UndefOr[Boolean] = js.undefined,
            enableProfileSelector: js.UndefOr[Boolean] = js.undefined,
            profileSelectorIds: js.UndefOr[String] = js.undefined) = {
    val options = makeNew[FacebookLoginOptions]
    options.scope = scope
    options.auth_type = authType
    options.return_scopes = returnScopes
    options.enable_profile_selector = enableProfileSelector
    options
  }
}
