import domain.entity.user.User
import domain.repository.quill.SetupDb
import domain.service.MixInUserService
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Main extends MixInUserService {
  def main(args: Array[String]): Unit = {
    SetupDb.createTables()

    val okumin = User(1, "okumin")
    val randy = User(2, "randy")

    val user1 = Await.result(userService.readOrCreate(okumin), Duration.Inf)
    val user2 = Await.result(userService.readOrCreate(okumin), Duration.Inf)
    val user3 = Await.result(userService.readOrCreate(randy), Duration.Inf)

    println(user1)
    println(user2)
    println(user3)
  }
}