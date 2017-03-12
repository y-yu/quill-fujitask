package domain.repository.quill

import fujitask._
import io.getquill.{H2JdbcContext, SnakeCase}
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

package object impl {
  lazy val ctx = new H2JdbcContext[SnakeCase]("ctx")

  def ask: Task[Transaction, H2JdbcContext[SnakeCase]] = new Task[Transaction, H2JdbcContext[SnakeCase]] {
    def execute(transaction: Transaction)(implicit ec: ExecutionContext): Future[H2JdbcContext[SnakeCase]] =
      Future.successful(transaction.asInstanceOf[QuillTransaction].ctx)
  }

  implicit def readRunner[R >: ReadTransaction] : TaskRunner[R] =
    new TaskRunner[R] {
      def run[A](task: Task[R, A]): Future[A] = {
        println("ReadRunner")
        ctx.transaction {
          task.execute(new QuillReadTransaction(ctx))
        }
      }
    }

  implicit def readWriteRunner[R >: ReadWriteTransaction]: TaskRunner[R] =
    new TaskRunner[R] {
      def run[A](task: Task[R, A]): Future[A] = {
        println("ReadWriteRunner")
        ctx.transaction {
          task.execute(new QuillReadWriteTransaction(ctx))
        }
      }
    }
}
