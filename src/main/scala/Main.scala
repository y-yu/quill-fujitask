import java.util.concurrent.TimeUnit
import domain.entity.user.User
import domain.repository.Db
import domain.service.MixInUserService
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.util.Try

object Main extends MixInUserService {
  def values[A](a: Future[A]): A = Await.result(a, Duration(10, TimeUnit.SECONDS))

  def setUp(): Future[Unit] = {
    Future {
      Db.setUp()
      Db.createTable()
    }
  }

  def tearDown(): Future[Unit] =
    Future(Db.close())

  def user(): Unit = {
    val okumin = User(1, "okumin")
    val randy = User(2, "randy")

    val user1 = values(userService.readOrCreate(okumin))
    val user2 = values(userService.readOrCreate(okumin))
    val user3 = values(userService.readOrCreate(randy))

    println(user1)
    println(user2)
    println(user3)

    val users1 = values(userService.readAll)

    println(users1)

    val okumin2 = okumin.copy(name = "okumin2")

    val user4 = values(userService.updateOrCreate(okumin2))

    println(user4)

    val user5 = Try(values(userService.readOrFail(2)))

    println(user5)
  }

  def main(args: Array[String]): Unit = {
    val f = for {
      _ <- setUp()
      u = user()
      _ <- tearDown()
    } yield ()

    values(f)
  }
}