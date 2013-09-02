package models

import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB

/**
 * Helper for otherwise verbose Slick model definitions
 */
trait CRUDModel[T <: AnyRef { val id: Option[Long] }] { self: Table[T] =>
 
  def id: Column[Long]
 
  def * : scala.slick.lifted.ColumnBase[T]
 
  def autoInc = * returning id
  
  def insert(entity: T)(implicit s:Session) = {
      autoInc.insert(entity)
  }
 
  def insertAll(entities: Seq[T])(implicit s:Session) {
      autoInc.insertAll(entities: _*)
  }
 
// non funziona
//  def update(id: Long, entity: T)(implicit s:Session) {
//      tableQueryToUpdateInvoker(
//        tableToQuery(this).where(_.id === id)
//      ).update(entity)
//  }
//  val byId = createFinderBy(_.id)
//  
//  def findById(id: Long, entity: T)(implicit s:Session) {
//     tableToQuery(this).where(_.id === id).first
//  }
  
  def delete(id: Long)(implicit s:Session) {
      queryToDeleteInvoker(
        tableToQuery(this).where(_.id === id)
      ).delete
  }
 
  def count = DB.withSession { implicit session: Session =>
    Query(tableToQuery(this).length).first
  }
 
}