package domain.repository

import domain.entity.user.User
import domain.repository.quill.impl.UserRepositoryQuillImpl
import fujitask.{ReadTransaction, ReadWriteTransaction, Task}

trait UsesUserRepository {
  val userRepository: UserRepository
}

trait MixInUserRepository {
  val userRepository: UserRepository = new UserRepositoryQuillImpl {}
}

trait UserRepository {
  def create(name: String): Task[ReadWriteTransaction, User]

  def read(id: Long): Task[ReadTransaction, Option[User]]

  def readAll: Task[ReadTransaction, Seq[User]]

  def update(user: User): Task[ReadWriteTransaction, Unit]

  def delete(id: Long): Task[ReadWriteTransaction, Unit]
}
