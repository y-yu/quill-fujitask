package domain.service

import domain.entity.user.User
import domain.repository.{MixInUserRepository, UsesUserRepository}
import domain.repository.quill.impl._
import domain.service.exception.UserNotFoundException
import fujitask.Task
import scala.concurrent.Future
import scala.util.control.NonFatal

trait UsesUserService {
  val userService: UserService
}

trait MixInUserService {
  val userService: UserService = new UserService with MixInUserRepository
}

trait UserService extends UsesUserRepository {
  def readAll: Future[Seq[User]] =
    userRepository.readAll.run()

  def readOrFail(id: Long): Future[User] =
    (for {
      userOpt <- userRepository.read(id)
      user <- userOpt match {
        case None => Task.failed(new UserNotFoundException)
        case Some(u) => Task(u)
      }
    } yield user).run()

  def readOrCreate(user: User): Future[User] =
    (for {
      userOpt <- userRepository.read(user.id)
      user <- userOpt match {
        case None =>  userRepository.create(user.name)
        case Some(u) => Task(u)
      }
    } yield user).run()

  def updateOrCreate(user: User): Future[User] =
    userRepository.update(user).map(_ => user).recoverWith {
      case NonFatal(_) => userRepository.create(user.name)
    }.run()
}
