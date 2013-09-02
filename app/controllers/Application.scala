package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._

import play.api.db.slick._
import play.api.Play.current

import views._
import models._

/**
 * Manage a database of computers
 */
object Application extends Controller { 
  /**
   * This result directly redirect to the application home.
   */
  val Home = Redirect(routes.Application.list(0, 2, ""))
  
  /**
   * Describe the computer form (used in both edit and create screens).
   */ 
  val itemForm = Form(
    mapping(
      "id" -> optional(longNumber),
      "name" -> nonEmptyText,
      "price" -> of[BigDecimal] ,
      "introduced" -> optional(date("yyyy-MM-dd")),
      "discontinued" -> optional(date("yyyy-MM-dd")),
      "shop" -> optional(longNumber)
    )(Item.apply)(Item.unapply)
  )
  
  // -- Actions

  /**
   * Handle default path requests, redirect to computers list
   */  
  def index = Action {
    Ok(html.index());
  }
  
  /**
   * Display the paginated list of computers.
   *
   * @param page Current page number (starts from 0)
   * @param orderBy Column to be sorted
   * @param filter Filter applied on computer names
   */
  def list(page: Int, orderBy: Int, filter: String) = DBAction { implicit rs =>
    Ok(html.list(
      Items.list(page = page, pageSize = 10, orderBy = orderBy, filter = ("%"+filter+"%")),
      orderBy, filter
    ))
  }
  
  /**
   * Display the 'edit form' of a existing Computer.
   *
   * @param id Id of the computer to edit
   */
  def edit(id: Long) = DBAction { implicit rs =>
    Items.findById(id).map { room =>
      Ok(html.editForm(id, itemForm.fill(room), Shops.options))
    }.getOrElse(NotFound)
  }
  
  /**
   * Handle the 'edit form' submission 
   *
   * @param id Id of the computer to edit
   */
  def update(id: Long) = DBAction { implicit rs =>
    itemForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.editForm(id, formWithErrors, Shops.options)),
      room => {
        Items.update(id, room)
        Home.flashing("success" -> "Room %s has been updated".format(room.name))
      }
    )
  }
  
  /**
   * Display the 'new computer form'.
   */
  def create = DBAction { implicit rs =>
    Ok(html.createForm(itemForm, Shops.options))
  }
  
  /**
   * Handle the 'new computer form' submission.
   */
  def save = DBAction { implicit rs =>
    itemForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.createForm(formWithErrors, Shops.options)),
      room => {
        Items.insert(room)
        Home.flashing("success" -> "Room %s has been created".format(room.name))
      }
    )
  }
  
  /**
   * Handle room deletion.
   */
  def delete(id: Long) = DBAction { implicit rs =>
    Items.delete(id)
    Home.flashing("success" -> "Room has been deleted")
  }

}
            