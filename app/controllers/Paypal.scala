package controllers
 
import play.api._
import play.api.mvc._
import models.Purchase
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.data.validation.Constraints._
import play.api.libs.ws.WS
import play.api.libs.concurrent.Execution.Implicits._
import java.net.{URLDecoder, URLEncoder}

import views._

object Paypal extends Controller {
 
  //val url = "https://www.paypal.com/cgi-bin/webscr?cmd=_notify-validate&";
	val url = "https://www.sandbox.paypal.com/cgi-bin/webscr?cmd=_notify-validate&"
	  
	  
	val ipnForm = Form(
	  tuple(
	    "txn_type" -> nonEmptyText,
	    "payment_status" -> nonEmptyText,
	    "txn_id" -> longNumber(min=1)
	  )
	)
    
	def test = Action {
	  Ok(html.testIpn())
	}
	
	import collection.mutable.HashMap
	
	def parseParams(body: String): HashMap[String,List[String]] = {
	  val map = new HashMap[String,List[String]]() 
	  URLDecoder.decode(body, "UTF-8").split("&").foreach { keyValue =>
        val kvList = keyValue.split("=") 
        if (kvList.length > 1) {
        	val l = map.get(kvList(0)) match {
			  case Some(values: List[String]) =>
			    values :+ kvList(1)
			  case None =>
			    map.put(kvList(0), List(kvList(1)))
			}
        }
      }
	  map
	}
	
	def ipn = Action(parse.tolerantText) { implicit request =>	  
		Async {
		  	val params = parseParams(request.body)
		  	val txn_id: Long = params.get("txn_id").get(0).toLong
		  	val txn_type = params.get("txn_type").get(0)
		  	Logger.info(s"txn_id=$txn_id, txn_type=$txn_type")
		    WS.url(url).post(request.body).map { response =>
		      
		      response.body match {
		        case "INVALID" => InternalServerError(s"Oops: txn_id $txn_id is invalid")
		        case "OK" => Ok(s"Response for txn_id $txn_id:  " + response.body)
		        case _ => InternalServerError(s"Unexpected response for txn_id $txn_id:  " + response.body)
		      }			   
			  
		//	  params.foreach(l => {
		//	    val key = l._1
		//	    val values: List[String] = l._2
		//	    Logger.info(key + "=" + values(0))   
		//	  })
			  
		    }
		}  
	}

 /*
  * 
  * https://cms.paypal.com/cms_content/GB/en_GB/files/developer/IPN_JAVA_JSP.txt
  * 
  * // Java JSP

<%@ page import="java.util.*" %>
<%@ page import="java.net.*" %>
<%@ page import="java.io.*" %>

<%
// read post from PayPal system and add 'cmd'
Enumeration en = request.getParameterNames();
String str = "cmd=_notify-validate";
while(en.hasMoreElements()){
String paramName = (String)en.nextElement();
String paramValue = request.getParameter(paramName);
str = str + "&" + paramName + "=" + URLEncoder.encode(paramValue);
}

// post back to PayPal system to validate
// NOTE: change http: to https: in the following URL to verify using SSL (for increased security).
// using HTTPS requires either Java 1.4 or greater, or Java Secure Socket Extension (JSSE)
// and configured for older versions.
URL u = new URL("https://www.paypal.com/cgi-bin/webscr");
URLConnection uc = u.openConnection();
uc.setDoOutput(true);
uc.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
PrintWriter pw = new PrintWriter(uc.getOutputStream());
pw.println(str);
pw.close();

BufferedReader in = new BufferedReader(
new InputStreamReader(uc.getInputStream()));
String res = in.readLine();
in.close();

// assign posted variables to local variables
String itemName = request.getParameter("item_name");
String itemNumber = request.getParameter("item_number");
String paymentStatus = request.getParameter("payment_status");
String paymentAmount = request.getParameter("mc_gross");
String paymentCurrency = request.getParameter("mc_currency");
String txnId = request.getParameter("txn_id");
String receiverEmail = request.getParameter("receiver_email");
String payerEmail = request.getParameter("payer_email");

check notification validation
if(res.equals("VERIFIED")) {
// check that paymentStatus=Completed
// check that txnId has not been previously processed
// check that receiverEmail is your Primary PayPal email
// check that paymentAmount/paymentCurrency are correct
// process payment
}
else if(res.equals("INVALID")) {
// log for investigation
}
else {
// error
}
%>*/
	
	
//  def ipn = Action(parse.raw) {request =>
//    request.body.asBytes() match {
//      case Some(arrBytes) => {
//        val queryString = new String(arrBytes);
//        Logger.info("Paypal request: " + queryString)
//        Logger.info("Verifying request: " + url + queryString)
// 
//        // for some reason I always get invalid if I use play.api.lib.ws.WS instead...
//        val client = new HttpClient
//        val methodGet: GetMethod = new GetMethod(url + queryString)
//        client.executeMethod(methodGet)
//        val response = methodGet.getResponseBodyAsString
//        methodGet.releaseConnection
//        if (response.trim != "VERIFIED") {
//          logAndFail("Couldn't verify paypal call", "Verification response: " + response.trim)
//        } else {
//          val res = queryString.split('&') map { str =>
//            val pair = str.split('=')
//            if (pair.length == 2)
//              (pair(0) -> pair(1))
//            else
//              (pair(0) -> "")
//          } toMap;
//          if (res.get("payment_status").exists(_ == "Completed")) {
//            res.get("custom") match {
//              case Some(custom) => custom.split("_").toList match {
//                //case List("purchase", "id", id) => {
//                  Purchases.findById(id).map { purchase =>
//                    
//				      Ok("ok")
//				    
//                  }
//                //}
//                case _ => throw new RuntimeException("invalid custom payload")
//              }
//              case None => logAndFail("custom var not available")
//            }
//          } 
//        } 
//      }
//      case None => logAndFail("Couldn't process paypal ipn")
//    }
//  }
}
