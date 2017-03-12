package domain.service

import domain.entity.user.User
import domain.repository.{MixInUserRepository, UsesUserRepository}
import domain.repository.quill.impl._
import fujitask.Task
import scala.concurrent.Future

trait UsesUserService {
  val userService: UserService
}

trait MixInUserService {
  val userService: UserService = new UserService with MixInUserRepository
}

trait UserService extends UsesUserRepository {
  def readOrCreate(user: User): Future[User] =
    (for {
      userOpt <- userRepository.read(user.id)
      user <- if (userOpt.isEmpty)
                userRepository.create(user.name)
              else
                Task(userOpt.get)
    } yield user).run()
}
