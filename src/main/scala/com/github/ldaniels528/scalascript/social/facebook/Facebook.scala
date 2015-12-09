package com.github.ldaniels528.scalascript.social.facebook

import com.github.ldaniels528.scalascript.core.{Q, QDefer}
import com.github.ldaniels528.scalascript.util.ScalaJsHelper._
import com.github.ldaniels528.scalascript.{Service, angular}
import org.scalajs.dom.console

import scala.scalajs.js
import scala.util.{Failure, Success, Try}

/**
  * Facebook Service (v2.5)
  * @author lawrence.daniels@gmail.com
  * @see [[https://developers.facebook.com/docs/graph-api/using-graph-api/v2.5]]
  */
class Facebook($q: Q) extends Service {
  type CallbackObject = js.Function1[js.Dynamic, Unit]
  type PaginationCallback[T] = js.Function1[FacebookPagination[T], Unit]

  // define the Facebook SDK singleton
  private lazy val FB = Facebook.SDK

  // define the API properties
  var appID: js.UndefOr[String] = js.undefined
  var auth: js.UndefOr[FacebookAuthResponse] = js.undefined
  var version = "v2.5"

  ///////////////////////////////////////////////////////////////////////////
  //      Authentication and User Profile-related Functions
  ///////////////////////////////////////////////////////////////////////////

  def accessToken: js.UndefOr[String] = auth.map(_.accessToken)

  def facebookID: js.UndefOr[String] = auth.map(_.userID)

  def getLoginStatus = {
    val deferred = $q.defer[FacebookLoginStatusResponse]()
    FB.getLoginStatus((response: js.UndefOr[FacebookLoginStatusResponse]) =>
      specialHandling(deferred, response) {
        case resp if resp.status == "connected" =>
          auth = resp.authResponse
          console.log(s"facebookID = $facebookID, auth = ${angular.toJson(auth)}")
          Success(resp)
        case resp =>
          Failure(new RuntimeException(s"Facebook is not connected (status: ${resp.status})"))
      })
    deferred.promise
  }

  def getUserProfile = {
    val deferred = $q.defer[FacebookProfileResponse]()
    FB.api(fbURL(), (response: js.UndefOr[FacebookProfileResponse]) => handleResponse(deferred, response))
    deferred.promise
  }

  def login() = {
    val deferred = $q.defer[FacebookLoginStatusResponse]()
    FB.login((response: js.UndefOr[FacebookLoginStatusResponse]) =>
      specialHandling(deferred, response) { resp =>
        auth = resp.authResponse
        console.log(s"facebookID = $facebookID, auth = ${angular.toJson(auth)}")
        Success(resp)
      })
    deferred.promise
  }

  def logout() = {
    val deferred = $q.defer[FacebookLoginStatusResponse]()
    FB.logout((response: js.UndefOr[FacebookLoginStatusResponse]) => handleResponse(deferred, response))
    deferred.promise
  }

  ///////////////////////////////////////////////////////////////////////////
  //      Achievement Functions
  ///////////////////////////////////////////////////////////////////////////

  /**
    * Retrieves an achievement by ID
    * @param achievementID the given achievement ID
    * @return a promise of a [[FacebookAchievementResponse achievement response]]
    * @see [[https://developers.facebook.com/docs/games/achievements]]
    */
  def getAchievement(achievementID: String) = {
    val deferred = $q.defer[FacebookAchievementResponse]()
    FB.api(fbURL(s"/achievements"), (response: js.UndefOr[FacebookAchievementResponse]) => handleResponse(deferred, response))
    deferred.promise
  }

  ///////////////////////////////////////////////////////////////////////////
  //      Friend-related Functions
  ///////////////////////////////////////////////////////////////////////////

  def createFriendList(friendListId: String) = {
    val deferred = $q.defer[FacebookResponse]()
    FB.api(fbURL(s"/$friendListId/member"), (response: js.UndefOr[FacebookResponse]) => handleResponse(deferred, response))
    deferred.promise
  }

