# ScalaScript: Social
Type-safe Scala.js Bindings for Social Networks (including Facebook and LinkedIn)

## Introduction

ScalaScript-Social is a type-safe Scala.js binding for Social Networks; currently limited to Facebook and LinkedIn, 
but others will follow (including Google Plus and Twitter)

## Sample Code for Facebook

Within your AngularJS Scala.js application, you define the Facebook service.

```scala
module.serviceOf[Facebook]("Facebook")
```

Within your Scala.js application, you can call the Facebook login

```scala
var facebookID: js.UndefOr[String] = js.undefined

def loginToFacebook() {
    facebook.login() onComplete {
      case Success(response) =>
        facebookID = response.authResponse.userID

        // retrieve the taggle friends
        facebook.getTaggableFriends onComplete {
          case Success(friends) => fbFriends = friends
          case Failure(e) =>
            console.error(s"Facebook friends retrieval failed: ${e.displayMessage}")
        }
      case Failure(e) =>
        toaster.error("Facebook Login Error", e.displayMessage)
    }
  }
```

Afterwards, you may call any Facebook API that you have the permissions to execute:

```scala
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
    toaster.error(s"Failed to retrieve Facebook profile and friends - ${e.getMessage}")
}
()
```

## Sample Code for LinkedIn

Inside of your HTML page:

```html
<script type="text/javascript" src="//platform.linkedin.com/in.js">
    api_key: [YOUR_KEY_GOES_HERE]
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
var linkedInID: js.UndefOr[String] = js.undefined

// read the authenticated user's profile
IN.API.Profile(js.Array("me")) onComplete {
  case Success(response) =>
    linkedInID = response.values.headOption.flatMap(_.id.toOption).orUndefined
  case Failure(e) =>
    console.error(s"Failed to retrieve LinkedIn profile - ${e.getMessage}")
}
```