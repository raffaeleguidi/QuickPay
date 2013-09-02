import java.text.SimpleDateFormat
import play.api._
import models._
import play.api.db.slick._
import play.api.Play.current

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("Application is starting...")
    InitialData.insert()
    Logger.info("...done")
  }  
  
  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }      

  /**
 * Initial set of data to be imported 
 * in the sample application.
 */
}

  object InitialData {
	  val sdf = new SimpleDateFormat("yyyy-MM-dd")
  
	  def insert(){
		  DB.withSession{ implicit s:Session =>
		    if (Shops.count == 0) {
			    for( n <- 1L to 100L){
			    	Shops.insert(Shop(Option(n), s"Shop #$n"))
			    	Seq(
				        Item(None, s"drink #$n", 10.0, None, None, Option(n)),
				        Item(None, s"food #$n", 20.0,  None, None, Option(n)),
				        Item(None, s"gift #$n", 30.0, None, None, Option(n))
			    	).foreach(Items.insert)
			    } 
		     }
		  }
	  }
	  
  }
  
