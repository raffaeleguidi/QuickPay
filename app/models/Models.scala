package models


import java.util.Date
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import slick.lifted.{Join, MappedTypeMapper}

case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}

case class Shop(id: Option[Long], name: String)
case class Item(id: Option[Long] = None, name: String, price: BigDecimal, introduced: Option[Date]= None, discontinued: Option[Date]= None, itemId: Option[Long]=None)
case class Purchase(id: Option[Long], paid: Boolean)

object Purchases extends Table[Purchase]("PURCHASE") with CRUDModel[Purchase] {

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def paid = column[Boolean]("paid")
  def * = id.? ~ paid <>(Purchase.apply _, Purchase.unapply _)

  /**
   * Construct the Map[String,String] needed to fill a select options set
   */
   def options(implicit s:Session): Seq[(String, String)] = {
    val query = (for {
      purchase <- Purchases
    } yield (purchase.id, purchase.paid)
      ).sortBy(_._2)
    query.list.map(row => (row._1.toString, row._2.toString))
  }
  
  val byId = createFinderBy(_.id)
 
  def findById(id: Long)(implicit s:Session): Option[Purchase] =
	  Purchases.byId(id).firstOption
}

object Shops extends Table[Shop]("SHOP") with CRUDModel[Shop] {

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name", O.NotNull)
  def * = id.? ~ name <>(Shop.apply _, Shop.unapply _)

  /**
   * Construct the Map[String,String] needed to fill a select options set
   */
   def options(implicit s:Session): Seq[(String, String)] = {
    val query = (for {
      shop <- Shops
    } yield (shop.id, shop.name)
      ).sortBy(_._2)
    query.list.map(row => (row._1.toString, row._2))
  }
}

object Items extends Table[Item]("ITEM") with CRUDModel[Item] {

  implicit val javaUtilDateTypeMapper = MappedTypeMapper.base[java.util.Date, java.sql.Date](
    x => new java.sql.Date(x.getTime),
    x => new java.util.Date(x.getTime)
  )

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name", O.NotNull)
  def price = column[BigDecimal]("price", O.NotNull)
  def introduced = column[Date]("introduced", O.Nullable)
  def discontinued = column[Date]("discontinued", O.Nullable)
  def shopId = column[Long]("shopId", O.Nullable)

  def * = id.? ~ name ~ price ~ introduced.? ~ discontinued.? ~ shopId.? <>(Item.apply _, Item.unapply _)

//  def autoInc = * returning id

  val byId = createFinderBy(_.id)

  /**
   * Retrieve a computer from the id
   * @param id
   */
  def findById(id: Long)(implicit s:Session): Option[Item] =
      Items.byId(id).firstOption

  /**
   * Count all Items
   */
  def count(implicit s:Session): Int =
      Query(Items.length).first

  /**
   * Count computers with a filter
   * @param filter
   */
  def count(filter: String)(implicit s:Session) : Int =
      Query(Items.where(_.name.toLowerCase like filter.toLowerCase).length).first

  /**
   * Return a page of (Computer,Company)
   * @param page
   * @param pageSize
   * @param orderBy
   * @param filter
   */
  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%")(implicit s:Session): Page[(Item, Option[Shop])] = {

    val offset = pageSize * page
    val query =
      (for {
        (item, shop) <- Items leftJoin Shops on (_.shopId === _.id)
        if item.name.toLowerCase like filter.toLowerCase()
      }
      yield (item, shop.id.?, shop.name.?))
        .drop(offset)
        .take(pageSize)

    val totalRows = count(filter)
    val result = query.list.map(row => (row._1, row._2.map(value => Shop(Option(value), row._3.get))))

    Page(result, page, offset, totalRows)
  }

  /**
   * Insert a new computer
   * @param computer
   */
//  def insert(item: Room)(implicit s:Session) {
//    Items.autoInc.insert(item)
//  }

  /**
   * Update a computer
   * @param id
   * @param computer
   */
  def update(id: Long, item: Item)(implicit s:Session) {
    val itemToUpdate: Item = item.copy(Some(id))
    Items.where(_.id === id).update(itemToUpdate)
  }

  /**
   * Delete a computer
   * @param id
   */
//  def delete(id: Long)(implicit s:Session) {
//    Items.where(_.id === id).delete
//  }
}


