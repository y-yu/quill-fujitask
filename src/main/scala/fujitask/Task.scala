package fujitask

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.control.NonFatal

/**
  * @see [[https://github.com/hexx/fujitask-simple/blob/master/fujitask/src/main/scala/fujitask/Task.scala]]
  */

/**
  * 『PofEAA』の「Unit of Work」パターンの実装
  *
  * トランザクションとはストレージに対するまとまった処理である
  * トランザクションオブジェクトとはトランザクションを表現するオブジェクトで、
  * 具体的にはデータベースライブラリのセッションオブジェクトなどが該当する
  *
  * @tparam R トランザクションオブジェクトの型
  * @tparam A トランザクションを実行して得られる値の型
  */
trait Task[-R, +A] { lhs =>
  /**
    * トランザクションの内部で実行される個々の処理の実装
    * このメソッドを実装することでTaskが作られる
    *
    * @param resource トランザクションオブジェクト
    * @param ec ExecutionContext
    * @return トランザクションの内部で実行される個々の処理で得られる値
    */
  def execute(resource: R)(implicit ec: ExecutionContext): Future[A]

  /**
    * Taskモナドを合成する
    * その際、変位指定によりResourceの型は両方のTaskのResourceの共通のサブクラスの型になる
    *
    * @param f モナド関数
    * @tparam ER トランザクションオブジェクトの型
    * @tparam B 合成されたTaskを実行すると得られる値の型
    * @return 合成されたTask
    */
  def flatMap[ER <: R, B](f: A => Task[ER, B]): Task[ER, B] =
    new Task[ER, B] {
      def execute(resource: ER)(implicit ec: ExecutionContext): Future[B] =
        lhs.execute(resource).map(f).flatMap(_.execute(resource))
    }

  /**
    * 関数をTaskの結果に適用する
    *
    * @param f 適用したい関数
    * @tparam B 関数を適用して得られた値の型
    * @return 関数が適用されたTask
    */
  def map[B](f: A => B): Task[R, B] = flatMap(a => Task(f(a)))

  /**
    * TaskRunnerを使ってTaskを実行する
    * implicitによりResourceに合ったTaskRunnerが選ばれる
    *
    * @param runner Taskを実行するためのTaskRunner
    * @tparam ER トランザクションオブジェクトの型
    * @return 個々のTaskの処理の結果得られる値
    */
  def run[ER <: R]()(implicit runner: TaskRunner[ER]): Future[A] = runner.run(this)

  /**
    * 失敗したTaskを部分関数で回復する
    *
    * @param pf 部分関数
    * @tparam B 部分関数を適用して得られるTaskの値の型
    * @return 回復したTask
    */
  def recover[B >: A](pf: PartialFunction[Throwable, B]): Task[R, B] =
    new Task[R, B] {
      def execute(resource: R)(implicit ec: ExecutionContext): Future[B] =
        lhs.execute(resource).recover(pf)
    }

  /**
    * 失敗したTaskをTaskを返す部分関数で回復する
    *
    * @param pf 部分関数
    * @tparam ER 新しいTaskのトランザクションオブジェクトの型
    * @tparam B 部分関数を適用して得られるTaskの値の型
    * @return　回復したTask
    */
  def recoverWith[ER <: R, B >: A](pf: PartialFunction[Throwable, Task[ER, B]]): Task[ER, B] =
    new Task[ER, B] {
      def execute(resource: ER)(implicit ec: ExecutionContext): Future[B] =
        lhs.execute(resource).recoverWith {
          case NonFatal(e) => if (pf.isDefinedAt(e)) pf(e).execute(resource) else Future.failed(e)
        }
    }
}

object Task {
  /**
    * Taskのデータコンストラクタ
    *
    * @param a Taskの値
    * @tparam Resource トランザクションオブジェクトの型
    * @tparam A Taskの値の型
    * @return 実行するとaの値を返すTask
    */
  def apply[Resource, A](a: => A): Task[Resource, A] =
    new Task[Resource, A] {
      def execute(resource: Resource)(implicit executor: ExecutionContext): Future[A] =
        Future(a)
    }

  /**
    * 失敗を表すTaskのコンストラクタ
    *
    * @param e エラー
    * @return 失敗を表すTask
    */
  def failed(e: Throwable): Task[Any, Nothing] =
    new Task[Any, Nothing] {
      def execute(resource: Any)(implicit ec: ExecutionContext): Future[Nothing] =
        Future.failed(e)
    }
}

/**
  * Taskを実行する
  * トランザクションオブジェクトの型ごとにインスタンスを作成すること
  *
  * @tparam Resource トランザクションオブジェクトの型
  */
trait TaskRunner[Resource] {
  /**
    * Taskを実行する
    *
    * @param task 実行するTask
    * @tparam A Task実行すると得られる値の型
    * @return Task実行して得られた値
    */
  def run[A](task: Task[Resource, A]): Future[A]
}