  def getFriends = {
    val deferred = $q.defer[js.Array[TaggableFriend]]()
    val friends = emptyArray[TaggableFriend]
    FB.api(fbURL("/friends"), (response: FacebookPagination[TaggableFriend]) => {
      console.log(s"response = ${angular.toJson(response)}")
      val results = response.data
      if (results.nonEmpty) {
        friends.push(results: _*)
        console.log(s"${friends.length} friend(s) loaded")
      }
      ()
    })
    deferred.promise
  }

  def getFriendList(listType: js.UndefOr[String] = "close_friends") = {
    val deferred = $q.defer[FacebookResponse]()
    FB.api(fbURL("/friendlists", s"list_type=$listType"), (response: js.UndefOr[FacebookResponse]) => handleResponse(deferred, response))
    deferred.promise
  }

  def getFriendListMembers(friendListId: String) = {
    val deferred = $q.defer[FacebookResponse]()
    FB.api(fbURL(s"/$friendListId/members"), (response: js.UndefOr[FacebookResponse]) => handleResponse(deferred, response))
    deferred.promise
  }

  /**
    * Retrieves all "taggable" friends for the authenticated user
    * @return the array of [[TaggableFriend taggable friends]]
    */
  def getTaggableFriends = {
    val deferred = $q.defer[js.Array[TaggableFriend]]()
    val friends = emptyArray[TaggableFriend]
    val callback: PaginationCallback[TaggableFriend] = (response: FacebookPagination[TaggableFriend]) => {
      console.log(s"response = ${angular.toJson(response)}")
      val results = response.data
      if (results.nonEmpty) {
        friends.push(results: _*)
        console.log(s"${friends.length} friend(s) loaded")
      }
      ()
    }
    FB.api(fbURL("/taggable_friends"), { (response: TaggableFriendsResponse) =>
      handlePaginatedResults(response, callback)
      deferred.resolve(friends)
    })
    deferred.promise
  }

  ///////////////////////////////////////////////////////////////////////////
  //      Photo-related Functions
  ///////////////////////////////////////////////////////////////////////////

  /**
    * Retrieves photos for the current user
    * @param `type` the given photo type (e.g. "uploaded" or "tagged")
    * @return a promise of an array of [[FacebookPhoto photos]]
    * @see [[https://developers.facebook.com/docs/graph-api/reference/user/photos/]]
    */
  def getPhotos(`type`: js.UndefOr[String] = js.undefined) = {
    val deferred = $q.defer[FacebookPhotosResponse]()
    FB.api(fbURL("/photos", `type` map (myType => s"type=$myType")), (response: FacebookPhotosResponse) => handleResponse(deferred, response))
    deferred.promise
  }

  ///////////////////////////////////////////////////////////////////////////
  //      Other Functions
  ///////////////////////////////////////////////////////////////////////////

  def feed(caption: String, link: String) = {
    val deferred = $q.defer[FacebookResponse]()
    FB.ui(FacebookCommand(app_id = appID, method = "feed", link = link, caption = caption), (response: js.UndefOr[FacebookResponse]) => handleResponse(deferred, response))
    deferred.promise
  }

  def send(message: String, link: String) = {
    val deferred = $q.defer[FacebookResponse]()
    FB.ui(FacebookCommand(app_id = appID, method = "send", link = link), (response: js.UndefOr[FacebookResponse]) => handleResponse(deferred, response))
    deferred.promise
  }

  def share(link: String) = {
    val deferred = $q.defer[FacebookResponse]()
    FB.ui(FacebookCommand(app_id = appID, method = "share", href = link), (response: js.UndefOr[FacebookResponse]) => handleResponse(deferred, response))
    deferred.promise
  }

  ///////////////////////////////////////////////////////////////////////////
  //      Private Functions
  ///////////////////////////////////////////////////////////////////////////

