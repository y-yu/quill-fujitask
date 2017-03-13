import java.util.concurrent.TimeUnit
import domain.entity.user.User
import domain.repository.quill.Db
import domain.service.MixInUserService
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.util.Try

object Main extends MixInUserService {
  def values[A](a: Future[A]): A = Await.result(a, Duration(10, TimeUnit.SECONDS))

  def main(args: Array[String]): Unit = {
    Db.setUp()
    Db.createTables()

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

    Db.close()
  }
}