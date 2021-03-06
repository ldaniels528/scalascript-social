# ScalaScript: Social
Type-safe Scala.js Bindings for Social Networks (including Facebook and LinkedIn)

## Introduction

ScalaScript-Social is a type-safe Scala.js binding for Social Networks; currently limited to Facebook and LinkedIn, 
but others will follow (including Google Plus and Twitter)

<a name="development"></a>
## Development

<a name="build-requirements"></a>
### Build Requirements

* [Java SDK 1.7] (http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html)
* [Scala 2.11.x] (http://scala-lang.org/download/)
* [SBT 0.13+] (http://www.scala-sbt.org/download.html)

### Building and installation

```
~\> sbt clean publish-local
```

## Sample Code for Facebook

Inside of your HTML index page:

```javascript
<script>
    (function(d, s, id){
        var js, fjs = d.getElementsByTagName(s)[0];
        if (d.getElementById(id)) {return;}
        js = d.createElement(s); js.id = id;
        js.src = "//connect.facebook.net/en_US/sdk.js";
        fjs.parentNode.insertBefore(js, fjs);
    }(document, 'script', 'facebook-jssdk'));
</script>
```

Within your Scala.js application, you can initialize the Facebook SDK:

```scala
import com.github.ldaniels528.scalascript.social.facebook.Facebook.FB

val config = FacebookAppConfig(appId = "[YOUR_APP_KEY_GOES_HERE]", status = true, xfbml = true, version = "v2.5")
FB.init(config)
```

Assuming you're using AngularJS, within your Scala.js application, you define the AngularJS Facebook service:

```scala
module.serviceOf[FacebookService]("Facebook")
```
      
Finally, within your AngularJS controller or service you invoke the Facebook login:   
  
```scala    
import com.github.ldaniels528.scalascript.util.ScalaJsHelper._

class SocialController($scope: SocialControllerScope, @injected("Facebook") facebook: FacebookService) extends Controller {
    private var facebookID: js.UndefOr[String] = js.undefined

    $scope.loginToFacebook = () => {
        facebook.login() onComplete {
          case Success(response) =>
            facebookID = response.authResponse.userID
          case Failure(e) =>
            toaster.error("Facebook Login Error", e.displayMessage)
        }
    }   
}

@js.native
trait SocialControllerScope extends Scope {
    var loginToFacebook: js.Function0[Unit] = js.native
}
```

Afterwards, you may call any Facebook API that you have the permissions to execute:

```scala
import com.github.ldaniels528.scalascript.util.ScalaJsHelper._

val outcome = for {
  // load the user"s Facebook profile
  fbProfile <- facebook.getUserProfile
  fbFriends <- facebook.getTaggableFriends
} yield (fbProfile, friends)

outcome onComplete {
  case Success((fbProfile, friends)) =>
    console.log("fbProfile = ${angular.toJson(fbProfile, pretty = true)}")
    console.log(s"fbFriends = ${angular.toJson(fbFriends, pretty = true)}")
  case Failure(e) =>
    toaster.error(s"Failed to retrieve Facebook profile and friends - ${e.displayMessage}")
}
()
```

If you're not using AngularJS, you can use the Facebook SDK directly:

```scala
FB.login((response: js.UndefOr[FacebookLoginStatusResponse]) =>
  response.toOption match {
    case Some(resp) if resp.error.isEmpty =>
        console.log(s"auth = ${angular.toJson(auth)}")
    case Some(resp) => console.error(s"Error logging in to Facebook: ${resp.error}")
    case None => console.error("No response from Facebook servers")
  })     
```

## Sample Code for LinkedIn

Inside of your HTML index page:

```html
<script type="text/javascript" src="//platform.linkedin.com/in.js">
    api_key: [YOUR_API_KEY_GOES_HERE]
    authorize: true
    onLoad: linkedInInit
    scope: r_basicprofile r_emailaddress rw_company_admin w_share
</script>
```

Within your Scala.js application:

```scala
js.Dynamic.global.linkedInInit = () => {
  val injector = angular.element(jQuery("#Main")).injector()
  injector.get[MySessionService]("MySession").toOption match {
    case Some(mySession) =>
      console.info("Initializing LinkedIn API...")
      mySession.initLinkedIn(LinkedIn.IN)
    case None =>
      console.error("MySession could not be retrieved.")
  }
}
```

Afterwards, you may call any LinkedIn API that you have the permissions to execute: 

```scala
import com.github.ldaniels528.scalascript.social.linkedin.LinkedIn.IN

var linkedInID: js.UndefOr[String] = js.undefined

// read the authenticated user's profile
IN.API.Profile(js.Array("me")) onComplete {
  case Success(response) =>
    linkedInID = response.values.headOption.flatMap(_.id.toOption).orUndefined
  case Failure(e) =>
    console.error(s"Failed to retrieve LinkedIn profile - ${e.getMessage}")
}
```