  /**
    * Returns the URL for a Facebook API call
    * @param path the optional Facebook node (e.g. "/photos")
    * @param args the optional parameters
    * @param fbUserID the optional Facebook User ID (e.g. "10203751019174743")
    * @return the URL (e.g. "/v2.5/me/photos?access_token=....")
    */
  private def fbURL(path: String = "", args: js.UndefOr[String] = js.undefined, fbUserID: String = "me") = {
    s"/$version/$fbUserID$path?access_token=$accessToken" + (args map (myArgs => s"&$myArgs") getOrElse "")
  }

  private def handleResponse[A <: FacebookResponse](deferred: QDefer[A], response: js.UndefOr[A]) = {
    response.toOption match {
      case Some(resp) if resp.error.isEmpty => deferred.resolve(resp)
      case Some(resp) => deferred.reject(resp.error)
      case None => deferred.reject("No response from Facebook")
    }
  }

  /**
    * Recursive retrieves a set of paginated results
    * @param response the given [[FacebookPagination pagination set]]
    * @param callback the function callback
    * @tparam A the paginated data type
    * @see [[https://developers.facebook.com/docs/graph-api/using-graph-api/v2.5#paging]]
    */
  private def handlePaginatedResults[A](response: FacebookPagination[A], callback: PaginationCallback[A]) {
    // TODO implement both cursor and time-based pagination
    // perform the callback for this response
    callback(response)

    // if there are more results, recursively extract them
    response.paging.foreach(_.next foreach { url =>
      FB.api(url, (response: FacebookPagination[A]) => handlePaginatedResults(response, callback))
    })
  }

  private def specialHandling[A <: FacebookResponse](deferred: QDefer[A], response: js.UndefOr[A])(handler: A => Try[A]) = {
    response.toOption match {
      case Some(resp) if resp.error.isEmpty =>
        handler(resp) match {
          case Success(value) => deferred.resolve(value)
          case Failure(e) => deferred.reject(e.getMessage)
        }
      case Some(resp) => deferred.reject(resp.error)
      case None => deferred.reject("No response from Facebook")
    }
  }

}

/**
  * Facebook Singleton
  * @author lawrence.daniels@gmail.com
  */
object Facebook {
  lazy val SDK = js.Dynamic.global.FB.asInstanceOf[js.UndefOr[FacebookSDK]]
    .getOrElse(throw new IllegalStateException("Facebook SDK is not loaded"))

}

/**
  * Facebook SDK
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait FacebookSDK extends js.Object {

  def api(url: String, callback: js.Function): Unit

  def getLoginStatus(callback: js.Function1[js.UndefOr[FacebookLoginStatusResponse], Any]): Unit

  def init(config: FacebookAppConfig): Unit

  def login(callback: js.Function1[js.UndefOr[FacebookLoginStatusResponse], Any]): Unit

  def logout(callback: js.Function1[js.UndefOr[FacebookLoginStatusResponse], Any]): Unit

  def ui(command: FacebookCommand, callback: js.Function): Unit

}

/*
 * Facebook Application Object
 * @author lawrence.daniels@gmail.com
 * @see [[https://developers.facebook.com/docs/graph-api/reference/application]]
 */
@js.native
trait FacebookApp extends FacebookResponse {
  var id: String
  var android_key_hash: js.Array[String]
  var android_sdk_error_categories: js.Array[FacebookMobileSdkErrorCategory]
  var app_ad_debug_info: FacebookApplicationAppAdDebugInfo
  var app_domains: js.Array[String]
  var app_events_feature_bitmask: Long
  var app_install_tracked: Boolean
  var app_name: String
  var app_type: Long
  var auth_dialog_data_help_url: String
  var auth_dialog_headline: String
  var auth_dialog_perms_explanation: String
  var auth_referral_default_activity_privacy: String
  var auth_referral_enabled: Long
  var auth_referral_extended_perms: js.Array[String]
  var auth_referral_friend_perms: js.Array[String]
  var auth_referral_response_type: String
  var auth_referral_user_perms: js.Array[String]
  var canvas_fluid_height: Boolean
  var canvas_fluid_width: Long
  var canvas_url: String
  var category: String
  var client_config: js.Dictionary[js.Any]
  var company: String
  var configured_ios_sso: Boolean
  var contact_email: String
  var context: js.Object
  // ApplicationContext
  var created_time: js.Date
  var creator_uid: js.Object // id
  // ...
}

