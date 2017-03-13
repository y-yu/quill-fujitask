package domain.repository.quill

import scalikejdbc._
import scalikejdbc.config._

object Db {
  private def createUserTable = sql"""
    create table `user` (
      `id` bigint not null auto_increment,
      `name` varchar(64) not null
    )
  """

  def setUp(): Unit = DBs.setupAll()

  def close(): Unit = DBs.closeAll()

  def createTables(): Unit = {
    DB localTx { implicit s =>
      createUserTable.execute().apply()
    }
  }
}