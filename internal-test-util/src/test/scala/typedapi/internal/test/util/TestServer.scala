package typedapi.internal.test.util

import cats.effect.IO
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.server.Server
import org.http4s.server.blaze._

object Age extends QueryParamDecoderMatcher[Int]("age")

object Reasons {
  def unapplySeq(params: Map[String, Seq[String]]) = params.get("reasons")
  def unapply(params: Map[String, Seq[String]]) = unapplySeq(params)
}

object TestServer {

  import UserCoding._
   
  val service = HttpService[IO] {
    case GET -> Root / "path" => Ok(User("foo", 27))
    case GET -> Root / "segment" / name => Ok(User(name, 27))

    case GET -> Root / "query" :? Age(age) => Ok(User("foo", age))

    case req @ GET -> Root / "header" => 
      val headers = req.headers.toList
      val age     = headers.find(_.name.value == "age").get.value.toInt

      Ok(User("foo", age))

    case req @ GET -> Root / "header" / "raw" => 
      val headers = req.headers.toList
      val name    = headers.find(_.name.value == "name").get.value
      val age     = headers.find(_.name.value == "age").get.value.toInt

      Ok(User(name, age))

    case GET -> Root => Ok(User("foo", 27))
    case PUT -> Root => Ok(User("foo", 27))
    case req @ PUT -> Root / "body" => Ok(User("foo", 27))
      for {
        user <- req.as[User]
        resp <- Ok(user.asJson)
      } yield resp

    case POST -> Root => Ok(User("foo", 27))
    case req @ POST -> Root / "body" => Ok(User("foo", 27))
      for {
        user <- req.as[User]
        resp <- Ok(user.asJson)
      } yield resp

    case DELETE -> Root :? Reasons(reasons) => 
      println(reasons)
      Ok(User("foo", 27))
  }

  def start(): Server[IO] = {
    val builder = BlazeBuilder[IO]
      .bindHttp(9001, "localhost")
      .mountService(service, "/")
      .start

    builder.unsafeRunSync()
  }
}