/*
 * Facebook Application App Ad Debug Info
 * @author lawrence.daniels@gmail.com
 * @see [[https://developers.facebook.com/docs/graph-api/reference/application]]
 */
@js.native
trait FacebookApplicationAppAdDebugInfo extends FacebookResponse

/*
 * Facebook Achievement Response
 * @author lawrence.daniels@gmail.com
 * @see [[https://developers.facebook.com/docs/graph-api/reference/v2.5/achievement]]
 */
@js.native
trait FacebookAchievementResponse extends FacebookResponse {
  var id: js.UndefOr[String]
  var from: js.UndefOr[js.Object]
  // User
  var publish_time: js.UndefOr[js.Date]
  var application: js.UndefOr[FacebookApp]
  var data: js.Object
  var `type`: String
  var no_feed_story: Boolean
}

/*
 * Facebook Authorization Response
 * @author lawrence.daniels@gmail.com
 */
@js.native
trait FacebookAuthResponse extends FacebookResponse {
  var accessToken: String
  var signedRequest: String
  var userID: String
  var expiresIn: Int
}

/*
 * Facebook Login Response
 * @author lawrence.daniels@gmail.com
 */
@js.native
trait FacebookLoginStatusResponse extends FacebookResponse {
  var authResponse: FacebookAuthResponse
  var status: String
}

/*
 * Facebook Mobile SDK Error Category
 * @author lawrence.daniels@gmail.com
 */
@js.native
trait FacebookMobileSdkErrorCategory extends js.Object

/**
  * Facebook Photo
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait FacebookPhoto extends js.Object {
  var data: js.Array[FacebookPictureData]
}

/**
  * Facebook Photos Response
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait FacebookPhotosResponse extends FacebookResponse {
  var data: js.Array[FacebookPhoto]
  var paging: js.UndefOr[FacebookPaging]
}

/**
  * Represents a Facebook Profile
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait FacebookProfileResponse extends FacebookResponse {
  var id: String
  var first_name: String
  var last_name: String
  var name: String
  var gender: String
  var link: String
  var locale: String
  var updated_time: js.Date
  var timezone: Int
  var verified: Boolean
}

/**
  * Facebook Friend Picture
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait FacebookPicture extends js.Object {
  var data: FacebookPictureData
}

/**
  * Facebook Friend Picture Data
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait FacebookPictureData extends js.Object {
  var is_silhouette: Boolean
  var url: String
}

/**
  * Facebook Cursor Trait
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait FacebookCursor extends js.Object {
  var before: js.UndefOr[String]
  var after: js.UndefOr[String]
}

/**
  * Facebook Pagination Trait
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait FacebookPagination[T] extends js.Object {
  var data: js.Array[T]
  var paging: js.UndefOr[FacebookPaging]
}

/**
  * Facebook Paging Trait
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait FacebookPaging extends js.Object {
  var cursors: FacebookCursor
  var previous: js.UndefOr[String]
  var next: js.UndefOr[String]
}

/**
  * Generic Facebook Response
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait FacebookResponse extends js.Object {
  var error: js.UndefOr[String]
}

/**
  * Facebook Taggable Friend
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait TaggableFriend extends js.Object {
  var id: String
  var name: String
  var picture: FacebookPicture
}

/**
  * Facebook Taggable Friends Response
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait TaggableFriendsResponse extends FacebookPagination[TaggableFriend]