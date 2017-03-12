package domain.repository.quill

import scalikejdbc._
import scalikejdbc.config._

object SetupDb {
  private def createUserTable = sql"""
    create table `user` (
      `id` bigint not null auto_increment,
      `name` varchar(64) not null
    )
  """

  def createTables(): Unit = {
    DBs.setupAll()
    DB localTx { implicit s =>
      createUserTable.execute().apply()
    }
  }
}