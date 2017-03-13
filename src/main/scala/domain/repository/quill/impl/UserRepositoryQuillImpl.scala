package domain.repository.quill.impl

import domain.entity.user.User
import domain.repository.UserRepository
import fujitask.{ReadTransaction, ReadWriteTransaction, Task}

class UserRepositoryQuillImpl extends UserRepository {
  override def create(name: String): Task[ReadWriteTransaction, User] =
    ask map { ctx =>
      import ctx._

      val q = quote {
        query[User].insert(_.name -> lift(name)).returning(user => user.id)
      }
      val id = ctx.run(q)
      User(id, name)
    }

  override def read(id: Long): Task[ReadTransaction, Option[User]] =
    ask map { ctx =>
      import ctx._

      val q = quote {
        query[User].filter(_.id == lift(id))
      }
      ctx.run(q).headOption
  }

  override def readAll: Task[ReadTransaction, Seq[User]] =
    ask map { ctx =>
      import ctx._

      val q = quote {
        query[User]
      }
      ctx.run(q)
    }

  override def update(user: User): Task[ReadWriteTransaction, Unit] =
    ask map { ctx =>
      import ctx._

      val q = quote {
        query[User].filter(_.id == lift(user.id)).update(lift(user))
      }
      ctx.run(q)
    }

  override def delete(id: Long): Task[ReadWriteTransaction, Unit] =
    ask map { ctx =>
      import ctx._

      val q = quote {
        query[User].filter(_.id == lift(id)).delete
      }
      ctx.run(q)
    }
}
